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
package server.life;

import client.MapleClient;
import server.maps.objects.MapleMapObjectType;
import server.shops.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.field.NpcPool;

public class MapleNPC extends AbstractLoadedMapleLife{

	public MapleNPCStats stats;

	public MapleNPC(int id, MapleNPCStats stats){
		super(id);
		this.stats = stats;
	}

	public MapleNPC(MapleNPC npc){
		super(npc);
	}

	public MapleNPC(){
		super();
	}

	public boolean hasShop(){
		return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
	}

	public void sendShop(MapleClient c){
		MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
	}

	@Override
	public void sendSpawnData(MapleClient client){
		if(this.getId() > 9010010 && this.getId() < 9010014){
			client.announce(MaplePacketCreator.spawnNPCRequestController(this, false));
		}else{
			client.announce(NpcPool.spawnNPC(this));
			client.announce(MaplePacketCreator.spawnNPCRequestController(this, true));
		}
	}

	@Override
	public void sendDestroyData(MapleClient client){
		// setId(9390409);
		// client.announce(MaplePacketCreator.spawnNPC(this, false));
		// client.announce(MaplePacketCreator.removeNPC(getId()));
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.NPC;
	}

	public String getName(){
		return stats.getName();
	}

	@Override
	public MapleNPC clone(){
		MapleNPC clone = new MapleNPC(this);
		clone.setStance(getStance());
		clone.setPosition(getPosition());
		clone.stats = stats;
		return clone;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		super.save(mplew);
		stats.save(mplew);
		mplew.writeInt(getCy());
		mplew.writeInt(getRx0());
		mplew.writeInt(getRx1());
		mplew.writeBoolean(isHidden());
	}

	@Override
	public void load(LittleEndianAccessor slea){
		super.load(slea);
		stats = new MapleNPCStats();
		stats.load(slea);
		setCy(slea.readInt());
		setRx0(slea.readInt());
		setRx1(slea.readInt());
		setHide(slea.readBoolean());
	}
}
