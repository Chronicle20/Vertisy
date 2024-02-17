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

public class CharacterNameAndId implements Externalizable{

	private static final long serialVersionUID = -4885958996091756477L;
	private int id;
	private String name;

	public CharacterNameAndId(){
		super();
	}

	public CharacterNameAndId(int id, String name){
		super();
		this.id = id;
		this.name = name;
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(id);
		out.writeObject(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		id = in.readInt();
		name = (String) in.readObject();
	}
}
