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
import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import client.MapleJob;
import net.channel.ChannelServer;
import server.maps.objects.MapleDoor;

public class MaplePartyCharacter implements Externalizable{

	private static final long serialVersionUID = 7349798658862138846L;
	private String name;
	private int id;
	private int level;
	private int channel, world;
	private int jobid;
	private int mapid;
	private boolean online;
	private MapleJob job;
	public int hp, maxHP;
	private List<MapleDoor> doors = new ArrayList<>();

	public MaplePartyCharacter(MapleCharacter maplechar){
		this.name = maplechar.getName();
		this.level = maplechar.getLevel();
		this.channel = maplechar.getClient().getChannel();
		this.world = maplechar.getWorld();
		this.id = maplechar.getId();
		this.jobid = maplechar.getJob().getId();
		this.mapid = maplechar.getMapId();
		this.online = true;
		this.job = maplechar.getJob();
		this.hp = maplechar.getHp();
		this.maxHP = maplechar.getMaxHp();
		this.doors.addAll(maplechar.getDoors());
	}

	public MaplePartyCharacter(){
		super();
		this.name = "";
	}

	public MapleCharacter getPlayerInChannel(){
		return ChannelServer.getInstance().getChannel(channel).getPlayerStorage().getCharacterById(id);
	}

	public MapleJob getJob(){
		return job;
	}

	public int getLevel(){
		return level;
	}

	public int getChannel(){
		return channel;
	}

	public void setChannel(int channel){
		this.channel = channel;
	}

	public boolean isOnline(){
		return online;
	}

	public void setOnline(boolean online){
		this.online = online;
	}

	public int getMapId(){
		return mapid;
	}

	public void setMapId(int mapid){
		this.mapid = mapid;
	}

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	public int getJobId(){
		return jobid;
	}

	public int getHP(){
		return hp;
	}

	public int getMaxHP(){
		return maxHP;
	}

	public List<MapleDoor> getDoors(){
		return doors;
	}

	public void addDoor(MapleDoor door){
		doors.add(door);
	}

	public void clearDoors(){
		doors.clear();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final MaplePartyCharacter other = (MaplePartyCharacter) obj;
		if(name == null){
			if(other.name != null) return false;
		}else if(!name.equals(other.name)) return false;
		return true;
	}

	public int getWorld(){
		return world;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeObject(name);
		out.writeInt(id);
		out.writeInt(level);
		out.writeByte(channel);
		out.writeByte(world);
		out.writeInt(jobid);
		out.writeInt(mapid);
		out.writeBoolean(online);
		out.writeShort(hp);
		out.writeShort(maxHP);
		out.writeByte(doors.size());
		for(MapleDoor door : doors){
			door.writeExternal(out);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		name = (String) in.readObject();
		id = in.readInt();
		level = in.readInt();
		channel = in.readByte();
		world = in.readByte();
		jobid = in.readInt();
		job = MapleJob.getById(jobid);
		mapid = in.readInt();
		online = in.readBoolean();
		hp = in.readShort();
		maxHP = in.readShort();
		byte totalDoors = in.readByte();
		for(byte i = 0; i < totalDoors; i++){
			MapleDoor door = new MapleDoor();
			door.readExternal(in);
			doors.add(door);
		}
	}
}
