package constants;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import net.channel.ChannelServer;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import server.life.MapleMonster;
import server.life.SpawnPoint;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.Pair;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 28, 2016
 */
public class MobConstants{

	public static int[] NX_GAIN_EXEMPTION = {3400004, 3400006};
	public static Map<Integer, Pair<Integer, String>> slayerMobs = new HashMap<>();
	//
	public static final int HIGH_LEVEL_MOB = 120;

	public static boolean isDropExempted(int mobid){
		switch (mobid){
			// case 9400640://haunted house mobs
			// case 9400639:
			// case 9400638:
			case 3400004:
			case 3400006:
			case 9420001:// frog for halloween
			case 9300379:// aran tutorial mobs
			case 9300380:
			case 9300381:
			case 9300382:
			case 9300383:
				// case 8190000:// Jr. Newtie
				// case 8190001:// Jr. Newtie
				return true;
		}
		return false;
	}

	public static boolean isNXGainExemption(int mobid){
		for(int i : NX_GAIN_EXEMPTION){
			if(i == mobid) return true;
		}
		return false;
	}

	public static void loadSlayerMonsters(ChannelServer channelServer){
		Set<Integer> badmobs = new HashSet<>(Arrays.asList(9300003, 9300173, 9300183, 9300921, 9300928, 9300172, 9300920, 9400585, 9400586, 3230300, 3230301, 9400587, 9400588));
		try{
			slayerMobs.clear();
			File slayerMobDat = new File(System.getProperty("wzpath") + "/bin/Life/SlayerMobs.bin");
			if(slayerMobDat.exists()){
				byte[] in = Files.readAllBytes(slayerMobDat.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				while(glea.available() > 0){
					slayerMobs.put(glea.readInt(), new Pair<Integer, String>(glea.readInt(), glea.readMapleAsciiString()));
				}
				glea = null;
				babs = null;
				in = null;
			}else if(ServerConstants.BIN_DUMPING){
				MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
				MapleDataProvider maps = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz"));
				MapleDataProvider string = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
				MapleMapFactory mapFactory = new MapleMapFactory(maps, string, channelServer);
				for(MapleDataDirectoryEntry entry : maps.getRoot().getSubdirectories()){
					if(entry.getName().equals("Map")){
						for(MapleDataDirectoryEntry area : entry.getSubdirectories()){
							if(area.getName().contains("Map")){
								for(MapleDataFileEntry mapid : area.getFiles()){
									MapleMap map = null;
									map = mapFactory.getMap(0, Integer.parseInt(mapid.getName().replace(".img", "")));
									if(!map.getMapData().getMapMark().equals("Event")){
										for(SpawnPoint sp : map.getSpawnPoints()){
											MapleMonster monster = sp.getFakeMonster();
											if(monster != null){
												if(monster.getStats().getLevel() > 10 && !monster.isBoss() && monster.getMobTime() != -1 && monster.getStats().getHp() > 100){
													if(monster.getStats().getDefaultMoveType() != null && monster.getStats().getSpeed() != 0 && monster.getExp() > 3){
														if(monster.getStats().getPADamage() > 10 || monster.getStats().getMADamage() > 10){
															if(monster.getStats().getFixedDamage() == 0 && monster.getStats().getLink() == 0){
																if(badmobs.contains(monster.getId())) continue;
																if(monster.getStats().isNameHidden()) continue;
																if(slayerMobs.containsKey(monster.getId())) continue;
																slayerMobs.put(monster.getId(), new Pair<Integer, String>(monster.getStats().getLevel(), map.getMapData().getMapName()));
																mplew.writeInt(monster.getId());
																mplew.writeInt(monster.getStats().getLevel());
																mplew.writeMapleAsciiString(map.getMapData().getMapName());
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				mapFactory.getMaps().clear();
				mapFactory = null;
				string = null;
				maps = null;
				slayerMobDat.createNewFile();
				mplew.saveToFile(slayerMobDat);
				mplew = null;
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		System.out.println("Loaded " + slayerMobs.size() + " Slayer Mobs");
	}
}
