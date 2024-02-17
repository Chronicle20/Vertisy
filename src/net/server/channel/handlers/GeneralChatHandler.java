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
import client.command.CommandHandler;
import client.command.Commands;
import tools.BigBrother;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public final class GeneralChatHandler extends net.AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		slea.readInt();//
		String s = slea.readMapleAsciiString();
		MapleCharacter chr = c.getPlayer();
		if(chr.isChatBanned()){
			chr.dropMessage(5, "You are curently banned from using chat!");
			return;
		}
		if(chr.getAutobanManager().getLastSpam(7) + 200 > System.currentTimeMillis()) return;
		if(s.length() > Byte.MAX_VALUE && !chr.isGM()){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit in General Chat with text length of " + s.length());
			c.disconnect(false, false);
			return;
		}
		char heading = s.charAt(0);
		if(heading == '/' || heading == '!' || heading == '@'){
			String[] sp = s.split(" ");
			sp[0] = sp[0].toLowerCase().substring(1);
			// TimerManager.getInstance().execute("generalChat-" + sp[0], ()-> {
			if(!CommandHandler.handleCommand(c, s)){
				if(!Commands.executePlayerCommand(c, sp, heading)){
					if(chr.isGM()){
						if(Commands.executeGMCommand(c, sp)) Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + s);
					}
				}else Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + s);
			}else Logger.log(LogType.INFO, LogFile.COMMAND, c.getAccountName(), MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " used: " + s);
			// });
		}else{
			int show = slea.readByte();
			if(chr.getMap().isMuted() && !chr.isGM()){
				chr.dropMessage(5, "The map you are in is currently muted. Please try again later.");
				return;
			}
			if(!chr.isHidden()){
				chr.getMap().broadcastMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.getWhiteChat(), show));
			}else{
				chr.getMap().broadcastGMMessage(MaplePacketCreator.getChatText(chr.getId(), s, chr.getWhiteChat(), show));
			}
			if(show == 0){
				BigBrother.general(s, chr.getName(), chr.getMap().getAllPlayer());
			}
		}
		chr.getAutobanManager().spam(7);
	}
}
