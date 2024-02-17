package server.quest;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import provider.MapleData;
import provider.MapleDataTool;
import server.quest.actions.MapleQuestAction;
import server.quest.requirements.MapleQuestRequirement;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 12, 2017
 */
public class CompleteQuestData{

	public int npc;
	public String script;
	public short infoNumber;
	public List<Integer> relevantMobs = new LinkedList<>();
	public Map<MapleQuestRequirementType, MapleQuestRequirement> completeReqs = new EnumMap<>(MapleQuestRequirementType.class);
	public Map<MapleQuestActionType, MapleQuestAction> completeActs = new EnumMap<>(MapleQuestActionType.class);

	public void read(MapleQuest quest, MapleData checkData, MapleData actData){
		if(checkData != null){
			for(MapleData completeReq : checkData.getChildren()){
				MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(completeReq.getName());
				if(type.equals(MapleQuestRequirementType.INFO_NUMBER)){
					infoNumber = (short) MapleDataTool.getInt(completeReq, 0);
				}
				if(type.equals(MapleQuestRequirementType.END_SCRIPT)){
					script = MapleDataTool.getString(completeReq, null);
				}
				MapleQuestRequirement req = quest.getRequirement(type, completeReq);
				if(req == null) continue;
				if(type.equals(MapleQuestRequirementType.MOB)){
					for(MapleData mob : completeReq.getChildren()){
						relevantMobs.add(MapleDataTool.getInt(mob.getChildByPath("id")));
					}
				}
				completeReqs.put(type, req);
			}
		}
		if(actData != null){
			for(MapleData completeAct : actData.getChildren()){
				MapleQuestActionType questActionType = MapleQuestActionType.getByWZName(completeAct.getName());
				MapleQuestAction act = quest.getAction(questActionType, completeAct);
				if(act == null) continue;
				completeActs.put(questActionType, act);
			}
		}
	}

	public void encode(LittleEndianWriter lew){
		lew.writeBoolean(script != null);
		if(script != null) lew.writeMapleAsciiString(script);
		lew.writeShort(infoNumber);
		lew.writeInt(relevantMobs.size());
		for(int mob : relevantMobs){
			lew.writeInt(mob);
		}
		lew.writeInt(completeReqs.size());
		for(MapleQuestRequirementType req : completeReqs.keySet()){
			lew.write(req.getType());
			completeReqs.get(req).writeData(lew);
		}
		lew.writeInt(completeActs.size());
		for(MapleQuestActionType req : completeActs.keySet()){
			lew.write(req.getType());
			completeActs.get(req).writeData(lew);
		}
	}

	public void decode(MapleQuest quest, LittleEndianAccessor lea){
		if(lea.readBoolean()) script = lea.readMapleAsciiString();
		infoNumber = lea.readShort();
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			relevantMobs.add(lea.readInt());
		}
		total = lea.readInt();// completeReqs
		for(int i = 0; i < total; i++){
			MapleQuestRequirementType type = MapleQuestRequirementType.getByType(lea.readByte());
			completeReqs.put(type, quest.getRequirement(type, lea));
		}
		total = lea.readInt();// completeActs
		for(int i = 0; i < total; i++){
			MapleQuestActionType type = MapleQuestActionType.getByType(lea.readByte());
			completeActs.put(type, quest.getAction(type, lea));
		}
	}
}
