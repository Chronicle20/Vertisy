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
package server.maps;

import client.MapleClient;
import tools.MaplePacketCreator;
import tools.Randomizer;

public class MapleMapEffect{

	private String msg;
	private int itemId;
	private boolean active = true;
	private int rand;

	public MapleMapEffect(String msg, int itemId){
		this.msg = msg;
		this.itemId = itemId;
		this.rand = Randomizer.nextInt();
	}

	public int getItemID(){
		return itemId;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + itemId;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + rand;
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		MapleMapEffect other = (MapleMapEffect) obj;
		if(active != other.active) return false;
		if(itemId != other.itemId) return false;
		if(msg == null){
			if(other.msg != null) return false;
		}else if(!msg.equals(other.msg)) return false;
		if(rand != other.rand) return false;
		return true;
	}

	public final byte[] makeDestroyData(){
		return MaplePacketCreator.removeMapEffect();
	}

	public final byte[] makeStartData(){
		return MaplePacketCreator.startMapEffect(msg, itemId, active);
	}

	public void sendStartData(MapleClient client){
		if(itemId == 5120010 && !client.isNightOverlayEnabled()) return;
		client.announce(makeStartData());
	}
}
