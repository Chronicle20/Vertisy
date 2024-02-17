package server.reactors;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import client.MapleClient;
import client.MapleQuestStatus;
import client.MessageType;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.objects.AbstractMapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.quest.MapleQuest;
import server.reactors.actions.MapleReactorEvent;
import server.reactors.actions.TimeOutAction;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.field.ReactorPool;

/**
 * @author Lerk
 */
public class MapleReactor extends AbstractMapleMapObject{

	private int rid, link;
	private Integer currState;
	private int delay, quest, runeid = -1;
	private MapleMap map;
	private String name, action;
	// private boolean timerActive;
	private boolean alive, canSetAlive = true;
	private Map<Integer, ReactorState> states = new HashMap<>();
	private ScheduledFuture<?> timeoutHit;

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		mplew.writeInt(rid);
		mplew.writeInt(link);
		mplew.writeInt(delay);
		mplew.writeInt(quest);
		mplew.writeInt(runeid);
		mplew.writeBoolean(action != null);
		if(action != null) mplew.writeMapleAsciiString(action);
		mplew.writeBoolean(alive);
		mplew.writeBoolean(canSetAlive);
		mplew.writeInt(states.size());
		for(int key : states.keySet()){
			mplew.writeInt(key);
			ReactorState state = states.get(key);
			state.save(mplew);
		}
	}

	@Override
	public void load(LittleEndianAccessor slea){
		rid = slea.readInt();
		link = slea.readInt();
		delay = slea.readInt();
		quest = slea.readInt();
		runeid = slea.readInt();
		if(slea.readBoolean()) action = slea.readMapleAsciiString();
		alive = slea.readBoolean();
		canSetAlive = slea.readBoolean();
		int size = slea.readInt();
		for(int i = 0; i < size; i++){
			int key = slea.readInt();
			ReactorState state = new ReactorState();
			state.load(slea);
			states.put(key, state);
		}
	}

	public MapleReactor(){
		super();
	}

	public MapleReactor(int rid){
		this.rid = rid;
		alive = true;
	}

	public MapleReactor(MapleReactor react){
		this.rid = react.getId();
		this.link = react.getLink();
		Map<Integer, ReactorState> newStates = new HashMap<>();
		for(int i : react.getStates().keySet()){
			newStates.put(i, react.getStates().get(i).clone());
		}
		states = newStates;
		name = react.getName();
		action = react.getAction();
		quest = react.getQuest();
		alive = true;
	}

	public int getId(){
		return rid;
	}

	public int getLink(){
		return link;
	}

	public void setLink(MapleReactor link){
		this.link = link.getId();
		Map<Integer, ReactorState> newStates = new HashMap<>();
		for(int i : link.getStates().keySet()){
			newStates.put(i, link.getStates().get(i).clone());
		}
		states = newStates;
	}

	public void setDelay(int delay){
		this.delay = delay;
	}

	public int getDelay(){
		return delay;
	}

	public void setQuest(int quest){
		this.quest = quest;
	}

	public int getQuest(){
		return quest;
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.REACTOR;
	}

	public void setState(Integer state){
		currState = state;
		checkForTimeout();
		if(getMap() != null) getMap().broadcastMessage(ReactorPool.triggerReactor(this, 2));
	}

	public void checkForTimeout(){
		if(getState() != null){
			for(MapleReactorEvent event : getState().getEvents()){
				if(event.getType().equals(ReactorActionType.TIME_OUT)){
					// System.out.println("Found timeout");
					final TimeOutAction action = (TimeOutAction) event;
					if(timeoutHit == null || timeoutHit.isDone()){
						timeoutHit = null;
						// System.out.println("Scheduling hitReactor in: " + action.getTimeout());
						timeoutHit = TimerManager.getInstance().schedule("timeoutHit", ()-> {
							timeoutHit = null;
							hitReactor(ReactorHitType.DEFAULT, new ReactorHitInfo(), null);
						}, action.getTimeout());
					}else{
						// System.out.println("timeoutHit is not null.");
					}
					break;
				}
			}
		}
	}

	public ScheduledFuture<?> getTimeoutHit(){
		return timeoutHit;
	}

	public int getCurrState(){
		if(currState == null) currState = 0;
		return currState;
	}

	public byte getCurrStateAsByte(){
		if(currState == null) currState = 0;
		return currState.byteValue();
	}

	public ReactorState getState(){
		return states.get(getCurrState());
	}

	public void setMap(MapleMap map){
		this.map = map;
	}

	public MapleMap getMap(){
		return map;
	}

	public boolean isAlive(){
		return alive;
	}

	public void setAlive(boolean alive){
		this.alive = alive;
	}

	public boolean canSetAlive(){
		return canSetAlive;
	}

	public void setCanSetAlive(boolean canSetAlive){
		this.canSetAlive = canSetAlive;
	}

	@Override
	public void sendDestroyData(MapleClient client){
		client.announce(makeDestroyData());
	}

	public final byte[] makeDestroyData(){
		return ReactorPool.destroyReactor(this);
	}

	@Override
	public void sendSpawnData(MapleClient client){
		client.announce(makeSpawnData());
	}

	public final byte[] makeSpawnData(){
		return ReactorPool.spawnReactor(this);
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getAction(){
		return action;
	}

	public void setAction(String action){
		this.action = action;
	}

	public void addState(Integer index, ReactorState newState){
		states.put(index, newState);
	}

	public Map<Integer, ReactorState> getStates(){
		return states;
	}

	public void runEvents(MapleClient c, ReactorHitInfo info){
		if(!checkQuest(c)) return;
		states.get(getCurrState()).runEvents(c, this, ReactorHitType.DEFAULT, info);
	}

	public boolean checkEvents(MapleClient c, ReactorHitInfo info){
		if(!checkQuest(c)) return false;
		return states.get(getCurrState()).checkEvents(c, this, ReactorHitType.DEFAULT, info);
	}

	public void hitReactor(MapleClient c){// legacy shit
		try{
			if(checkQuest(c)) states.get(getCurrState()).runEvents(c, this, ReactorHitType.HIT, new ReactorHitInfo());
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Reactor2: " + rid + ", Name: " + name + ", Hit Type: " + ReactorHitType.HIT.name() + ", Skillid: " + 0);
		}
	}

	public void hitReactor(ReactorHitType type, ReactorHitInfo info, MapleClient c){
		try{
			if(checkQuest(c)){
				if(c != null && c.getPlayer() != null && c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Reactor: " + getId() + " action: " + getAction());
				states.get(getCurrState()).runEvents(c, this, type, info);
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Reactor: " + rid + ", Name: " + name + ", Hit Type: " + type.name() + ", Skillid: " + info.toString());
		}
	}

	private boolean checkQuest(MapleClient c){
		if(c == null) return false;
		if(quest > 0){
			MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(quest));
			if(status.getStatus().equals(MapleQuestStatus.Status.STARTED)) return true;
			else return false;
		}
		return true;
	}

	@Override
	public int hashCode(){
		int hash = 7;
		hash = 89 * hash + this.rid;
		hash = 89 * hash + this.getObjectId();
		return hash;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		final MapleReactor other = (MapleReactor) obj;
		if(this.rid != other.rid) return false;
		if(this.getObjectId() != other.getObjectId()) return false;
		return true;
	}

	@Override
	public MapleReactor clone(){
		MapleReactor reactor = new MapleReactor(rid);
		reactor.setMap(null);
		reactor.setPosition((Point) getPosition().clone());
		reactor.setDelay(getDelay());
		reactor.setName(getName());
		reactor.setState(0);
		reactor.setAction(getAction());
		Map<Integer, ReactorState> newStates = new HashMap<>();
		for(int i : getStates().keySet()){
			newStates.put(i, getStates().get(i).clone());
		}
		reactor.states = newStates;
		reactor.quest = getQuest();
		return reactor;
	}
}