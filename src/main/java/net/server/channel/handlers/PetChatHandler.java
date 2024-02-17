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
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class PetChatHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int petId = slea.readInt();
		/*int unk = */slea.readInt();
		byte n = slea.readByte();
		int nAction = slea.readByte();
		byte pet = c.getPlayer().getPetIndex(petId);
		if((pet < 0 || pet > 3)) return;
		String text = slea.readMapleAsciiString();
		if(text.length() > Byte.MAX_VALUE){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with pets with text length of " + text.length());
			c.disconnect(true, false);
			return;
		}
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.petChat(c.getPlayer().getId(), pet, n, nAction, text), true);
		Logger.log(LogType.INFO, LogFile.PET_CHAT, c.getPlayer().getName() + ".txt", text + "\r\nPetId: " + petId + " act: " + nAction);
		c.announce(CWvsContext.enableActions());
	}
}
