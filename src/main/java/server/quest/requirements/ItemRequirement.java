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

import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import provider.MapleData;
import provider.MapleDataTool;
import server.ItemInformationProvider;
import server.quest.MapleQuest;
import server.quest.MapleQuestRequirementType;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Tyler (Twdtwd)
 */
public class ItemRequirement extends MapleQuestRequirement{

	Map<Integer, Integer> items = new HashMap<>();

	public ItemRequirement(MapleQuest quest, MapleData data){
		super(MapleQuestRequirementType.ITEM);
		processData(data);
	}

	public ItemRequirement(MapleQuest quest, LittleEndianAccessor lea){
		super(MapleQuestRequirementType.ITEM);
		processData(lea);
	}

	@Override
	public void processData(MapleData data){
		for(MapleData itemEntry : data.getChildren()){
			int itemId = MapleDataTool.getInt(itemEntry.getChildByPath("id"));
			int count = MapleDataTool.getInt(itemEntry.getChildByPath("count"), 0);
			items.put(itemId, count);
		}
	}

	@Override
	public void processData(LittleEndianAccessor lea){
		int total = lea.readInt();
		for(int i = 0; i < total; i++){
			items.put(lea.readInt(), lea.readInt());
		}
	}

	@Override
	public void writeData(LittleEndianWriter lew){
		lew.writeInt(items.size());
		for(int id : items.keySet()){
			lew.writeInt(id);
			lew.writeInt(items.get(id));
		}
	}

	@Override
	public boolean check(MapleCharacter chr, Integer npcid){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		for(Integer itemId : items.keySet()){
			int countNeeded = items.get(itemId);
			int count = 0;
			MapleInventoryType iType = ii.getInventoryType(itemId);
			if(iType.equals(MapleInventoryType.UNDEFINED)) return false;
			for(Item item : chr.getInventory(iType).listById(itemId)){
				count += item.getQuantity();
			}
			// Weird stuff, nexon made some quests only available when wearing gm clothes. This enables us to accept it ><
			if(iType.equals(MapleInventoryType.EQUIP)){
				for(Item item : chr.getInventory(MapleInventoryType.EQUIPPED).listById(itemId)){
					count += item.getQuantity();
				}
			}
			if(count < countNeeded || countNeeded <= 0 && count > 0) return false;
		}
		return true;
	}

	public int getItemAmountNeeded(int itemid){
		if(items.containsKey(itemid)) return items.get(itemid);
		return 0;
	}
}
