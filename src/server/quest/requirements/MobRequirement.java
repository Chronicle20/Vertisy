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
package server.quest.requirements;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import client.MapleQuestStatus;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Tyler (Twdtwd)
 */
public class MobRequirement extends MapleQuestRequirement{

	Map<Integer, Integer> mobs = new HashMap<>();
	private int questID;

	public MobRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.MOB);
		processData(data);
		questID = quest.getId();
	}

	public MobRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.MOB);
		processData(lea);
		questID = quest.getId();
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		for(MapleData questEntry : data.getChildren()){
			int mobID = MapleDataTool.getInt(questEntry.getChildByPath("id"));
			int countReq = MapleDataTool.getInt(questEntry.getChildByPath("count"));
			mobs.put(mobID, countReq);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			mobs.put(lea.readInt(), lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(mobs.size());
		for(int id : mobs.keySet()){
			lew.writeInt(id);
			lew.writeInt(mobs.get(id));
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		MapleQuestStatus status = chr.getQuest(MapleQuest.getInstance(questID));
		for(Integer mobID : mobs.keySet()){
			int countReq = mobs.get(mobID);
			int progress;
			try{
				progress = Integer.parseInt(status.getProgress(mobID));
			}catch(NumberFormatException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Mob: " + mobID + " Quest: " + questID + "CID: " + chr.getId() + " Progress: " + status.getProgress(mobID));
				return false;
			}
			if(progress < countReq) return false;
		}
		return true;
	}

	public int getRequiredMobCount(int mobid){
		if(mobs.containsKey(mobid)) return mobs.get(mobid);
		return 0;
	}
}
