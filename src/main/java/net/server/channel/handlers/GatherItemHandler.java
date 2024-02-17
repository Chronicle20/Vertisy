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

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class GatherItemHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		chr.getAutobanManager().setTimestamp(2, slea.readInt(), 3);
		MapleInventoryType inventoryType = MapleInventoryType.getByType(slea.readByte());
		if(inventoryType.equals(MapleInventoryType.UNDEFINED) || c.getPlayer().getInventory(inventoryType).isFull()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		MapleInventory inventory = c.getPlayer().getInventory(inventoryType);
		boolean sorted = false;
		while(!sorted){
			byte freeSlot = (byte) inventory.getNextFreeSlot();
			if(freeSlot != -1){
				short itemSlot = -1;
				for(byte i = (byte) (freeSlot + 1); i <= inventory.getSlotLimit(); i++){
					if(inventory.getItem(i) != null){
						itemSlot = i;
						break;
					}
				}
				if(itemSlot > 0){
					MapleInventoryManipulator.move(c, inventoryType, itemSlot, freeSlot);
				}else{
					sorted = true;
				}
			}else{
				sorted = true;
			}
		}
		c.announce(MaplePacketCreator.finishedGather(inventoryType.getType()));
		c.announce(CWvsContext.enableActions());
	}
}
