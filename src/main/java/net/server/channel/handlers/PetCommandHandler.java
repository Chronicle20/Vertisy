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
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory.PetData;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class PetCommandHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		int petId = slea.readInt();
		byte petIndex = chr.getPetIndex(petId);
		MaplePet pet;
		if(petIndex == -1){
			return;
		}else{
			pet = chr.getPet(petIndex);
		}
		// decode 1,
		// v4 = decode1
		/*int i = */slea.readInt();
		/*int b = */slea.readByte();
		int command = slea.readByte();
		// System.out.println("PetCommand: " + "i: " + i + " b: " + b + " command: " + command);
		ItemData data = ItemInformationProvider.getInstance().getItemData(pet.getItemId());
		PetData petData = data.petData.get("" + command);
		if(petData == null){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use invalid pet command: " + command);
			return;
		}
		boolean success = false;
		if(Randomizer.nextInt(101) <= petData.prob){
			success = true;
			pet.gainCloseness(chr, petData.inc);
			Item petz = chr.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
			chr.forceUpdateItem(petz);
		}
		chr.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(chr.getId(), petIndex, command, success), true);
		c.announce(CWvsContext.enableActions());
	}
}
