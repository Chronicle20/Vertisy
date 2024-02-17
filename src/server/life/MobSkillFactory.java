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

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Danny (Leifde)
 */
public class MobSkillFactory{

	private static Map<String, MobSkill> mobSkills = new HashMap<>();
	private static MapleDataProvider dataSource = null;
	private static MapleData skillRoot = null;

	public static void loadMobSkills(){
		File binFile = new File(System.getProperty("wzpath") + "/bin/Life/MobSkills.bin");
		binFile.getParentFile().mkdirs();
		if(ServerConstants.WZ_LOADING){
			if(dataSource == null){
				dataSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Skill.wz"));
				skillRoot = dataSource.getData("MobSkill.img");
			}
			for(MapleData skillID : skillRoot.getChildren()){
				for(MapleData levelData : skillID.getChildByPath("level").getChildren()){
					MobSkill ret = null;
					int mpCon = MapleDataTool.getInt(levelData.getChildByPath("mpCon"), 0);
					List<Integer> toSummon = new ArrayList<>();
					for(int i = 0; i > -1; i++){
						if(levelData.getChildByPath(String.valueOf(i)) == null){
							break;
						}
						toSummon.add(MapleDataTool.getInt(levelData.getChildByPath(String.valueOf(i)), 0));
					}
					int effect = MapleDataTool.getInt("summonEffect", levelData, 0);
					int hp = MapleDataTool.getInt("hp", levelData, 100);
					int x = MapleDataTool.getInt("x", levelData, 1);
					int y = MapleDataTool.getInt("y", levelData, 1);
					long duration = MapleDataTool.getInt("time", levelData, 0) * 1000;
					long cooltime = MapleDataTool.getInt("interval", levelData, 0) * 1000;
					int iprop = MapleDataTool.getInt("prop", levelData, 100);
					float prop = iprop / 100;
					int limit = MapleDataTool.getInt("limit", levelData, 0);
					MapleData ltd = levelData.getChildByPath("lt");
					Point lt = null;
					Point rb = null;
					if(ltd != null){
						lt = (Point) ltd.getData();
						rb = (Point) levelData.getChildByPath("rb").getData();
					}
					ret = new MobSkill(Integer.parseInt(skillID.getName()), Integer.parseInt(levelData.getName()));
					ret.addSummons(toSummon);
					ret.setCoolTime(cooltime);
					ret.setDuration(duration);
					ret.setHp(hp);
					ret.setMpCon(mpCon);
					ret.setSpawnEffect(effect);
					ret.setX(x);
					ret.setY(y);
					ret.setProp(prop);
					ret.setLimit(limit);
					ret.setLtRb(lt, rb);
					mobSkills.put(skillID.getName() + levelData.getName(), ret);
				}
			}
			if(ServerConstants.BIN_DUMPING && !binFile.exists()){
				MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
				mplew.writeInt(mobSkills.size());
				for(String data : mobSkills.keySet()){
					mplew.writeMapleAsciiString(data);
					mobSkills.get(data).save(mplew);
				}
				try{
					binFile.createNewFile();
					mplew.saveToFile(binFile);
				}catch(IOException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}
		}else{
			try{
				byte[] in = Files.readAllBytes(binFile.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				int size = glea.readInt();
				for(int i = 0; i < size; i++){
					String data = glea.readMapleAsciiString();
					MobSkill skill = new MobSkill();
					skill.load(glea);
					mobSkills.put(data, skill);
				}
				glea = null;
				babs = null;
				in = null;
			}catch(IOException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public static MobSkill getMobSkill(final int skillId, final int level){
		return mobSkills.get(skillId + "" + level);
	}
}
