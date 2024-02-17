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

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.MapleClient;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.FamilyPackets;

/**
 * @author Jay Estrella
 */
public final class AcceptFamilyHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.FAMILY){
			c.announce(CWvsContext.enableActions());
			return;
		}
		if(c.getPlayer().getFamilyId() >= 0){
			// check if they are the parent.
			System.out.println("B");
			return;
		}
		int inviterID = slea.readInt();
		MapleCharacter inviter = ChannelServer.getInstance().getCharacterById(inviterID);
		if(inviter == null){
			// send msg
			System.out.println("A");
			return;
		}
		try{
			if(ChannelServer.getInstance().getWorldInterface().joinFamily(inviter.getFamilyId(), inviter.getId(), c.getPlayer().getId())){
				c.announce(FamilyPackets.sendFamilyMessage(0, 0));
				inviter.getClient().announce(FamilyPackets.sendFamilyJoinResponse(true, c.getPlayer().getName()));
			}else System.out.println("Failed");
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		/*if(!ServerConstants.USE_FAMILY_SYSTEM) return;
		// System.out.println(slea.toString());
		int inviterId = slea.readInt();
		// String inviterName = slea.readMapleAsciiString();
		MapleCharacter inviter = c.getWorldServer().getCharacterById(inviterId);
		if(inviter != null){
			inviter.getClient().announce(MaplePacketCreator.sendFamilyJoinResponse(true, c.getPlayer().getName()));
		}
		c.announce(MaplePacketCreator.sendFamilyMessage(0, 0));*/
	}
}
