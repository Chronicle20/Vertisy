package server.quest;

import java.util.EnumMap;
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
public class StartQuestData{

	public int npc;
	public String script;
	public short infoNumber;
	public boolean repeatable;
	public Map<MapleQuestRequirementType, MapleQuestRequirement> startReqs = new EnumMap<>(MapleQuestRequirementType.class);
	public Map<MapleQuestActionType, MapleQuestAction> startActs = new EnumMap<>(MapleQuestActionType.class);

	public void read(MapleQuest quest, MapleData checkData, MapleData actData){
		if(checkData != null){
			for(MapleData startReq : checkData.getChildren()){
				MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(startReq.getName());
				if(type.equals(MapleQuestRequirementType.INTERVAL)){
					repeatable = true;
				}
				if(type.equals(MapleQuestRequirementType.INFO_NUMBER)){
					infoNumber = (short) MapleDataTool.getInt(startReq, 0);
				}
				if(type.equals(MapleQuestRequirementType.START_SCRIPT)){
					script = MapleDataTool.getString(startReq, null);
				}
				MapleQuestRequirement req = quest.getRequirement(type, startReq);
				if(req == null) continue;
				startReqs.put(type, req);
			}
		}
		if(actData != null){
			for(MapleData startAct : actData.getChildren()){
				MapleQuestActionType questActionType = MapleQuestActionType.getByWZName(startAct.getName());
				MapleQuestAction act = quest.getAction(questActionType, startAct);
				if(act == null) continue;
				startActs.put(questActionType, act);
			}
		}
	}

	public void encode(LittleEndianWriter lew){
		lew.writeBoolean(script != null);
		if(script != null) lew.writeMapleAsciiString(script);
		lew.writeShort(infoNumber);
		lew.writeInt(startReqs.size());
		for(MapleQuestRequirementType req : startReqs.keySet()){
			lew.write(req.getType());
			startReqs.get(req).writeData(lew);
		}
		lew.writeInt(startActs.size());
		for(MapleQuestActionType req : startActs.keySet()){
			lew.write(req.getType());
			startActs.get(req).writeData(lew);
		}
	}

	public void decode(MapleQuest quest, LittleEndianAccessor lea){
		if(lea.readBoolean()) script = lea.readMapleAsciiString();
		infoNumber = lea.readShort();
		int total = lea.readInt();// startReqs
		for(int i = 0; i < total; i++){
			MapleQuestRequirementType type = MapleQuestRequirementType.getByType(lea.readByte());
			startReqs.put(type, quest.getRequirement(type, lea));
		}
		total = lea.readInt();// startActs
		for(int i = 0; i < total; i++){
			MapleQuestActionType type = MapleQuestActionType.getByType(lea.readByte());
			startActs.put(type, quest.getAction(type, lea));
		}
	}
}
