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
public class MinWorldRequirement extends MapleQuestRequirement{

	private int minWorld;

	public MinWorldRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.MIN_WORLD);
		processData(data);
	}

	public MinWorldRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.MIN_WORLD);
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		minWorld = MapleDataTool.getIntConvert(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		minWorld = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(minWorld);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		return chr.getWorld() >= minWorld;
	}
}
