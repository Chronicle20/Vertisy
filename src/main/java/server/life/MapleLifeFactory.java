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
package server.life;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import constants.ServerConstants;
import provider.*;
import provider.wz.MapleDataType;
import server.life.MapleNPCStats.NPCScriptData;
import server.propertybuilder.MobStatProperty;
import tools.ObjectParser;
import tools.Pair;
import tools.StringUtil;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleLifeFactory{

	private static MapleDataProvider data = null;
	private static MapleDataProvider stringDataWZ = null;
	private static MapleData mobStringData = null;
	private static MapleData npcStringData = null;
	private static MapleDataProvider npcDataWZ = null;
	private static Map<MobStatProperty, Map<Integer, MapleMonsterStats>> monsterStatsMultiplied = new HashMap<>();
	private static Map<Integer, MapleMonsterStats> custom = new HashMap<>();
	private static Map<Integer, MapleNPCStats> npcStats = new HashMap<>();
	public static Map<Integer, List<Integer>> questCountGroup = new HashMap<>();

	public static AbstractLoadedMapleLife getLife(int id, String type){
		if(type.equalsIgnoreCase("n")){
			return getNPC(id);
		}else if(type.equalsIgnoreCase("m")){
			return getMonster(id);
		}else{
			System.out.println("Unknown Life type: " + type);
			return null;
		}
	}

	private static void loadData(){
		if(ServerConstants.WZ_LOADING && data == null){
			data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Mob.wz"));
			stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
			npcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Npc.wz"));
			mobStringData = stringDataWZ.getData("Mob.img");
			npcStringData = stringDataWZ.getData("Npc.img");
			for(MapleDataDirectoryEntry mdde : data.getRoot().getSubdirectories()){
				for(MapleDataFileEntry mdfe : mdde.getFiles()){
					Integer questMobID = ObjectParser.isInt(mdfe.getName().substring(0, mdfe.getName().length() - 4));
					List<Integer> mobs = new ArrayList<>();
					for(MapleData id : data.getData("QuestCountGroup/" + mdfe.getName()).getChildByPath("info").getChildren()){
						mobs.add(MapleDataTool.getInt(id));
					}
					if(!mobs.isEmpty()) questCountGroup.put(questMobID, mobs);
				}
			}
			if(ServerConstants.BIN_DUMPING){
				for(MapleDataFileEntry mdfe : npcDataWZ.getRoot().getFiles()){
					Integer npcID = ObjectParser.isInt(mdfe.getName().substring(0, mdfe.getName().length() - 4));
					getLife(npcID, "n");
				}
				MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
				for(Entry<Integer, List<Integer>> e : MapleLifeFactory.questCountGroup.entrySet()){
					mplew.writeInt(e.getKey());
					mplew.writeInt(e.getValue().size());
					for(int mob : e.getValue()){
						mplew.writeInt(mob);
					}
				}
				File bin = new File(System.getProperty("wzpath") + "/bin/Life/QuestCountGroup.bin");
				if(!bin.exists()){
					try{
						bin.getParentFile().mkdirs();
						bin.createNewFile();
						mplew.saveToFile(bin);
					}catch(Exception ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					}
				}
				mplew = null;
			}
		}else if(!ServerConstants.WZ_LOADING){
			File bin = new File(System.getProperty("wzpath") + "/bin/Life/QuestCountGroup.bin");
			if(bin.exists()){
				try{
					byte[] in = Files.readAllBytes(bin.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					while(glea.available() > 0){
						int questmobid = glea.readInt();
						int size = glea.readInt();
						List<Integer> mobs = new ArrayList<>();
						for(int i = 0; i < size; i++){
							mobs.add(glea.readInt());
						}
						questCountGroup.put(questmobid, mobs);
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}
	}

	public static void loadAllMobs(){
		loadData();
		for(MapleDataFileEntry mdfe : data.getRoot().getFiles()){
			if(mdfe.getName().equals("QuestCountGroup")) continue;
			getMonster(Integer.parseInt(mdfe.getName().substring(0, mdfe.getName().length() - 4)));
		}
	}

	public static MapleMonsterStats getStats(int mid){
		return getStats(mid, 1D);
	}

	public static MapleMonsterStats getStats(int mid, double multiplier){
		return getStats(mid, new MobStatProperty().expMultiplier(multiplier).hpMultiplier(multiplier));
	}

	public static MapleMonsterStats getStats(int mid, MobStatProperty property){
		Map<Integer, MapleMonsterStats> monsterStats = monsterStatsMultiplied.get(property);
		if(monsterStats == null) monsterStats = new HashMap<>();
		MapleMonsterStats stats = monsterStats.get(mid);
		MapleMonsterStats customStats = custom.get(mid);
		if(customStats != null){
			stats = customStats;
		}
		if(stats == null){
			if(ServerConstants.WZ_LOADING){
				loadData();
				MapleData monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
				if(monsterData == null) return null;
				MapleData monsterInfoData = monsterData.getChildByPath("info");
				int link = MapleDataTool.getInt("link", monsterInfoData, 0);
				stats = new MapleMonsterStats();
				loadMonsterStats(mid, monsterData, monsterInfoData, stats);
				if(link != 0){
					stats.setLink(link);
					saveToBin(mid, stats);
					MapleData monsterData2 = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(link) + ".img", '0', 11));
					if(monsterData2 != null){
						MapleData monsterInfoData2 = monsterData2.getChildByPath("info");
						loadMonsterStats(link, monsterData2, monsterInfoData2, stats);
						stats.setLink(0);
						saveToBin(link, stats);
					}
				}else saveToBin(mid, stats);
			}else{
				try{
					File bin = new File(System.getProperty("wzpath") + "/bin/Life/Mob/" + mid + ".bin");
					if(bin.exists()){
						byte[] in = Files.readAllBytes(bin.toPath());
						ByteArrayByteStream babs = new ByteArrayByteStream(in);
						GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
						stats = new MapleMonsterStats();
						int link = glea.readInt();
						if(link != 0){
							bin = new File(System.getProperty("wzpath") + "/bin/Life/Mob/" + link + ".bin");
							in = Files.readAllBytes(bin.toPath());
							babs = new ByteArrayByteStream(in);
							glea = new GenericLittleEndianAccessor(babs);
							glea.readInt();
							stats.load(glea);
						}else stats.load(glea);
						glea = null;
						babs = null;
						in = null;
					}
					if(stats == null){
						Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Unknown bin mob: " + mid);
						return null;
					}
					long hp = stats.getHp();
					hp *= property.hpMultiplier;
					stats.setHp((int) Math.min(Integer.MAX_VALUE, hp));
					long mp = stats.getMp();
					mp *= property.mpMultiplier;
					stats.setMp((int) Math.min(Integer.MAX_VALUE, mp));
					long exp = stats.getExp();
					exp *= property.expMultiplier;
					stats.setExp((int) Math.min(Integer.MAX_VALUE, exp));
					int level = stats.getLevel();
					level *= property.levelMultiplier;
					stats.setLevel(Math.min(Integer.MAX_VALUE, level));
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					return null;
				}
			}
			monsterStats.put(mid, stats);
			monsterStatsMultiplied.put(property, monsterStats);
		}
		return stats;
	}

	public static MapleMonster getMonster(int mid){
		return getMonster(mid, 1D);
	}

	public static MapleMonster getMonster(int mid, double multiplier){
		return getMonster(mid, new MobStatProperty().expMultiplier(multiplier).hpMultiplier(multiplier));
	}

	public static MapleMonster getMonster(int mid, MobStatProperty property){
		MapleMonster ret = new MapleMonster(mid, getStats(mid, property));
		return ret;
	}

	private static void loadMonsterStats(int mid, MapleData monsterData, MapleData monsterInfoData, MapleMonsterStats stats){
		for(MapleData data : monsterData.getChildren()){// Linked mobs might not work with this?
			if(data.getName().startsWith("attack")){
				MapleData attackData = data.getChildByPath("info");
				if(attackData == null) return;
				MobAttackInfo info = new MobAttackInfo();
				info.setDeadlyAttack(attackData.getChildByPath("deadlyAttack") != null);
				info.setMpBurn(MapleDataTool.getInt("mpBurn", attackData, 0));
				info.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
				info.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
				info.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
				stats.addMobAttack(ObjectParser.isInt(data.getName().replace("attack", "")), info);
			}
		}
		//
		if(monsterInfoData.getChildByPath("maxHP") != null){
			long hp = MapleDataTool.getIntConvert("maxHP", monsterInfoData);
			// hp *= property.hpMultiplier;
			stats.setHp((int) Math.min(Integer.MAX_VALUE, hp));
		}
		if(monsterInfoData.getChildByPath("damagedByMob") != null) stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) == 1);
		if(monsterInfoData.getChildByPath("PADamage") != null) stats.setPADamage(MapleDataTool.getInt("PADamage", monsterInfoData));
		if(monsterInfoData.getChildByPath("PDDamage") != null) stats.setPDDamage(MapleDataTool.getInt("PDDamage", monsterInfoData));
		if(monsterInfoData.getChildByPath("MADamage") != null) stats.setMADamage(MapleDataTool.getInt("MADamage", monsterInfoData));
		if(monsterInfoData.getChildByPath("MDDamage") != null) stats.setMDDamage(MapleDataTool.getInt("MDDamage", monsterInfoData));
		if(monsterInfoData.getChildByPath("maxMP") != null){
			int mp = MapleDataTool.getInt("maxMP", monsterInfoData, 0);
			// mp *= property.mpMultiplier;
			stats.setMp(Math.min(Integer.MAX_VALUE, mp));
		}
		long exp = MapleDataTool.getInt("exp", monsterInfoData, 0);
		// exp *= property.expMultiplier;
		stats.setExp((int) Math.min(Integer.MAX_VALUE, exp));
		int level = MapleDataTool.getInt("level", monsterInfoData);
		// level *= property.levelMultiplier;
		stats.setLevel(Math.min(Integer.MAX_VALUE, level));
		stats.setRemoveAfter(MapleDataTool.getInt("removeAfter", monsterInfoData, 0));
		stats.setBoss(MapleDataTool.getInt("boss", monsterInfoData, 0) > 0);
		stats.setExplosiveReward(MapleDataTool.getInt("explosiveReward", monsterInfoData, 0) > 0);
		stats.setFfaLoot(MapleDataTool.getInt("publicReward", monsterInfoData, 0) > 0);
		stats.setUndead(MapleDataTool.getInt("undead", monsterInfoData, 0) > 0);
		try{
			stats.setName(MapleDataTool.getString(mid + "/name", mobStringData, "MISSINGNO"));
		}catch(Throwable ex){}
		stats.setBuffToGive(MapleDataTool.getInt("buff", monsterInfoData, -1));
		stats.setCP(MapleDataTool.getInt("getCP", monsterInfoData, 0));
		stats.setRemoveOnMiss(MapleDataTool.getInt("removeOnMiss", monsterInfoData, 0) > 0);
		stats.setSpeed(MapleDataTool.getInt("speed", monsterInfoData, 0));
		stats.setFixedDamage(MapleDataTool.getInt("fixedDamage", monsterInfoData, 0));
		stats.setHideName(MapleDataTool.getInt("hideName", monsterInfoData, 0) == 1);
		stats.setMobType(MapleDataTool.getInt("mobType", monsterInfoData, -1));
		stats.setEvade(MapleDataTool.getInt("eva", monsterInfoData, 0));
		MapleData special = monsterInfoData.getChildByPath("coolDamage");
		if(special != null){
			int coolDmg = MapleDataTool.getInt("coolDamage", monsterInfoData);
			int coolProb = MapleDataTool.getInt("coolDamageProb", monsterInfoData, 0);
			stats.setCool(new Pair<>(coolDmg, coolProb));
		}
		special = monsterInfoData.getChildByPath("loseItem");
		if(special != null){
			for(MapleData liData : special.getChildren()){
				stats.addLoseItem(new loseItem(MapleDataTool.getInt(liData.getChildByPath("id")), (byte) MapleDataTool.getInt(liData.getChildByPath("prop")), (byte) MapleDataTool.getInt(liData.getChildByPath("x"))));
			}
		}
		special = monsterInfoData.getChildByPath("selfDestruction");
		if(special != null){
			stats.setSelfDestruction(new selfDestruction((byte) MapleDataTool.getInt(special.getChildByPath("action")), MapleDataTool.getIntConvert("removeAfter", special, -1), MapleDataTool.getIntConvert("hp", special, -1)));
		}
		MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
		int firstAttack = 0;
		if(firstAttackData != null){
			if(firstAttackData.getType() == MapleDataType.FLOAT){
				firstAttack = Math.round(MapleDataTool.getFloat(firstAttackData));
			}else{
				firstAttack = MapleDataTool.getInt(firstAttackData);
			}
		}
		stats.setFirstAttack(firstAttack > 0);
		stats.setDropPeriod(MapleDataTool.getInt("dropItemPeriod", monsterInfoData, 0) * 10000);
		stats.setTagColor(MapleDataTool.getInt("hpTagColor", monsterInfoData, 0));
		stats.setTagBgColor(MapleDataTool.getInt("hpTagBgcolor", monsterInfoData, 0));
		for(MapleData idata : monsterData){
			if(!idata.getName().equals("info")){
				int delay = 0;
				for(MapleData pic : idata.getChildren()){
					delay += MapleDataTool.getIntConvert("delay", pic, 0);
				}
				stats.setAnimationTime(idata.getName(), delay);
			}
		}
		MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
		if(reviveInfo != null){
			List<Integer> revives = new LinkedList<>();
			for(MapleData data_ : reviveInfo){
				revives.add(MapleDataTool.getInt(data_));
			}
			stats.setRevives(revives);
		}
		decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));
		MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
		if(monsterSkillData != null){
			int i = 0;
			List<Pair<Integer, Integer>> skills = new ArrayList<>();
			while(monsterSkillData.getChildByPath(Integer.toString(i)) != null){
				skills.add(new Pair<>(Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
				i++;
			}
			stats.setSkills(skills);
		}
		MapleData banishData = monsterInfoData.getChildByPath("ban");
		if(banishData != null){
			stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
		}
		MapleData monsterStandData = monsterData.getChildByPath("stand");
		if(monsterStandData != null){
			MapleData monsterStandData0 = monsterStandData.getChildByPath("0");
			if(monsterStandData0 != null) stats.setOrigin(MapleDataTool.getPoint("origin", monsterStandData0));
		}
		stats.bDamagedByMob = MapleDataTool.getInt("damagedByMob", monsterInfoData, 0) > 0;
		MapleData hasStand = monsterData.getChildByPath("stand");
		MapleData hasMove = monsterData.getChildByPath("move");
		if(hasStand != null){
			stats.setDefaultMoveType("stand");
		}else{
			if(hasMove != null){
				stats.setDefaultMoveType("move");
			}else{
				MapleData hasFly = monsterData.getChildByPath("fly");
				if(hasFly != null){
					stats.setDefaultMoveType("fly");
				}
			}
		}
		stats.setCanJump(monsterData.getChildByPath("jump") != null);
		stats.setCanMove(hasMove != null);
	}

	private static void saveToBin(int mid, MapleMonsterStats stats){
		if(ServerConstants.BIN_DUMPING){
			File bin = new File(System.getProperty("wzpath") + "/bin/Life/Mob/" + mid + ".bin");
			if(!bin.exists()){
				try{
					bin.getParentFile().mkdirs();
					bin.createNewFile();
					MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
					stats.save(mplew);
					mplew.saveToFile(bin);
					mplew = null;
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}
	}

	public static void removeMobData(){
		monsterStatsMultiplied.clear();
	}

	public static void removeNpcData(){
		npcStats.clear();
	}

	private static void decodeElementalString(MapleMonsterStats stats, String elemAttr){
		for(int i = 0; i < elemAttr.length(); i += 2){
			stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
		}
	}

	public static MapleNPC getNPC(int nid){
		loadData();
		MapleNPCStats stats = npcStats.get(nid);
		if(stats == null){
			if(ServerConstants.WZ_LOADING){
				stats = new MapleNPCStats(MapleDataTool.getString(nid + "/name", npcStringData, "MISSINGNO"));
				MapleData npcData = npcDataWZ.getData(StringUtil.getLeftPaddedStr(Integer.toString(nid) + ".img", '0', 11));
				if(npcData == null) return null;
				MapleData npcInfo = npcData.getChildByPath("info");
				if(npcInfo != null){
					stats.imitate = MapleDataTool.getInt("imitate", npcInfo, 0) > 0;
					MapleData scriptInfo = npcInfo.getChildByPath("script");
					if(scriptInfo != null){
						for(MapleData scriptID : scriptInfo.getChildren()){
							String script = MapleDataTool.getString("script", scriptID);
							if(script != null){
								Integer key = ObjectParser.isInt(scriptID.getName());
								NPCScriptData scriptData = new NPCScriptData(script, MapleDataTool.getInt("start", scriptID, 0), MapleDataTool.getInt("end", scriptID, 0));
								stats.addScriptData(key, scriptData);
							}
						}
					}
				}
				MapleData moveInfo = npcData.getChildByPath("move");
				stats.move = moveInfo != null;
				/*if(!stats.getScriptData().isEmpty()){
					StringBuilder sb = new StringBuilder("NPC: " + nid);
					for(int key : stats.getScriptData().keySet()){
						sb.append(" script" + key + ": ");
						sb.append(stats.getScriptData().get(key).script);
						sb.append(",");
					}
					if(sb.toString().contains(",")) sb.setLength(sb.length() - ",".length());
					System.out.println(sb.toString());
				}*/
				File bin = new File(System.getProperty("wzpath") + "/bin/Life/Npc/" + nid + ".bin");
				if(!bin.exists()){
					try{
						bin.getParentFile().mkdirs();
						bin.createNewFile();
						MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
						stats.save(mplew);
						mplew.saveToFile(bin);
						mplew = null;
					}catch(Exception ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					}
				}
			}else{
				try{
					File bin = new File(System.getProperty("wzpath") + "/bin/Life/Npc/" + nid + ".bin");
					if(bin.exists()){
						byte[] in = Files.readAllBytes(bin.toPath());
						ByteArrayByteStream babs = new ByteArrayByteStream(in);
						GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
						stats = new MapleNPCStats();
						stats.load(glea);
						glea = null;
						babs = null;
						in = null;
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
			npcStats.put(nid, stats);
		}
		return new MapleNPC(nid, stats);
	}

	public static class BanishInfo{

		private int map;
		private String portal, msg;

		public BanishInfo(){
			super();
		}

		public BanishInfo(String msg, int map, String portal){
			this.msg = msg;
			this.map = map;
			this.portal = portal;
		}

		public int getMap(){
			return map;
		}

		public String getPortal(){
			return portal;
		}

		public String getMsg(){
			return msg;
		}

		public void save(LittleEndianWriter lew){
			lew.writeInt(map);
			lew.writeMapleAsciiString(portal);
			lew.writeMapleAsciiString(msg);
		}

		public void load(LittleEndianAccessor lea){
			map = lea.readInt();
			portal = lea.readMapleAsciiString();
			msg = lea.readMapleAsciiString();
		}
	}

	public static class loseItem{

		private int id;
		private byte chance, x;

		public loseItem(){
			super();
		}

		private loseItem(int id, byte chance, byte x){
			this.id = id;
			this.chance = chance;
			this.x = x;
		}

		public int getId(){
			return id;
		}

		public byte getChance(){
			return chance;
		}

		public byte getX(){
			return x;
		}

		public void save(LittleEndianWriter lew){
			lew.writeInt(id);
			lew.write(chance);
			lew.write(x);
		}

		public void load(LittleEndianAccessor lea){
			id = lea.readInt();
			chance = lea.readByte();
			x = lea.readByte();
		}
	}

	public static class selfDestruction{

		private byte action;
		private int removeAfter;
		private int hp;

		public selfDestruction(){
			super();
		}

		private selfDestruction(byte action, int removeAfter, int hp){
			this.action = action;
			this.removeAfter = removeAfter;
			this.hp = hp;
		}

		public int getHp(){
			return hp;
		}

		public byte getAction(){
			return action;
		}

		public int removeAfter(){
			return removeAfter;
		}

		public void save(LittleEndianWriter lew){
			lew.write(action);
			lew.writeInt(removeAfter);
			lew.writeInt(hp);
		}

		public void load(LittleEndianAccessor lea){
			action = lea.readByte();
			removeAfter = lea.readInt();
			hp = lea.readInt();
		}
	}

	public static void clearMobs(){
		removeMobData();
		custom.clear();
	}
}
