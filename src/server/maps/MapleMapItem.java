/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.maps;

import java.awt.Point;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import server.maps.objects.AbstractMapleMapObject;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.packets.field.DropPool;

public class MapleMapItem extends AbstractMapleMapObject{

	protected Item item;
	protected MapleMapObject dropper;
	protected int chrid, ownerid, meso, questid = -1;
	protected byte type;
	protected boolean pickedUp = false, playerDrop;
	protected long dropTime, deleteTime;
	public ReentrantLock itemLock = new ReentrantLock();
	private boolean ironMan;

	public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop){
		this(item, position, dropper, owner, type, playerDrop, 0);
	}

	public MapleMapItem(int meso, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop){
		this(null, position, dropper, owner, type, playerDrop, 0);
		this.meso = meso;
	}

	public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop, int questid){
		this(item, position, dropper, owner.isIronMan(), owner.getParty() == null ? owner.getId() : owner.getPartyId(), owner.getId(), type, playerDrop, questid);
	}

	public MapleMapItem(Item item, Point position, MapleMapObject dropper, boolean ironMan, int ownerID, int chrid, byte type, boolean playerDrop, int questid){
		setPosition(position);
		this.item = item;
		this.dropper = dropper;
		this.ironMan = ironMan;
		this.ownerid = ownerID;
		this.chrid = chrid;
		this.meso = 0;
		this.type = type;
		this.playerDrop = playerDrop;
		this.questid = questid;
	}

	public final Item getItem(){
		return item;
	}

	public final int getQuest(){
		return questid;
	}

	public final int getItemId(){
		if(getMeso() > 0) return meso;
		return item.getItemId();
	}

	public final MapleMapObject getDropper(){
		return dropper;
	}

	/**
	 * @return Can be a character id or party id.
	 */
	public final int getOwner(){
		return ownerid;
	}

	public final int getOwnerChrId(){
		return chrid;
	}

	public final int getMeso(){
		return meso;
	}

	public final boolean isPlayerDrop(){
		return playerDrop;
	}

	public final boolean isPickedUp(){
		return pickedUp;
	}

	public void setPickedUp(final boolean pickedUp){
		this.pickedUp = pickedUp;
	}

	public long getDropTime(){
		return dropTime;
	}

	public void setDropTime(long time){
		this.dropTime = time;
	}

	public long getDeleteTime(){
		return deleteTime;
	}

	public void setDeleteTime(long time){
		this.deleteTime = time;
	}

	public byte getDropType(){
		return type;
	}

	@Override
	public final MapleMapObjectType getType(){
		return MapleMapObjectType.ITEM;
	}

	@Override
	public void sendSpawnData(final MapleClient client){
		if(questid <= 0 || (client.getPlayer().getQuestStatus(questid) == 1 && client.getPlayer().needQuestItem(questid, item.getItemId()))){
			client.announce(DropPool.dropItemFromMapObject(this, null, getPosition(), (byte) 2));
		}
	}

	@Override
	public void sendDestroyData(final MapleClient client){
		client.announce(DropPool.removeItemFromMap(getObjectId(), 1, 0));
	}

	public boolean isIronMan(){
		return ironMan;
	}

	@Override
	public MapleMapItem clone(){
		return new MapleMapItem(item.copy(), getPosition(), dropper, ironMan, ownerid, chrid, type, playerDrop, 0);
	}
}