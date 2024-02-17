/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * @author Tyler (Twdtwd)
 */
public class QuestRequirement extends MapleQuestRequirement{

	Map<Integer, Integer> quests = new HashMap<>();

	public QuestRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.QUEST);
		processData(data);
	}

	public QuestRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.QUEST);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		for(MapleData questEntry : data.getChildren()){
			int questID = MapleDataTool.getInt(questEntry.getChildByPath("id"));
			int stateReq = MapleDataTool.getInt(questEntry.getChildByPath("state"));
			quests.put(questID, stateReq);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
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
	public boolean check(MapleCharacter chr, Integer npcid){
		for(Integer questID : quests.keySet()){
			int stateReq = quests.get(questID);
			MapleQuestStatus q = chr.getQuest(MapleQuest.getInstance(questID));
			if(q == null && MapleQuestStatus.Status.getById(stateReq).equals(MapleQuestStatus.Status.NOT_STARTED)) continue;
			if(q == null || !q.getStatus().equals(MapleQuestStatus.Status.getById(stateReq))) return false;
		}
		return true;
	}
}
