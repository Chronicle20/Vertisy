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
package net.server.channel;

import java.awt.Point;
import java.io.File;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import client.MapleCharacter;
import client.MessageType;
import constants.ServerConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.channel.ChannelServer;
import net.mina.MapleCodecFactory;
import net.server.PlayerStorage;
import net.server.Server;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventScriptManager;
import server.TimerManager;
import server.events.gm.Event;
import server.expeditions.MapleExpedition;
import server.life.SpawnPoint;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.objects.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public final class Channel{

	private int port = 7575;
	private PlayerStorage players = new PlayerStorage();
	private ChannelServer channelServer;
	private int channel;
	private io.netty.channel.Channel channelConnection;
	private EventLoopGroup parentGroup, childGroup;
	private ServerBootstrap boot;
	private String ip;
	private EventScriptManager eventSM;
	private Map<Integer, HiredMerchant> hiredMerchants = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> storedVars = new HashMap<>();
	private List<MapleExpedition> expeditions = new ArrayList<>();
	private Event event;
	private boolean finishedShutdown = false;
	private int currentCathedralMarriageID = -1, currentChapelMarriageID;
	private ScheduledFuture<?> updateMapScheduler;

	public Channel(final ChannelServer channelServer, final int channel){
		long start = System.currentTimeMillis();
		this.channelServer = channelServer;
		this.channel = channel;
//		eventSM = new EventScriptManager();
//		eventSM.load(this, getEvents());
		port = 7575 + this.channel;
		port += (channelServer.getWorldID() * 100);
		ip = ServerConstants.HOST + ":" + port;
		parentGroup = new NioEventLoopGroup(1);
		childGroup = new NioEventLoopGroup();
		boot = new ServerBootstrap().group(parentGroup, childGroup).channel(NioServerSocketChannel.class).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new MapleCodecFactory(channelServer.getWorldID(), channel));
		try{
			channelConnection = boot.bind(port).sync().channel().closeFuture().channel();
//			eventSM.init();
			loadMobTimes();
			System.out.println("");
			System.out.println("    Channel " + getId() + ": Listening on port " + port + ". Took " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	public ChannelServer getChannelServer(){
		return channelServer;
	}

	public void reloadEventScriptManager(){
		eventSM.cancel();
		eventSM = null;
		eventSM = new EventScriptManager();
		eventSM.load(this, getEvents());
		eventSM.init();
	}

	public final void shutdown(){
		try{
			System.out.println("Shutting down Channel " + channel + " on " + channelServer.getWorldID());
			closeAllMerchants();
			players.disconnectAll();
			if(channelConnection != null) channelConnection.close();
			childGroup.shutdownGracefully();
			parentGroup.shutdownGracefully();
			try{
				int spawns = 0;
				MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
				for(MapleMap map : getMaps().values()){
					List<SpawnPoint> points = map.getSpawnPoints().stream().filter(sp-> sp.getMobTime() > 0 && System.currentTimeMillis() < sp.getNextPossibleSpawn()).collect(Collectors.toList());
					if(!points.isEmpty()){
						mplew.writeInt(map.getId());
						mplew.writeInt(points.size());
						for(SpawnPoint sp : points){
							spawns++;
							mplew.writeInt(sp.getMonsterId());
							mplew.writePos(sp.getPosition());
							mplew.writeLong(sp.getNextPossibleSpawn());
						}
					}
				}
				if(spawns > 0){
					File mobTimeBin = new File(System.getProperty("wzpath") + "/bin/Life/MobTimes-" + channel + ".bin");
					mobTimeBin.createNewFile();
					mplew.saveToFile(mobTimeBin);
					System.out.println("Saved " + spawns + " spawnpoint times in channel " + channel);
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Error while shutting down Channel " + channel + " on " + channelServer.getWorldID());
			}
			finishedShutdown = true;
			System.out.println("Successfully shut down Channel " + channel + " on " + channelServer.getWorldID());
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Error while shutting down Channel " + channel + " on " + channelServer.getWorldID());
		}
	}

	public void loadMobTimes(){
		try{
			File mobTimeBin = new File(System.getProperty("wzpath") + "/bin/Life/MobTimes-" + channel + ".bin");
			if(mobTimeBin.exists()){
				byte[] in = Files.readAllBytes(mobTimeBin.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				while(glea.available() > 0){
					MapleMap map = this.getMapFactory().getMap(channel, glea.readInt());
					int points = glea.readInt();
					for(int i = 0; i < points; i++){
						int mobid = glea.readInt();
						Point pos = glea.readPos();
						long nextPossibleSpawn = glea.readLong();
						for(SpawnPoint sp : map.getSpawnPoints()){
							if(sp.getMonsterId() == mobid && sp.getPosition().equals(pos)){
								sp.setNextPossibleSpawn(nextPossibleSpawn);
								break;
							}
						}
					}
				}
				glea = null;
				babs = null;
				in = null;
				mobTimeBin.delete();
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public void closeAllMerchants(){
		final Iterator<HiredMerchant> hmit = hiredMerchants.values().iterator();
		while(hmit.hasNext()){
			hmit.next().forceClose();
			hmit.remove();
		}
	}

	public MapleMapFactory getMapFactory(){
		return this.channelServer.getMapFactory();
	}

	public MapleMap getMap(int mapid){
		return this.channelServer.getMapFactory().getMap(this.channel, mapid);
	}

	public MapleMap getMap(int mapid, boolean instance){// old instance ;-;
		return this.channelServer.getMapFactory().getMap(this.channel, mapid, instance);
	}

	public Map<Integer, MapleMap> getMaps(){
		return this.channelServer.getMapFactory().getMaps(this.channel);
	}

	public int getWorld(){
		return channelServer.getWorldID();
	}

	public PlayerStorage getPlayerStorage(){
		return players;
	}

	public void addPlayer(MapleCharacter chr){
		players.addPlayer(chr);
		try{
			chr.announce(MaplePacketCreator.serverMessage(ChannelServer.getInstance().getWorldInterface().getServerMessage()));
			ChannelServer.getInstance().getWorldInterface().updateConnectedClients(this.channel, players.getAllCharacters().size());
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public void removePlayer(MapleCharacter chr){
		chr.dispose();
		players.removePlayer(chr.getId());
		try{
			ChannelServer.getInstance().getWorldInterface().updateConnectedClients(this.channel, players.getAllCharacters().size());
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public int getConnectedClients(){
		return players.getAllCharacters().size();
	}

	public void broadcastPacket(final byte[] data){
		for(MapleCharacter chr : players.getAllCharacters()){
			chr.announce(data);
		}
	}

	public List<Integer> broadcastPacket(final List<Integer> player, final byte[] data){
		List<Integer> sentTo = new ArrayList<>();
		for(int id : player){
			MapleCharacter chr = players.getCharacterById(id);
			if(chr != null){
				sentTo.add(id);
				chr.announce(data);
			}
			if(player.size() == sentTo.size()) break;
		}
		return sentTo;
	}

	public List<String> broadcastPacketToPlayers(final List<String> player, final byte[] data){
		List<String> sentTo = new ArrayList<>();
		for(String id : player){
			MapleCharacter chr = players.getCharacterByName(id);
			if(chr != null){
				sentTo.add(id);
				chr.announce(data);
			}
			if(player.size() == sentTo.size()) break;
		}
		return sentTo;
	}

	public final int getId(){
		return channel;
	}

	public String getIP(){
		return ip;
	}

	public Event getEvent(){
		return event;
	}

	public void setEvent(Event event){
		this.event = event;
	}

	public EventScriptManager getEventSM(){
		return eventSM;
	}

	public void broadcastGMPacket(final byte[] data){
		for(MapleCharacter chr : players.getAllCharacters()){
			if(chr.isGM()){
				chr.announce(data);
			}
		}
	}

	public List<MapleCharacter> getPartyMembers(MapleParty party){
		List<MapleCharacter> partym = new ArrayList<>(8);
		for(MaplePartyCharacter partychar : party.getMembers()){
			if(partychar.getChannel() == getId()){
				MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
				if(chr != null){
					partym.add(chr);
				}
			}
		}
		return partym;
	}

	public class updateMaps implements Runnable{

		@Override
		public void run(){
			try{
				Map<Integer, MapleMap> maps = getMaps();
				if(maps != null){
					for(Entry<Integer, MapleMap> map : maps.entrySet()){
						MapleMap m = map.getValue();
						if(m != null){
							m.update();
						}
					}
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public Map<Integer, HiredMerchant> getHiredMerchants(){
		return hiredMerchants;
	}

	public void restartUpdateThread(){
		if(updateMapScheduler != null){
			updateMapScheduler.cancel(true);
			updateMapScheduler = null;
		}
		updateMapScheduler = TimerManager.getInstance().register("updateMapScheduler", new updateMaps(), 100);
	}

	public void stopUpdateThread(){
		if(updateMapScheduler != null){
			updateMapScheduler.cancel(true);
			updateMapScheduler = null;
		}
	}

	public void addHiredMerchant(int chrid, HiredMerchant hm){
		hiredMerchants.put(chrid, hm);
	}

	public void removeHiredMerchant(int chrid){
		hiredMerchants.remove(chrid);
	}

	public Map<Integer, HiredMerchant> getMerchants(){
		return hiredMerchants;
	}

	public int[] multiBuddyFind(int charIdFrom, int[] characterIds){
		List<Integer> ret = new ArrayList<>(characterIds.length);
		PlayerStorage playerStorage = getPlayerStorage();
		for(int characterId : characterIds){
			MapleCharacter chr = playerStorage.getCharacterById(characterId);
			if(chr != null){
				if(chr.getBuddylist().containsVisible(charIdFrom)){
					ret.add(characterId);
				}
			}
		}
		int[] retArr = new int[ret.size()];
		int pos = 0;
		for(Integer i : ret){
			retArr[pos++] = i.intValue();
		}
		return retArr;
	}

	public List<MapleExpedition> getExpeditions(){
		return expeditions;
	}

	public boolean isConnected(String name){
		return getPlayerStorage().getCharacterByName(name) != null;
	}

	public boolean finishedShutdown(){
		return finishedShutdown;
	}

	public void setServerMessage(String message){
		broadcastPacket(MaplePacketCreator.serverMessage(message));
	}

	private static String[] getEvents(){
		List<String> events = new ArrayList<String>();
		for(File file : new File("scripts/event").listFiles()){
			if(!file.isDirectory()) events.add(file.getName().substring(0, file.getName().length() - 3));
		}
		return events.toArray(new String[0]);
	}

	public int getStoredVar(int key){
		if(storedVars.containsKey(key)) return storedVars.get(key);
		return 0;
	}

	public void setStoredVar(int key, int val){
		this.storedVars.put(key, val);
	}

	public int getCurrentCathedralMarriageID(){
		return this.currentCathedralMarriageID;
	}

	public void setCurrentCathedralMarriageID(int marriageID){
		this.currentCathedralMarriageID = marriageID;
	}

	public int getCurrentChapelMarriageID(){
		return this.currentChapelMarriageID;
	}

	public void setCurrentChapelMarriageID(int marriageID){
		this.currentChapelMarriageID = marriageID;
	}

	public void addExpEventTime(long time){
		long cur = Calendar.getInstance().getTimeInMillis();
		boolean extended = true;
		if(ServerConstants.expEventEnd < cur){
			extended = false;
			ServerConstants.expEventEnd = cur;
		}
		ServerConstants.expEventEnd += time;
		String end = getExpEventTimeLeft();
		if(extended) Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(MessageType.NOTICE.getValue(), "The Maple Tree 1.5x exp boost has been extended for an additional 30 minutes and will end in " + end + "."));
		else Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(MessageType.NOTICE.getValue(), "As the Maple Tree falls it has granted everyone a 1.5x exp boost for 30 minutes."));
	}

	public String getExpEventTimeLeft(){
		long time = (ServerConstants.expEventEnd) - Calendar.getInstance().getTimeInMillis();
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;
		long monthsInMilli = daysInMilli * 30;
		long months = time / monthsInMilli;
		time = time % monthsInMilli;
		long days = time / daysInMilli;
		time = time % daysInMilli;
		long hours = time / hoursInMilli;
		time = time % hoursInMilli;
		StringBuilder sb = new StringBuilder();
		if(months > 0){
			sb.append(months + (months == 1 ? " month" : " months") + ((days > 0 || hours > 0) ? ", " : ""));
		}
		if(days > 0){
			sb.append(days + (days == 1 ? " day" : " days") + ((hours > 0) ? ", " : ""));
		}
		if(hours > 0){
			sb.append(hours + (hours == 1 ? " hour" : " hours"));
		}
		return sb.toString();
	}
}