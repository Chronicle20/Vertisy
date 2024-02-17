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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import constants.ServerConstants;
import net.channel.ChannelServer;
import provider.*;
import server.MaplePortal;
import server.PortalFactory;
import server.life.*;
import server.maps.objects.MapleMapObject;
import server.maps.objects.PlayerNPC;
import server.reactors.MapleReactor;
import server.reactors.MapleReactorFactory;
import tools.DatabaseConnection;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleMapFactory{

	private MapleDataProvider source;
	private MapleData nameData;
	private Map<Integer, Map<Integer, MapleMap>> maps = new ConcurrentHashMap<>();
	private ChannelServer channel;

	public MapleMapFactory(MapleDataProvider source, MapleDataProvider stringSource, ChannelServer channel){
		this.source = source;
		if(stringSource != null) this.nameData = stringSource.getData("Map.img");
		this.channel = channel;
	}

	public MapleMap getMap(int channel, int mapid){
		return getMap(channel, mapid, false);
	}

	public MapleMap getMap(int channel, int mapid, boolean instance){// Old instance system
		MapleMap map = null;
		Map<Integer, MapleMap> chMaps = null;
		if(!instance){
			chMaps = maps.get(channel);
			if(chMaps != null && chMaps.size() > 0) map = chMaps.get(mapid);
		}
		if(map == null){
			map = loadMap(channel, mapid);
			if(!instance && map != null){
				if(chMaps == null) chMaps = new ConcurrentHashMap<>();
				chMaps.put(mapid, map);
				maps.put(channel, chMaps);
			}
		}
		return map;
	}

	private final File baseBin = new File(System.getProperty("wzpath") + "/bin/Maps/");

	public MapleMapData getMapData(int mapid){
		MapleMapData mapData = channel == null ? null : channel.getMapData(mapid);
		if(mapData != null) return mapData;
		mapData = new MapleMapData();
		File binFolder = new File(baseBin, "/Map" + (mapid / 100000000) + "/");
		binFolder.mkdirs();
		File bin = new File(binFolder, mapid + ".bin");
		if(ServerConstants.WZ_LOADING){
			binFolder.mkdirs();
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			//
			String mapName = getMapName(mapid);
			MapleData mapleData = source.getData(mapName);
			if(mapleData == null){
				System.out.println("MapleData for: " + mapid + " is null");
				return null;
			}
			//
			String link = MapleDataTool.getString(mapleData.getChildByPath("info/link"), "");
			if(!link.equals("") && mapName != null){ // nexon made hundreds of dojo maps so to reduce the size they added links.
				mapName = getMapName(Integer.parseInt(link));
				mapleData = source.getData(mapName);
			}
			//
			float monsterRate = 0;
			MapleData mobRate = mapleData.getChildByPath("info/mobRate");
			if(mobRate != null){
				monsterRate = ((Float) mobRate.getData());
			}
			mapData.setMonsterRate(monsterRate);
			mapData.setReturnMap(MapleDataTool.getInt("info/returnMap", mapleData));
			String onFirstEnter = MapleDataTool.getString(mapleData.getChildByPath("info/onFirstUserEnter"), String.valueOf(mapid));
			mapData.setOnFirstUserEnter(onFirstEnter.equals("") ? String.valueOf(mapid) : onFirstEnter);
			String onEnter = MapleDataTool.getString(mapleData.getChildByPath("info/onUserEnter"), String.valueOf(mapid));
			mapData.setOnUserEnter(onEnter.equals("") ? String.valueOf(mapid) : onEnter);
			mapData.setFieldLimit(MapleDataTool.getInt(mapleData.getChildByPath("info/fieldLimit"), 0));
			mapData.setMobInterval((short) MapleDataTool.getInt(mapleData.getChildByPath("info/createMobInterval"), 5000));
			mapData.setVRTop(MapleDataTool.getInt(mapleData.getChildByPath("info/VRTop"), 0));
			mapData.setVRBottom(MapleDataTool.getInt(mapleData.getChildByPath("info/VRBottom"), 0));
			mapData.setVRLeft(MapleDataTool.getInt(mapleData.getChildByPath("info/VRLeft"), 0));
			mapData.setVRRight(MapleDataTool.getInt(mapleData.getChildByPath("info/VRRight"), 0));
			//
			MapleData timeMob = mapleData.getChildByPath("info/timeMob");
			if(timeMob != null){
				mapData.timeMob(MapleDataTool.getInt(timeMob.getChildByPath("id")), MapleDataTool.getString(timeMob.getChildByPath("message")));
			}
			//
			List<MapleFoothold> allFootholds = new LinkedList<>();
			Point lBound = new Point();
			Point uBound = new Point();
			for(MapleData footRoot : mapleData.getChildByPath("foothold")){
				for(MapleData footCat : footRoot){
					for(MapleData footHold : footCat){
						int x1 = MapleDataTool.getInt(footHold.getChildByPath("x1"));
						int y1 = MapleDataTool.getInt(footHold.getChildByPath("y1"));
						int x2 = MapleDataTool.getInt(footHold.getChildByPath("x2"));
						int y2 = MapleDataTool.getInt(footHold.getChildByPath("y2"));
						MapleFoothold fh = new MapleFoothold(new Point(x1, y1), new Point(x2, y2), Integer.parseInt(footHold.getName()));
						fh.setPrev(MapleDataTool.getInt(footHold.getChildByPath("prev")));
						fh.setNext(MapleDataTool.getInt(footHold.getChildByPath("next")));
						if(fh.getX1() < lBound.x){
							lBound.x = fh.getX1();
						}
						if(fh.getX2() > uBound.x){
							uBound.x = fh.getX2();
						}
						if(fh.getY1() < lBound.y){
							lBound.y = fh.getY1();
						}
						if(fh.getY2() > uBound.y){
							uBound.y = fh.getY2();
						}
						allFootholds.add(fh);
					}
				}
			}
			MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
			for(MapleFoothold fh : allFootholds){
				fTree.insert(fh);
			}
			mapData.setFootholds(fTree);
			MapleData ladderRope = mapleData.getChildByPath("ladderRope");
			if(ladderRope != null){
				for(MapleData footRoot : ladderRope){
					int x = MapleDataTool.getInt(footRoot.getChildByPath("x"));
					int y1 = MapleDataTool.getInt(footRoot.getChildByPath("y1"));
					int y2 = MapleDataTool.getInt(footRoot.getChildByPath("y2"));
					int type = MapleDataTool.getInt(footRoot.getChildByPath("l"));
					int layer = MapleDataTool.getInt(footRoot.getChildByPath("page"));
					// int uf = MapleDataTool.getInt(footRoot.getChildByPath("uf"));//TODO: Figure out what this is
					MapleLadderFoothold fh = new MapleLadderFoothold(Integer.parseInt(footRoot.getName()), x, y1, y2, type, layer);
					mapData.getLadderFootholds().add(fh);
				}
			}
			//
			MapleData areaData = mapleData.getChildByPath("area");
			if(areaData != null){
				for(MapleData area : areaData){
					int x1 = MapleDataTool.getInt(area.getChildByPath("x1"));
					int y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
					int x2 = MapleDataTool.getInt(area.getChildByPath("x2"));
					int y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
					mapData.addMapleArea(new Rectangle(x1, y1, (x2 - x1), (y2 - y1)));
				}
			}
			//
			for(MapleData life : mapleData.getChildByPath("life")){
				String id = MapleDataTool.getString(life.getChildByPath("id"));
				String type = MapleDataTool.getString(life.getChildByPath("type"));
				if(id.equals("9001105")){
					id = "9001108";// soz
				}
				try{
					AbstractLoadedMapleLife myLife = loadLife(life, id, type);
					if(myLife == null) continue;
					if(myLife instanceof MapleMonster){
						MapleMonster monster = (MapleMonster) myLife;
						int mobTime = MapleDataTool.getInt("mobTime", life, 0);
						int team = MapleDataTool.getInt("team", life, -1);
						monster.setMobTime(mobTime);
						if(mobTime == -1){ // does not respawn, force spawn once
							mapData.addCustomMonsterSpawn(monster, mobTime, team);
						}else{
							mapData.addMonsterSpawn(monster, mobTime, team);
						}
					}else{
						if(myLife instanceof MapleNPC){
							MapleNPC npc = (MapleNPC) myLife;
							if(npc.stats.imitate){
								PlayerNPC pNPC = new PlayerNPC();
								if(pNPC.createPlayerNPC(npc.getId())){
									pNPC.loadEquipsFromDB();
									mapData.addMapObject(pNPC);
									continue;
								}
							}
						}
						mapData.addMapObject(myLife);
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
			//
			PortalFactory portalFactory = new PortalFactory();
			for(MapleData portal : mapleData.getChildByPath("portal")){
				mapData.addPortal(portalFactory.makePortal(MapleDataTool.getInt(portal.getChildByPath("pt")), portal));
			}
			//
			if(mapleData.getChildByPath("reactor") != null){
				for(MapleData reactor : mapleData.getChildByPath("reactor")){
					String id = MapleDataTool.getString(reactor.getChildByPath("id"));
					if(id != null){
						mapData.addReactor(loadReactor(reactor, id));
					}
				}
			}
			//
			HashMap<Integer, Integer> backTypes = new HashMap<>();
			try{
				for(MapleData layer : mapleData.getChildByPath("back")){ // yolo
					int layerNum = Integer.parseInt(layer.getName());
					int type = MapleDataTool.getInt(layer.getChildByPath("type"), 0);
					backTypes.put(layerNum, type);
				}
			}catch(Exception e){
				// swallow cause I'm cool
			}
			mapData.setBackgroundTypes(backTypes);
			mapData.setMapMark(MapleDataTool.getString("info/mapMark", mapleData));
			mapData.setClock(mapleData.getChildByPath("clock") != null);
			mapData.setEverlast(mapleData.getChildByPath("everlast") != null);
			mapData.setTown(MapleDataTool.getIntConvert("info/town", mapleData, 0)); // Town = 1, else 0
			mapData.setHPDec(MapleDataTool.getIntConvert("info/decHP", mapleData, 0));
			mapData.setHPDecProtect(MapleDataTool.getIntConvert("info/protectItem", mapleData, 0));
			mapData.setForcedReturnMap(MapleDataTool.getInt(mapleData.getChildByPath("info/forcedReturn"), 999999999));
			mapData.setBoat(mapleData.getChildByPath("shipObj") != null);
			mapData.setTimeLimit(MapleDataTool.getIntConvert("timeLimit", mapleData.getChildByPath("info"), -1));
			mapData.setFieldType(MapleDataTool.getIntConvert("info/fieldType", mapleData, 0));
			mapData.setMobCapacity(MapleDataTool.getIntConvert("fixedMobCapacity", mapleData.getChildByPath("info"), 500));// Is there a map that contains more than 500 mobs?
			mapData.setSwim(MapleDataTool.getIntConvert("swim", mapleData.getChildByPath("info"), 0) > 0 || mapleData.getChildByPath("swimArea") != null);
			mapData.setFly(MapleDataTool.getIntConvert("fly", mapleData.getChildByPath("info"), 0) > 0);
			mapData.setBGM(MapleDataTool.getString("info/bgm", mapleData));
			mapData.setRecovery(MapleDataTool.getFloat("recovery", mapleData.getChildByPath("info"), 1F));
			//
			try{
				mapData.setMapName(MapleDataTool.getString("mapName", nameData.getChildByPath(getMapStringName(mapid)), ""));
				mapData.setStreetName(MapleDataTool.getString("streetName", nameData.getChildByPath(getMapStringName(mapid)), ""));
			}catch(Exception e){
				mapData.setMapName("");
				mapData.setStreetName("");
			}
			//
			MapleData mc = mapleData.getChildByPath("monsterCarnival");
			if(mc != null){
				MonsterCarnivalSettings mcs = new MonsterCarnivalSettings();
				mcs.setDeathCP(MapleDataTool.getInt("deathCP", mc));
				mcs.setEffectLose(MapleDataTool.getString("effectLose", mc));
				mcs.setEffectWin(MapleDataTool.getString("effectWin", mc));
				// skip guardian stuff here. TODO: Identify use.
				mcs.setGuardianGenMax(MapleDataTool.getInt("guardianGenMax", mc, -1));
				MapleData guardGenPos = mc.getChildByPath("guardianGenPos");
				if(guardGenPos != null){
					for(MapleData ggp : guardGenPos){
						mcs.addGuardianGenPos(Integer.parseInt(ggp.getName()), MapleDataTool.getInt(ggp.getChildByPath("f")), new Point(MapleDataTool.getInt(ggp.getChildByPath("x")), MapleDataTool.getInt(ggp)));
					}
				}
				mcs.setMapDivided(MapleDataTool.getInt("mapDivided", mc));
				MapleData mcMob = mc.getChildByPath("mob");
				if(mcMob != null) for(MapleData mob : mcMob){
					mcs.addMob(Integer.parseInt(mob.getName()), MapleDataTool.getInt(mob.getChildByPath("id")), MapleDataTool.getInt(mob.getChildByPath("mobTime")), MapleDataTool.getInt(mob.getChildByPath("spendCP")));
				}
				mcs.setMobGenMax(MapleDataTool.getInt("mobGenMax", mc, -1));
				MapleData mobGenPos = mc.getChildByPath("mobGenPos");
				if(mobGenPos != null){
					for(MapleData mgp : mobGenPos){
						mcs.addMobGenPos(Integer.parseInt(mgp.getName()), MapleDataTool.getInt(mgp.getChildByPath("cy")), MapleDataTool.getInt(mgp.getChildByPath("fh")), new Point(MapleDataTool.getInt(mgp.getChildByPath("x")), MapleDataTool.getInt(mgp.getChildByPath("y"))));
					}
				}
				mcs.setReactorBlue(MapleDataTool.getInt("reactorBlue", mc));
				mcs.setReactorRed(MapleDataTool.getInt("reactorRed", mc));
				// skip reward stuff here. TODO: Identify use.
				mcs.setRewardMapLose(MapleDataTool.getInt("rewardMapLose", mc));
				mcs.setRewardMapWin(MapleDataTool.getInt("rewardMapWin", mc));
				// skip skill stuff here. TODO: Identify use.
				mcs.setSoundLose(MapleDataTool.getString("soundLose", mc));
				mcs.setSoundWin(MapleDataTool.getString("soundWin", mc));
				mcs.setTimeDefault(MapleDataTool.getInt("timeDefault", mc));
				mcs.setTimeExpand(MapleDataTool.getInt("timeExpand", mc));
				mcs.setTimeFinish(MapleDataTool.getInt("timeFinish", mc));
				mcs.setTimeMessage(MapleDataTool.getInt("timeMessage", mc));
				mapData.setMCS(mcs);
			}
			try{
				/*List<String> scriptsPrinted = new ArrayList<>();
				Collection<MaplePortal> portals = mapData.getPortals();
				if(!portals.isEmpty()){
					StringBuilder sb = new StringBuilder("Map: " + mapid);
					int portalsWithScript = 0;
					for(MaplePortal mp : portals){
						if(mp.getScriptName() != null && !scriptsPrinted.contains(mp.getScriptName())){
							sb.append("\r\nPortal Script: " + mp.getScriptName());
							portalsWithScript++;
							scriptsPrinted.add(mp.getScriptName());
						}
					}
					if(portalsWithScript > 0){
						FilePrinter.print(FilePrinter.LOG, sb.toString());
					}
				}*/
				if(ServerConstants.BIN_DUMPING && !bin.exists()){
					mapData.save(mplew);
					bin.createNewFile();
					mplew.saveToFile(bin);
				}
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				return null;
			}
		}else{
			try{
				byte[] in = Files.readAllBytes(bin.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				mapData.load(glea);
				glea = null;
				babs = null;
				in = null;
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				return null;
			}
		}
		if(channel != null) channel.addMapData(mapid, mapData);
		return mapData;
	}

	public MapleMap loadMap(int channel, int mapid){
		MapleMapData mapData = getMapData(mapid);
		if(mapData == null) return null;
		MapleMap map = new MapleMap(mapData, mapid);
		map.setChannel(channel);
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM playernpcs WHERE map = ?")){
			ps.setInt(1, mapid);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					PlayerNPC npc = new PlayerNPC();
					npc.loadMainStuffFromDB(rs.getInt("id"));
					if(npc.getName().equals("FangBlade")) continue;
					npc.loadEquipsFromDB();
					map.addMapObject(npc);
				}
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		for(SpawnPoint sp : mapData.getMonsterSpawns()){
			map.addMonsterSpawn(sp.getFakeMonster(), sp.getMobTime(), sp.getTeam());
		}
		for(SpawnPoint sp : mapData.getCustomMonsterSpawns()){
			map.addCustomMonsterSpawn(sp.getFakeMonster(), sp.getMobTime(), sp.getTeam());
		}
		for(MapleMapObject mmo : mapData.getMapObjects()){
			map.addMapObject(mmo);
		}
		for(MaplePortal portal : mapData.getPortals()){
			map.addPortal(portal);
		}
		for(MapleReactor reactor : mapData.getReactors()){
			map.spawnReactor(reactor);
		}
		map.setTimeLimit(mapData.getTimeLimit());
		return map;
	}

	public boolean isMapLoaded(int mapId){
		return maps.containsKey(mapId);
	}

	private AbstractLoadedMapleLife loadLife(MapleData life, String id, String type){
		if(life == null) return null;
		AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);
		if(myLife == null){
			System.out.println("Mob: " + id + " type: " + type + " returned null");
			return null;
		}
		myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
		MapleData dF = life.getChildByPath("f");
		if(dF != null){
			myLife.setF(MapleDataTool.getInt(dF));
		}
		myLife.setFh(MapleDataTool.getInt(life.getChildByPath("fh")));
		myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
		myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
		int x = MapleDataTool.getInt(life.getChildByPath("x"));
		int y = MapleDataTool.getInt(life.getChildByPath("y"));
		myLife.setPosition(new Point(x, y));
		int hide = MapleDataTool.getInt("hide", life, 0);
		if(hide == 1){
			myLife.setHide(true);
		}
		return myLife;
	}

	private MapleReactor loadReactor(MapleData reactor, String id){
		MapleReactor myReactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(id)));
		int x = MapleDataTool.getInt(reactor.getChildByPath("x"));
		int y = MapleDataTool.getInt(reactor.getChildByPath("y"));
		myReactor.setPosition(new Point(x, y));
		myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
		myReactor.setState(0);
		myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));
		return myReactor;
	}

	public String getMapName(int mapid){
		String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
		StringBuilder builder = new StringBuilder("Map/Map");
		int area = mapid / 100000000;
		builder.append(area);
		builder.append("/");
		builder.append(mapName);
		builder.append(".img");
		mapName = builder.toString();
		return mapName;
	}

	private String getMapStringName(int mapid){
		StringBuilder builder = new StringBuilder();
		if(mapid < 100000000){
			builder.append("maple");
		}else if(mapid >= 100000000 && mapid < 200000000){
			builder.append("victoria");
		}else if(mapid >= 200000000 && mapid < 300000000){
			builder.append("ossyria");
		}else if(mapid >= 540000000 && mapid < 551030200){
			builder.append("singapore");
		}else if(mapid >= 600000000 && mapid < 620000000){
			builder.append("MasteriaGL");
		}else if(mapid >= 670000000 && mapid < 682000000){
			builder.append("weddingGL");
		}else if(mapid >= 682000000 && mapid < 683000000){
			builder.append("HalloweenGL");
		}else if(mapid >= 800000000 && mapid < 900000000){
			builder.append("jp");
		}else{
			builder.append("etc");
		}
		builder.append("/").append(mapid);
		return builder.toString();
	}

	public Map<Integer, Map<Integer, MapleMap>> getMaps(){
		return maps;
	}

	public Map<Integer, MapleMap> getMaps(int channel){
		return maps.get(channel);
	}

	public void clearMap(int channel, int mapid){
		Map<Integer, MapleMap> maps = getMaps(channel);
		if(maps != null && maps.size() > 0) maps.remove(mapid);
	}

	public void clearMaps(boolean nullHashMaps){
		for(int channel : maps.keySet()){
			maps.get(channel).clear();
		}
		maps.clear();
		if(nullHashMaps) maps = null;
	}

	public void loadAll(){
		if(ServerConstants.BIN_DUMPING){
			if(ServerConstants.WZ_LOADING){
				for(MapleDataDirectoryEntry entry : source.getRoot().getSubdirectories()){
					if(entry.getName().equals("Map")){
						for(MapleDataDirectoryEntry area : entry.getSubdirectories()){
							if(area.getName().contains("Map")){
								for(MapleDataFileEntry mapid : area.getFiles()){
									getMap(0, Integer.parseInt(mapid.getName().replace(".img", "")));
								}
							}
						}
					}
				}
			}else{
				for(File f : baseBin.listFiles()){
					for(File map : f.listFiles()){
						getMap(0, Integer.parseInt(map.getName().replace(".bin", "")));
					}
				}
			}
		}
	}
}
