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
import java.util.List;

import client.*;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.PlayerBuffValueHolder;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class PartyRequestHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int operation = slea.readByte();
		MapleCharacter player = c.getPlayer();
		MapleParty party = player.getParty();
		MaplePartyCharacter partyplayer = player.getMPC();
		switch (operation){
			case 1:{ // create
				if(player.getLevel() < 10){
					c.announce(CWvsContext.Party.partyStatusMessage(10));
					return;
				}
				if(player.getParty() == null){
					partyplayer = new MaplePartyCharacter(player);
					try{
						party = ChannelServer.getInstance().getWorldInterface().createParty(partyplayer);
						player.setParty(party);
						player.setMPC(partyplayer);
						player.silentPartyUpdate();
						c.announce(CWvsContext.Party.partyCreated(party, partyplayer));
					}catch(Exception ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
						c.announce(CWvsContext.enableActions());
					}
				}else{
					c.announce(MaplePacketCreator.serverNotice(5, "You can't create a party as you are already in one."));
				}
				break;
			}
			case 2:{// Disband/Leave
				if(party != null && partyplayer != null){
					if(partyplayer.equals(party.getLeader())){
						try{
							ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
							c.announce(CWvsContext.enableActions());
						}
						if(player.getEventInstance() != null){
							player.getEventInstance().disbandParty();
						}
						/*for(MaplePartyCharacter mpc : party.getMembers()){
							if(mpc.isOnline()){
								MapleCharacter chr = mpc.getPlayer();
								if(chr.getMap().getInstanceID() != null){// If the map you are in is instanced.
									if(!chr.isMapPurchased(chr.getMap().getInstanceID())){// If you don't own the map.. kick you out.
										chr.changeMap(chr.getMapId());
									}
								}
							}
						}*/
					}else{
						try{
							ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
							c.announce(CWvsContext.enableActions());
						}
						if(player.getEventInstance() != null){
							player.getEventInstance().leftParty(player);
						}
						/*for(MaplePartyCharacter mpc : party.getMembers()){
							if(mpc.isOnline()){
								if(mpc.getId() != partyplayer.getId()){
									if(mpc.getPlayer().getMap().getInstanceID() != null){// If the map you are in is instanced.
										if(partyplayer.getPlayer().isMapPurchased(mpc.getPlayer().getMap().getInstanceID())){// If the map you are in is owned by the person who left.
											mpc.getPlayer().changeMap(mpc.getMapId());
										}
									}
								}
							}
						}*/
					}
					// cancel leavers aura if doesn't have them.
					for(PlayerBuffValueHolder buff : player.getAllBuffs()){
						for(Pair<MapleBuffStat, BuffDataHolder> p : buff.getEffect().getStatups()){
							if(p.left.equals(MapleBuffStat.DarkAura) || p.left.equals(MapleBuffStat.BlueAura) || p.left.equals(MapleBuffStat.YellowAura)){
								if(player.getSkillLevel(p.right.getSourceID()) <= 0){
									player.cancelEffectFromBuffStat(p.left);
								}
							}
						}
					}
					player.setParty(null);
				}
				break;
			}
			case 4:{// invite
				String name = slea.readMapleAsciiString();
				try{
					String response = ChannelServer.getInstance().getWorldInterface().getGuildInviteResponse(name);
					// MapleCharacter invited = world.getCharacterByName(name);
					if(response != null){
						if(response.equals("below10")){
							// if(invited.getLevel() < 10){ // min requirement is level 10
							c.announce(MaplePacketCreator.serverNotice(5, "The player you have invited does not meet the requirements."));
							return;
						}
						if(response.equals("ironman") || player.isIronMan()){
							// if(invited.isIronMan() || player.isIronMan()){
							c.announce(CWvsContext.enableActions());
							return;
						}
						// if(invited.getParty() == null){
						if(!response.equals("inparty")){
							if(player.getParty() == null){
								partyplayer = new MaplePartyCharacter(player);
								try{
									party = ChannelServer.getInstance().getWorldInterface().createParty(partyplayer);
									player.setParty(party);
									player.setMPC(partyplayer);
									c.announce(CWvsContext.Party.partyCreated(party, partyplayer));
								}catch(RemoteException | NullPointerException ex){
									Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
									player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
									c.announce(CWvsContext.enableActions());
								}
							}
							if(party.getMembers().size() < 6){
								// player.getParty().addInvited(name);
								List<String> toPlayer = new ArrayList<>();
								toPlayer.add(name);
								try{
									ChannelServer.getInstance().getWorldInterface().addPartyInvited(player.getParty().getId(), name);
									ChannelServer.getInstance().getWorldInterface().broadcastPacketToPlayers(toPlayer, CWvsContext.Party.partyInvite(player));
								}catch(RemoteException | NullPointerException ex){
									Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
									player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
									c.announce(CWvsContext.enableActions());
								}
								// invited.getClient().announce(MaplePacketCreator.partyInvite(player));
							}else{
								c.announce(CWvsContext.Party.partyStatusMessage(17));
							}
						}else{
							c.announce(CWvsContext.Party.partyStatusMessage(16));
						}
					}else{
						c.announce(CWvsContext.Party.partyStatusMessage(19));
					}
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					c.announce(CWvsContext.enableActions());
				}
				break;
			}
			case 5:{ // expel
				int cid = slea.readInt();
				if(partyplayer.equals(party.getLeader())){
					MaplePartyCharacter expelled = party.getMemberById(cid);
					if(expelled != null){
						try{
							ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.EXPEL, expelled);
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
							c.announce(CWvsContext.enableActions());
						}
						if(player.getEventInstance() != null){
							if(expelled.isOnline()){
								player.getEventInstance().disbandParty();
							}
						}
						/*MapleCharacter expell = c.getWorldServer().getCharacterById(expelled.getId());
						if(expell != null){
							if(expell.getMap().getInstanceID() != null){// In an instanced map
								if(expell.isMapPurchased(expell.getMap().getInstanceID())){// If current map is his..
									expell.getMap().getCharacters().stream().filter(p-> p.getId() != expell.getId()).collect(Collectors.toList()).forEach(p-> p.changeMap(p.getMapId()));
								}else{// Someone else in the party owns the map
									expell.changeMap(expell.getMapId());
								}
							}
						}*/
					}
				}
				break;
			}
			case 6:{
				int newLeader = slea.readInt();
				MaplePartyCharacter newLeadr = party.getMemberById(newLeader);
				party.setLeader(newLeadr);
				try{
					ChannelServer.getInstance().getWorldInterface().updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newLeadr);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					c.announce(CWvsContext.enableActions());
				}
				break;
			}
		}
	}
}