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
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class PartyChatHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter player = c.getPlayer();
		if(player.isChatBanned()){
			player.dropMessage(5, "You are curently banned from talking!");
			return;
		}
		if(player.getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) return;
		slea.readInt();
		int type = slea.readByte(); // 0 for buddys, 1 for partys
		int numRecipients = slea.readByte();
		int recipients[] = new int[numRecipients];
		for(int i = 0; i < numRecipients; i++){
			recipients[i] = slea.readInt();
		}
		String chattext = slea.readMapleAsciiString();
		if(chattext.length() > Byte.MAX_VALUE && !player.isGM()){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit chats with text length of " + chattext.length());
			c.disconnect(true, false);
			return;
		}
		if(type == 0){
			try{
				ChannelServer.getInstance().getWorldInterface().buddyChat(recipients, player.getName(), player.getId(), chattext);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
				c.announce(CWvsContext.enableActions());
			}
		}else if(type == 1 && player.isInParty()){
			try{
				ChannelServer.getInstance().getWorldInterface().partyChat(player.getParty(), chattext, player.getName());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
				c.announce(CWvsContext.enableActions());
			}
		}else if(type == 2 && player.getGuildId() > 0){
			try{
				ChannelServer.getInstance().getWorldInterface().guildChat(player.getGuildId(), player.getName(), player.getId(), chattext);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
				c.announce(CWvsContext.enableActions());
			}
		}else if(type == 3 && player.getGuild() != null){
			int allianceId = player.getGuild().getAllianceId();
			if(allianceId > 0){
				try{
					ChannelServer.getInstance().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.multiChat(player.getName(), chattext, 3), player.getId(), -1);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					c.announce(CWvsContext.enableActions());
				}
			}
		}
		player.getAutobanManager().spam(7);
	}
}
