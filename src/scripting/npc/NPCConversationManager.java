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
package scripting.npc;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import client.*;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.MobConstants;
import net.channel.ChannelServer;
import net.server.Server;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.AbstractPlayerInteraction;
import scripting.event.EventInstanceManager;
import server.*;
import server.events.gm.Event;
import server.gachapon.MapleGachapon;
import server.gachapon.MapleGachapon.MapleGachaponItem;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import server.propertybuilder.ExpProperty;
import server.quest.MapleQuest;
import tools.*;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.UserLocal;

/**
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction{

	// InitialQuiz
	public static final int InitialQuizRes_Request = 0x0;
	public static final int InitialQuizRes_Fail = 0x1;
	// InitialSpeedQuiz
	public static final int TypeSpeedQuizNpc = 0x0;
	public static final int TypeSpeedQuizMob = 0x1;
	public static final int TypeSpeedQuizItem = 0x2;
	// SpeakerTypeID
	public static final byte NoESC = 0x1;
	public static final byte NpcReplacedByUser = 0x2;
	public static final byte NpcReplayedByNpc = 0x4;
	public static final byte FlipImage = 0x8;
	private int npc;
	private String scriptName;
	private String getText;
	private NpcTalkData talkData;

	public NPCConversationManager(MapleClient c, int npc, String scriptName){
		super(c);
		this.npc = npc;
		this.scriptName = scriptName;
	}

	public NpcTalkData getTalkData(){
		return talkData;
	}

	public int getNpc(){
		return npc;
	}

	public String getScriptName(){
		return scriptName;
	}

	public void dispose(){
		c.getPlayer().dispose();
	}

	public void sendNext(String text, byte speaker, int npc){
		sendNext(npc, speaker, text);
	}

	public void sendNext(String text){
		sendNext(text, false);
	}

	public void sendNext(String text, byte speaker){
		sendNext(text, speaker, false);
	}

	public void sendNext(String text, boolean bPrev){
		sendNext(text, (byte) 0, bPrev);
	}

	public void sendNext(String text, byte speaker, boolean bPrev){
		sendNext(npc, text, speaker, bPrev);
	}

	public void sendNext(int nSpeakerTemplateID, String text){
		sendNext(nSpeakerTemplateID, text, false);
	}

	public void sendNext(int nSpeakerTemplateID, byte speaker, String text){
		sendNext(nSpeakerTemplateID, text, speaker, false);
	}

	public void sendNext(int nSpeakerTemplateID, String text, boolean bPrev){
		sendNext(nSpeakerTemplateID, text, (byte) 0, bPrev);
	}

	public void sendNext(int nSpeakerTemplateID, String text, byte speaker, boolean bPrev){
		sendSay(nSpeakerTemplateID, text, speaker, bPrev, true);
	}

	public void sendPrev(String text){
		sendPrev(text, (byte) 0, npc);
	}

	public void sendPrev(String text, byte speaker){
		sendPrev(text, speaker, npc);
	}

	public void sendPrev(String text, byte speaker, int npc){
		// getClient().announce(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
		sendSay(npc, text, speaker, true, false);
	}

	public void sendSay(String text, boolean bPrev, boolean bNext){
		sendSay(npc, text, (byte) 0, bPrev, bNext);
	}

	public void sendSay(int nSpeakerTemplateID, String text, byte speaker, boolean bPrev, boolean bNext){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.Say;
		talkData.parseText(text);
		talkData.prev = bPrev;
		talkData.next = bNext;
		if(talkData.validSelections.isEmpty()) getClient().announce(MaplePacketCreator.onSay(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text, bPrev, bNext));
		else{
			talkData.messageType = ScriptMessageType.AskMenu;
			getClient().announce(MaplePacketCreator.onAskMenu(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text));
		}
	}

	public void sendSayUser(String text, boolean bPrev, boolean bNext){
		sendSayUser(npc, text, bPrev, bNext);
	}

	public void sendSayUser(int nSpeakerTemplateID, String text, boolean bPrev, boolean bNext){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.Say;
		talkData.parseText(text);
		talkData.prev = bPrev;
		talkData.next = bNext;
		if(talkData.validSelections.isEmpty()) getClient().announce(MaplePacketCreator.onSay(NpcReplayedByNpc, nSpeakerTemplateID, NpcReplacedByUser, text, bPrev, bNext));
		else{
			talkData.messageType = ScriptMessageType.AskMenu;
			getClient().announce(MaplePacketCreator.onAskMenu(NpcReplayedByNpc, nSpeakerTemplateID, NpcReplacedByUser, text));
		}
	}

	public void sendSay(int nSpeakerTypeID, String text, boolean bPrev, boolean bNext){
		sendSay(nSpeakerTypeID, npc, text, (byte) 0, bPrev, bNext);
	}

	public void sendSay(int nSpeakerTypeID, int nSpeakerTemplateID, String text, boolean bPrev, boolean bNext){
		sendSay(nSpeakerTypeID, nSpeakerTemplateID, text, (byte) 0, bPrev, bNext);
	}

	public void sendSay(int nSpeakerTypeID, int nSpeakerTemplateID, String text, byte speaker, boolean bPrev, boolean bNext){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.Say;
		talkData.parseText(text);
		talkData.prev = bPrev;
		talkData.next = bNext;
		getClient().announce(MaplePacketCreator.onSay(nSpeakerTypeID, nSpeakerTemplateID, speaker, text, bPrev, bNext));
	}

	// Old method replaced by sendNext/sendSay parameters
	public void sendNextPrev(String text){
		sendNextPrev(text, (byte) 0, npc);
	}

	// Old method replaced by sendNext/sendSay parameters
	public void sendNextPrev(String text, byte speaker){
		sendNextPrev(text, speaker, npc);
	}

	// Old method replaced by sendNext/sendSay parameters
	public void sendNextPrev(String text, byte speaker, int npc){
		sendNext(npc, text, speaker, true);
	}

	public void sendOk(String text){
		sendOk(npc, text, (byte) 0);
	}

	public void sendOk(String text, byte speaker){
		sendOk(npc, text, speaker);
	}

	public void sendOk(int nSpeakerTemplateID, String text, byte speaker){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.Say;
		talkData.parseText(text);
		getClient().announce(MaplePacketCreator.onSay(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text, false, false));
	}

	public void sendYesNo(String text){
		sendYesNo(npc, text, (byte) 0);
	}

	public void sendYesNo(String text, byte speaker){
		sendYesNo(npc, text, speaker);
	}

	public void sendYesNo(int nSpeakerTemplateID, String text, byte speaker){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskYesNo;
		talkData.parseText(text);
		talkData.next = true;
		talkData.prev = true;
		getClient().announce(MaplePacketCreator.onAskYesNo(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text));
	}

	public void sendAcceptDecline(String text){
		sendAcceptDecline(npc, text, (byte) 0);
	}

	public void sendAcceptDecline(String text, byte speaker){
		sendAcceptDecline(npc, text, speaker);
	}

	public void sendAcceptDecline(int nSpeakerTemplateID, String text, byte speaker){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskAccept;
		talkData.parseText(text);
		talkData.next = true;
		talkData.prev = true;
		getClient().announce(MaplePacketCreator.onAskAccept(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text));
	}

	public void sendSimple(String text){
		sendSimple(npc, text, (byte) 0);
	}

	public void sendSimple(String text, byte speaker){
		sendSimple(npc, text, speaker);
	}

	public void sendSimple(int nSpeakerTemplateID, String text, byte speaker){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskMenu;
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskMenu(NpcReplayedByNpc, nSpeakerTemplateID, speaker, text));
	}

	public void sendStyle(String text, int aCanadite[]){
		sendStyle(npc, text, aCanadite);
	}

	public void sendStyle(int nSpeakerTemplateID, String text, int aCanadite[]){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskMemberShopAvatar;
		talkData.max = aCanadite.length;
		talkData.parseText(text);
		getClient().announce(MaplePacketCreator.onAskMembershopAvatar(NpcReplayedByNpc, nSpeakerTemplateID, text, aCanadite));
	}

	public void sendGetNumber(String text, int def, int min, int max){
		sendGetNumber(npc, text, def, min, max);
	}

	public void sendGetNumber(int nSpeakerTemplateID, String text, int def, int min, int max){
		talkData = new NpcTalkData();
		talkData.def = def;
		talkData.min = min;
		talkData.max = max;
		talkData.messageType = ScriptMessageType.AskNumber;
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskNumber(NpcReplayedByNpc, nSpeakerTemplateID, (byte) 0, text, def, min, max));
	}

	public void sendGetText(String text){
		sendGetText(npc, text);
	}

	public void sendGetText(int nSpeakerTemplateID, String text){
		sendGetText(nSpeakerTemplateID, text, "", 0, 250);
	}

	public void sendGetText(int nSpeakerTemplateID, String text, String sMsgDefault, int nLenMin, int nLenMax){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskText;
		talkData.lenMin = nLenMin;
		talkData.lenMax = nLenMax;
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskText(NpcReplayedByNpc, nSpeakerTemplateID, (byte) 0, text, sMsgDefault, nLenMin, nLenMax));
	}

	public void sendPets(String text, List<Item> pets){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskPet;
		talkData.max = pets.size();
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskPet(NpcReplayedByNpc, npc, text, pets));
	}

	public void sendPetsAll(String text, List<Item> pets){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskPetAll;
		talkData.max = pets.size();
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskPetAll(NpcReplayedByNpc, npc, text, pets, false));
	}

	/*
	 * 0 = ariant colliseum
	 * 1 = Dojo
	 * 2 = Carnival 1
	 * 3 = Carnival 2
	 * 4 = Ghost Ship PQ?
	 * 5 = Pyramid PQ
	 * 6 = Kerning Subway
	 */
	public void sendDimensionalMirror(String text){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskSlideMenu;
		talkData.parseText(text);
		talkData.next = true;
		getClient().announce(MaplePacketCreator.onAskSlideMenu(0x0, npc, false, 0, text));
		// getClient().announce(MaplePacketCreator.getDimensionalMirror(text));
	}

	public void sendSpeedQuiz(byte result, byte isMob, int objectID, int questions, int points, int timeLimit){
		talkData = new NpcTalkData();
		talkData.messageType = ScriptMessageType.AskSpeedQuiz;
		talkData.next = true;
		getClient().announce(MaplePacketCreator.getSpeedQuiz(npc, result, isMob, objectID, questions, points, timeLimit));
	}

	public void setGetText(String text){
		this.getText = text;
	}

	public String getText(){
		return this.getText;
	}

	public int getJobId(){
		return getPlayer().getJob().getId();
	}

	public MapleJob getJob(){
		return getPlayer().getJob();
	}

	public void startQuest(int id){
		try{
			MapleQuest.getInstance(id).forceStart(getPlayer(), npc);
		}catch(NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void completeQuest(int id){
		try{
			MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
		}catch(NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public int getMeso(){
		return getPlayer().getMeso();
	}

	public void gainMeso(int gain){
		getPlayer().gainMeso(gain, true, false, true);
	}

	@Override
	public void gainExp(int gain){
		getPlayer().gainExp(new ExpProperty(ExpGainType.SCRIPT).gain(gain).show().inChat().logData("From npc: " + npc));
	}

	public int getLevel(){
		return getPlayer().getLevel();
	}

	@Override
	public void showEffect(String effect){
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(effect, 3));
	}

	public void setHair(int hair){
		getPlayer().setHair(hair);
		getPlayer().updateSingleStat(MapleStat.HAIR, hair);
		getPlayer().equipChanged();
	}

	public void setFace(int face){
		getPlayer().setFace(face);
		getPlayer().updateSingleStat(MapleStat.FACE, face);
		getPlayer().equipChanged();
	}

	public void setSkin(int color){
		getPlayer().setSkinColor(MapleSkinColor.getById(color));
		getPlayer().updateSingleStat(MapleStat.SKIN, color);
		getPlayer().equipChanged();
	}

	public int itemQuantity(int itemid){
		return getPlayer().getInventory(ItemInformationProvider.getInstance().getInventoryType(itemid)).countById(itemid);
	}

	public void displayGuildRanks(){
		MapleGuild.displayGuildRanks(getClient(), npc);
	}

	public void warpPartyOut(int map){
		for(MaplePartyCharacter mpc : getParty().getMembers()){
			mpc.getPlayerInChannel().changeMap(map);
		}
	}

	@Override
	public MapleParty getParty(){
		return getPlayer().getParty();
	}

	@Override
	public void resetMap(int mapid){
		getClient().getChannelServer().getMap(mapid).resetReactors();
	}

	public void gainCloseness(int closeness){
		for(MaplePet pet : getPlayer().getPets()){
			if(pet.getCloseness() > 30000){
				pet.setCloseness(30000);
				return;
			}
			pet.gainCloseness(getPlayer(), closeness);
			Item petz = getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
			getPlayer().forceUpdateItem(petz);
		}
	}

	public String getName(){
		return getPlayer().getName();
	}

	public int getGender(){
		return getPlayer().getGender();
	}

	public void changeJobById(int a){
		getPlayer().changeJob(MapleJob.getById(a));
	}

	public void changeJob(MapleJob job){
		getPlayer().changeJob(job);
	}

	public MapleJob getJobName(int id){
		return MapleJob.getById(id);
	}

	public MapleStatEffect getItemEffect(int itemId){
		return ItemInformationProvider.getInstance().getItemData(itemId).itemEffect;
	}

	public void resetStats(){
		getPlayer().resetStats();
	}

	public void doGachapon(){
		int[] maps = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};
		MapleGachaponItem item = MapleGachapon.getInstance().process(npc);
		Item itemGained = gainItem(item.getId(), (short) (item.getId() / 10000 == 200 ? 100 : 1), true, true); // For normal potions, make it give 100.
		sendNext("You have obtained a #b#t" + item.getId() + "##k.");
		String map = c.getChannelServer().getMap(maps[(getNpc() != 9100117 && getNpc() != 9100109) ? (getNpc() - 9100100) : getNpc() == 9100109 ? 8 : 9]).getMapData().getMapName();
		BigBrother.logGacha(getPlayer(), item.getId(), map);
		if(item.getTier() > 1){ // Uncommon and Rare
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.gachaponMessage(itemGained, map, getPlayer()));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
	}

	public void disbandAlliance(MapleClient c, int allianceId){
		PreparedStatement ps = null;
		try{
			ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM `alliance` WHERE id = ?");
			ps.setInt(1, allianceId);
			ps.executeUpdate();
			ps.close();
			try{
				ChannelServer.getInstance().getWorldInterface().allianceMessage(c.getPlayer().getGuild().getAllianceId(), MaplePacketCreator.disbandAlliance(allianceId), -1, -1);
				ChannelServer.getInstance().getWorldInterface().disbandAlliance(allianceId);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}catch(SQLException sqle){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
		}finally{
			try{
				if(ps != null && !ps.isClosed()){
					ps.close();
				}
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public boolean canBeUsedAllianceName(String name){
		if(name.contains(" ") || name.length() > 12) return false;
		try{
			ResultSet rs;
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM alliance WHERE name = ?")){
				ps.setString(1, name);
				rs = ps.executeQuery();
				if(rs.next()){
					ps.close();
					rs.close();
					return false;
				}
			}
			rs.close();
			return true;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return false;
		}
	}

	public MapleAlliance createAlliance(MapleCharacter chr1, MapleCharacter chr2, String name){
		int id;
		int guild1 = chr1.getGuildId();
		int guild2 = chr2.getGuildId();
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `alliance` (`name`, `guild1`, `guild2`) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
				ps.setString(1, name);
				ps.setInt(2, guild1);
				ps.setInt(3, guild2);
				ps.executeUpdate();
				try(ResultSet rs = ps.getGeneratedKeys()){
					rs.next();
					id = rs.getInt(1);
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return null;
		}
		MapleAlliance alliance = new MapleAlliance(name, id, guild1, guild2);
		try{
			try{
				ChannelServer.getInstance().getWorldInterface().setGuildAllianceId(guild1, id);
				ChannelServer.getInstance().getWorldInterface().setGuildAllianceId(guild2, id);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
			chr1.setAllianceRank(1, true);
			chr2.setAllianceRank(2, true);
			chr1.saveGuildStatus();
			chr2.saveGuildStatus();
			try{
				ChannelServer.getInstance().getWorldInterface().addAlliance(id, alliance);
				ChannelServer.getInstance().getWorldInterface().allianceMessage(id, MaplePacketCreator.makeNewAlliance(alliance, chr1.getClient()), -1, -1);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return null;
		}
		return alliance;
	}

	public boolean hasMerchant(){
		return getPlayer().hasMerchant();
	}

	public boolean hasMerchantItems(){
		try{
			if(!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) return true;
		}catch(SQLException e){
			return false;
		}
		if(getPlayer().getMerchantMeso() == 0){
			return false;
		}else{
			return true;
		}
	}

	public List<Pair<Item, MapleInventoryType>> getFredrickItems(){
		try{
			return ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false);
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Failed to retrieve fredrick merchant items.");
		}
		return null;
	}

	public void showFredrick(){
		c.announce(MaplePacketCreator.getFredrick(getPlayer()));
	}

	public int partyMembersInMap(){
		int inMap = 0;
		for(MapleCharacter char2 : getPlayer().getMap().getCharacters()){
			if(char2.getParty() == getPlayer().getParty()){
				inMap++;
			}
		}
		return inMap;
	}

	public Event getEvent(){
		return c.getChannelServer().getEvent();
	}

	public void divideTeams(){
		if(getEvent() != null){
			getPlayer().setTeam(getEvent().getLimit() % 2); // muhaha :D
		}
	}

	public boolean createPyramid(String mode, boolean party){// lol
		PyramidMode mod = PyramidMode.valueOf(mode);
		MapleParty partyz = getPlayer().getParty();
		MapleMapFactory mf = c.getChannelServer().getMapFactory();
		MapleMap map = null;
		int mapid = 926010100;
		if(party){
			mapid += 10000;
		}
		mapid += (mod.getMode() * 1000);
		for(byte b = 0; b < 5; b++){// They cannot warp to the next map before the timer ends (:
			map = mf.getMap(c.getChannel(), mapid + b);
			if(map.getCharacters().size() > 0){
				continue;
			}else{
				break;
			}
		}
		if(map == null) return false;
		if(!party){
			partyz = new MapleParty(-1, new MaplePartyCharacter(getPlayer()));
		}
		Pyramid py = new Pyramid(partyz, mod, map.getId());
		getPlayer().setPartyQuest(py);
		py.warp(mapid);
		dispose();
		return true;
	}

	public final void closeUI(int map){
		c.getChannelServer().getMap(map).broadcastMessage(UserLocal.disableUI(false));
	}

	public String getMobImg(int mob){
		String mobStr = String.valueOf(mob);
		while(mobStr.length() < 7){
			String newStr = "0" + mobStr;
			mobStr = newStr;
		}
		return "#fMob/" + mobStr + ".img/stand/0#";
	}

	public String getRSSkillHighestLevelName(String skillName){
		String name = "";
		RSSkill skill = RSSkill.valueOf(skillName);
		for(Triple<RSSkill, String, Byte> t : Server.getInstance().getHighestRSLevels()){
			if(t.left.equals(skill)){
				name = t.mid;
			}
		}
		return name;
	}

	public byte getRSSkillHighestLevel(String skillName){
		byte level = 0;
		RSSkill skill = RSSkill.valueOf(skillName);
		for(Triple<RSSkill, String, Byte> t : Server.getInstance().getHighestRSLevels()){
			if(t.left.equals(skill)){
				level = t.right;
			}
		}
		return level;
	}

	public long getRSSkillHighestExp(String skillName){
		long exp = 0;
		RSSkill skill = RSSkill.valueOf(skillName);
		for(Triple<RSSkill, String, Long> t : Server.getInstance().getHighestRSExp()){
			if(t.left.equals(skill)){
				exp = t.right;
			}
		}
		return exp;
	}

	public SlayerTask createSlayerTask(){
		Random rand = new Random();
		SlayerTask task = null;
		int calls = 0;
		int levelRange = 10;
		List<Integer> mobIds = new ArrayList<Integer>(MobConstants.slayerMobs.keySet());
		double maxCalls = mobIds.size();
		maxCalls /= 10D;
		while(task == null){
			int monsterID = mobIds.get(rand.nextInt(mobIds.size()));
			Pair<Integer, String> data = MobConstants.slayerMobs.get(monsterID);
			int monsterLevel = data.left;
			int level = getLevel() - monsterLevel;
			if(((getLevel() > MobConstants.HIGH_LEVEL_MOB && monsterLevel > MobConstants.HIGH_LEVEL_MOB) || (level <= levelRange && level >= -levelRange)) && rand.nextBoolean()){
				if(MapleLifeFactory.getMonster(monsterID).getStats().getLink() == 0) task = new SlayerTask(monsterID, monsterLevel, data.right);
			}
			if(++calls >= maxCalls){
				levelRange += 2;
				calls = 0;
			}
		}
		task.setRequiredKills(getLevel() * 2);
		return task;
	}

	public String getSlayerMobImg(int mob){
		MapleMonster monster = MapleLifeFactory.getMonster(mob);
		String mobStr = String.valueOf(mob);
		if(monster.getStats().getLink() != 0){
			mobStr = String.valueOf(monster.getStats().getLink());
		}
		while(mobStr.length() < 7){
			String newStr = "0" + mobStr;
			mobStr = newStr;
		}
		if(monster.getStats().getDefaultMoveType() != null) return "#fMob/" + mobStr + ".img/" + monster.getStats().getDefaultMoveType() + "/0#";
		else return "";
	}

	public void makeReservation(boolean cathedral, boolean premium){// TODO: Chapel invites
		MapleCharacter groom = getPlayer();
		MapleCharacter bride = getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(groom.getMarriedTo());
		MapleWedding wedding = Server.getInstance().getWeddingByID(getPlayer().getMarriageID());// We make the MapleWedding setup at ingagement.
		if(wedding != null){
			wedding.setCathedral(cathedral ? 1 : 0);
			wedding.setPremium(premium ? 1 : 0);
			MapleInventoryManipulator.addFromDrop(groom.getClient(), new Item(4031395, (short) (premium ? 15 : 10)), false);// Invites
			MapleInventoryManipulator.addFromDrop(bride.getClient(), new Item(4031395, (short) (premium ? 15 : 10)), false);
			int receipt = 4031375;
			receipt += (!cathedral ? 1 : 0);
			receipt += (!premium ? 105 : 0);
			MapleInventoryManipulator.addFromDrop(groom.getClient(), new Item(receipt, (short) 1), false);// Receipt
		}
	}

	public MapleWedding getCurrentWedding(boolean cathedral){
		int marriageID = cathedral ? getClient().getChannelServer().getCurrentCathedralMarriageID() : getClient().getChannelServer().getCurrentChapelMarriageID();
		if(marriageID >= 0) return Server.getInstance().getWeddingByID(marriageID);
		return null;
	}

	public boolean isInvitedToCurrentWedding(boolean cathedral){
		MapleWedding wedding = getCurrentWedding(cathedral);
		if(wedding != null) return wedding.getInvited().contains(getPlayer().getId());
		return false;
	}

	public void removeCurrentWedding(boolean cathedral){
		if(cathedral) getClient().getChannelServer().setCurrentCathedralMarriageID(-1);
		else getClient().getChannelServer().setCurrentChapelMarriageID(-1);
	}

	public void startWedding(EventInstanceManager eim){// Sets are current wedding in channel, sets wedding status to 1,
		// Creates 30 minutes time(think its 20 in gms). After 10 minutes if ceremony hasn't started auto start it.
		MapleWedding wedding = Server.getInstance().getWeddingByID(getPlayer().getMarriageID());
		wedding.setEIM(eim);
		wedding.setState(1);
		if(wedding.isCathedral()) getClient().getChannelServer().setCurrentCathedralMarriageID(getPlayer().getMarriageID());
		else getClient().getChannelServer().setCurrentChapelMarriageID(getPlayer().getMarriageID());
		getPlayer().changeMap(wedding.isCathedral() ? 680000210 : 680000110, 2);
		getPlayer().getPartner().changeMap(wedding.isCathedral() ? 680000210 : 680000110, 2);
	}

	public void startCeremony(){// After 10 minutes(or the bride/groom talk to debbie) the ceremony starts.
		// Set wedding status to 2
		getPlayer().getEventInstance().schedule("autoCeremony", 0);
		// MapleWedding wedding = Server.getInstance().getWeddingByID(getPlayer().getMarriageID());
		// wedding.setState(2);
	}

	public int getWeddingStatus(){
		MapleWedding wedding = Server.getInstance().getWeddingByID(getPlayer().getMarriageID());
		if(wedding == null) return -1;
		return wedding.getStatus();
	}

	public int isWeddingCouple(){
		MapleWedding wedding = getCurrentWedding(isCathedral());
		if(wedding != null){
			if(wedding.getPlayer1() == getPlayer().getId() || wedding.getPlayer2() == getPlayer().getId()) return 1;
			else return 0;
		}
		return 0;
	}

	public int getWeddingState(){
		MapleWedding wedding = getCurrentWedding(isCathedral());
		if(wedding != null) return wedding.getState();
		return 0;
	}

	public void setWeddingState(int state){
		MapleWedding wedding = getCurrentWedding(isCathedral());
		if(wedding != null){
			wedding.setState(state);
		}
	}

	public boolean isCathedral(){
		return getPlayer().getMapId() == 680000210 || getPlayer().getMapId() == 680000200;
	}

	public String getWhatDropsFrom(String monsterName){
		// String monsterName = joinStringFrom(sub, 1);
		String output = "";
		int drops = 0;
		int limit = 3;
		Iterator<Pair<Integer, String>> listIterator = MapleMonsterInformationProvider.getInstance().getMobsIDsFromName(monsterName).iterator();
		for(int i = 0; i < limit; i++){
			if(listIterator.hasNext()){
				Pair<Integer, String> data = listIterator.next();
				int mobId = data.getLeft();
				String mobName = data.getRight();
				output += "#b" + mobName + " drops the following items:#k\r\n\r\n";
				for(MonsterDropEntry drop : MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId)){
					try{
						String name = ItemInformationProvider.getInstance().getItemData(drop.itemId).name;
						if(name == null || name.isEmpty() || drop.chance == 0){
							continue;
						}
						// float chance = (float) (1000000 / drop.chance / getPlayer().getStats().getDropRate());
						output += " " + name + "\r";// + " (1/" + (int) chance + ")\r\n";
						drops++;
					}catch(Exception ex){
						continue;
					}
				}
				output += "\r\n\r\n";
			}
		}
		if(drops == 0) return "";
		return output;
	}

	public String getWhoDrops(String searchString){
		String output = "";
		Iterator<Pair<Integer, String>> listIterator = ItemInformationProvider.getInstance().getItemDataByName(searchString).iterator();
		if(listIterator.hasNext()){
			int count = 1;
			while(listIterator.hasNext() && count <= 3){
				Pair<Integer, String> data = listIterator.next();
				output += "#b" + data.getRight() + "#k is dropped by:\r\n";
				try{
					PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM drop_data WHERE itemid = ? LIMIT 50");
					ps.setInt(1, data.getLeft());
					ResultSet rs = ps.executeQuery();
					while(rs.next()){
						String resultName = MapleMonsterInformationProvider.getMobNameFromID(rs.getInt("dropperid"));
						if(resultName != null){
							output += resultName + ", ";
						}
					}
					rs.close();
					ps.close();
				}catch(Exception e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					return "There was a problem retreiving the required data. Please try again.";
				}
				output += "\r\n\r\n";
				count++;
			}
			return output;
		}
		return "The item you searched for doesn't exist.";
	}

	public boolean isValidHair(int hairid){
		return MapleCharacterInfo.getInstance().getFaces().get(hairid) != null;
	}

	@Override
	public Item gainItem(MapleCharacter mc, int id, short quantity, boolean randomStats, boolean showMessage, long expires, boolean checkSpace, String gainLog){
		gainLog += " NPC: " + this.npc + " Script Name: " + this.scriptName;
		return super.gainItem(mc, id, quantity, randomStats, showMessage, expires, checkSpace, gainLog);
	}

	public void addGuildMeso(long meso){
		try{
			ChannelServer.getInstance().getWorldInterface().addGuildMeso(getPlayer().getGuildId(), meso);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public String getVeteranHunter(){
		Connection con = DatabaseConnection.getConnection();
		Calendar cal = Calendar.getInstance();
		String yearMonth = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONDAY);
		Map<Integer, Long> kills = new HashMap<>();
		try(PreparedStatement ps = con.prepareStatement("SELECT id, higher FROM monsterkills WHERE date = ? ORDER BY higher DESC LIMIT 10")){
			ps.setString(1, yearMonth);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					kills.put(rs.getInt(1), rs.getLong(2));
				}
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		Map<Integer, String> killNames = new HashMap<>();
		try(PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")){
			for(int id : kills.keySet()){
				ps.setInt(1, id);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()){
						killNames.put(id, rs.getString(1));
					}
				}
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		String result = "";
		int rank = 0;
		for(int id : kills.keySet()){
			result += ++rank + ". #b" + killNames.get(id) + "#k : #r" + kills.get(id) + "#k monsters.\r\n";
		}
		return result;
	}
}
