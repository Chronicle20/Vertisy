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
import net.AbstractMaplePacketHandler;
import scripting.item.ItemScriptManager;
import server.ItemInformationProvider;
import server.ItemInformationProvider.scriptedItem;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author Jay Estrella
 */
public final class ScriptedItemHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		slea.readInt(); // trash stamp (thx rmzero)
		short itemSlot = slea.readShort(); // item sl0t (thx rmzero)
		int itemId = slea.readInt(); // itemId
		scriptedItem info = ii.getItemData(itemId).scriptedItem;
		if(info == null || info.npc == 0){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use an invalid scripted item: " + itemId);
			return;
		}
		ItemScriptManager ism = ItemScriptManager.getInstance();
		Item item = c.getPlayer().getInventory(ii.getInventoryType(itemId)).getItem(itemSlot);
		if(item == null || item.getItemId() != itemId || item.getQuantity() < 1) return;
		ism.getItemScript(c, info.getScript());
		c.announce(CWvsContext.enableActions());
		// NPCScriptManager.getInstance().start(c, info.getNpc(), null, null);
	}
}
