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
import client.autoban.AutobanManager;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class PetFoodHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		AutobanManager abm = chr.getAutobanManager();
		if(abm.getLastSpam(2) + 500 > System.currentTimeMillis()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		abm.spam(2);
		abm.setTimestamp(1, slea.readInt(), 3);
		if(chr.getNoPets() == 0){
			c.announce(CWvsContext.enableActions());
			return;
		}
		int previousFullness = 100;
		byte slot = 0;
		MaplePet[] pets = chr.getPets();
		for(byte i = 0; i < 3; i++){
			if(pets[i] != null){
				if(pets[i].getFullness() < previousFullness){
					slot = i;
					previousFullness = pets[i].getFullness();
				}
			}
		}
		MaplePet pet = chr.getPet(slot);
		short pos = slea.readShort();
		int itemId = slea.readInt();
		Item use = chr.getInventory(MapleInventoryType.USE).getItem(pos);
		if(use == null || (itemId / 10000) != 212 || use.getItemId() != itemId){
			if((itemId / 10000) != 212){
				if(itemId < 0){
					chr.gainMeso(Math.abs(itemId), true);
				}else{
					short quantity = slea.readShort();
					Item item = null;
					if(ItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP){
						item = ItemInformationProvider.getInstance().getEquipById(itemId);
					}else{
						item = new Item(itemId, quantity);
					}
					MapleInventoryManipulator.addFromDrop(c, item, true);
				}
			}
			c.announce(CWvsContext.enableActions());
			return;
		}
		pet.feed(c.getPlayer());
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, pos, (short) 1, true, false);
		pet.saveToDb();
		Item petz = chr.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
		if(petz == null){ // Not a real fix but fuck it you know?
			c.announce(CWvsContext.enableActions());
			return;
		}
		chr.forceUpdateItem(petz);
		c.announce(CWvsContext.enableActions());
	}
}
