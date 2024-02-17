package net.channel;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.command.CommandHandler;
import constants.MobConstants;
import constants.ServerConstants;
import constants.WorldConstants.WorldInfo;
import net.MiscLoading;
import net.rmi.VertisyClientSocketFactory;
import net.server.PlayerStorage;
import net.server.channel.Channel;
import net.world.WorldChannelInterface;
import net.world.WorldRegistry;
import provider.MapleDataProviderFactory;
import server.ItemInformationProvider;
import server.MakerItemFactory;
import server.MapleCharacterInfo;
import server.TimerManager;
import server.cashshop.CashItemFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapData;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import server.reactors.MapleReactorFactory;
import tools.AutoJCE;
import tools.ObjectParser;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class ChannelServer{

	private Map<Integer, Channel> channels = new HashMap<>();
	private MapleMapFactory mapFactory;
	private static int worldServerID = -1;
	private static int[] channelServerID = null;
	private static ChannelServer instance;
	private Map<Integer, MapleMapData> mapDatas = new ConcurrentHashMap<>();
	private VertisyClientSocketFactory socketFactory;
	private WorldRegistry worldRegistry;
	private ChannelWorldInterface cwi;
	private WorldChannelInterface wci;
	private int expRate = 1, questExpRate = 1, dropRate = 1, mesoRate = 1;
	private PlayerStorage storage = new PlayerStorage();// mts/cs
	private PlayerStorage tempStorage = new PlayerStorage();// ccing
	private int[] topGuilds = new int[0];
	private boolean afterStartup = false;

	private ChannelServer(){
		long start = System.currentTimeMillis();
		instance = this;
		System.setProperty("java.rmi.server.hostname", ServerConstants.HOST);
		System.setProperty("wzpath", "wz");
		AutoJCE.removeCryptographyRestrictions();
		TimerManager tMan = TimerManager.getInstance();
		tMan.start();
		Logger.start();
		long timeToTake = System.currentTimeMillis();
		MobSkillFactory.loadMobSkills();
		System.out.println("MobSkills loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
		timeToTake = System.currentTimeMillis();
		if(ServerConstants.WZ_LOADING){
			this.mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), this);
		}else{
			this.mapFactory = new MapleMapFactory(null, null, this);
		}
		WorldInfo info = WorldInfo.values()[worldServerID];
		expRate = info.getExpRate();
		questExpRate = info.getQuestExpRate();
		dropRate = info.getDropRate();
		mesoRate = info.getMesoRate();
		// CrcCheck.load();
		for(int chid : channelServerID){
			loadChannel(chid);
		}
		CommandHandler.loadCommands();
		System.out.println("Loaded Channels in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
		timeToTake = System.currentTimeMillis();
		SkillFactory.loadAllSkills();
		System.out.println("Skills loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
		timeToTake = System.currentTimeMillis();
		MakerItemFactory.getInstance();
		ItemInformationProvider.getInstance();
		ItemInformationProvider.getInstance().getAllItemNames();
		ItemInformationProvider.getInstance().loadAllItems();
		CashItemFactory.getModifiedCommodity();
		System.out.println("Items loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
		timeToTake = System.currentTimeMillis();
		MapleQuest.loadAllQuest();
		System.out.println("Quests loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		timeToTake = System.currentTimeMillis();
		MobConstants.loadSlayerMonsters(this);
		System.out.println("Slayer Monsters loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		timeToTake = System.currentTimeMillis();
		MapleCharacterInfo.getInstance();
		System.out.println("Faces & Hairs loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		if(ServerConstants.WZ_LOADING && ServerConstants.BIN_DUMPING){
			timeToTake = System.currentTimeMillis();
			MapleReactorFactory.loadAllReactors();
			System.out.println("Dumped Reactor Data Bin in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
			timeToTake = System.currentTimeMillis();
			mapFactory.loadAll();
			System.out.println("Dumped Map Bin in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
			timeToTake = System.currentTimeMillis();
			MapleLifeFactory.loadAllMobs();
			System.out.println("Dumped Mob Data Bin in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		}
		timeToTake = System.currentTimeMillis();
		MapleMonsterInformationProvider.getInstance().loadData();
		System.out.println("Loaded Mob Data in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		timeToTake = System.currentTimeMillis();
		MiscLoading.load();
		System.out.println("Misc loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		socketFactory = new VertisyClientSocketFactory((byte) 0xAF);
		System.out.println("Vertisy loaded in " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds\r\n");
		tMan.register("ChannelServer-RegistryConnection", ()-> {
			Registry registry;
			try{
				if(worldRegistry != null && wci != null){
					worldRegistry.isConnected();
					wci.isConnected();
				}else{
					worldRegistry = null;
					wci = null;
					cwi = null;
					registry = null;
					System.out.println("Connecting to world server: " + ServerConstants.WORLD_SERVER_HOST[worldServerID] + ":" + ServerConstants.WORLD_SERVER_PORT[worldServerID]);
					registry = LocateRegistry.getRegistry(ServerConstants.WORLD_SERVER_HOST[worldServerID], ServerConstants.WORLD_SERVER_PORT[worldServerID], socketFactory);
					worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry-" + worldServerID);
					cwi = new ChannelWorldInterfaceImpl();
					wci = worldRegistry.registerChannelServer(channelServerID[0], cwi);
					System.out.println("Connected to World Server.");
					afterStartup = true;
				}
			}catch(Exception ex){
				// ex.printStackTrace();
				System.out.println("[ChannelServer] Failed to reconnect to the World Server. Trying again in 10 seconds");
				worldRegistry = null;
				wci = null;
				cwi = null;
				registry = null;
			}
		}, 10000, 0);
	}

	public void loadChannel(int chid){
		System.out.println("Loading ch: " + chid);
		Channel channel = new Channel(this, chid);
		channel.restartUpdateThread();
		channels.put(chid, channel);
		if(afterStartup){
			try{
				worldRegistry.registerChannelServer(channelServerID[0], cwi);
			}catch(RemoteException | NullPointerException e){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
			}
		}
	}

	public void shutdown(){
		for(Channel ch : getChannels()){
			ch.shutdown();
		}
		// CrcCheck.save();
		System.out.println("Shutting down TimerManager");
		TimerManager.getInstance().stop();
		System.out.println("ChannelServer shutdown");
	}

	public int getChannelServerID(){
		return channelServerID[0];
	}

	public int[] getChannelIDs(){
		return channelServerID;
	}

	public void setChannelIDs(int[] chArray){
		channelServerID = chArray;
	}

	public WorldRegistry getWorldRegistry(){
		return worldRegistry;
	}

	public WorldChannelInterface getWorldInterface(){// runs code on world, gives response to channel
		return wci;
	}

	public int getWorldID(){
		return worldServerID;
	}

	public Collection<Channel> getChannels(){
		return channels.values();
	}

	public Channel getChannel(int channel){
		return channels.get(channel);
	}

	public Map<Integer, Channel> getAllChannels(){
		return channels;
	}

	public MapleCharacter getCharacterById(int id){
		for(Channel ch : getChannels()){
			MapleCharacter chr = ch.getPlayerStorage().getCharacterById(id);
			if(chr != null) return chr;
		}
		return this.getMTSCharacterById(id);
	}

	public MapleCharacter getCharacterByName(String name){
		for(Channel ch : getChannels()){
			MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(name);
			if(chr != null) return chr;
		}
		return this.getMTSCharacterByName(name);
	}

	public Collection<MapleCharacter> getMTSCharacters(){
		return storage.getAllCharacters();
	}

	public void addMTSPlayer(MapleCharacter chr){
		storage.addPlayer(chr);
	}

	public MapleCharacter getMTSCharacterByName(String name){
		return storage.getCharacterByName(name);
	}

	public MapleCharacter getMTSCharacterById(int id){
		return storage.getCharacterById(id);
	}

	public void removeMTSPlayer(int chrid){
		storage.removePlayer(chrid);
	}

	public MapleMapFactory getMapFactory(){
		return mapFactory;
	}

	public MapleMap getMap(int channel, int mapid){
		return getMapFactory().getMap(channel, mapid);
	}

	public MapleMap getMap(int channel, int mapid, boolean instance){// old instance ;-;
		return getMapFactory().getMap(channel, mapid, instance);
	}

	public Map<Integer, MapleMap> getMaps(int channel){
		return getMapFactory().getMaps(channel);
	}

	public MapleMapData getMapData(int mapid){
		return mapDatas.get(mapid);
	}

	public void removeMapData(int mapid){
		mapDatas.remove(mapid);
	}

	public void addMapData(int mapid, MapleMapData mapData){
		mapDatas.put(mapid, mapData);
	}

	public void reloadMap(int mapid){
		removeMapData(mapid);
		for(Channel channel : channels.values()){
			MapleMap oldMap = channel.getMap(mapid);
			getMapFactory().clearMap(channel.getId(), mapid);
			MapleMap newMap = channel.getMap(mapid);
			for(MapleCharacter ch : oldMap.getCharacters()){
				ch.changeMap(newMap);
			}
			oldMap = null;
			newMap.respawn();
		}
	}

	public void broadcastPacket(byte[] packet){
		getChannels().forEach(ch-> ch.broadcastPacket(packet));
	}

	public List<Integer> broadcastPacket(List<Integer> players, byte[] packet){
		List<Integer> sentTo = new ArrayList<>();
		for(Channel channel : getChannels()){
			sentTo.addAll(channel.broadcastPacket(players, packet));
			if(players.size() == sentTo.size()) break;
		}
		return sentTo;
	}

	public List<String> broadcastPacketToPlayers(List<String> players, byte[] packet){
		List<String> sentTo = new ArrayList<>();
		for(Channel channel : getChannels()){
			sentTo.addAll(channel.broadcastPacketToPlayers(players, packet));
			if(players.size() == sentTo.size()) break;
		}
		return sentTo;
	}

	public void broadcastGMPacket(byte[] packet){
		getChannels().forEach(ch-> ch.broadcastGMPacket(packet));
	}

	public static void main(String[] args){
		worldServerID = ObjectParser.isInt(args[0]);
		channelServerID = new int[args.length - 1];
		for(int in = 1; in < args.length; in++){
			channelServerID[in - 1] = ObjectParser.isInt(args[in]);
		}
		instance = new ChannelServer();
	}

	public static ChannelServer getInstance(){
		return instance;
	}

	public int getExpRate(){
		return expRate;
	}

	public void setExpRate(int expRate){
		this.expRate = expRate;
	}

	public int getQuestExpRate(){
		return questExpRate;
	}

	public void setQuestExpRate(int questExpRate){
		this.questExpRate = questExpRate;
	}

	public int getDropRate(){
		return dropRate;
	}

	public void setDropRate(int dropRate){
		this.dropRate = dropRate;
	}

	public int getMesoRate(){
		return mesoRate;
	}

	public void setMesoRate(int mesoRate){
		this.mesoRate = mesoRate;
	}

	public PlayerStorage getTempStorage(){
		return tempStorage;
	}

	public void removePlayerFromTempStorage(int chrid){
		tempStorage.removePlayer(chrid);
	}

	public MapleCharacter getPlayerFromTempStorage(int chrid){
		return tempStorage.getCharacterById(chrid);
	}

	public MapleCharacter getPlayerFromTempStorage(String name){
		return tempStorage.getCharacterByName(name);
	}

	public void addPlayerToTempStorage(MapleCharacter chr){
		tempStorage.addPlayer(chr);
	}

	public int[] getTopGuilds(){
		return topGuilds;
	}

	public void setTopGuilds(int[] topGuilds){
		this.topGuilds = topGuilds;
	}

	public MapleClient getClientGlobal(int id){
		for(Channel ch : getChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.getAccountID() == id || player.getClient().getAccountName().equals(String.valueOf(id))) return player.getClient();
			}
		}
		return null;
	}

	public MapleClient getClientGlobal(String id){
		for(Channel ch : getChannels()){
			for(MapleCharacter player : ch.getPlayerStorage().getAllCharacters()){
				if(player.getClient().getAccountName().equalsIgnoreCase(id)) return player.getClient();
			}
		}
		return null;
	}
}
