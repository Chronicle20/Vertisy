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

import client.MapleBuffStat;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author Matze
 */
public final class UseItemHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!c.getPlayer().isAlive()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		slea.readInt();
		short slot = slea.readShort();
		int itemId = slea.readInt();
		Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if(toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId){
			if(itemId == 2022178 || itemId == 2022433 || itemId == 2050004){
				c.getPlayer().dispelDebuffs();
				remove(c, slot);
				return;
			}else if(itemId == 2050001){
				c.getPlayer().cancelBuffStats(MapleBuffStat.DARKNESS);
				remove(c, slot);
				return;
			}else if(itemId == 2050002){
				c.getPlayer().cancelBuffStats(MapleBuffStat.WEAKEN);
				remove(c, slot);
				return;
			}else if(itemId == 2050003){
				c.getPlayer().cancelBuffStats(MapleBuffStat.SEAL);
				c.getPlayer().cancelBuffStats(MapleBuffStat.CURSE);
				remove(c, slot);
				return;
			}
			if(isTownScroll(itemId)){
				if(ii.getItemData(toUse.getItemId()).itemEffect.applyTo(c.getPlayer())){

					remove(c, slot);
				}
				return;
			}
			if(ii.getItemData(toUse.getItemId()).itemEffect.applyTo(c.getPlayer())){
				remove(c, slot);
			}
			c.getPlayer().getStats().recalcLocalStats(c.getPlayer());
		}
	}

	private void remove(MapleClient c, short slot){
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
		c.announce(CWvsContext.enableActions());
	}

	private boolean isTownScroll(int itemId){
		return itemId >= 2030000 && itemId < 2030021;
	}
}
