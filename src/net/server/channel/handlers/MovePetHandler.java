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
import server.movement.Elem;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class MovePetHandler extends AbstractMovementPacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		long petId = slea.readLong();
		MovePath res = new MovePath();
		res.decode(slea);
		if(res.lElem.isEmpty()) return;
		MapleCharacter player = c.getPlayer();
		if(player == null) return;
		byte slot = player.getPetIndex(petId);
		if(slot == -1) return;
		player.getPet(slot).updatePosition(res);
		if(c.getPlayer().bMoveAction != -1){
			for(Elem elem : res.lElem){
				elem.bMoveAction = c.getPlayer().bMoveAction;
			}
		}
		player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), slot, res), false);
	}
}
