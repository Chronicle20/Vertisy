package server.quest.requirements;

import client.MapleCharacter;
import net.server.PlayerBuffValueHolder;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class ExceptBuffRequirement extends MapleQuestRequirement{

	private int buff;

	public ExceptBuffRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.EXCEPT_BUFF);
		processData(data);
	}

	public ExceptBuffRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.EXCEPT_BUFF);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		buff = MapleDataTool.getIntConvert(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		buff = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(buff);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		for(PlayerBuffValueHolder pbvh : chr.getAllBuffs()){
			if(pbvh.getEffect().getSourceId() == buff) return false;
		}
		return true;
	}
}
