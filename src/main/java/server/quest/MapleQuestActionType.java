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

import server.quest.actions.*;

/**
 * Used to get the action type from the WZ file based on name.
 * 
 * @author Matze
 * @author Tyler (Twdtwd)
 */
public enum MapleQuestActionType{
	UNDEFINED(-1, "", null),
	EXP(0, "exp", ExpAction.class),
	ITEM(1, "item", ItemAction.class),
	NEXTQUEST(2, "nextQuest", NextQuestAction.class),
	MESO(3, "money", MesoAction.class),
	QUEST(4, "quest", null),
	SKILL(5, "skill", SkillAction.class),
	FAME(6, "pop", FameAction.class),
	BUFF(7, "buffItemID", BuffAction.class),
	PETSKILL(8, "petskill", PetSkillAction.class),
	YES(9, "yes", null),
	NO(10, "no", null),
	NPC(11, "npc", null),
	MIN_LEVEL(12, "lvmin", null),
	NORMAL_AUTO_START(13, "normalAutoStart", null),
	ZERO(14, "0", null),
	INFO(15, "info", InfoAction.class),
	SP(16, "sp", SPAction.class);

	final byte type;
	final String wzName;
	final Class<? extends MapleQuestAction> classType;

	private MapleQuestActionType(int type, String wzName, Class<? extends MapleQuestAction> classType){
		this.type = (byte) type;
		this.wzName = wzName;
		this.classType = classType;
	}

	public byte getType(){
		return type;
	}

	public String getWzName(){
		return this.wzName;
	}

	public Class<? extends MapleQuestAction> getClassType(){
		return this.classType;
	}

	public static MapleQuestActionType getByWZName(String name){
		for(MapleQuestActionType type : MapleQuestActionType.values()){
			if(type.getWzName().equals(name)) return type;
		}
		// FilePrinter.printError(FilePrinter.UNCODED, "Unknown quest action type wz name: " + name);
		return MapleQuestActionType.UNDEFINED;
	}

	public static MapleQuestActionType getByType(byte type){
		for(MapleQuestActionType reqType : MapleQuestActionType.values()){
			if(reqType.getType() == type) return reqType;
		}
		return MapleQuestActionType.UNDEFINED;
	}
}