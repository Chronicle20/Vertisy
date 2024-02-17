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

import java.util.Collection;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.MapleSummon;
import server.movement.MovePath;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.SummonedPool;

public final class MoveSummonHandler extends AbstractMovementPacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int oid = slea.readInt();
		MapleCharacter player = c.getPlayer();
		Collection<MapleSummon> summons = player.getSummons().values();
		MapleSummon summon = null;
		for(MapleSummon sum : summons){
			if(sum.getObjectId() == oid){
				summon = sum;
				break;
			}
		}
		MovePath res = new MovePath();
		res.decode(slea);
		if(summon != null){
			updatePosition(res, summon, 0);
			player.getMap().broadcastMessage(player, SummonedPool.moveSummon(player.getId(), oid, res), summon.getPosition());
		}
	}
}
