package server.quest.requirements;

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import client.Skill;
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
public class SkillRequirement extends MapleQuestRequirement{

	Map<Integer, Integer> skills = new HashMap<>();

	public SkillRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.SKILL);
		processData(data);
	}

	public SkillRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.SKILL);
		processData(lea);
	}

	/**
	 * @param data
	 */
	@Override
	public void processData(MapleData data){
		for(MapleData itemEntry : data.getChildren()){
			int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
			int level = MapleDataTool.getInt(itemEntry.getChildByPath("acquire"), 0);
			skills.put(itemId, level);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			skills.put(lea.readInt(), lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(skills.size());
		for(int id : skills.keySet()){
			lew.writeInt(id);
			lew.writeInt(skills.get(id));
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		for(int skillID : skills.keySet()){
			int levelReq = skills.get(skillID);
			boolean foundSkill = false;
			for(Skill skill : chr.getSkills().keySet()){
				if(skillID == skill.getId()){
					foundSkill = true;
					if(chr.getSkills().get(skill).skillevel < levelReq) return false;
				}
			}
			if(!foundSkill && levelReq != 0) return false;
		}
		return true;
	}
}
