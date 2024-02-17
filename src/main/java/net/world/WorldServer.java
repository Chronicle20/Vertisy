package net.world;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import client.BuddyList;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.BuddylistEntry;
import constants.ServerConstants;
import net.center.CenterRegistry;
import net.center.CenterWorldInterface;
import net.channel.ChannelWorldInterface;
import net.rmi.VertisyClientSocketFactory;
import net.rmi.VertisyServerSocketFactory;
import net.server.PlayerBuffStorage;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import net.server.world.*;
import net.world.family.Family;
import server.TimerManager;
import tools.BigBrother;
import tools.DatabaseConnection;
import tools.MapUtil;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class WorldServer{

	private static int worldServerID = -1;
	private static WorldServer instance;
	private CenterRegistry centerRegistry;
	private WorldCenterInterface wci;
	private CenterWorldInterface cwi;
	private VertisyClientSocketFactory socketFactory;
	private boolean settingUpRMI = false;
	private Map<Integer, MapleAlliance> alliances = new LinkedHashMap<>();
	private Map<Integer, MapleGuild> guilds = new LinkedHashMap<>();
	private Map<Integer, MapleGuildSummary> gsStore = new HashMap<>();
	private Map<Integer, MapleParty> parties = new HashMap<>();
	private Map<Integer, Family> families = new HashMap<>();
	private AtomicInteger runningPartyId = new AtomicInteger();
	private PlayerBuffStorage buffStorage = new PlayerBuffStorage();
	private Map<Integer, MapleMessenger> messengers = new HashMap<>();
	private AtomicInteger runningMessengerId = new AtomicInteger();
	private ScheduledFuture<?> serverCheck = null;
	private boolean checkingServers = false;
	public String serverMessage = "";

	private WorldServer(){
		long start = System.currentTimeMillis();
		if(worldServerID == -1){
			System.out.println("World Server ID not set. Shutting down.");
			return;
		}
		System.setProperty("java.rmi.server.hostname", ServerConstants.HOST);
		System.setProperty("wzpath", "wz");
		TimerManager tMan = TimerManager.getInstance();
		tMan.start();
		Logger.start();
		socketFactory = new VertisyClientSocketFactory((byte) 0xAF);
		runningPartyId.set(1);
		runningMessengerId.set(1);
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT guildid FROM guilds WHERE world = ?")){
			ps.setInt(1, worldServerID);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					getGuild(rs.getInt(1), null);
				}
			}
			System.out.println("Loaded " + guilds.size() + " guilds.");
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT familyid, world, bossID, familyName, statistic, privilege, privilegeUse FROM families WHERE world = ?")){
			ps.setInt(1, worldServerID);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					synchronized(families){
						Family family = new Family();
						family.load(rs);
						families.put(rs.getInt(1), family);
					}
				}
			}
			System.out.println("Loaded " + families.size() + " families.");
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		tMan.register("WorldServer-CenterConnection", ()-> {
			if(settingUpRMI) return;
			settingUpRMI = true;
			Registry registry;
			try{
				if(centerRegistry != null){
					centerRegistry.isConnected();
				}else{
					System.out.println("Connecting to center server: " + ServerConstants.CENTER_SERVER_HOST + ":" + ServerConstants.CENTER_SERVER_PORT);
					registry = LocateRegistry.getRegistry(ServerConstants.CENTER_SERVER_HOST, ServerConstants.CENTER_SERVER_PORT, socketFactory);
					centerRegistry = (CenterRegistry) registry.lookup("CenterRegistry");
					wci = new WorldCenterInterfaceImpl();
					cwi = centerRegistry.registerWorldServer(worldServerID, wci);
					for(int channelServer : WorldRegistryImpl.getInstance().getChannelServers().keySet()){
						ChannelWorldInterface cwii = WorldRegistryImpl.getInstance().getChannelServers().get(channelServer);
						for(int ch : cwii.getAllChannels()){
							cwi.registerChannel(getID(), ch, cwii.getChannelLoad(ch));
						}
					}
					System.out.println("Connected to Center Server.");
				}
				settingUpRMI = false;
			}catch(RemoteException | NullPointerException | NotBoundException ex){
				// ex.printStackTrace();
				System.out.println("[WorldServer] Failed to reconnect to the Center Server. Trying again in 10 seconds");
				centerRegistry = null;
				wci = null;
				cwi = null;
				registry = null;
				settingUpRMI = false;
				if(serverCheck != null) serverCheck.cancel(true);
				serverCheck = null;
			}
		}, 10000, 0);
		try{
			System.out.println("Binding to port: " + ServerConstants.WORLD_SERVER_PORT[worldServerID]);
			// SslRMIClientSocketFactory
			// SslRMIServerSocketFactory
			Registry worldRegistry = LocateRegistry.createRegistry(ServerConstants.WORLD_SERVER_PORT[worldServerID], new VertisyClientSocketFactory((byte) 0xAF), new VertisyServerSocketFactory((byte) 0xAF));
			worldRegistry.rebind("WorldRegistry-" + worldServerID, WorldRegistryImpl.getInstance());
			System.out.println("WorldRegistry binded.");
			if(serverCheck != null) serverCheck.cancel(true);
			serverCheck = TimerManager.getInstance().register("WorldServerCheck", ()-> {
				if(checkingServers) return;
				checkingServers = true;
				try{
					// List<Integer> checkedChannels = new ArrayList<>();
					// Currently if a channel forcefully dies the World has no idea what channels where on that ChannelServer.
					// This results in likely login/site status showing incorrect status.
					for(int ch : WorldRegistryImpl.getInstance().getChannelServers().keySet()){
						ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannelServers().get(ch);
						try{
							cwi.isConnected();
							// checkedChannels.addAll(cwi.getAllChannels());
						}catch(Exception ex){
							WorldRegistryImpl.getInstance().getChannelServers().remove(ch);
							WorldServer.getInstance().getCenterInterface().removeChannel(WorldServer.getInstance().getID(), ch);
							System.out.println("Lost connection to channel server: " + ch);
						}
					}
					// for(int i = 0; i < 20; i++) {
					// }
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				checkingServers = false;
			}, 30000);
		}catch(RemoteException | NullPointerException ex){
			System.out.println("Could not initialize RMI system");
			ex.printStackTrace();
			shutdown();
			return;
		}
		System.out.println("World Server " + worldServerID + " loaded in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
	}

	public int getID(){
		return worldServerID;
	}

	public CenterRegistry getCenterRegistry(){
		return centerRegistry;
	}

	public CenterWorldInterface getCenterInterface(){
		return cwi;
	}

	public static WorldServer getInstance(){
		return instance;
	}

	public void broadcastPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastPacket(packet);
		}
	}

	public void broadcastPacket(List<Integer> players, byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastPacket(players, packet);
		}
	}

	public void broadcastPacketToPlayers(List<String> players, byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastPacketToPlayers(players, packet);
		}
	}

	public void broadcastGMPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastGMPacket(packet);
		}
	}

	public void shutdown(){
		System.out.println("Shutting down World Server");
		try{
			if(centerRegistry != null) centerRegistry.removeWorldServer(worldServerID);
		}catch(RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		centerRegistry = null;
		wci = null;
		cwi = null;
		TimerManager.getInstance().stop();
		System.exit(0);
	}

	public void saveWorldData(){
		for(MapleGuild guild : WorldServer.getInstance().getGuilds()){
			guild.writeToDB(false);
		}
		for(MapleAlliance alliance : WorldServer.getInstance().getAlliances()){
			alliance.saveToDB();
		}
		for(Family family : families.values()){
			family.save();
		}
	}

	public static void main(String[] args){
		worldServerID = Integer.parseInt(args[0]);
		instance = new WorldServer();
	}

	public Collection<MapleGuild> getGuilds(){
		return guilds.values();
	}

	public Collection<MapleAlliance> getAlliances(){
		return alliances.values();
	}

	public MapleAlliance getAlliance(int id){
		synchronized(alliances){
			if(alliances.containsKey(id)) return alliances.get(id);
			return null;
		}
	}

	public void addAlliance(int id, MapleAlliance alliance){
		synchronized(alliances){
			if(!alliances.containsKey(id)){
				alliances.put(id, alliance);
			}
		}
	}

	public void disbandAlliance(int id){
		synchronized(alliances){
			MapleAlliance alliance = alliances.get(id);
			if(alliance != null){
				for(Integer gid : alliance.getGuilds()){
					guilds.get(gid).setAllianceId(0);
				}
				alliances.remove(id);
			}
		}
	}

	public void allianceMessage(int id, final byte[] packet, int exception, int guildex){
		MapleAlliance alliance = alliances.get(id);
		if(alliance != null){
			for(Integer gid : alliance.getGuilds()){
				if(guildex == gid){
					continue;
				}
				MapleGuild guild = guilds.get(gid);
				if(guild != null){
					guild.broadcast(packet, exception);
				}
			}
		}
	}

	public boolean addGuildtoAlliance(int aId, int guildId){
		MapleAlliance alliance = alliances.get(aId);
		if(alliance != null){
			alliance.addGuild(guildId);
			return true;
		}
		return false;
	}

	public boolean removeGuildFromAlliance(int aId, int guildId){
		MapleAlliance alliance = alliances.get(aId);
		if(alliance != null){
			alliance.removeGuild(guildId);
			return true;
		}
		return false;
	}

	public boolean setAllianceRanks(int aId, String[] ranks){
		MapleAlliance alliance = alliances.get(aId);
		if(alliance != null){
			alliance.setRankTitle(ranks);
			return true;
		}
		return false;
	}

	public boolean setAllianceNotice(int aId, String notice){
		MapleAlliance alliance = alliances.get(aId);
		if(alliance != null){
			alliance.setNotice(notice);
			return true;
		}
		return false;
	}

	public int createGuild(int leaderId, String name){
		return MapleGuild.createGuild(leaderId, name);
	}

	public MapleGuild getGuild(int id, MapleGuildCharacter mgc){
		synchronized(guilds){
			MapleGuild g = guilds.get(id);
			if(g != null) return g;
			g = new MapleGuild(id, worldServerID);
			if(g.getId() == -1) return null;
			if(mgc != null){
				g.setOnline(mgc, true, mgc.getChannel());
			}
			guilds.put(id, g);
			return g;
		}
	}

	public MapleGuild getGuildIfExists(int id){
		synchronized(guilds){
			return guilds.get(id);
		}
	}

	public MapleGuild getGuild(String name){
		synchronized(guilds){
			for(MapleGuild g : guilds.values()){
				if(g.getName().equalsIgnoreCase(name)) return g;
			}
			return null;
		}
	}

	public void clearGuilds(){// remake
		synchronized(guilds){
			guilds.clear();
		}
		// for (List<Channel> world : worlds.values()) {
		// reloadGuildCharacters();
	}

	public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel){
		MapleGuild g = getGuild(mgc.getGuildId(), mgc);
		g.setOnline(mgc, bOnline, channel);
	}

	public int addGuildMember(MapleGuildCharacter mgc){
		MapleGuild g = guilds.get(mgc.getGuildId());
		if(g != null) return g.addGuildMember(mgc);
		return 0;
	}

	public boolean setGuildAllianceId(int gId, int aId){
		MapleGuild guild = guilds.get(gId);
		if(guild != null){
			guild.setAllianceId(aId);
			return true;
		}
		return false;
	}

	public void leaveGuild(MapleGuildCharacter mgc){
		MapleGuild g = guilds.get(mgc.getGuildId());
		if(g != null){
			g.leaveGuild(mgc);
		}
	}

	public void guildChat(int gid, String sourceName, int sourceid, String msg){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.guildChat(sourceName, sourceid, msg);
		}
	}

	public void changeRank(int gid, int cid, int newRank){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.changeRank(cid, newRank);
		}
	}

	public void expelMember(MapleGuildCharacter initiator, String name, int cid){
		MapleGuild g = guilds.get(initiator.getGuildId());
		if(g != null){
			g.expelMember(initiator, name, cid);
		}
	}

	public void setGuildNotice(int gid, String notice){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.setGuildNotice(notice);
		}
	}

	public void memberLevelJobUpdate(MapleGuildCharacter mgc){
		MapleGuild g = guilds.get(mgc.getGuildId());
		if(g != null){
			g.memberLevelJobUpdate(mgc);
		}
	}

	public void changeRankTitle(int gid, String[] ranks){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.changeRankTitle(ranks);
		}
	}

	public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.setGuildEmblem(bg, bgcolor, logo, logocolor);
		}
	}

	public void disbandGuild(int gid){
		synchronized(guilds){
			MapleGuild g = guilds.get(gid);
			g.disbandGuild();
			guilds.remove(gid);
		}
	}

	public String increaseGuildCapacity(int gid){
		MapleGuild g = guilds.get(gid);
		if(g != null) return g.increaseCapacity();
		return "null";
	}

	public void gainGP(int gid, int chrid, int amount) throws RemoteException{
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.gainGP(amount);
		}
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			try{
				cwi.gainGP(chrid, amount);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
	}

	public void guildMessage(int gid, byte[] packet){
		guildMessage(gid, packet, -1);
	}

	public void guildMessage(int gid, byte[] packet, int exception){
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.broadcast(packet, exception);
		}
	}

	public void deleteGuildCharacter(MapleGuildCharacter mgc){
		setGuildMemberOnline(mgc, false, (byte) -1);
		if(mgc.getGuildRank() > 1){
			leaveGuild(mgc);
		}else{
			disbandGuild(mgc.getGuildId());
		}
	}

	public void setAllianceRank(int chrid, int rank) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			if(cwi.setAllianceRank(chrid, rank)) return;
		}// if it wasn't updated.
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET allianceRank = ? WHERE id = ?")){
			ps.setInt(1, rank);
			ps.setInt(2, chrid);
			ps.executeUpdate();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	/*public void reloadGuildCharacters(int world){
		for(MapleCharacter mc : worlda.getAllCharacters()){
			if(mc.getGuildId() > 0){
				setGuildMemberOnline(mc.getMGC(), true, worlda.getId());
				memberLevelJobUpdate(mc.getMGC());
			}
		}
		reloadGuildSummary();
	}*/
	public int[] getTopGuilds(){
		Map<Integer, Integer> allGuilds = new LinkedHashMap<Integer, Integer>();
		for(MapleGuild guild : guilds.values()){
			if(guild.getName() != null && !guild.getName().equalsIgnoreCase("Vertisy") && guild.getName().length() > 0) allGuilds.put(guild.getId(), guild.getGP());
		}
		allGuilds = MapUtil.sortByComparator(allGuilds, false);
		int pos = 0;
		int[] top = new int[allGuilds.size()];
		for(int guildID : allGuilds.keySet()){
			top[pos++] = guildID;
		}
		return top;
	}

	public MapleGuild getGuild(MapleGuildCharacter mgc){
		if(mgc == null) return null;
		int gid = mgc.getGuildId();
		MapleGuild g = getGuild(gid, mgc);
		if(gsStore.get(gid) == null){
			gsStore.put(gid, new MapleGuildSummary(g));
		}
		return g;
	}

	public MapleGuildSummary getGuildSummary(int gid){
		if(gsStore.containsKey(gid)){
			return gsStore.get(gid);
		}else{
			MapleGuild g = getGuild(gid, null);
			if(g != null){
				gsStore.put(gid, new MapleGuildSummary(g));
			}
			return gsStore.get(gid);
		}
	}

	public void updateGuildSummary(int gid, MapleGuildSummary mgs){
		gsStore.put(gid, mgs);
	}

	public void reloadGuildSummary(){
		MapleGuild g;
		for(int i : gsStore.keySet()){
			g = getGuild(i, null);
			if(g != null){
				gsStore.put(i, new MapleGuildSummary(g));
			}else{
				gsStore.remove(i);
			}
		}
	}

	public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException{
		for(int cid : cids){
			if(cid != exception){
				setGuildAndRank(cid, guildid, rank);
			}
		}
	}

	public void setOfflineGuildStatus(int guildid, int guildrank, int cid, int gp){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, gp = ? WHERE id = ?")){
				ps.setInt(1, guildid);
				ps.setInt(2, guildrank);
				ps.setInt(3, gp);
				ps.setInt(4, cid);
				ps.execute();
			}
		}catch(SQLException se){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, se);
		}
	}

	public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.setGuildAndRank(cid, guildid, rank);
		}
	}

	public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException{
		updateGuildSummary(gid, mgs);
		try{
			for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
				cwi.broadcastPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()));
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		setGuildAndRank(affectedPlayers, -1, -1, -1); // respawn player
	}

	public void removeGP(int gid, int chrid, int gp) throws RemoteException{
		MapleGuild g = guilds.get(gid);
		if(g != null){
			g.removeGP(gp);
		}
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			try{
				cwi.removeGP(chrid, gp);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
	}

	public void updateTopGuilds() throws RemoteException{
		int[] topGuilds = getTopGuilds();
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			try{
				cwi.setTopGuilds(topGuilds);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
	}

	public MapleParty createParty(MaplePartyCharacter chrfor){
		int partyid = runningPartyId.getAndIncrement();
		MapleParty party = new MapleParty(partyid, chrfor);
		parties.put(party.getId(), party);
		return party;
	}

	public MapleParty getParty(int partyid){
		return parties.get(partyid);
	}

	public MapleParty disbandParty(int partyid){
		return parties.remove(partyid);
	}

	public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target){
		try{
			for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
				cwi.updateParty(party, operation, target);
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target){
		MapleParty party = getParty(partyid);
		if(party == null) throw new IllegalArgumentException("no party with the specified partyid exists");
		switch (operation){
			case JOIN:
				party.addMember(target);
				break;
			case EXPEL:
			case LEAVE:
				party.removeMember(target);
				break;
			case DISBAND:
				disbandParty(partyid);
				break;
			case SILENT_UPDATE:
			case LOG_ONOFF:
				party.updateMember(target);
				break;
			case CHANGE_LEADER:
				party.setLeader(target);
				break;
			case UPDATE_DOOR:
				party.updateMember(target);
				break;
			default:
				System.out.println("Unhandeled updateParty operation " + operation.name());
		}
		updateParty(party, operation, target);
	}

	public void partyChat(MapleParty party, String chattext, String source) throws RemoteException{
		BigBrother.party(party, chattext, source);
		List<String> targets = new ArrayList<>();
		for(MaplePartyCharacter partychar : party.getMembers()){
			if(!(partychar.getName().equals(source))){
				targets.add(partychar.getName());
				/*MapleCharacter chr = getCharacterByName(partychar.getName());
				if(chr != null){
					chr.getClient().announce(MaplePacketCreator.multiChat(source.getName(), chattext, 1));
				}*/
			}
		}
		broadcastPacketToPlayers(targets, MaplePacketCreator.multiChat(source, chattext, 1));
	}

	public void buddyChat(int[] recipientCharacterIds, String source, int sourceid, String chattext) throws RemoteException{
		BigBrother.buddy(recipientCharacterIds, source, chattext);
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.buddyChat(recipientCharacterIds, source, sourceid, chattext);
		}
	}

	public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) throws RemoteException{
		List<Integer> target = new ArrayList<>();
		target.add(cid);
		BuddyList buddylist = null;
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			BuddyList list = cwi.getBuddylist(cid);
			if(list != null) buddylist = list;
		}
		if(buddylist != null){
			switch (operation){
				case ADDED:
					if(buddylist.contains(cidFrom)){
						BuddylistEntry entry = new BuddylistEntry(name, "Default Group", cidFrom, channel, true);
						for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
							if(cwi.addToBuddylist(cid, entry)) continue;
						}
						broadcastPacket(target, CWvsContext.Friend.updateBuddyChannel(entry));
					}
					break;
				case DELETED:
					if(buddylist.contains(cidFrom)){
						BuddylistEntry entry = new BuddylistEntry(name, "Default Group", cidFrom, (byte) -1, buddylist.get(cidFrom).isVisible());
						for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
							if(cwi.addToBuddylist(cid, entry)) break;
						}
						broadcastPacket(target, CWvsContext.Friend.updateBuddyChannel(entry));
					}
					break;
			}
		}
	}

	public BuddyAddResult requestBuddyAdd(int chrid, int channelFrom, int cidFrom, String nameFrom) throws RemoteException{
		BuddyList buddylist = null;
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			BuddyList list = cwi.getBuddylist(chrid);
			if(list != null){
				buddylist = list;
				break;
			}
		}
		if(buddylist != null){
			if(buddylist.isFull()) return BuddyAddResult.BUDDYLIST_FULL;
			if(!buddylist.contains(cidFrom)){
				for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
					if(cwi.addBuddyRequest(chrid, cidFrom, nameFrom, channelFrom)) break;
				}
			}else if(buddylist.containsVisible(cidFrom)) return BuddyAddResult.ALREADY_ON_LIST;
		}
		return BuddyAddResult.OK;
	}

	public void updateBuddies(int characterId, int channel, int[] buddies, boolean offline) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.updateBuddies(buddies, characterId, channel, offline);
		}
	}

	public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException{
		List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
		for(int channel : WorldRegistryImpl.getInstance().getChannelServers().keySet()){
			ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannelServers().get(channel);
			for(int charid : cwi.multiBuddyFind(charIdFrom, characterIds)){
				foundsChars.add(new CharacterIdChannelPair(charid, channel));
			}
		}
		return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
	}

	public PlayerBuffStorage getPlayerBuffStorage(){
		return buffStorage;
	}

	public MapleMessenger getMessenger(int messengerid){
		return messengers.get(messengerid);
	}

	public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException{
		MapleMessenger messenger = getMessenger(messengerid);
		if(messenger == null){ throw new IllegalArgumentException("No messenger with the specified messengerid exists"); }
		int position = messenger.getPositionByName(target.getName());
		messenger.removeMember(target);
		removeMessengerPlayer(messenger, position);
	}

	public boolean isInAMessenger(String target){
		for(MapleMessenger messenger : messengers.values()){
			for(MapleMessengerCharacter mmc : messenger.getMembers()){
				if(mmc.getName().equals(target)) return true;
			}
		}
		return false;
	}

	public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException{
		if(!isInAMessenger(target)){
			broadcastPacketToPlayers(Arrays.asList(target), MaplePacketCreator.messengerInvite(sender, messengerid));
			broadcastPacketToPlayers(Arrays.asList(sender), MaplePacketCreator.messengerNote(target, 4, 1));
		}else{
			broadcastPacketToPlayers(Arrays.asList(sender), MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
		}
	}

	public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.addMessengerPlayer(messenger, namefrom, fromchannel, position);
		}
	}

	public void removeMessengerPlayer(MapleMessenger messenger, int position) throws RemoteException{
		List<Integer> sendTo = new ArrayList<>();
		for(MapleMessengerCharacter messengerchar : messenger.getMembers()){
			sendTo.add(messengerchar.getId());
		}
		if(!sendTo.isEmpty()) this.broadcastPacket(sendTo, MaplePacketCreator.removeMessengerPlayer(position));
	}

	public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException{
		String from = "";
		String to1 = "";
		String to2 = "";
		List<Integer> target = new ArrayList<>();
		for(MapleMessengerCharacter messengerchar : getMessenger(messengerid).getMembers()){
			if(!(messengerchar.getName().equals(namefrom))){
				target.add(messengerchar.getId());
				if(to1.equals("")){
					to1 = messengerchar.getName();
				}else if(to2.equals("")){
					to2 = messengerchar.getName();
				}
			}else{
				from = messengerchar.getName();
			}
		}
		if(!target.isEmpty()) broadcastPacket(target, MaplePacketCreator.messengerChat(chattext));
		BigBrother.messenger(from, to1, to2, chattext);
	}

	public void declineChat(String target, String namefrom) throws RemoteException{
		broadcastPacketToPlayers(Arrays.asList(target), MaplePacketCreator.messengerNote(namefrom, 5, 0));
	}

	public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException{
		MapleMessenger messenger = getMessenger(messengerid);
		int position = messenger.getPositionByName(namefrom);
		updateMessenger(messenger, namefrom, position, fromchannel);
	}

	public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.updateMessenger(messenger, namefrom, position, fromchannel);
		}
	}

	public void silentLeaveMessenger(int messengerid, MapleMessengerCharacter target){
		MapleMessenger messenger = getMessenger(messengerid);
		if(messenger == null){ throw new IllegalArgumentException("No messenger with the specified messengerid exists"); }
		messenger.addMember(target, target.getPosition());
	}

	public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException{
		MapleMessenger messenger = getMessenger(messengerid);
		if(messenger == null){ throw new IllegalArgumentException("No messenger with the specified messengerid exists"); }
		messenger.addMember(target, target.getPosition());
		addMessengerPlayer(messenger, from, fromchannel, target.getPosition());
	}

	public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position){
		MapleMessenger messenger = getMessenger(messengerid);
		if(messenger == null){ throw new IllegalArgumentException("No messenger with the specified messengerid exists"); }
		messenger.addMember(target, position);
	}

	public MapleMessenger createMessenger(MapleMessengerCharacter chrfor){
		int messengerid = runningMessengerId.getAndIncrement();
		MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
		messengers.put(messenger.getId(), messenger);
		return messenger;
	}

	public Family getFamily(int familyid){
		synchronized(families){
			return families.get(familyid);
		}
	}

	public void addFamily(Family family){
		synchronized(families){
			families.put(family.familyID, family);
		}
	}
}
