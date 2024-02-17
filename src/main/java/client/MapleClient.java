/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.ScriptEngine;

import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import constants.ItemConstants;
import constants.ServerConstants;
import crypto.BCrypt;
import io.netty.util.AttributeKey;
import net.channel.ChannelServer;
import net.login.LoginCharacter;
import net.login.LoginServer;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.MapleMessengerCharacter;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestActionManager;
import scripting.quest.QuestScriptManager;
import server.*;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import server.maps.objects.HiredMerchant;
import server.quest.MapleQuest;
import server.shark.SharkLogger;
import tools.*;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CLogin;
import tools.packets.CWvsContext;

public class MapleClient{

	public static final int LOGIN_NOTLOGGEDIN = 0;
	public static final int LOGIN_SERVER_TRANSITION = 1;
	public static final int LOGIN_LOGGEDIN = 2;
	public static final AttributeKey<MapleClient> CLIENT_KEY = AttributeKey.valueOf("CLIENT");
	private MapleAESOFB send;
	private MapleAESOFB receive;
	private io.netty.channel.Channel session;
	private MapleCharacter player;
	private int channel = 0;
	private int accId = 1;
	private boolean loggedIn = false;
	private boolean serverTransition = false;
	private Calendar birthday = null;
	private String accountName = null;
	private int world;
	private long lastPong;
	private int gmlevel;
	private Set<String> macs = new HashSet<>();
	private Set<String> hwids = new HashSet<>();
	private Map<String, ScriptEngine> engines = new HashMap<>();
	private byte characterSlots = 3;
	private byte loginattempt = 0;
	private String pin = null;
	private int pinattempt = 0;
	private String pic = null;
	private int picattempt = 0;
	private byte gender = -1;
	private boolean disconnecting = false;
	private final Lock mutex = new ReentrantLock(true);
	private long lastNpcClick;
	public SharkLogger sl = new SharkLogger(); // no boilerplate because i'm lazy
	public long sessionID;
	private long eliteStart, eliteLength, lastNameChange;
	private boolean petvac;
	public boolean skipTutorial;
	private Map<String, Object> progressValues;
	private boolean alpha;
	private String banreason;
	private boolean sendServerList;
	//
	public short nBuyCharacterCount;
	//
	public boolean isFakeLogin;

	public MapleClient(MapleAESOFB send, MapleAESOFB receive, io.netty.channel.Channel session){
		this.send = send;
		this.receive = receive;
		this.session = session;
		this.progressValues = new HashMap<>();
		session.eventLoop().scheduleAtFixedRate(this::update, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	public void update(){
		//
		if(player != null){
			player.update();
		}
	}

	public synchronized MapleAESOFB getReceiveCrypto(){
		return receive;
	}

	public synchronized MapleAESOFB getSendCrypto(){
		return send;
	}

	public synchronized io.netty.channel.Channel getSession(){
		return session;
	}

	public MapleCharacter getPlayer(){
		if(player == null){
			disconnect(true, false, false);
			updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
			getSession().attr(MapleClient.CLIENT_KEY).set(null); // prevents double dcing during login
			getSession().close();
			/*try{
				throw new RuntimeException();
			}catch(Exception ex){
				FilePrinter.printError(FilePrinter.ACCOUNT_STUCK, ex);
			}*/
		}
		return player;
	}

	public MapleCharacter getPlayerNullable(){
		return player;
	}

	public void disconnectFully(){
		boolean inCashShop = false;
		if(!isPlayerNull()){
			inCashShop = getPlayer().getCashShop().isOpened();
		}
		disconnect(true, false, inCashShop);
		if(!isPlayerNull() && ChannelServer.getInstance() != null){
			ChannelServer.getInstance().removeMTSPlayer(getPlayer().getId());
			ChannelServer.getInstance().removePlayerFromTempStorage(getPlayer().getId());
			if(getChannelInstance() != null) getChannelInstance().removePlayer(getPlayer());
		}
		updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
		getSession().attr(MapleClient.CLIENT_KEY).set(null);
		getSession().close();
	}

	public boolean isPlayerNull(){
		return player == null;
	}

	public void setPlayer(MapleCharacter player){
		this.player = player;
	}

	public void sendCharList(int server){
		this.announce(CLogin.getCharList(this, server));
	}

	public boolean canSkipTutorial(int server){
		if(skipTutorial) return skipTutorial;
		try{
			for(CharNameAndId cni : loadCharactersInternal(server)){
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT level FROM characters WHERE id = ?")){
					ps.setInt(1, cni.id);
					try(ResultSet rs = ps.executeQuery()){
						if(rs.getInt("level") >= 10){
							skipTutorial = true;
						}
					}
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return skipTutorial;
	}

	public List<MapleCharacter> loadCharacters(int serverId){
		List<MapleCharacter> chars = new ArrayList<>();
		try{
			for(CharNameAndId cni : loadCharactersInternal(serverId)){
				MapleCharacter mc = MapleCharacter.loadCharFromDB(cni.id, this, false);
				chars.add(mc);
				if(mc.getLevel() > 10){
					skipTutorial = true;
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return chars;
	}

	public List<LoginCharacter> loadLoginCharacters(int serverId){
		List<LoginCharacter> chars = new ArrayList<>();
		try{
			for(CharNameAndId cni : loadCharactersInternal(serverId)){
				LoginCharacter mc = LoginCharacter.loadCharFromDB(cni.id, this, false);
				chars.add(mc);
				if(mc.getLevel() > 10){
					skipTutorial = true;
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return chars;
	}

	public List<String> loadCharacterNames(int serverId){
		List<String> chars = new ArrayList<>();
		for(CharNameAndId cni : loadCharactersInternal(serverId)){
			chars.add(cni.name);
		}
		return chars;
	}

	private List<CharNameAndId> loadCharactersInternal(int serverId){
		PreparedStatement ps;
		List<CharNameAndId> chars = new ArrayList<>();
		try{
			ps = DatabaseConnection.getConnection().prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ? AND deleted = 0");
			ps.setInt(1, this.getAccID());
			ps.setInt(2, serverId);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
				}
			}
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return chars;
	}

	public boolean isLoggedIn(){
		return loggedIn;
	}

	public boolean hasBannedIP(){
		boolean ret = false;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")){
				ps.setString(1, session.remoteAddress().toString());
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					if(rs.getInt(1) > 0){
						ret = true;
					}
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return ret;
	}

	public int finishLogin(){
		synchronized(MapleClient.class){
			if(getLoginState() > LOGIN_NOTLOGGEDIN){ // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
				loggedIn = false;
				return 7;
			}
			updateLoginState(LOGIN_LOGGEDIN);
		}
		return 0;
	}

	public void setPin(String pin){
		this.pin = pin;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET pin = ? WHERE id = ?")){
				ps.setString(1, pin);
				ps.setInt(2, accId);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public String getPin(){
		return pin;
	}

	public boolean checkPin(String other){
		pinattempt++;
		if(pinattempt > 5){
			getSession().close();
		}
		if(pin.equals(other)){
			pinattempt = 0;
			return true;
		}
		return false;
	}

	public void setPic(String pic){
		String salt = BCrypt.gensalt();
		pic = BCrypt.hashpw(pic, salt);
		this.pic = pic;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET pic = ? WHERE id = ?")){
				ps.setString(1, pic);
				ps.setInt(2, accId);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public String getPic(){
		return pic;
	}

	public boolean checkPic(String other){
		if(picattempt > 5){
			getSession().close();
		}
		if(!ServerConstants.BCRYPT || BCrypt.checkpw(other, pic)){
			picattempt = 0;
			return true;
		}else picattempt++;
		return false;
	}

	private static boolean checkHash(String hash, String type, String password){
		try{
			MessageDigest digester = MessageDigest.getInstance(type);
			digester.update(password.getBytes("UTF-8"), 0, password.length());
			return HexTool.toString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
		}catch(NoSuchAlgorithmException | UnsupportedEncodingException e){
			throw new RuntimeException("Encoding the string failed", e);
		}
	}

	public int login(String login, String password){
		if(loginattempt > 4){
			getSession().close();
		}
		int loginok = 5;
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement ps = con.prepareStatement("SELECT id, password, gender, banned, banreason, gm, pin, pic, characterslots, tos, petvac, alpha, macs, hwid FROM accounts WHERE name = ?")){
			ps.setString(1, login);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					banreason = rs.getString("banreason");
					if(rs.getByte("banned") == 1) return 3;
					accId = rs.getInt("id");
					gmlevel = rs.getInt("gm");
					pin = rs.getString("pin");
					pic = rs.getString("pic");
					gender = rs.getByte("gender");
					characterSlots = rs.getByte("characterslots");
					petvac = rs.getBoolean("petvac");
					alpha = rs.getByte("alpha") == 1;
					String hashedPassword = rs.getString("password");
					// we do not unban
					byte tos = rs.getByte("tos");
					String macs = rs.getString("macs");
					String hwid = rs.getString("hwid");
					if(getLoginState() > LOGIN_NOTLOGGEDIN){ // already loggedin
						loggedIn = false;
						loginok = 7;
					}else if(!ServerConstants.BCRYPT || BCrypt.checkpw(password, hashedPassword) || checkHash(hashedPassword, "SHA-1", password)){
						parseMac(macs);
						parseHWID(hwid);
						if(loginok != 13){
							if(tos == 0){
								loginok = 23;
							}else{
								loginok = 0;
							}
						}
					}else{
						loginattempt++;
						loggedIn = false;
						loginok = 4;
					}
					Logger.log(LogType.INFO, LogFile.LOGIN_INFO, null, "IP %s attempted login on %s", getSession().remoteAddress().toString(), this.accountName);
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		if(loginok == 0) loginattempt = 0;
		return loginok;
	}

	public Calendar getTempBanCalendar(){
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		final Calendar lTempban = Calendar.getInstance();
		try{
			ps = con.prepareStatement("SELECT `tempban` FROM accounts WHERE id = ?");
			ps.setInt(1, getAccID());
			rs = ps.executeQuery();
			if(!rs.next()) return null;
			long blubb = rs.getTimestamp("tempban").getTime();
			if(blubb == 0){ // basically if timestamp in db is 0000-00-00
				return null;
			}
			lTempban.setTimeInMillis(rs.getTimestamp("tempban").getTime());
			return lTempban;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
				if(rs != null){
					rs.close();
				}
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
		return null;// why oh why!?!
	}

	public static long dottedQuadToLong(String dottedQuad) throws RuntimeException{
		String[] quads = dottedQuad.split("\\.");
		if(quads.length != 4){ throw new RuntimeException("Invalid IP Address format."); }
		long ipAddress = 0;
		for(int i = 0; i < 4; i++){
			int quad = Integer.parseInt(quads[i]);
			ipAddress += (long) (quad % 256) * (long) Math.pow(256, (double) (4 - i));
		}
		return ipAddress;
	}

	public String convertHWID(String newHwid){
		String[] split = newHwid.split("_");
		if(split.length > 1 && split[1].length() == 8){
			StringBuilder hwid = new StringBuilder();
			String convert = split[1];
			int len = convert.length();
			for(int i = len - 2; i >= 0; i -= 2){
				hwid.append(convert.substring(i, i + 2));
			}
			hwid.insert(4, "-");
			return hwid.toString();
		}
		return "";
	}

	public void setAccID(int id){
		this.accId = id;
	}

	public int getAccID(){
		return accId;
	}

	public void updateLoginState(int newstate){
		try{
			Connection con = DatabaseConnection.getConnection();
			try(PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, updated_at = ? WHERE id = ?")){
				ps.setInt(1, newstate);
				ps.setLong(2, System.currentTimeMillis());
				ps.setInt(3, getAccID());
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		if(newstate == LOGIN_NOTLOGGEDIN){
			loggedIn = false;
			serverTransition = false;
		}else{
			serverTransition = (newstate == LOGIN_SERVER_TRANSITION);
			loggedIn = !serverTransition;
		}
	}

	public int getLoginState(){ // 0 = LOGIN_NOTLOGGEDIN, 1= LOGIN_SERVER_TRANSITION, 2 = LOGIN_LOGGEDIN
		try{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT loggedin, updated_at, UNIX_TIMESTAMP(birthday) as birthday FROM accounts WHERE id = ?");
			ps.setInt(1, getAccID());
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps.close();
				throw new RuntimeException("getLoginState - MapleClient");
			}
			birthday = Calendar.getInstance();
			long blubb = rs.getLong("birthday");
			if(blubb > 0){
				birthday.setTimeInMillis(blubb * 1000);
			}
			int state = rs.getInt("loggedin");
			if(state == LOGIN_SERVER_TRANSITION){
				if(rs.getLong("updated_at") + 30000 < System.currentTimeMillis()){
					Logger.log(LogType.INFO, LogFile.LOGIN, "Account: " + getAccountName() + " failed getLoginState. Took over 30 seconds to transition. Lastlogin: " + rs.getLong("updated_at") + " at: " + System.currentTimeMillis());
					state = LOGIN_NOTLOGGEDIN;
					updateLoginState(LOGIN_NOTLOGGEDIN);
				}
			}else if(state == LOGIN_LOGGEDIN && player == null){
				state = LOGIN_LOGGEDIN;
				updateLoginState(LOGIN_LOGGEDIN);
			}
			rs.close();
			ps.close();
			if(state == LOGIN_LOGGEDIN){
				loggedIn = true;
			}else if(state == LOGIN_SERVER_TRANSITION){
				ps = con.prepareStatement("UPDATE accounts SET loggedin = 0 WHERE id = ?");
				ps.setInt(1, getAccID());
				ps.executeUpdate();
				ps.close();
			}else{
				loggedIn = false;
			}
			return state;
		}catch(SQLException e){
			loggedIn = false;
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			throw new RuntimeException("login state");
		}
	}

	public boolean checkBirthDate(Calendar date){
		return date.get(Calendar.YEAR) == birthday.get(Calendar.YEAR) && date.get(Calendar.MONTH) == birthday.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == birthday.get(Calendar.DAY_OF_MONTH);
	}

	public void removePlayer(){
		try{
			player.cancelAllBuffs();
			player.cancelAllDebuffs();
			final MaplePlayerShop mps = player.getPlayerShop();
			if(mps != null){
				mps.removeVisitors();
				player.setPlayerShop(null);
			}
			final HiredMerchant merchant = player.getHiredMerchant();
			if(merchant != null){
				if(merchant.isOwner(player)){
					merchant.setOpen(true);
				}else{
					merchant.removeVisitor(player);
				}
				try{
					merchant.saveItems();
				}catch(SQLException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Error while saving Hired Merchant items.");
				}
			}
			player.setMessenger(null);
			final MapleMiniGame game = player.getMiniGame();
			if(game != null){
				player.setMiniGame(null);
				if(game.isOwner(player)){
					player.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(player));
					game.broadcastToVisitor(MaplePacketCreator.getMiniGameClose());
				}else{
					game.removeVisitor(player);
				}
			}
			NPCScriptManager.getInstance().dispose(this);
			QuestScriptManager.getInstance().dispose(this);
			if(player.getTrade() != null){
				MapleTrade.cancelTrade(player);
			}
			if(player.getEventInstance() != null){
				player.getEventInstance().playerDisconnected(player);
			}
			if(player.getMap() != null){
				player.getMap().removePlayer(player);
			}
		}catch(final Throwable t){
			Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, t);
		}
	}

	public void notDisconnecting(){
		disconnecting = false;
	}

	public final void disconnect(boolean shutdown, boolean cashshop){// once per MapleClient instance
		disconnect(true, shutdown, cashshop);
	}

	public final void disconnect(boolean closeConnection, boolean shutdown, boolean cashshop){// once per MapleClient instance
		try{
			this.sl.dump();
			if(disconnecting){
				return;
			}
			disconnecting = true;
			if(player != null && player.isLoggedin()){
				MapleMap map = player.getMap();
				final MapleParty party = player.getParty();
				final int idz = player.getId();
				final int messengerid = player.getMessenger() == null ? 0 : player.getMessenger().getId();
				// final int fid = player.getFamilyId();
				final BuddyList bl = player.getBuddylist();
				final MaplePartyCharacter chrp = new MaplePartyCharacter(player);
				final MapleMessengerCharacter chrm = new MapleMessengerCharacter(player, 0);
				final MapleGuildCharacter chrg = player.getMGC();
				removePlayer();
				player.saveCooldowns();
				player.saveToDB();
				if(channel == -1 || shutdown){
					player = null;
					return;
				}
				try{
					if(!this.serverTransition){ // meaning not changing channels
						if(messengerid > 0){
							try{
								ChannelServer.getInstance().getWorldInterface().leaveMessenger(messengerid, chrm);
							}catch(RemoteException | NullPointerException ex){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							}
						}
						for(MapleQuestStatus status : player.getStartedQuests()){ // This is for those quests that you have to stay logged in for a certain amount of time
							MapleQuest quest = status.getQuest();
							if(quest.getTimeLimit() > 0){
								MapleQuestStatus newStatus = new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
								newStatus.setForfeited(player.getQuest(quest).getForfeited() + 1);
								player.updateQuest(newStatus);
							}
						}
						MapleGuild guild = player.getGuild();
						if(guild != null){
							ChannelServer.getInstance().getWorldInterface().setGuildMemberOnline(chrg, false, getChannel());
							if(guild.getAllianceId() > 0){
								ChannelServer.getInstance().getWorldInterface().allianceMessage(guild.getAllianceId(), MaplePacketCreator.allianceMemberOnline(player, false), player.getId(), -1);
							}
							announce(MaplePacketCreator.showGuildInfo(player));
						}
						if(party != null){
							chrp.setOnline(false);
							try{
								ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.LOG_ONOFF, chrp);
							}catch(RemoteException | NullPointerException ex){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							}
							if(map != null && party.getLeader().getId() == idz){
								MaplePartyCharacter lchr = null;
								for(MaplePartyCharacter pchr : party.getMembers()){
									if(pchr != null && map.getCharacterById(pchr.getId()) != null && (lchr == null || lchr.getLevel() <= pchr.getLevel())){
										lchr = pchr;
									}
								}
								if(lchr != null){
									try{
										ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.CHANGE_LEADER, lchr);
									}catch(RemoteException | NullPointerException ex){
										Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
									}
								}
							}
						}
						if(bl != null){
							try{
								ChannelServer.getInstance().getWorldInterface().loggedOff(player.getName(), player.getId(), channel, player.getBuddylist().getBuddyIds());
							}catch(Exception ex){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							}
						}
						player.changeChannelCancel();
						if(!cashshop){
							player.empty(false);
						}
					}
				}catch(final Exception e){
					Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, e);
				}finally{
					if(!cashshop){
						getChannelServer().removePlayer(player);
					}
					if(ChannelServer.getInstance() != null){
						ChannelServer.getInstance().removeMTSPlayer(player.getId());
						ChannelServer.getInstance().removePlayerFromTempStorage(player.getId());
					}
					player.changeChannelCancel();
					if(!this.serverTransition){
						player.empty(false);
						player.logOff();
					}
					player = null;
				}
			}
			if(!serverTransition && isLoggedIn()){
				if(closeConnection){
					updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
					session.attr(MapleClient.CLIENT_KEY).set(null); // prevents double dcing during login
					session.close();
				}
			}
			engines.clear();
		}catch(final Throwable t){
			Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, t);
		}
	}

	public int getChannel(){
		return channel;
	}

	public Channel getChannelServer(){
		return ChannelServer.getInstance().getChannel(channel);
	}

	public Channel getChannelInstance(){
		return ChannelServer.getInstance().getChannel(channel);
	}

	public Channel getChannelServer(byte channel){
		return ChannelServer.getInstance().getChannel(channel);
	}

	public boolean deleteCharacter(int world, int cid){
		Connection con = DatabaseConnection.getConnection();
		try{
			if(LoginServer.getInstance().getCenterInterface().disconnectCharacter(world, cid)) return false;
		}catch(Exception re){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, re);
			return false;
		}
		try{
			try(PreparedStatement ps = con.prepareStatement("SELECT id, guildid, guildrank, name, allianceRank, gp FROM characters WHERE id = ? AND accountid = ?")){
				ps.setInt(1, cid);
				ps.setInt(2, accId);
				try(ResultSet rs = ps.executeQuery()){
					if(!rs.next()) return false;
					if(rs.getInt("guildid") > 0){
						try{
							LoginServer.getInstance().getCenterInterface().deleteGuildCharacter(cid, rs.getString("name"), rs.getInt("guildrank"), rs.getInt("guildid"), rs.getInt("allianceRank"), rs.getInt("gp"));
						}catch(Exception re){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, re);
							return false;
						}
					}
				}
			}
			try(PreparedStatement ps = con.prepareStatement("UPDATE characters SET deleted = 1 WHERE id = ?")){
				ps.setInt(1, cid);
				ps.executeUpdate();
			}
			/*try (PreparedStatement ps = con.prepareStatement("DELETE FROM wishlists WHERE charid = ?")) {
			    ps.setInt(1, cid);
			    ps.executeUpdate();
			}
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE id = ?")) {
			    ps.setInt(1, cid);
			    ps.executeUpdate();
			}
			String[] toDel = {"famelog", "inventoryitems", "keymap", "queststatus", "savedlocations", "skillmacros", "skills", "eventstats"};
			for (String s : toDel) {
			    MapleCharacter.deleteWhereCharacterId(con, "DELETE FROM `" + s + "` WHERE characterid = ?", cid);
			}*/
			return true;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return false;
		}
	}

	public String getAccountName(){
		return accountName;
	}

	public void setAccountName(String a){
		this.accountName = a;
	}

	public void setChannel(int channel){
		this.channel = channel;
	}

	public int getWorld(){
		return world;
	}

	public void setWorld(int world){
		this.world = world;
	}

	public void pongReceived(){
		lastPong = System.currentTimeMillis();
	}

	public void sendPing(){
		final long then = System.currentTimeMillis();
		announce(MaplePacketCreator.getPing());
		TimerManager.getInstance().schedule("sendPing", new Runnable(){

			@Override
			public void run(){
				try{
					if(lastPong < then){
						if(getSession() != null && getSession().isActive()){
							getSession().close();
						}
					}
				}catch(NullPointerException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}
		}, 15000);
	}

	public int getGMLevel(){
		return gmlevel;
	}

	public void setGMLevel(int gm){
		this.gmlevel = gm;
	}

	public void setScriptEngine(String name, ScriptEngine e){
		engines.put(name, e);
	}

	public ScriptEngine getScriptEngine(String name){
		return engines.get(name);
	}

	public void removeScriptEngine(String name){
		engines.remove(name);
	}

	public void clearLocalScripts(){
		engines.clear();
	}

	public NPCConversationManager getCM(){
		return NPCScriptManager.getInstance().getCM(this);
	}

	public QuestActionManager getQM(){
		return QuestScriptManager.getInstance().getQM(this);
	}

	public boolean acceptToS(){
		boolean disconnectForBeingAFaggot = false;
		if(accountName == null) return true;
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `tos` FROM accounts WHERE id = ?");
			ps.setInt(1, accId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				if(rs.getByte("tos") == 1){
					disconnectForBeingAFaggot = true;
				}
			}
			ps.close();
			rs.close();
			ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET tos = 1 WHERE id = ?");
			ps.setInt(1, accId);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return disconnectForBeingAFaggot;
	}

	public final Lock getLock(){
		return mutex;
	}

	private static class CharNameAndId{

		public String name;
		public int id;

		public CharNameAndId(String name, int id){
			super();
			this.name = name;
			this.id = id;
		}
	}

	public byte getCharacterSlots(){
		return characterSlots;
	}

	public void setCharacterSlots(byte slots){
		this.characterSlots = slots;
	}

	public boolean gainCharacterSlot(){
		if(characterSlots < 15){
			Connection con = DatabaseConnection.getConnection();
			try{
				try(PreparedStatement ps = con.prepareStatement("UPDATE accounts SET characterslots = ? WHERE id = ?")){
					ps.setInt(1, ++characterSlots);
					ps.setInt(2, accId);
					ps.executeUpdate();
				}
				return true;
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				return false;
			}
		}
		return false;
	}

	public final byte getGReason(){
		final Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = con.prepareStatement("SELECT `greason` FROM `accounts` WHERE id = ?");
			ps.setInt(1, accId);
			rs = ps.executeQuery();
			if(rs.next()) return rs.getByte("greason");
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
				if(rs != null){
					rs.close();
				}
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
		return 0;
	}

	public byte getGender(){
		return gender;
	}

	public void setGender(byte m){
		this.gender = m;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?")){
				ps.setByte(1, gender);
				ps.setInt(2, accId);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public synchronized void announce(final byte[] packet){
		session.writeAndFlush(packet);
		if(MapleLogger.log){
			SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(packet));
			// TempStatistics.addValue(slea.available());
			short packetId = slea.readShort(); // packetId
			MapleLogger.logSend(packetId, packet);
			MapleLogger.logSend(this, packetId, packet);
			slea = null;
		}
	}

	public void changeChannel(int channel){
		if(player.isBanned()){
			disconnect(true, false, false);
			return;
		}
		if(!player.isAlive() || FieldLimit.CHANGECHANNEL.check(player.getMap().getMapData().getFieldLimit())){
			announce(CWvsContext.enableActions());
			return;
		}
		boolean sameChannelServer = ChannelServer.getInstance().getChannel(channel) != null;
		String[] socket = null;
		if(sameChannelServer){
			Channel ch = ChannelServer.getInstance().getChannel(channel);
			if(ch != null){
				String ip = ch.getIP();
				if(ip != null) socket = ip.split(":");
			}
		}else{
			try{
				String ip = ChannelServer.getInstance().getWorldInterface().getIP(channel);
				if(ip != null){
					socket = ip.split(":");
				}
			}catch(RemoteException | NullPointerException ex){
				getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				announce(CWvsContext.enableActions());
				return;
			}
		}
		if(socket == null){
			getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			announce(CWvsContext.enableActions());
			return;
		}
		if(player.getTrade() != null){
			MapleTrade.cancelTrade(getPlayer());
		}
		HiredMerchant merchant = player.getHiredMerchant();
		if(merchant != null){
			if(merchant.isOwner(getPlayer())){
				merchant.setOpen(true);
			}else{
				merchant.removeVisitor(getPlayer());
			}
		}
		try{
			ChannelServer.getInstance().getWorldInterface().addBuffsToStorage(player.getId(), player.getAllBuffs());
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		player.cancelAllBuffs();
		// Canceling mounts? Noty
		if(player.getBuffedValue(MapleBuffStat.PUPPET) != null){
			player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
		}
		if(player.getBuffedValue(MapleBuffStat.COMBO) != null){
			player.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
		}
		player.getInventory(MapleInventoryType.EQUIPPED).checked(false); // test
		player.getMap().removePlayer(player);
		player.getClient().getChannelServer().removePlayer(player);
		if(sameChannelServer) ChannelServer.getInstance().addPlayerToTempStorage(player);
		player.getClient().updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
		try{
			announce(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
		}catch(IOException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public boolean canClickNPC(){
		return lastNpcClick + 500 < System.currentTimeMillis();
	}

	public void setClickedNPC(){
		lastNpcClick = System.currentTimeMillis();
	}

	public void removeClickedNPC(){
		lastNpcClick = 0;
	}

	public void setSessionID(long sessionID){
		/*
		 This mutator method sets the sessionID and saves the information
		 in the database.
		 */
		this.sessionID = sessionID;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET sessionID = ? WHERE id = ?")){
				ps.setLong(1, sessionID);
				ps.setInt(2, accId);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public long fetchSessionID(int accountId){
		/*
		 This method fetches the sessionID of the account.
		 */
		final Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = con.prepareStatement("SELECT `sessionID` FROM `accounts` WHERE id = ?");
			ps.setInt(1, accountId);
			rs = ps.executeQuery();
			if(rs.next()) return rs.getLong("sessionID");
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return 0; // just for the sakes of returning. This will never be returned
	}

	public long getSessionID(){
		/*
		 This accessor method sets the sessionID.
		 */
		return sessionID;
	}

	public void setEliteStart(long eliteStart){
		this.eliteStart = eliteStart;
	}

	public long getEliteStart(){
		return eliteStart;
	}

	public void setEliteLength(long eliteLength){
		this.eliteLength = eliteLength;
	}

	public long getEliteLength(){
		return eliteLength;
	}

	public void setLastNameChange(long time){
		this.lastNameChange = time;
	}

	public long getLastNameChange(){
		return lastNameChange;
	}

	public boolean checkEliteStatus(){
		if(eliteLength > 0 && eliteStart > 0){
			if(((eliteStart + eliteLength) - Calendar.getInstance().getTimeInMillis()) <= 0){
				eliteStart = 0;
				eliteLength = 0;
				updateEliteStatus(false, false);
				return false;
			}// Could do an else and return true here..but meh
		}else{
			return false;
		}
		return true;
	}

	public void addEliteDays(int days){
		eliteLength += days * 24 * 60 * 60 * 1000L;
		if(eliteStart == 0) eliteStart = Calendar.getInstance().getTimeInMillis();
		updateEliteStatus(false, true);
	}

	public void addEliteHours(int hours){
		eliteLength += hours * 60 * 60 * 1000L;
		if(eliteStart == 0) eliteStart = Calendar.getInstance().getTimeInMillis();
		updateEliteStatus(false, true);
	}

	public void addEliteTimeInMillis(long length){
		eliteLength += length;
		if(eliteStart == 0) eliteStart = Calendar.getInstance().getTimeInMillis();
		updateEliteStatus(false, true);
	}

	public void updateEliteStatus(boolean checkIfElite, boolean expirationUpdate){
		// if(expirationUpdate){
		if(checkIfElite){
			if(!checkEliteStatus()) return;
		}
		for(int itemID : ItemConstants.ELITE_ITEMS){
			boolean found = false;
			for(MapleInventoryType type : MapleInventoryType.values()){
				MapleInventory inv = getPlayer().getInventory(type);
				for(Item item : inv.listById(itemID)){
					found = true;
					if(expirationUpdate){
						item.setExpiration(eliteLength + eliteStart);
						announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, item))));
					}
				}
			}
			if(!found){
				ItemInformationProvider ii = ItemInformationProvider.getInstance();
				MapleInventory inv = getPlayer().getInventory(ii.getInventoryType(itemID));
				short slot = inv.getNextFreeSlot();
				if(slot >= 0){
					Item item = new Item(itemID, slot, (short) 1);
					item.setExpiration(eliteLength + eliteStart);
					MapleInventoryManipulator.addFromDrop(this, item, false);
				}
			}
		}
		int[] balrog = {1031, 10001031, 2000103, 20011031};
		for(int i : balrog){
			Skill skill = SkillFactory.getSkill(i);
			if(skill != null){
				player.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), eliteLength + eliteStart);
			}
		}
		// }
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET eliteStart = ?, eliteLength = ? WHERE id = ?")){
			ps.setLong(1, eliteStart);
			ps.setLong(2, eliteLength);
			ps.setInt(3, accId);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public String getEliteTimeLeft(){
		long time = (eliteStart + eliteLength) - Calendar.getInstance().getTimeInMillis();
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;
		long monthsInMilli = daysInMilli * 30;
		long months = time / monthsInMilli;
		time = time % monthsInMilli;
		long days = time / daysInMilli;
		time = time % daysInMilli;
		long hours = time / hoursInMilli;
		time = time % hoursInMilli;
		long minutes = time / minutesInMilli;
		time = time % minutesInMilli;
		StringBuilder sb = new StringBuilder();
		if(months > 0){
			sb.append(months + (months == 1 ? " month" : " months") + ((days > 0 || hours > 0 || minutes > 0) ? ", " : ""));
		}
		if(days > 0){
			sb.append(days + (days == 1 ? " day" : " days") + ((hours > 0 || minutes > 0) ? ", " : ""));
		}
		if(hours > 0){
			sb.append(hours + (hours == 1 ? " hour" : " hours") + (minutes > 0 ? ", " : ""));
		}
		if(minutes > 0){
			sb.append(minutes + (minutes == 1 ? " minute" : " minutes"));
		}
		return sb.toString();
	}

	public int canChangeName(){
		if(eliteLength < (5 * 24 * 60 * 60 * 1000L)) return 1;// 5 days of elite
		if(lastNameChange == 0) return 0;// if they never changed their name, allow em.
		if((Calendar.getInstance().getTimeInMillis() - lastNameChange) < 30 * 24 * 60 * 60 * 1000L) return 2;// check if they changed it in the last 30 days
		return 0;// we gud
	}

	public void changedName(){
		lastNameChange = Calendar.getInstance().getTimeInMillis();
	}

	public Map<String, Object> getProgressValues(){
		return progressValues;
	}

	public void setProgressValues(Map<String, Object> progressValues){
		this.progressValues = progressValues;
	}

	public void addProgressValue(String key, Object value){
		progressValues.put(key, value);
	}

	public void setProgressValue(String key, Object value){
		progressValues.put(key, value);
	}

	public Object getProgressValue(String key){
		Object val = progressValues.get(key);
		if(val != null) return val;
		return "";
	}

	public boolean isProgressValueSet(String key){
		Object value = getProgressValue(key);
		return value != null && ((value instanceof String && ((String) value).length() > 0) || !(value instanceof String));
	}

	public boolean isNightOverlayEnabled(){
		if(isProgressValueSet("nightoverlay")){
			Object val = getProgressValue("nightoverlay");
			if(val instanceof Boolean){
				return (Boolean) val;
			}else if(val instanceof String) return Boolean.parseBoolean((String) val);
		}
		return true;
	}

	public boolean getPetVac(){
		return petvac;
	}

	public void setPetVac(boolean petvac){
		this.petvac = petvac;
	}

	public boolean isAlphaUser(){
		return alpha;
	}

	public void setAlphaUser(boolean alpha){
		this.alpha = alpha;
	}

	public String getBanReason(){
		return banreason;
	}

	public boolean canSendServerList(){
		return sendServerList;
	}

	public void sendServerList(boolean val){
		sendServerList = val;
	}

	public void addMac(String mac){
		for(String m : mac.split(", ")){
			if(!macs.contains(m) && macs.add(m)){
				Logger.log(LogType.INFO, LogFile.LOGIN_INFO, null, "Account %s added new mac %s on ip %s", this.accountName, mac, getSession().remoteAddress().toString());
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?")){
					ps.setString(1, toStringMac());
					ps.setInt(2, accId);
					ps.executeUpdate();
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}
		}
	}

	public String toStringMac(){
		return macs.toString().trim().replace("[", "").replace("]", "");
	}

	public Set<String> toStringMacSet(){
		return macs;
	}

	public void parseMac(String rawMacs){
		if(rawMacs == null || rawMacs.isEmpty()) return;
		for(String mac : rawMacs.trim().split(",")){
			if(!mac.isEmpty()) this.macs.add(mac.trim());
		}
	}

	public void banMacs(){
		Connection con = DatabaseConnection.getConnection();
		for(String mac : macs){
			try(PreparedStatement ps = con.prepareStatement("INSERT INTO macbans(mac) VALUES(?)")){
				ps.setString(1, mac);
				ps.setInt(2, accId);
				ps.executeUpdate();
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public boolean hasBannedMac(){
		if(macs.isEmpty()) return false;
		boolean ret = false;
		try{
			StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM macbans WHERE mac IN (");
			for(int i = 0; i < macs.size(); i++){
				sql.append("?");
				if(i != macs.size() - 1){
					sql.append(", ");
				}
			}
			sql.append(")");
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())){
				int i = 0;
				for(String mac : toStringMacSet()){
					ps.setString(++i, mac);
				}
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					if(rs.getInt(1) > 0){
						ret = true;
					}
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return ret;
	}

	public void addHwid(String hwid){
		if(hwids.add(hwid)){
			Logger.log(LogType.INFO, LogFile.LOGIN_INFO, null, "Account %s added new hwid %s on ip %s", this.accountName, hwid, getSession().remoteAddress().toString());
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET hwid = ? WHERE id = ?")){
				ps.setString(1, toStringHwid());
				ps.setInt(2, accId);
				ps.executeUpdate();
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public String toStringHwid(){
		return hwids.toString().trim().replace("[", "").replace("]", "");
	}

	public Set<String> toStringHwidSet(){
		return hwids;
	}

	public void parseHWID(String rawHWID){
		if(rawHWID == null || rawHWID.isEmpty()) return;
		for(String hwid : rawHWID.trim().split(",")){
			if(!hwid.isEmpty()) this.hwids.add(hwid.trim());
		}
	}

	public boolean hasBannedHWID(){
		if(hwids.isEmpty()) return false;
		boolean ret = false;
		try{
			StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM hwidbans WHERE hwid IN (");
			for(int i = 0; i < hwids.size(); i++){
				sql.append("?");
				if(i != hwids.size() - 1){
					sql.append(", ");
				}
			}
			sql.append(")");
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql.toString())){
				int i = 0;
				for(String hwid : toStringHwidSet()){
					ps.setString(++i, hwid);
				}
				try(ResultSet rs = ps.executeQuery()){
					rs.next();
					if(rs.getInt(1) > 0){
						ret = true;
					}
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return ret;
	}

	public void banHwids(){
		Connection con = DatabaseConnection.getConnection();
		for(String hwid : hwids){
			try(PreparedStatement ps = con.prepareStatement("INSERT INTO hwidbans(hwid) VALUES(?)")){
				ps.setString(1, hwid);
				ps.executeUpdate();
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}
}
