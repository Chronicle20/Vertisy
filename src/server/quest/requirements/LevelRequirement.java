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
public class LevelRequirement extends MapleQuestRequirement{

	private int level;

	public LevelRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.LEVEL);
		processData(data);
	}

	public LevelRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.LEVEL);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		level = MapleDataTool.getInt(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		level = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(level);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		return chr.getLevel() >= level;
	}
}
