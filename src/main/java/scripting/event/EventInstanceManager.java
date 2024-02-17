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
package scripting.event;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import javax.script.ScriptException;

import client.MapleCharacter;
import client.MapleRing;
import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import constants.MobConstants;
import constants.ServerConstants;
import net.server.Server;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import provider.MapleDataProviderFactory;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.MapleWedding;
import server.TimerManager;
import server.expeditions.MapleExpedition;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.UserLocal;

/**
 * @author Matze
 * @modified David
 */
public class EventInstanceManager{

	private List<MapleCharacter> chars = new ArrayList<>();
	private List<MapleMonster> mobs = new LinkedList<>();
	private Map<MapleCharacter, Integer> killCount = new HashMap<>();
	private EventManager em;
	private MapleMapFactory mapFactory;
	private final String name;
	private Properties props = new Properties();
	private long timeStarted = 0;
	private long eventTime = 0;
	private MapleExpedition expedition = null;
	private boolean disposed = false;
	private Map<String, ScheduledFuture<?>> schedules;
	private MapleParty party;
	private long lastClockSet, clockLength;
	private final boolean instanced;

	public EventInstanceManager(EventManager em, String name, boolean instanced){
		this.em = em;
		this.name = name;
		this.instanced = instanced;
		if(instanced){
			Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Loading an instanced MapFactory for event: " + name);
			if(ServerConstants.WZ_LOADING){
				mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")), em.getChannel().getChannelServer());// Fk this
			}else{
				mapFactory = new MapleMapFactory(null, null, em.getChannel().getChannelServer());// Fk this
			}
		}else{
			Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Not using instanced mapFactory: " + name);
			mapFactory = em.getChannel().getMapFactory();
		}
		schedules = new HashMap<>();
	}

	public EventManager getEm(){
		return em;
	}

	public void setClock(int time){
		lastClockSet = System.currentTimeMillis();
		clockLength = time;
		sendClock(time);
	}

	public int getClockTimeLeft(){
		long end = lastClockSet + (clockLength * 1000L);
		if(System.currentTimeMillis() > end) return 0;
		else return (int) ((end - System.currentTimeMillis()) / 1000L);
	}

	public void sendClock(int time){// seconds
		for(MapleCharacter chr : chars){
			chr.getClient().announce(MaplePacketCreator.getClock(time));
		}
	}

	public void cancelSchedules(){
		for(String key : schedules.keySet()){
			ScheduledFuture<?> sf = schedules.get(key);
			if(sf != null) sf.cancel(true);
		}
		schedules.clear();
	}

	public void cancelSchedule(String key){
		ScheduledFuture<?> sf = schedules.get(key);
		if(sf != null){
			sf.cancel(true);
			schedules.remove(key);
		}
	}

	public void addSchedule(String key, ScheduledFuture<?> sf){
		cancelSchedule(key);
		schedules.put(key, sf);
	}

	public void registerPlayer(MapleCharacter chr){
		if(chr == null || !chr.isLoggedin()) return;
		try{
			chars.add(chr);
			chr.setEventInstance(this);
			em.getIv().invokeFunction("playerEntry", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerEntry:");
		}
	}

	public void startEventTimer(long time){
		timeStarted = System.currentTimeMillis();
		eventTime = time;
	}

	// NOTE: use this instead of schedule + startEventTimer(long time) combo for PQ/ anything relevant.
	// schedule the invocation of the timeOut/ scheduledTimeOut function used to enforce the event timer.
	// and calls startEventTimer(long time) to record the start time and time limit
	public void scheduleEventTimer(long delay){
		if(timeOut(delay)){
			startEventTimer(delay);
		}
	}

	public void restartEventTimer(long time){
		try{
			if(disposed) return;
			timeStarted = System.currentTimeMillis();
			eventTime = time;
			cancelSchedules();
			final int timesend = (int) time / 1000;
			for(MapleCharacter chr : getPlayers()){
				chr.getClient().announce(MaplePacketCreator.getClock(timesend));
			}
			timeOut(time, this);
		}catch(NumberFormatException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : restartEventTimer:");
		}
	}

	public void changedMap(final MapleCharacter chr, final int mapid){
		if(disposed) return;
		try{
			em.getIv().invokeFunction("changedMap", this, chr, mapid);
		}catch(NullPointerException npe){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, npe, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : leftParty:");
		}catch(NoSuchMethodException | ScriptException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : changedMap:");
		}
	}

	// NOTE: should not use this. make sure you changed all the references before you remove it.
	// should be removed, but im just gonna leave it here lol.
	public boolean timeOut(final long delay, final EventInstanceManager eim){
		if(disposed || eim == null || eim.getEm() == null){ return false; }
		addSchedule("scheduledTimeOut", TimerManager.getInstance().schedule("scheduledTimeOut-" + em.getName(), ()-> {
			if(disposed || eim == null || eim.getEm() == null){ return; }
			try{
				eim.getEm().getIv().invokeFunction("scheduledTimeOut", eim);
			}catch(NoSuchMethodException | ScriptException ex){
				// System.out.println("Event name " + em.getName() + ", Instance name : " + name
				// + ", method Name : scheduledTimeout:\n" + ex);
				// added this cuz the function is called timeOut in some scripts
				try{
					eim.getEm().getIv().invokeFunction("timeOut", eim);
				}catch(NoSuchMethodException | ScriptException ex1){
					System.out.println("Event name " + eim.getEm().getName() + ", Instance name : " + eim.getName() + ", method Name : scheduledTimeOut and timeOut:\n" + ex1);
				}
			}
		}, delay));
		return true;
	}

	public boolean timeOut(final long delay){
		if(disposed || em == null){ return false; }
		addSchedule("scheduledTimeOut", TimerManager.getInstance().schedule("scheduledTimeOut2-" + em.getName(), ()-> {
			if(disposed || em == null){ return; }
			try{
				em.getIv().invokeFunction("scheduledTimeOut", this);
			}catch(NoSuchMethodException | ScriptException ex){ // i know, it should be just NoSuchMethodException, but idc.
				// System.out.println("Event name " + em.getName() + ", Instance name : " + name
				// + ", method Name : scheduledTimeout:\n" + ex);
				// added this cuz the function is called timeOut in some scripts
				try{
					em.getIv().invokeFunction("timeOut", this);
				}catch(NoSuchMethodException | ScriptException ex1){
					System.out.println("Event name " + em.getName() + ", Instance name : " + name + ", method Name : scheduledTimeOut and timeOut:\n" + ex1);
				}
			}
		}, delay));
		return true;
	}

	public boolean isTimerStarted(){
		return eventTime > 0 && timeStarted > 0;
	}

	public long getDuration(){
		return System.currentTimeMillis() - timeStarted;
	}

	public long getTimeLeft(){
		return eventTime - (System.currentTimeMillis() - timeStarted);
	}

	public long getTimeStarted(){
		return timeStarted;
	}

	public void stopEventTimer(){
		eventTime = 0;
		timeStarted = 0;
		cancelSchedule("scheduledTimeOut");
	}

	public void registerParty(MapleParty party, MapleMap map){
		this.party = party;
		em.removeParty(party);
		for(MaplePartyCharacter pc : party.getMembers()){
			MapleCharacter c = map.getCharacterById(pc.getId());
			registerPlayer(c);
		}
	}

	public void registerExpedition(MapleExpedition exped){
		expedition = exped;
		registerPlayer(exped.getLeader());
	}

	public MapleExpedition getExpedition(){
		return expedition;
	}

	public void endExpedition(){
		if(expedition != null){
			expedition.dispose(true);
			// this.getEm().getChannel().getExpeditions().remove(expedition);
		}
	}

	public void unregisterPlayer(MapleCharacter chr){
		unregisterPlayer(chr, false);
	}

	public void unregisterPlayer(MapleCharacter chr, boolean disposeIfEmpty){
		chars.remove(chr);
		chr.setEventInstance(null);
		if(chr.getPartyQuest() != null){ // shouldnt need this, but yahh, in case not all PQ might have a PQ class and other shiets
			chr.getPartyQuest().removeParticipant(chr, getEm(), this);
		}
		if(disposeIfEmpty){
			disposeIfPlayerBelow((byte) 0, 0);
		}
	}

	public MapleParty getParty(){
		return party;
	}

	public MapleCharacter getPartyLeaderChar(){
		// if (getParty() != null/* && getParty().getLeader() != null*/) { //the leader null checking is commented until needed.
		// return getParty().getLeader().getPlayer();
		// }
		return getParty() != null ? getParty().getLeader().getPlayerInChannel() : null;
	}

	public int getPlayerCount(){
		return chars.size();
	}

	public List<MapleCharacter> getPlayers(){
		return new ArrayList<>(chars);
	}

	public void registerMonster(MapleMonster mob){
		if(!mob.getStats().isFriendly() && mob.getEventInstance() == null){ // We cannot register moon bunny
			mobs.add(mob);
			mob.setEventInstance(this);
		}
	}

	public void movePlayer(MapleCharacter chr){
		try{
			em.getIv().invokeFunction("moveMap", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : moveMap:");
		}
	}

	public void monsterKilled(MapleMonster mob){
		mobs.remove(mob);
		if(mobs.isEmpty()){
			try{
				em.getIv().invokeFunction("allMonstersDead", this);
			}catch(ScriptException | NoSuchMethodException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : moveMap:");
			}
		}
	}

	public void playerKilled(MapleCharacter chr){
		try{
			em.getIv().invokeFunction("playerDead", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerKilled:");
		}
	}

	public boolean revivePlayer(MapleCharacter chr, boolean wheel){
		try{
			// The method returns a boolean, but it takes a boolean?
			Object b = em.getIv().invokeFunction("playerRevive", this, chr, wheel);
			if(b instanceof Boolean) return (Boolean) b;
		}catch(NoSuchMethodException e){
			try{
				Object b = em.getIv().invokeFunction("playerRevive", this, chr);
				if(b instanceof Boolean) return (Boolean) b;
			}catch(ScriptException | NoSuchMethodException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive2:");
			}
		}catch(ScriptException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive:");
		}
		return true;
	}

	public void playerDisconnected(MapleCharacter chr){
		try{
			em.getIv().invokeFunction("playerDisconnected", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerDisconnected:");
		}
	}

	/**
	 * @param chr
	 * @param mob
	 */
	public void monsterKilled(MapleCharacter chr, MapleMonster mob){
		Integer kc = killCount.get(chr);
		int inc = 1;
		try{
			inc = (Integer) em.getIv().invokeFunction("monsterValue", this, mob.getId());
		}catch(NoSuchMethodException | ScriptException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Failed to get monsterValue " + "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name);
		}
		if(kc == null){
			kc = inc;
		}else{
			kc += inc;
		}
		killCount.put(chr, kc);
		if(expedition != null){
			expedition.monsterKilled(chr, mob);
		}
	}

	public int getKillCount(MapleCharacter chr){
		Integer kc = killCount.get(chr);
		if(kc == null){
			return 0;
		}else{
			return kc;
		}
	}

	public final boolean disposeIfPlayerBelow(final byte size, final int towarp){
		if(disposed) return true;
		MapleMap map = null;
		if(towarp > 0){
			if(getMapFactory() == null) System.out.println("mapfactory is null");
			if(em == null) System.out.println("EM is null");
			if(em.getChannel() == null) System.out.println("em channel is null");
			map = this.getMapFactory().getMap(em.getChannel().getId(), towarp);
		}
		try{
			if(chars != null && chars.size() <= size){
				final List<MapleCharacter> chrs = new LinkedList<>(chars);
				for(MapleCharacter chr : chrs){
					if(chr == null){
						continue;
					}
					unregisterPlayer(chr);
					if(towarp > 0){
						chr.changeMap(map, map.getPortal(0));
					}
				}
				dispose();
				return true;
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : disposeIfPlayerBlow:");
		}
		return false;
	}

	public void dispose(){
		dispose(null);
	}

	public void dispose(String pq){
		if(getEm() == null){
			// it was already disposed of.
			return;
		}
		if(pq != null) getEm().setProperty(pq + "Open", "true");
		chars.clear();
		mobs.clear();
		killCount.clear();
		if(mapFactory != null && instanced) mapFactory.clearMaps(instanced);
		mapFactory = null;
		if(expedition != null){
			em.getChannel().getExpeditions().remove(expedition);
		}
		cancelSchedules();
		em.disposeInstance(name);
		disposed = true;
	}

	public MapleMapFactory getMapFactory(){
		return mapFactory;
	}

	public MapleMap getMap(int mapid){
		return mapFactory.getMap(em.getChannel().getId(), mapid);
	}

	public MapleMap getMap(int mapid, boolean instanced){
		return mapFactory.getMap(em.getChannel().getId(), mapid, instanced);
	}

	public void schedule(final String methodName, long delay){
		if(getEm() == null) return; // already disposed...
		addSchedule(methodName, TimerManager.getInstance().schedule(methodName + "-" + em.getName(), new Runnable(){

			@Override
			public void run(){
				try{
					// FilePrinter.print(FilePrinter.EVENT_LOGS + em.getName() + ".txt", "Invoking " + methodName + " in Event Instance " + em.getName() + ":" + name);
					em.getIv().invokeFunction(methodName, EventInstanceManager.this);
				}catch(ScriptException | NoSuchMethodException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + em.getName() + ", Instance name : " + name + ", method Name : " + methodName);
				}
			}
		}, delay));
	}

	public String getName(){
		return name;
	}

	public void saveWinner(MapleCharacter chr){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)")){
				ps.setString(1, em.getName());
				ps.setString(2, getName());
				ps.setInt(3, chr.getId());
				ps.setInt(4, chr.getClient().getChannel());
				ps.executeUpdate();
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public MapleMap getMapInstance(int mapId){
		if(mapFactory == null){
			Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, name + ": attempted to access a map when mapFactory is null");
			return null;
		}
		MapleMap map = mapFactory.getMap(em.getChannel().getId(), mapId);
		if(!mapFactory.isMapLoaded(mapId)){// this will.. never work
			if(em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")){
				map.shuffleReactors();
			}
		}
		return map;
	}

	public void setProperty(String key, String value){
		props.setProperty(key, value);
	}

	public Object setProperty(String key, String value, boolean prev){
		return props.setProperty(key, value);
	}

	public String getProperty(String key){
		return props.getProperty(key);
	}

	public Properties getProperties(){
		return props;
	}

	public void leftParty(MapleCharacter chr){
		try{
			em.getIv().invokeFunction("leftParty", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : leftParty:");
		}
	}

	public void disbandParty(){
		try{
			em.getIv().invokeFunction("disbandParty", this);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : disbandParty:");
		}
	}

	public void finishPQ(){
		try{
			em.getIv().invokeFunction("clearPQ", this);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : clearPQ:");
		}
	}

	public void removePlayer(MapleCharacter chr){
		try{
			em.getIv().invokeFunction("playerExit", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerExit:");
		}
	}

	public boolean isLeader(MapleCharacter chr){
		return(chr.getParty().getLeader().getId() == chr.getId());
	}

	public void openUI(int mapid, int type){
		this.getMapFactory().getMap(em.getChannel().getId(), mapid).broadcastMessage(MaplePacketCreator.openUI((byte) type));
	}

	public void closeUI(int map){
		this.getMapFactory().getMap(em.getChannel().getId(), map).broadcastMessage(UserLocal.disableUI(false));
	}

	public final void broadcastPlayerMsg(final int type, final String msg){
		for(MapleCharacter chr : getPlayers()){
			chr.dropMessage(type, msg);
		}
	}

	public void startCarnival(final MapleCharacter chr){
		try{
			em.getIv().invokeFunction("startCarnival", this, chr);
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : startCarnival:");
		}
	}

	public void summonPepeKing(MapleCharacter player){
		player.getClient().getChannelServer().getMap(player.getMapId()).clearAndReset(true);
		int rand = Randomizer.nextInt(10);
		int mob_ToSpawn = 100100;
		if(rand >= 4){ // 60%
			mob_ToSpawn = 3300007;
		}else if(rand >= 1){
			mob_ToSpawn = 3300006;
		}else{
			mob_ToSpawn = 3300005;
		}
		player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mob_ToSpawn), player.getPosition());
	}

	public int getWeddingStatus(){
		MapleWedding wedding = Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
		if(wedding == null) return -1;
		return wedding.getStatus();
	}

	public void setWeddingStatus(int status){
		MapleWedding wedding = Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
		if(wedding != null){
			wedding.setStatus(status);
		}
	}

	public int getWeddingState(){
		MapleWedding wedding = Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
		if(wedding == null) return -1;
		return wedding.getState();
	}

	public void setWeddingState(int state){
		MapleWedding wedding = Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
		if(wedding != null){
			wedding.setState(state);
		}
	}

	public void endCeremony(){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		MapleWedding wedding = Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
		wedding.setState(3);
		wedding.setStatus(1);
		MapleCharacter mc1 = em.getChannel().getPlayerStorage().getCharacterById(wedding.getPlayer1());
		MapleCharacter mc2 = em.getChannel().getPlayerStorage().getCharacterById(wedding.getPlayer2());
		int engagementRing = mc1.getEngagementRingID();
		int ringid = engagementRing == 4031358 ? 1112803 : engagementRing == 4031360 ? 1112806 : engagementRing == 4031362 ? 1112807 : 1112809;
		int ringbox = engagementRing - 1;
		mc1.setMarriageRingID(ringid);
		mc2.setMarriageRingID(ringid);
		int ringid1 = MapleRing.createRing(ringid, mc1, mc2);// Add new rings
		Equip ring1 = (Equip) ii.getEquipById(ringid);
		ring1.setRingId(ringid1);
		MapleInventoryManipulator.addFromDrop(mc1.getClient(), ring1, false);
		mc1.setMarriageRing(MapleRing.loadFromDb(ringid1));
		int ringid2 = ringid1 + 1;
		Equip ring2 = (Equip) ii.getEquipById(ringid);
		ring1.setRingId(ringid2);
		MapleInventoryManipulator.addFromDrop(mc2.getClient(), ring2, false);
		mc2.setMarriageRing(MapleRing.loadFromDb(ringid2));
		MapleInventoryManipulator.removeById(mc1.getClient(), MapleInventoryType.ETC, engagementRing, 1, true, false);// Remove old rings
		MapleInventoryManipulator.removeById(mc2.getClient(), MapleInventoryType.ETC, engagementRing, 1, true, false);
		MapleInventoryManipulator.removeById(mc1.getClient(), MapleInventoryType.ETC, ringbox, 1, true, false);// Engagement box
		mc1.getClient().announce(MaplePacketCreator.onMarriageResult(mc1, true, mc1.getMarriageID()));// idfk
		mc2.getClient().announce(MaplePacketCreator.onMarriageResult(mc2, true, mc2.getMarriageID()));
		mc1.getMap().broadcastMessage(MaplePacketCreator.onWeddingProgress(true, mc1.getId(), mc2.getId(), (byte) 0));
	}

	public void removeCurrentWedding(boolean cathedral){
		if(cathedral) em.getChannel().setCurrentCathedralMarriageID(-1);
		else em.getChannel().setCurrentChapelMarriageID(-1);
	}

	public MapleWedding getWedding(){
		return Server.getInstance().getWeddingByID(Integer.parseInt(getProperty("weddingid")));
	}

	public void dropMessage(int type, String message){
		this.getPlayers().forEach(mc-> mc.dropMessage(type, message));
	}

	public int getMobByAverageLevel(int averageLevel){
		Random rand = new Random();
		int mobid = 0;
		int calls = 0;
		int levelRange = 10;
		List<Integer> mobIds = new ArrayList<Integer>(MobConstants.slayerMobs.keySet());
		double maxCalls = mobIds.size();
		maxCalls /= 10D;
		while(mobid == 0){
			int id = mobIds.get(rand.nextInt(mobIds.size()));
			Pair<Integer, String> data = MobConstants.slayerMobs.get(id);
			int monsterLevel = data.left;
			int level = averageLevel - monsterLevel;
			if(level <= levelRange && level >= -levelRange && rand.nextBoolean()){
				mobid = id;
			}
			if(++calls >= maxCalls){
				levelRange += 5;
				calls = 0;
			}
			/*for(int id : mobIds){
				Pair<Integer, String> data = MobConstants.slayerMobs.get(id);
				int monsterLevel = data.left;
				int level = averageLevel - monsterLevel;
				if(level <= levelRange && level >= -levelRange && rand.nextBoolean()){
					mobid = id;
				}
			}
			levelRange += 4;*/
			//
			/*int monsterID = mobIds.get(rand.nextInt(mobIds.size()));
			Pair<Integer, String> data = MobConstants.slayerMobs.get(monsterID);
			int monsterLevel = data.left;
			int level = averageLevel - monsterLevel;
			if(level <= levelRange && level >= -levelRange && rand.nextBoolean()){
				mobid = monsterID;
			}else{
				levelRange += 2;
			}
			if(++calls >= mobIds.size()){
				// levelRange += 2;
				calls = 0;
			}*/
		}
		return mobid;
	}
}
