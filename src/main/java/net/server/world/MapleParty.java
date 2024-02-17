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
package net.server.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.channel.ChannelServer;
import server.events.DemonDoor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleParty implements Externalizable{

	private static final long serialVersionUID = 5378315691081047027L;
	private MaplePartyCharacter leader;
	private List<MaplePartyCharacter> members = new LinkedList<>();
	private List<String> invited = new ArrayList<>();
	private int id;
	public DemonDoor demon_instance = null;
	private int monsterKills;

	/**
	 * LoadParty(0),
	 * CreateNewParty(1),
	 * WithdrawParty(2),
	 * JoinParty(3),
	 * InviteParty(4),
	 * KickParty(5),
	 * LoadParty_Done(6),
	 * CreateNewParty_Done(7),
	 * CreateNewParty_AlreayJoined(8),
	 * CreateNewParty_Beginner(9),
	 * CreateNewParty_Unknown(10),
	 * WithdrawParty_Done(11),
	 * WithdrawParty_NotJoined(12),
	 * WithdrawParty_Unknown(13),
	 * JoinParty_Done(14),
	 * JoinParty_AlreadyJoined(15),
	 * JoinParty_AlreadyFull(16),
	 * JoinParty_UnknownUser(17),
	 * JoinParty_Unknown(18),
	 * InviteParty_BlockedUser(19),
	 * InviteParty_AlreadyInvited(20),
	 * InviteParty_Rejected(21),
	 * AdminCannotCreate(24),
	 * AdminCannotInvite(23),
	 * UnableToFindPlayer(25),
	 * UserMigration(26),
	 * ChangeLevelOrJob(27),
	 * TownPortalChanged(28);
	 */
	public MapleParty(){
		super();
	}

	public MapleParty(int id, MaplePartyCharacter chrfor){
		this.leader = chrfor;
		this.members.add(this.leader);
		this.id = id;
	}

	public boolean containsMembers(MaplePartyCharacter member){
		return members.contains(member);
	}

	public void addMember(MaplePartyCharacter member){
		members.add(member);
	}

	public void removeMember(MaplePartyCharacter member){
		members.remove(member);
	}

	public void setLeader(MaplePartyCharacter victim){
		this.leader = victim;
	}

	public void updateMember(MaplePartyCharacter member){
		for(int i = 0; i < members.size(); i++){
			if(members.get(i).getId() == member.getId()){
				members.set(i, member);
			}
		}
		if(member.getId() == leader.getId()){
			leader = member;
		}
	}

	public int getIndex(MaplePartyCharacter target){
		for(int i = 0; i < members.size(); i++){
			if(members.get(i).getId() == target.getId()) return i;
		}
		return -1;
	}

	public MaplePartyCharacter getMemberById(int id){
		for(MaplePartyCharacter chr : members){
			if(chr.getId() == id) return chr;
		}
		return null;
	}

	public List<MaplePartyCharacter> getMembers(){
		return Collections.unmodifiableList(members);
	}

	public int getPartySize(){
		return members.size();
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public MaplePartyCharacter getLeader(){
		for(MaplePartyCharacter mpc : members){
			if(mpc.getId() == leader.getId()) return mpc;
		}
		return leader;
	}

	public int getAverageLevel(){
		int avgLevel = 0;
		for(MaplePartyCharacter mpc : members){
			avgLevel += mpc.getLevel();
		}
		avgLevel /= members.size();
		return avgLevel;
	}

	public int getOnline(){
		return (int) members.stream().filter(mpc-> mpc.isOnline()).count();
	}

	public int getMonsterKills(){
		return monsterKills;
	}

	public void setMonsterKills(int amount){
		monsterKills = amount;
	}

	public void incrementMonsterKills(){
		monsterKills++;
	}

	public int getMonsterKillsWorld(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getMonsterKillsWorld(id);
		}catch(RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
		}
		return 0;
	}

	public void resetMonsterKills(){
		try{
			ChannelServer.getInstance().getWorldInterface().resetMonsterKills(id);
		}catch(RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
		}
	}

	public void addInvited(String invited){
		this.invited.add(invited.toLowerCase());
	}

	public boolean isInvited(String invited){
		return this.invited.contains(invited.toLowerCase());
	}

	public void removeInvited(String invited){
		this.invited.remove(invited.toLowerCase());
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final MapleParty other = (MapleParty) obj;
		if(id != other.id) return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		leader.writeExternal(out);
		out.writeByte(members.size());
		for(MaplePartyCharacter mpc : members){
			mpc.writeExternal(out);
		}
		out.writeInt(invited.size());
		for(String invite : invited){
			out.writeObject(invite);
		}
		out.writeInt(id);
		out.writeInt(monsterKills);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		leader = new MaplePartyCharacter();
		leader.readExternal(in);
		byte mem = in.readByte();
		for(int i = 0; i < mem; i++){
			MaplePartyCharacter mpc = new MaplePartyCharacter();
			mpc.readExternal(in);
			members.add(mpc);
		}
		int invited = in.readInt();
		for(int i = 0; i < invited; i++){
			this.invited.add((String) in.readObject());
		}
		id = in.readInt();
		monsterKills = in.readInt();
	}
}
