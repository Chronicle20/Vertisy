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
public class MorphRequirement extends MapleQuestRequirement{

	private int morph;

	public MorphRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.MORPH);
		processData(data);
	}

	public MorphRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.MORPH);
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		morph = MapleDataTool.getInt(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		morph = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(morph);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		for(PlayerBuffValueHolder pbvh : chr.getAllBuffs()){
			if(pbvh.getEffect().getMorph() == morph) return true;
		}
		return false;
	}
}
