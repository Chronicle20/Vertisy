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
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.SummonedPool;

public final class DamageSummonHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int objectid = slea.readInt();
		int unkByte = slea.readByte();
		int damage = slea.readInt();
		int monsterIdFrom = slea.readInt();
		slea.readBoolean();
		MapleCharacter player = c.getPlayer();
		MapleMapObject mmo = player.getMap().getMapObject(objectid);
		if(mmo != null && mmo instanceof MapleSummon){
			MapleSummon summon = (MapleSummon) mmo;
			if(summon.getOwner().getId() == player.getId()){
				if(summon != null){
					summon.addHP(-damage);
					if(summon.getHP() <= 0){
						player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
					}
					player.getMap().broadcastMessage(player, SummonedPool.damageSummon(player.getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getPosition());
				}
			}else AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to damage a summon that isn't his.");
		}else AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to damage a summon that doesn't exist.");
	}
}
