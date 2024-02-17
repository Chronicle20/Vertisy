/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.quest;

import server.quest.requirements.*;

/**
 * Used to get the requirement type from the WZ files by name.
 * 
 * @author Matze
 * @author Tyler (Twdtwd)
 */
public enum MapleQuestRequirementType{
	UNDEFINED(-1, "", null),
	JOB(0, "job", JobRequirement.class),
	ITEM(1, "item", ItemRequirement.class),
	QUEST(2, "quest", QuestRequirement.class),
	MIN_LEVEL(3, "lvmin", MinLevelRequirement.class),
	MAX_LEVEL(4, "lvmax", MaxLevelRequirement.class),
	END_DATE(5, "end", EndDateRequirement.class),
	MOB(6, "mob", MobRequirement.class),
	NPC(7, "npc", NpcRequirement.class),
	FIELD_ENTER(8, "fieldEnter", FieldEnterRequirement.class),
	INTERVAL(9, "interval", IntervalRequirement.class),
	START_SCRIPT(10, "startscript", null),
	END_SCRIPT(11, "endscript", null),
	PET(12, "pet", PetRequirement.class),
	MIN_PET_TAMENESS(13, "pettamenessmin", MinTamenessRequirement.class),
	MONSTER_BOOK(14, "mbmin", MonsterBookCountRequirement.class),
	NORMAL_AUTO_START(15, "normalAutoStart", null),
	INFO_NUMBER(16, "infoNumber", null),
	INFO_EX(17, "infoex", InfoExRequirement.class),
	COMPLETED_QUEST(18, "questComplete", CompletedQuestRequirement.class),
	START(19, "start", null),
	END(20, "end", null),
	DAY_BY_DAY(21, "daybyday", null),
	DAY_OF_WEEK(22, "dayOfWeek", DayOfWeekRequirement.class),
	MB_CARD(23, "mbcard", MonsterBookCardRequirement.class),
	LEVEL(24, "level", LevelRequirement.class),
	END_MESO(25, "endmeso", EndMesoRequirement.class),
	BUFF(26, "buff", BuffRequirement.class),
	SKILL(27, "skill", SkillRequirement.class),
	MORPH(28, "morph", MorphRequirement.class),
	MIN_WORLD(29, "worldmin", MinWorldRequirement.class),
	MAX_WORLD(30, "worldmax", MaxWorldRequirement.class),
	TAMING_MOB_MIN_LEVEL(31, "tamingmoblevelmin", TamingMobMinLevelRequirement.class),
	EQUIP_ALL_NEED(32, "equipAllNeed", EquipAllNeedRequirement.class),
	EQUIP_SELECT_NEED(32, "equipSelectNeed", EquipSelectNeedRequirement.class),
	EXCEPT_BUFF(33, "exceptBuff", ExceptBuffRequirement.class),;

	// partyQuest_S: 5 S ranks in party quests.
	final byte type;
	final String wzName;
	final Class<? extends MapleQuestRequirement> classType;

	private MapleQuestRequirementType(int type, String wzName, Class<? extends MapleQuestRequirement> classType){
		this.type = (byte) type;
		this.classType = classType;
		this.wzName = wzName;
	}

	public byte getType(){
		return type;
	}

	public Class<? extends MapleQuestRequirement> getClassType(){
		return classType;
	}

	public String getWzName(){
		return wzName;
	}

	public static MapleQuestRequirementType getByWZName(String name){
		for(MapleQuestRequirementType type : MapleQuestRequirementType.values()){
			if(type.getWzName().equalsIgnoreCase(name)) return type;
		}
		// FilePrinter.printError(FilePrinter.UNCODED, "Unknown quest requirement type wz name: " + name);
		return MapleQuestRequirementType.UNDEFINED;
	}

	public static MapleQuestRequirementType getByType(byte type){
		for(MapleQuestRequirementType reqType : MapleQuestRequirementType.values()){
			if(reqType.getType() == type) return reqType;
		}
		return MapleQuestRequirementType.UNDEFINED;
	}
}