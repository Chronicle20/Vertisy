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

import java.awt.Point;
import java.util.Arrays;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import constants.ItemConstants;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author TheRamon
 */
public class PetLootHandler extends ItemPickupHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		long petIndex = slea.readLong();// petLockerSN
		if(petIndex < 0){
			c.announce(CWvsContext.enableActions());
			return;
		}
		MaplePet pet = chr.getPet(chr.getPetIndex(petIndex));
		if(pet == null || !pet.isSummoned()) return;
		slea.readByte();
		slea.readInt();
		Point petPos = slea.readPos();
		int oid = slea.readInt();
		slea.readInt();// dwCliCrc
		slea.readBoolean();// bPickupOthers
		slea.readBoolean();// bSweepForDrop
		slea.readBoolean();// bLongRange
		MapleMapObject ob = chr.getMap().getMapObject(oid);
		if(ob == null){
			c.announce(MaplePacketCreator.getInventoryFull());
			return;
		}
		if(ob instanceof MapleMapItem){
			pickupItem(c, pet, ob, (MapleMapItem) ob, petPos, false);
			if(chr.getItemQuantity(ItemConstants.PET_ITEM_VAC, false) >= 1 && c.getPetVac()){
				for(MapleMapObject mpo : chr.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))){
					pickupItem(c, pet, mpo, (MapleMapItem) mpo, petPos, true);
				}
			}
		}
		c.announce(CWvsContext.enableActions());
	}
}
