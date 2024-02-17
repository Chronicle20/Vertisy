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
package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author BubblesDev
 */
public final class SortItemHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		chr.getAutobanManager().setTimestamp(4, slea.readInt(), 3);
		byte inventoryType = slea.readByte();
		if(inventoryType < 1 || inventoryType > 5){
			c.disconnect(false, false);
			return;
		}
		if(inventoryType == 5){
			chr.dropMessage("Unfortunately, sorting of cash inventory is disabled.");
			return;
		}
		final MapleInventoryType invType = MapleInventoryType.getByType(inventoryType);
		MapleInventory inventory = chr.getInventory(MapleInventoryType.getByType(inventoryType));
		final List<Item> itemList = new LinkedList<>();
		for(Item item : inventory.list()){
			itemList.add(item.copy(false)); // clone all items T___T.
		}
		for(Item itemStats : itemList){
			MapleInventoryManipulator.removeItem(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, true);
		}
		final List<Item> sortedItemsList = sortItems(itemList);
		for(Item item : sortedItemsList){
			MapleInventoryManipulator.addFromDrop(c, item, false);
		}
		c.announce(MaplePacketCreator.finishedSort2(inventoryType));
		c.announce(CWvsContext.enableActions());
	}

	private static List<Item> sortItems(final List<Item> passedMap){
		final List<Integer> itemIds = new ArrayList<>(); // empty list.
		for(Item item : passedMap){
			itemIds.add(item.getItemId()); // adds all item ids to the empty list to be sorted.
		}
		Collections.sort(itemIds); // sorts item ids
		final List<Item> sortedList = new LinkedList<>(); // ordered list pl0x <3.
		for(Integer val : itemIds){
			for(Item item : passedMap){
				if(val == item.getItemId()){ // Goes through every index and finds the first value that matches
					sortedList.add(item);
					passedMap.remove(item);
					break;
				}
			}
		}
		return sortedList;
	}
}
