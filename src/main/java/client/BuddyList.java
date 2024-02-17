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
package client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import net.channel.ChannelServer;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public class BuddyList implements Externalizable{

	private static final long serialVersionUID = -5678601069582180901L;

	public enum BuddyOperation{
		ADDED,
		DELETED
	}

	public enum BuddyAddResult{
		BUDDYLIST_FULL,
		ALREADY_ON_LIST,
		OK
	}

	private Map<Integer, BuddylistEntry> buddies = new LinkedHashMap<>();
	private int capacity;
	private Deque<CharacterNameAndId> pendingRequests = new LinkedList<>();

	public BuddyList(){
		super();
	}

	public BuddyList(int capacity){
		this.capacity = capacity;
	}

	public boolean contains(int characterId){
		return buddies.containsKey(Integer.valueOf(characterId));
	}

	public boolean containsVisible(int characterId){
		BuddylistEntry ble = buddies.get(characterId);
		if(ble == null) return false;
		return ble.isVisible();
	}

	public int getCapacity(){
		return capacity;
	}

	public void setCapacity(int capacity){
		this.capacity = capacity;
	}

	public BuddylistEntry get(int characterId){
		return buddies.get(Integer.valueOf(characterId));
	}

	public BuddylistEntry get(String characterName){
		String lowerCaseName = characterName.toLowerCase();
		for(BuddylistEntry ble : buddies.values()){
			if(ble.getName().toLowerCase().equals(lowerCaseName)) return ble;
		}
		return null;
	}

	public void put(BuddylistEntry entry){
		buddies.put(Integer.valueOf(entry.getCharacterId()), entry);
	}

	public void remove(int characterId){
		buddies.remove(Integer.valueOf(characterId));
	}

	public Collection<BuddylistEntry> getBuddies(){
		return buddies.values();
	}

	public boolean isFull(){
		return buddies.size() >= capacity;
	}

	public int[] getBuddyIds(){
		int buddyIds[] = new int[buddies.size()];
		int i = 0;
		for(BuddylistEntry ble : buddies.values()){
			buddyIds[i++] = ble.getCharacterId();
		}
		return buddyIds;
	}

	public void loadFromDb(int characterId){
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT b.buddyid, b.pending, b.group, c.name as buddyname FROM buddies as b, characters as c WHERE c.id = b.buddyid AND b.characterid = ?");
			ps.setInt(1, characterId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				if(rs.getInt("pending") == 1){
					pendingRequests.push(new CharacterNameAndId(rs.getInt("buddyid"), rs.getString("buddyname")));
				}else{
					put(new BuddylistEntry(rs.getString("buddyname"), rs.getString("group"), rs.getInt("buddyid"), (byte) -1, true));
				}
			}
			rs.close();
			ps.close();
			ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM buddies WHERE pending = 1 AND characterid = ?");
			ps.setInt(1, characterId);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public CharacterNameAndId pollPendingRequest(){
		return pendingRequests.pollLast();
	}

	/*public void addBuddyRequest(MapleClient c, int cidFrom, String nameFrom, int channelFrom){
		put(new BuddylistEntry(nameFrom, "Default Group", cidFrom, channelFrom, false));
		if(pendingRequests.isEmpty()){
			c.announce(MaplePacketCreator.requestBuddylistAdd(cidFrom, c.getPlayer().getId(), nameFrom));
		}else{
			pendingRequests.push(new CharacterNameAndId(cidFrom, nameFrom));
		}
	}*/
	public void addBuddyRequest(int target, int cidFrom, String nameFrom, int channelFrom){//
		put(new BuddylistEntry(nameFrom, "Default Group", cidFrom, channelFrom, false));
		if(pendingRequests.isEmpty()){
			List<Integer> players = new ArrayList<>();
			players.add(target);
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(players, CWvsContext.Friend.requestBuddylistAdd(cidFrom, target, nameFrom));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}else{
			pendingRequests.push(new CharacterNameAndId(cidFrom, nameFrom));
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(capacity);
		out.writeInt(buddies.size());
		for(Entry<Integer, BuddylistEntry> entry : buddies.entrySet()){
			out.writeInt(entry.getKey());
			entry.getValue().writeExternal(out);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		capacity = in.readInt();
		int size = in.readInt();
		for(int i = 0; i < size; i++){
			int chrid = in.readInt();
			BuddylistEntry entry = new BuddylistEntry();
			entry.readExternal(in);
			buddies.put(chrid, entry);
		}
	}
}
