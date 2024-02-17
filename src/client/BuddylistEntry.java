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

import tools.data.output.LittleEndianWriter;

public class BuddylistEntry implements Externalizable{

	private static final long serialVersionUID = 2832863246128897060L;
	private String name;
	private String group;
	private int cid;
	private int channel;
	private boolean visible;
	public boolean inShop;// TODO: Update, but eric says it does nothing. Apparently not for cashshop.

	public BuddylistEntry(){
		super();
	}

	/**
	 * @param name
	 * @param characterId
	 * @param channel should be -1 if the buddy is offline
	 * @param visible
	 */
	public BuddylistEntry(String name, String group, int characterId, int channel, boolean visible){
		this.name = name;
		this.group = group;
		this.cid = characterId;
		this.channel = channel;
		this.visible = visible;
	}

	/**
	 * @return the channel the character is on. If the character is offline returns -1.
	 */
	public int getChannel(){
		return channel;
	}

	public void setChannel(int channel){
		this.channel = channel;
	}

	public boolean isOnline(){
		return channel >= 0;
	}

	public String getName(){
		return name;
	}

	public String getGroup(){
		return group;
	}

	public int getCharacterId(){
		return cid;
	}

	public void setVisible(boolean visible){
		this.visible = visible;
	}

	public boolean isVisible(){
		return visible;
	}

	public void changeGroup(String group){
		this.group = group;
	}

	public void encode(LittleEndianWriter lew){
		// GW_Friend::Decode
		// struct GW_Friend
		lew.writeInt(cid);// dwFriendID
		lew.writeNullTerminatedAsciiString(getName(), 13);
		lew.write(0); // opposite status, nFlag
		lew.writeInt(getChannel());
		lew.writeNullTerminatedAsciiString(getGroup(), 17);
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + cid;
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final BuddylistEntry other = (BuddylistEntry) obj;
		if(cid != other.cid) return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeObject(name);
		out.writeObject(group);
		out.writeInt(cid);
		out.writeByte(channel);
		out.writeBoolean(visible);
		out.writeBoolean(inShop);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		name = (String) in.readObject();
		group = (String) in.readObject();
		cid = in.readInt();
		channel = in.readByte();
		visible = in.readBoolean();
		inShop = in.readBoolean();
	}
}
