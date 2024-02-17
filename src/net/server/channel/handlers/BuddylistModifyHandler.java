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

import static client.BuddyList.BuddyOperation.ADDED;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.*;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public class BuddylistModifyHandler extends AbstractMaplePacketHandler{

	private void nextPendingRequest(MapleClient c){
		CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
		if(pendingBuddyRequest != null){
			c.announce(CWvsContext.Friend.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
		}
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int mode = slea.readByte();
		MapleCharacter player = c.getPlayer();
		BuddyList buddylist = player.getBuddylist();
		if(mode == 1){ // add
			String addName = slea.readMapleAsciiString();
			String group = slea.readMapleAsciiString();
			if(group.length() > 16 || addName.length() < 4 || addName.length() > 13) return; // hax.
			BuddylistEntry ble = buddylist.get(addName);
			if(ble != null && !ble.isVisible() && group.equals(ble.getGroup())){
				c.announce(MaplePacketCreator.serverNotice(1, "You already have " + ble.getName() + " on your Buddylist"));
			}else if(buddylist.isFull() && ble == null){
				c.announce(MaplePacketCreator.serverNotice(1, "Your buddylist is already full"));
			}else if(ble == null){
				try{
					int chrid = -1;
					int buddyListCapacity = 0;
					int channel = -1;
					String chrName = null;
					MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
					if(otherChar != null){
						chrid = otherChar.getId();
						channel = c.getChannel();
						buddyListCapacity = otherChar.getBuddylist().getCapacity();
						chrName = otherChar.getName();
					}else{
						try{
							CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(addName);
							if(location != null){
								chrid = location.chrid;
								buddyListCapacity = location.buddylistCapacity;
								channel = location.channel;
								chrName = location.charName;
							}
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
							c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
						}
					}
					if(chrid == -1){
						try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, id, buddyCapacity FROM characters WHERE name = ?")){
							ps.setString(1, addName);
							try(ResultSet rs = ps.executeQuery()){
								if(rs.next()){
									chrName = rs.getString(1);
									chrid = rs.getInt(2);
									buddyListCapacity = rs.getInt(3);
								}
							}
						}
					}
					if(chrid != -1){
						BuddyAddResult buddyAddResult = BuddyAddResult.OK;
						if(channel != -1){// if they are online
							try{
								buddyAddResult = ChannelServer.getInstance().getWorldInterface().requestBuddyAdd(chrid, c.getChannel(), player.getId(), player.getName());
							}catch(RemoteException | NullPointerException ex){
								Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
								c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
							}
						}else{
							Connection con = DatabaseConnection.getConnection();
							PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
							ps.setInt(1, chrid);
							ResultSet rs = ps.executeQuery();
							if(!rs.next()){
								throw new RuntimeException("Result set expected");
							}else if(rs.getInt("buddyCount") >= buddyListCapacity){
								buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
							}
							rs.close();
							ps.close();
							ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
							ps.setInt(1, chrid);
							ps.setInt(2, player.getId());
							rs = ps.executeQuery();
							if(rs.next()){
								buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
							}
							rs.close();
							ps.close();
						}
						if(buddyAddResult != null){
							if(buddyAddResult == BuddyAddResult.BUDDYLIST_FULL){
								c.announce(MaplePacketCreator.serverNotice(1, addName + "'s Buddylist is full"));
							}else{
								int displayChannel = -1;
								if(buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1){
									displayChannel = channel;
									notifyRemoteChannel(c, chrid, ADDED);
								}else if(buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1){
									Connection con = DatabaseConnection.getConnection();
									try(PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 1)")){
										ps.setInt(1, chrid);
										ps.setInt(2, player.getId());
										ps.executeUpdate();
									}
								}
								buddylist.put(new BuddylistEntry(chrName, group, chrid, displayChannel, true));
								c.announce(CWvsContext.Friend.updateBuddylist(buddylist.getBuddies()));
							}
						}
					}else{
						c.announce(MaplePacketCreator.serverNotice(1, "A character called " + addName + " does not exist"));
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}else{
				ble.changeGroup(group);
				c.announce(CWvsContext.Friend.updateBuddylist(buddylist.getBuddies()));
			}
		}else if(mode == 2){ // accept buddy
			int otherCid = slea.readInt();
			if(!buddylist.isFull()){
				try{
					try{
						CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(otherCid);
						int channel = location != null ? location.channel : -1;
						String otherName = null;
						MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(otherCid);
						if(otherChar == null){
							Connection con = DatabaseConnection.getConnection();
							try(PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?")){
								ps.setInt(1, otherCid);
								try(ResultSet rs = ps.executeQuery()){
									if(rs.next()){
										otherName = rs.getString("name");
									}
								}
							}
						}else{
							otherName = otherChar.getName();
						}
						if(otherName != null){
							buddylist.put(new BuddylistEntry(otherName, "Default Group", otherCid, channel, true));
							c.announce(CWvsContext.Friend.updateBuddylist(buddylist.getBuddies()));
							notifyRemoteChannel(c, otherCid, ADDED);
						}
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}
			nextPendingRequest(c);
		}else if(mode == 3){ // delete
			int otherCid = slea.readInt();
			if(buddylist.containsVisible(otherCid)){
				notifyRemoteChannel(c, otherCid, BuddyOperation.DELETED);
			}
			buddylist.remove(otherCid);
			c.announce(CWvsContext.Friend.updateBuddylist(player.getBuddylist().getBuddies()));
			nextPendingRequest(c);
		}
	}

	private void notifyRemoteChannel(MapleClient c, int otherCid, BuddyOperation operation){
		MapleCharacter player = c.getPlayer();
		try{
			ChannelServer.getInstance().getWorldInterface().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}
}
