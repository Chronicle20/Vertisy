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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import client.MapleCharacter;
import client.MapleFamily;
import constants.WorldConstants.WorldInfo;
import net.server.PlayerStorage;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;

/**
 * @author kevintjuh93
 */
public class World{

	private final WorldInfo worldInfo;
	private List<Channel> channels = new ArrayList<>();
	private Map<Integer, MapleFamily> families = new LinkedHashMap<>();

	public World(WorldInfo worldInfo){
		this.worldInfo = worldInfo;
	}

	public WorldInfo getWorldInfo(){
		return worldInfo;
	}

	public List<Channel> getChannels(){
		return channels;
	}

	public Channel getChannel(int channel){
		return channels.get(channel - 1);
	}

	public void addChannel(Channel channel){
		channels.add(channel);
	}

	public void removeChannel(int channel){
		channels.remove(channel);
	}

	public int getFlag(){
		return worldInfo.getFlag();
	}

	public String getEventMessage(){
		return worldInfo.getEventMessage();
	}

	public int getExpRate(){
		return worldInfo.getExpRate();
	}

	public void setExpRate(int exp){
		this.worldInfo.setExpRate(exp);
		// getAllCharacters().forEach(chr-> chr.getStats().recalcLocalStats(chr));
	}

	public int getQuestExpRate(){
		return worldInfo.getQuestExpRate();
	}

	public void setQuestExpRate(int exp){
		this.worldInfo.setQuestExpRate(exp);
		// getAllCharacters().forEach(chr-> chr.getStats().recalcLocalStats(chr));
	}

	public int getDropRate(){
		return worldInfo.getDropRate();
	}

	public void setDropRate(int drop){
		this.worldInfo.setDropRate(drop);;
		// getAllCharacters().forEach(chr-> chr.getStats().recalcLocalStats(chr));
	}

	public int getMesoRate(){
		return worldInfo.getMesoRate();
	}

	public void setMesoRate(int meso){
		this.worldInfo.setMesoRate(meso);
		// getAllCharacters().forEach(chr-> chr.getStats().recalcLocalStats(chr));
	}

	public int getBossDropRate(){
		return worldInfo.getDropRate();
	}

	public List<PlayerStorage> getPlayerStorages(){
		return channels.stream().filter(ch-> ch != null && ch.getPlayerStorage() != null).map(Channel::getPlayerStorage).collect(Collectors.toList());
	}

	public List<MapleCharacter> getAllCharacters(){
		return getPlayerStorages().stream().flatMap(ps-> ps.getAllCharacters().stream().filter(mc-> mc != null)).collect(Collectors.toList());
	}

	public MapleCharacter getCharacterById(int id){
		for(PlayerStorage ps : getPlayerStorages()){
			MapleCharacter chr = ps.getCharacterById(id);
			if(chr != null) return chr;
		}
		return null;
	}

	public MapleCharacter getCharacterByName(String name){
		for(PlayerStorage ps : getPlayerStorages()){
			MapleCharacter chr = ps.getCharacterByName(name);
			if(chr != null) return chr;
		}
		return null;
	}

	public void removePlayer(MapleCharacter chr){
		channels.get(chr.getClient().getChannel() - 1).removePlayer(chr);
	}

	public int getId(){
		return worldInfo.ordinal();
	}

	public void addFamily(int id, MapleFamily f){
		synchronized(families){
			if(!families.containsKey(id)){
				families.put(id, f);
			}
		}
	}

	public MapleFamily getFamily(int id){
		synchronized(families){
			if(families.containsKey(id)) return families.get(id);
			return null;
		}
	}

	public void sendPacket(List<Integer> targetIds, final byte[] packet, int exception){
		MapleCharacter c;
		for(int i : targetIds){
			if(i == exception){
				continue;
			}
			c = getCharacterById(i);
			if(c != null){
				c.getClient().announce(packet);
			}
		}
	}

	public int find(String name){
		int channel = -1;
		MapleCharacter chr = getCharacterByName(name);
		if(chr != null){
			channel = chr.getClient().getChannel();
		}
		return channel;
	}

	public int find(int id){
		int channel = -1;
		MapleCharacter chr = getCharacterById(id);
		if(chr != null){
			channel = chr.getClient().getChannel();
		}
		return channel;
	}

	public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds){
		List<CharacterIdChannelPair> foundsChars = new ArrayList<>(characterIds.length);
		for(Channel ch : getChannels()){
			for(int charid : ch.multiBuddyFind(charIdFrom, characterIds)){
				foundsChars.add(new CharacterIdChannelPair(charid, ch.getId()));
			}
		}
		return foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
	}

	public boolean isConnected(String charName){
		return getCharacterByName(charName) != null;
	}

	public void setServerMessage(String msg){
		for(Channel ch : channels){
			ch.setServerMessage(msg);
		}
	}

	public void broadcastPacket(final byte[] data){
		for(MapleCharacter chr : getAllCharacters()){
			chr.announce(data);
		}
	}

	public void broadcastMessage(final byte[] data){
		broadcastPacket(data);
	}

	public void broadcastGMPacket(final byte[] data){
		for(MapleCharacter chr : getAllCharacters()){
			if(chr.isGM()){
				chr.announce(data);
			}
		}
	}

	public void broadcastGMMessage(final byte[] data){
		broadcastGMPacket(data);
	}

	public void checkHWID(int accountid, String hwid){
		/*Stream<MapleCharacter> filtered = getAllCharacters().stream().filter(mc-> mc != null && mc.isLoggedin() && mc.getClient() != null && mc.getClient().getHWID() != null && mc.getClient().getHWID().equals(mc.getClient().convertHWID(hwid)) && !mc.isAdmin());
		boolean val = filtered.count() > 0;
		if(filtered.anyMatch(mc-> mc.getClient().getAccID() == c.getAccID())){
			filtered.filter(mc-> mc.getAccountID() == c.getAccID()).forEach(mc-> {
				mc.getClient().disconnect(false, mc.getCashShop().isOpened());
			});
			val = false;
		}
		return val;*/
		// return getAllCharacters().stream().anyMatch(mc-> mc != null && mc.isLoggedin() && mc.getClient() != null && mc.getClient().getHWID() != null && mc.getClient().getHWID().equals(mc.getClient().convertHWID(hwid)) && !mc.isAdmin());
		/*getAllCharacters().stream().filter(mc-> mc != null && mc.isLoggedin() && mc.getClient() != null && mc.getClient().getHWID() != null && mc.getClient().getHWID().equals(mc.getClient().convertHWID(hwid)) && !mc.isAdmin() && mc.getAccountID() != accountid).forEach(mc-> {
			mc.getClient().disconnect(false, mc.getCashShop().isOpened());
			mc.getClient().updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
			mc.getClient().getSession().attr(MapleClient.CLIENT_KEY).set(null);
			mc.getClient().getSession().close();
		});*/
	}

	public final void shutdown(){
		for(Channel ch : getChannels()){
			ch.shutdown();
		}
	}
}
