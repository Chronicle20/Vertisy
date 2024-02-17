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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import client.MapleCharacter;
import client.player.boss.BossEntryType;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import server.TimerManager;
import server.expeditions.MapleExpedition;
import server.maps.MapleMap;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Matze
 * @modified David
 */
public class EventManager{

	private Invocable iv;
	private Channel cserv;
	private ConcurrentMap<String, EventInstanceManager> instances = new ConcurrentHashMap<>();
	private Properties props = new Properties();
	private List<Pair<Integer, Integer>> investigationResults = new ArrayList<>();
	private String name;
	private Map<String, ScheduledFuture<?>> schedule = new HashMap<>();
	private Map<String, MapleParty> pqParties = new HashMap<>();

	public EventManager(Channel cserv, ScriptEngine se, String name){
		this.iv = (Invocable) se;
		this.setProperty("channel", cserv.getId());
		this.cserv = cserv;
		this.name = name;
	}

	public void cancel(){
		try{
			if(iv != null) iv.invokeFunction("cancelSchedule");
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: cancelSchedule");
		}
		cancelSchedules();
	}

	public void schedule(String methodName, long delay){
		schedule(methodName, null, delay);
	}

	public void schedule(final String methodName, final EventInstanceManager eim, long delay){
		if(schedule.get(methodName) != null){
			schedule.get(methodName).cancel(true);
		}
		schedule.put(methodName, TimerManager.getInstance().schedule(methodName + "-" + name, new Runnable(){

			@Override
			public void run(){
				// Logger.log(LogType.ERROR, LogFile.EXCEPTION, "Invoking " + methodName + " in Event Script " + name);
				try{
					iv.invokeFunction(methodName, eim);
				}catch(ScriptException | NoSuchMethodException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: " + methodName);
				}
			}
		}, delay));
	}

	public void cancelSchedule(){
		cancelSchedules();
	}

	public void cancelSchedules(){
		schedule.values().forEach(sc-> sc.cancel(true));
	}

	public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp){
		if(schedule.get(methodName) != null){
			schedule.get(methodName).cancel(true);
		}
		ScheduledFuture<?> sch = TimerManager.getInstance().scheduleAtTimestamp(methodName + "-" + name, new Runnable(){

			@Override
			public void run(){
				try{
					iv.invokeFunction(methodName);
				}catch(ScriptException | NoSuchMethodException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: " + methodName);
				}
			}
		}, timestamp);
		schedule.put(methodName, sch);
		return sch;
	}

	@Deprecated
	public Channel getChannelServer(){
		return cserv;
	}

	public Channel getChannel(){
		return cserv;
	}

	public EventInstanceManager getInstance(String name){
		return instances.get(name);
	}

	public Collection<EventInstanceManager> getInstances(){
		return Collections.unmodifiableCollection(instances.values());
	}

	public EventInstanceManager newInstance(String name){
		return newInstance(name, true);
	}

	public EventInstanceManager newInstance(String name, boolean instanced){
		EventInstanceManager ret = new EventInstanceManager(this, name, instanced);
		instances.put(name, ret);
		return ret;
	}

	public void disposeInstance(String name){
		instances.remove(name);
	}

	public Invocable getIv(){
		return iv;
	}

	public MapleParty getParty(String pq){
		return pqParties.get(pq);
	}

	public void removeParty(MapleParty party){
		for(Entry<String, MapleParty> ptEntry : pqParties.entrySet()){
			if(ptEntry.getValue() == party){
				pqParties.remove(ptEntry.getKey());
				return;
			}
		}
	}

	public void setProperty(String key, String value){
		props.setProperty(key, value);
	}

	public String getProperty(String key){
		return props.getProperty(key);
	}

	public void setProperty(String key, int value){
		props.setProperty(key, value + "");
	}

	public int getIntProperty(String key){
		return Integer.parseInt(props.getProperty(key));
	}

	public List<Pair<Integer, Integer>> getInvestigationResults(){ // Testing for Investigation Result caching
		return investigationResults;
	}

	public void addInvestigationResults(int x, int y){
		investigationResults.add(new Pair<>(x, y));
	}

	public String getName(){
		return name;
	}

	// Expedition method: starts an expedition
	public EventInstanceManager startInstance(MapleExpedition exped){
		try{
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
			eim.registerExpedition(exped);
			for(MapleCharacter chr : exped.getMembers()){
				if(chr != null && chr.getMapId() == exped.getLeader().getMapId()){
					System.out.println("Adding boss entry for exped type: " + exped.getType());
					switch (exped.getType()){
						case HORNTAIL:
							chr.getBossEntries().addEntry(BossEntryType.HORNTAIL);
							break;
						case PINKBEAN:
							chr.getBossEntries().addEntry(BossEntryType.PINK_BEAN);
							break;
						case SCARGA:
							chr.getBossEntries().addEntry(BossEntryType.SCARGA_TARGA);
							break;
						case ZAKUM:
							chr.getBossEntries().addEntry(BossEntryType.ZAKUM);
							break;
						default:
							System.out.println("Unhandled startInstance for: " + exped.getType().name());
							break;
					}
				}
			}
			exped.start();
			return eim;
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: setup");
		}
		return null;
	}

	// Regular method: player
	public EventInstanceManager startInstance(MapleCharacter chr){
		try{
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", chr));
			eim.registerPlayer(chr);
			return eim;
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: setup");
		}
		return null;
	}

	// recommended to use this for PQ (especially for those with ranks)
	// NOTE: use this if you are using PartyQuest or its subclass.
	public void startPQ(String pq, MapleParty party, MapleMap map){
		pqParties.put(pq, party);
		// for HPQ cuz im lazy and dont need to change the existing codes, and why not, no harm for all PQ to get this also xD
		// props.setProperty(pq, pq + "_" + party.getLeader().getName());
		props.setProperty(pq + "Open", "false"); // cuz i can and its better. combo set, bruh.
		startInstance(party, map);
	}

	// PQ method: starts a PQ
	public void startInstance(MapleParty party, MapleMap map){
		if(map.getId() == 261000011 || map.getId() == 261000021){
			props.clear();
			investigationResults.clear();
		}
		this.setProperty("channel", "" + party.getLeader().getChannel());
		startInstance(party, map, null, -1);
	}

	public EventInstanceManager startInstance(MapleParty party, MapleMap currentMap, MapleMap battleMap, int id){
		try{
			EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) (id >= 0 ? id : party)));
			eim.registerParty(party, currentMap);
			return eim;
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: setup");
		}
		return null;
	}// 9201005, CathedralWedding

	// non-PQ method for starting instance
	public EventInstanceManager startInstance(EventInstanceManager eim, String leader){
		try{
			iv.invokeFunction("setup", eim);
			eim.setProperty("leader", leader);
			return eim;
		}catch(ScriptException | NoSuchMethodException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Event name: " + name + ", Method name: setup");
		}
		return null;
	}
}
