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
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.userpool.UserRemote;

public final class FaceExpressionHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int emote = slea.readInt();
		boolean usedCashItem = false;
		if(emote > 7 && emote < (5170000 - 5159992)){
			usedCashItem = true;
			int emoteid = 5159992 + emote;
			if(c.getPlayer().getInventory(ItemInformationProvider.getInstance().getInventoryType(emoteid)).findById(emoteid) == null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use Face Expression on invalid(or null item) emote: " + emoteid);
				return;
			}
		}
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), UserRemote.OnEmotion(c.getPlayer(), emote, usedCashItem), false);
	}
}
