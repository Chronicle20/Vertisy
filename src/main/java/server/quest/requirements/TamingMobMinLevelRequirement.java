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
public class TamingMobMinLevelRequirement extends MapleQuestRequirement{

	private int minLevel;

	public TamingMobMinLevelRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.TAMING_MOB_MIN_LEVEL);
		processData(data);
	}

	public TamingMobMinLevelRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.TAMING_MOB_MIN_LEVEL);
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		minLevel = MapleDataTool.getInt(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		minLevel = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(minLevel);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		if(chr.getMount() == null) return false;
		return chr.getMount().getLevel() >= minLevel;
	}
}
