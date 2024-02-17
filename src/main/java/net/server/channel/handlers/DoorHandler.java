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
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleDoor;
import server.maps.objects.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author Matze
 */
public final class DoorHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int chrid = slea.readInt();
		byte mode = slea.readByte(); // specifies if backwarp or not, 1 town to target, 0 target to town
		for(MapleMapObject obj : c.getPlayer().getMap().getMapObjects()){
			if(obj instanceof MapleDoor){
				MapleDoor door = (MapleDoor) obj;
				MapleCharacter owner = door.getOwnerInstance();
				if(owner != null && owner.getId() == chrid){// if the door is the proper one.
					if(c.getPlayer().getId() == owner.getId() || (owner.getPartyId() > 0 && owner.getPartyId() == c.getPlayer().getPartyId())){// if in party, or owner.
						if(mode == 0){
							c.getPlayer().changeMapPortalPosition(door.getTown(), door.getTownPortal());
						}else{
							c.getPlayer().changeMapPosition(door.getTarget(), door.getTargetPosition());
						}
						c.announce(CWvsContext.enableActions());
						break;
					}
				}
			}
		}
	}
}
