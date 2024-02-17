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

import client.CharacterLocation;
import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.world.MapleMessenger;
import net.server.world.MapleMessengerCharacter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public final class MessengerHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		String input;
		byte mode = slea.readByte();
		MapleCharacter player = c.getPlayer();
		MapleMessenger messenger = player.getMessenger();
		switch (mode){
			case 0x00:// join/create
				if(messenger == null){
					int messengerid = slea.readInt();
					if(messengerid == 0){
						MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, 0);
						try{
							messenger = ChannelServer.getInstance().getWorldInterface().createMessenger(messengerplayer);
							player.setMessenger(messenger);
							player.setMessengerPosition(0);
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
						}
					}else{
						try{
							messenger = ChannelServer.getInstance().getWorldInterface().getMessenger(messengerid);
							int position = messenger.getLowestPosition();
							MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, position);
							if(messenger.getMembers().size() < 3){
								player.setMessenger(messenger);
								player.setMessengerPosition(position);
								ChannelServer.getInstance().getWorldInterface().joinMessenger(messenger.getId(), messengerplayer, player.getName(), messengerplayer.getChannel());
							}
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
						}
					}
				}
				break;
			case 0x02:// leave
				if(messenger != null){
					try{
						MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, player.getMessengerPosition());
						ChannelServer.getInstance().getWorldInterface().leaveMessenger(messenger.getId(), messengerplayer);
						player.setMessenger(null);
						player.setMessengerPosition(4);
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					}
				}
				break;
			case 0x03:// invite
				if(messenger != null){
					if(messenger.getMembers().size() < 3){
						input = slea.readMapleAsciiString();
						MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
						if(target != null){
							if(target.getMessenger() == null){
								target.getClient().announce(MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));
								c.announce(MaplePacketCreator.messengerNote(input, 4, 1));
							}else{
								c.announce(MaplePacketCreator.messengerChat(player.getName() + " : " + input + " is already using Maple Messenger"));
							}
						}else{
							try{
								CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(input);
								if(location != null){
									ChannelServer.getInstance().getWorldInterface().messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel());
								}else{
									c.announce(MaplePacketCreator.messengerNote(input, 4, 0));
								}
							}catch(RemoteException | NullPointerException ex){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
								c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
							}
						}
					}else{
						c.announce(MaplePacketCreator.messengerChat(player.getName() + " : You cannot have more than 3 people in the Maple Messenger"));
					}
				}else{
					System.out.println("Messenger: " + (player.getMessenger() == null));
				}
				break;
			case 0x05:
				String targeted = slea.readMapleAsciiString();
				MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
				if(target != null){
					if(target.getMessenger() != null){
						target.getClient().announce(MaplePacketCreator.messengerNote(player.getName(), 5, 0));
					}
				}else{
					try{
						ChannelServer.getInstance().getWorldInterface().declineChat(targeted, player.getName());
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					}
				}
				break;
			case 0x06:// send message
				if(messenger != null){
					MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, player.getMessengerPosition());
					input = slea.readMapleAsciiString();
					try{
						ChannelServer.getInstance().getWorldInterface().messengerChat(messenger.getId(), input, messengerplayer.getName());
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					}
				}
				break;
		}
	}
}
