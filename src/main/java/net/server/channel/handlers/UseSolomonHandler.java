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

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author XoticStory; modified by kevintjuh93
 */
public final class UseSolomonHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		slea.readInt();
		short slot = slea.readShort();
		int itemId = slea.readInt();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Item slotItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		ItemData data = ii.getItemData(itemId);
		if(c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) <= 0 || slotItem.getItemId() != itemId || c.getPlayer().getLevel() > data.maxLevel){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use Solomon with no item, mismatch itemid, or invalid level");
			return;
		}
		if((c.getPlayer().getGachaExp() + data.exp) > Integer.MAX_VALUE) return;
		c.getPlayer().gainGachaExp(data.exp);
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
		c.announce(CWvsContext.enableActions());
	}
}
