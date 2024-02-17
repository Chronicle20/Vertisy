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
public class EndMesoRequirement extends MapleQuestRequirement{

	private int meso;

	public EndMesoRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.END_MESO);
		processData(data);
	}

	public EndMesoRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.END_MESO);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		meso = MapleDataTool.getInt(data);
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		meso = lea.readInt();
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(meso);
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		return chr.getMeso() >= meso;
	}
}
