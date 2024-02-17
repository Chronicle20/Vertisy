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

import java.util.List;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.life.MapleLifeFactory;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author AngelSL
 */
public final class UseSummonBag extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		// [4A 00][6C 4C F2 02][02 00][63 0B 20 00]
		if(!c.getPlayer().isAlive()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		slea.readInt();
		short slot = slea.readShort();
		int itemId = slea.readInt();
		Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if(toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId){
			MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
			List<Pair<Integer, Integer>> pList = ItemInformationProvider.getInstance().getItemData(itemId).mobs;
			for(Pair<Integer, Integer> p : pList){
				if(Randomizer.nextInt(101) <= p.right){
					c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(p.left), c.getPlayer().getPosition());
				}
			}
		}else AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use summon bag while item is null, or quantity is zero, or mismatching item in slot");
		c.announce(CWvsContext.enableActions());
	}
}
