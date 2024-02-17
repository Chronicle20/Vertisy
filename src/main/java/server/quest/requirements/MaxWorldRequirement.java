package server.quest.requirements;

import client.MapleCharacter;
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
public class MaxWorldRequirement extends MapleQuestRequirement{

	private int maxWorld;

	public MaxWorldRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.MAX_WORLD);
		processData(data);
	}

	public MaxWorldRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.MAX_WORLD);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		maxWorld = MapleDataTool.getIntConvert(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		maxWorld = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(maxWorld);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		return maxWorld >= chr.getWorld();
	}
}
