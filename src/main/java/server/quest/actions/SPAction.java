package server.quest.actions;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestActionType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 10, 2017
 */
public class SPAction extends MapleQuestAction{

	Map<Integer, Integer> sp = new HashMap<>();

	public SPAction(MapleQuest quest, MapleData data){
		super(MapleQuestActionType.SP, quest);
		questID = quest.getId();
		processData(data);
	}

	public SPAction(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestActionType.SP, quest);
		questID = quest.getId();
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		for(MapleData d : data.getChildren()){
			int sp = MapleDataTool.getInt(d.getChildByPath("sp_value"));
			int job = MapleDataTool.getInt(d.getChildByPath("job").getChildByPath("0"));
			this.sp.put(job, sp);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int size = lea.readInt();
		for(int i = 0; i < size; i++){
			sp.put(lea.readInt(), lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(sp.size());
		for(int job : sp.keySet()){
			lew.writeInt(job);
			lew.writeInt(sp.get(job));
		}
	}

	@Override
	public void run(MapleCharacter chr, Integer extSelection){
		for(int job : sp.keySet()){
			chr.gainSp(job, sp.get(job));
		}
	}
}
