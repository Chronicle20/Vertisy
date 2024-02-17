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
package server.quest.actions;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import client.MapleQuestStatus;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Tyler (Twdtwd)
 */
public class QuestAction extends MapleQuestAction{

	Map<Integer, Integer> quests = new HashMap<>();

	public QuestAction(MapleQuest quest, MapleData data){
		super(MapleQuestActionType.QUEST, quest);
		questID = quest.getId();
		processData(data);
	}

	public QuestAction(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestActionType.QUEST, quest);
		questID = quest.getId();
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		for(MapleData qEntry : data){
			int questid = MapleDataTool.getInt(qEntry.getChildByPath("id"));
			int stat = MapleDataTool.getInt(qEntry.getChildByPath("state"));
			quests.put(questid, stat);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int totalQuests = lea.readInt();
		for(int i = 0; i < totalQuests; i++){
			quests.put(lea.readInt(), lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(quests.size());
		for(int id : quests.keySet()){
			lew.writeInt(id);
			lew.writeInt(quests.get(id));
		}
	}

	@Override
	public void run(MapleCharacter chr, Integer extSelection){
		for(Integer questID : quests.keySet()){
			int stat = quests.get(questID);
			chr.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(questID), MapleQuestStatus.Status.getById(stat)));
		}
	}
}
