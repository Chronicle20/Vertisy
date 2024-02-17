package server.maps.objects.miniroom;

import java.lang.ref.WeakReference;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.MapleMapObjectType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 13, 2017
 */
public abstract class MiniGame extends MiniRoom{

	private final MiniGameType gameType;
	private WeakReference<MapleCharacter> visitor;
	private boolean started, isReady;

	public MiniGame(MapleCharacter chr, MiniGameType gameType){
		super(chr, MiniRoomTypee.MINIROOM);
		this.gameType = gameType;
	}

	@Override
	public int getGameType(){
		return gameType.ordinal();
	}

	public void addVisitor(MapleCharacter chr){
		visitor = new WeakReference<>(chr);
	}

	public MapleCharacter getVisitor(){
		return visitor == null ? null : visitor.get();
	}

	public void removeVisitor(MapleCharacter chr){
		visitor.clear();
		visitor = null;
	}

	public boolean hasStarted(){
		return started;
	}

	public void setStarted(boolean started){
		this.started = started;
	}

	public boolean isReady(){
		return isReady;
	}

	public void setReady(boolean ready){
		this.isReady = ready;
	}

	@Override
	public void sendSpawnData(MapleClient client){}

	@Override
	public void sendDestroyData(MapleClient client){}

	@Override
	public boolean hasFreeSlot(){
		// return getSlots() - visitors.size() > 0;
		return getVisitor() == null;
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.MINI_GAME;
	}

	@Override
	public MiniGame clone(){
		return null;
	}

	public enum MiniGameType{
		OMOK,
		MATCH_CARDS;
	}
}
