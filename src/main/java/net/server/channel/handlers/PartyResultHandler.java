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
import client.MessageType;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class PartyResultHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int operation = slea.readByte();
		MapleCharacter player = c.getPlayer();
		MapleParty party = player.getParty();
		MaplePartyCharacter partyplayer = player.getMPC();
		switch (operation){
			case CWvsContext.Party.Result.InviteParty_Sent:{
				break;
			}
			case CWvsContext.Party.Result.InviteParty_Rejected:{
				if(player.isIronMan()){
					c.announce(CWvsContext.enableActions());
					return;
				}
				int partyid = slea.readInt();
				try{
					party = ChannelServer.getInstance().getWorldInterface().getParty(partyid);
					ChannelServer.getInstance().getWorldInterface().removePartyInvited(party.getId(), c.getPlayer().getName());
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					c.announce(CWvsContext.enableActions());
				}
				break;
			}
			case CWvsContext.Party.Result.InviteParty_Accepted:{
				if(player.isIronMan()){
					c.announce(CWvsContext.enableActions());
					return;
				}
				int partyid = slea.readInt();
				try{
					if(c.getPlayer().getParty() == null){
						party = ChannelServer.getInstance().getWorldInterface().getParty(partyid);
						if(party != null){
							if(party.getMembers().size() < 6){
								if(ChannelServer.getInstance().getWorldInterface().isPartyInvited(party.getId(), c.getPlayer().getName())){
									ChannelServer.getInstance().getWorldInterface().removePartyInvited(party.getId(), c.getPlayer().getName());
									partyplayer = new MaplePartyCharacter(player);
									ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
									player.receivePartyMembers();
									player.updatePartyCharacter();
								}else{
									c.announce(CWvsContext.Party.partyStatusMessage(1));
								}
							}else{
								c.announce(CWvsContext.Party.partyStatusMessage(17));
							}
						}else{
							c.announce(MaplePacketCreator.serverNotice(5, "The person you have invited to the party is already in one."));
						}
					}else{
						c.announce(MaplePacketCreator.serverNotice(5, "You can't join the party as you are already in one."));
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					c.announce(CWvsContext.enableActions());
				}
				break;
			}
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Unknown Party Result: " + operation + " Data: " + slea.toString());
				System.out.println("Unknown Party Result: " + operation + " Data: " + slea.toString());
				break;
		}
	}
}
