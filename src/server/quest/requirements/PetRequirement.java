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
package server.quest.requirements;

import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.inventory.MaplePet;
import provider.MapleData;
import provider.MapleDataTool;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Tyler (Twdtwd)
 */
public class PetRequirement extends MapleQuestRequirement{

	List<Integer> petIDs = new ArrayList<>();

	public PetRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.PET);
		processData(data);
	}

	public PetRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.PET);
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		for(MapleData petData : data.getChildren()){
			petIDs.add(MapleDataTool.getInt(petData.getChildByPath("id")));
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			petIDs.add(lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(petIDs.size());
		for(int id : petIDs){
			lew.writeInt(id);
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		for(MaplePet pet : chr.getPets()){
			if(petIDs.contains(pet.getItemId())) return true;
		}
		return false;
	}
}
