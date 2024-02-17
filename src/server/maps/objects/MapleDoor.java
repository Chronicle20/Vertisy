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
package server.maps.objects;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import net.channel.ChannelServer;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.packets.CWvsContext;
import tools.packets.field.TownPortalPool;

/**
 * @author Matze
 */
public class MapleDoor extends AbstractMapleMapObject implements Externalizable{

	private int channel;
	private int ownerid, nSkillID;
	private int townid;
	private MapleMap town;
	private String townPortalName = "";
	private MaplePortal townPortal;
	private int targetid;
	private MapleMap target;
	private Point targetPosition;

	public MapleDoor(){
		super();
	}

	public MapleDoor(MapleCharacter owner, int nSkillID, Point targetPosition){
		super();
		this.channel = owner.getClient().getChannel();
		this.ownerid = owner.getId();
		this.nSkillID = nSkillID;
		this.target = owner.getMap();
		this.targetid = target.getId();
		this.targetPosition = targetPosition;
		setPosition(this.targetPosition);
		this.town = this.target.getReturnMap();
		this.townid = town.getId();
		this.townPortal = getFreePortal();
		if(townPortal != null) this.townPortalName = townPortal.getName();
	}

	public MapleDoor(MapleDoor origDoor){
		super();
		this.channel = origDoor.getChannel();
		this.ownerid = origDoor.getOwner();
		this.nSkillID = origDoor.getSkillID();
		this.town = origDoor.town;
		this.townid = town.getId();
		this.townPortal = origDoor.townPortal;
		this.target = origDoor.target;
		this.targetid = target.getId();
		this.targetPosition = origDoor.targetPosition;
		if(townPortal != null) this.townPortalName = townPortal.getName();
		setPosition(this.townPortal.getPosition());
	}

	private MaplePortal getFreePortal(){
		List<MaplePortal> freePortals = new ArrayList<MaplePortal>();
		for(MaplePortal port : town.getPortals()){
			if(port.getType() == 6){
				freePortals.add(port);
			}
		}
		Collections.sort(freePortals, new Comparator<MaplePortal>(){

			@Override
			public int compare(MaplePortal o1, MaplePortal o2){
				if(o1.getId() < o2.getId()){
					return -1;
				}else if(o1.getId() == o2.getId()){
					return 0;
				}else{
					return 1;
				}
			}
		});
		for(MapleMapObject obj : town.getMapObjects()){
			if(obj instanceof MapleDoor){
				MapleDoor door = (MapleDoor) obj;
				MapleCharacter owner = door.getOwnerInstance();
				if(owner != null && owner.isInParty() && owner.getParty().containsMembers(owner.getMPC())){
					freePortals.remove(door.getTownPortal());
				}
			}
		}
		return freePortals.iterator().next();
	}

	@Override
	public void sendSpawnData(MapleClient client){
		MapleCharacter owner = getOwnerInstance();
		if(client.getChannel() == channel && owner != null){
			// spawns the door object
			client.announce(TownPortalPool.spawnDoor(getOwner(), getTown().getId() == client.getPlayer().getMapId() ? getTownPortal().getPosition() : targetPosition, getTown().getId() == client.getPlayer().getMapId()));
			// spawns the portals to actually warp.
			client.announce(MaplePacketCreator.spawnPortal(getTown().getId(), getTarget().getId(), nSkillID, targetPosition));
			if(owner.isInParty()){
				// client.announce(CWvsContext.Party.partyPortal(getTown().getId(), getTarget().getId(), nSkillID, targetPosition));
			}
		}
	}

	@Override
	public void sendDestroyData(MapleClient client){
		MapleCharacter owner = getOwnerInstance();
		if(client.getChannel() == channel && owner != null){
			// if(target.getId() == client.getPlayer().getMapId()/* || owner.getId() == client.getPlayer().getId() || (owner.isInParty() && owner.getParty().getId() == client.getPlayer().getId())*/){
			if(owner.isInParty() && (getOwner() == client.getPlayer().getId() || owner.getParty().getId() == client.getPlayer().getId())){
				// client.announce(CWvsContext.Party.partyPortal(999999999, 999999999, 0, new Point(-1, -1)));
			}
			client.announce(TownPortalPool.removeDoor(getOwner(), false));
			client.announce(TownPortalPool.removeDoor(getOwner(), true));
			// }
		}
	}

	public void warp(MapleCharacter chr, boolean toTown){
		MapleCharacter owner = getOwnerInstance();
		if(chr.getId() == getOwner() || (owner != null && owner.isInParty() && owner.getParty().getId() == chr.getPartyId())){
			if(!toTown){
				chr.changeMap(getTarget(), targetPosition);
			}else{
				chr.changeMap(getTown(), getTownPortal());
			}
		}else{
			chr.getClient().announce(CWvsContext.enableActions());
		}
	}

	public int getChannel(){
		return channel;
	}

	public int getOwner(){
		return ownerid;
	}

	public MapleCharacter getOwnerInstance(){
		return ChannelServer.getInstance().getCharacterById(ownerid);
	}

	public MapleMap getTown(){
		if(town == null) town = ChannelServer.getInstance().getMap(channel, townid);
		return town;
	}

	public MaplePortal getTownPortal(){
		if(townPortal == null) townPortal = getTown().getPortal(townPortalName);
		return townPortal;
	}

	public MapleMap getTarget(){
		if(target == null) target = ChannelServer.getInstance().getMap(channel, targetid);
		return target;
	}

	public Point getTargetPosition(){
		return targetPosition;
	}

	public int getSkillID(){
		return nSkillID;
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.DOOR;
	}

	@Override
	public AbstractMapleMapObject clone(){
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(channel);
		out.writeInt(ownerid);
		out.writeInt(nSkillID);
		out.writeInt(townid);
		out.writeObject(townPortalName);
		out.writeInt(targetid);
		out.writeInt((int) getPosition().getX());
		out.writeInt((int) getPosition().getY());
		out.writeInt((int) targetPosition.getX());
		out.writeInt((int) targetPosition.getY());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		this.channel = in.readInt();
		this.ownerid = in.readInt();
		this.nSkillID = in.readInt();
		this.townid = in.readInt();
		townPortalName = (String) in.readObject();
		this.targetid = in.readInt();
		this.setPosition(new Point(in.readInt(), in.readInt()));
		this.targetPosition = new Point(in.readInt(), in.readInt());
	}
}
