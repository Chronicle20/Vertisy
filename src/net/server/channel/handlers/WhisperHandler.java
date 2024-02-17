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
import java.util.ArrayList;

import client.CharacterLocation;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.BigBrother;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Field;

/**
 * @author Matze
 */
public final class WhisperHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		byte mode = slea.readByte();
		slea.readInt();
		if(mode == 6){ // whisper
			String recipient = slea.readMapleAsciiString();
			// Removes the [Donor] from whisper
			recipient = recipient.replace("[GM] ", "");
			String text = slea.readMapleAsciiString();
			if(c.getPlayer().isChatBanned()){
				c.getPlayer().dropMessage(5, "You are curently banned from talking!");
				return;
			}
			if(c.getPlayer().getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) return;
			if(text.length() > Byte.MAX_VALUE && !c.getPlayer().isGM() && c.getPlayer().getClient().getGMLevel() < 1){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with whispers with text length of " + text.length());
				c.disconnect(false, false);
				return;
			}
			// MapleCharacter player = c.getWorldServer().getCharacterByName(recipient);
			ArrayList<String> player = new ArrayList<String>();
			player.add(recipient);
			// if(player != null){
			try{
				CharacterLocation findInfo = ChannelServer.getInstance().getWorldInterface().find(recipient);
				ChannelServer.getInstance().getWorldInterface().broadcastPacketToPlayers(player, Field.getWhisper(c.getPlayer().getName(), c.getChannel(), c.getPlayer().isGM(), text));
				if(findInfo == null || findInfo.gmLevel > c.getPlayer().getGMLevel()){
					c.announce(Field.getWhisperReply(recipient, (byte) 0));
				}else c.announce(Field.getWhisperReply(recipient, (byte) 1));
				BigBrother.whisper("[" + recipient + "] " + text, c.getPlayer().getName(), recipient);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				c.announce(Field.getWhisperReply(recipient, (byte) 0));
			}
			// }else{// not found
			// c.announce(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
			// }
			c.getPlayer().getAutobanManager().spam(7);
		}else if(mode == 5){ // - /find
			String recipient = slea.readMapleAsciiString();
			try{
				CharacterLocation findInfo = ChannelServer.getInstance().getWorldInterface().find(recipient);
				if(findInfo != null){
					if(findInfo.gmLevel > c.getPlayer().getGMLevel()){
						c.announce(Field.getWhisperReply(recipient, (byte) 0));
						return;
					}
					if(findInfo.cashshop || findInfo.mts){
						c.announce(MaplePacketCreator.getFindReply(recipient, -1, findInfo.cashshop ? 2 : 0));
						return;
					}
					if(findInfo.channel != c.getChannel()){
						c.announce(MaplePacketCreator.getFindReply(recipient, findInfo.channel, 3));
					}else{
						c.announce(MaplePacketCreator.getFindReply(recipient, findInfo.mapid, 1));
					}
				}else{
					c.announce(Field.getWhisperReply(recipient, (byte) 0));
				}
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				c.announce(Field.getWhisperReply(recipient, (byte) 0));
			}
		}else if(mode == 0x44){
			// Buddy find?
		}
	}
}
