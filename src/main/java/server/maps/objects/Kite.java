package server.maps.objects;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledFuture;

import client.MapleCharacter;
import client.MapleClient;
import server.TimerManager;
import server.maps.MapleMap;
import tools.packets.field.MessageBoxPool;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 31, 2016
 */
public class Kite extends AbstractMapleMapObject{

	private final String message, playerName;
	private final int itemid;
	private final WeakReference<MapleMap> map;
	private ScheduledFuture<?> schedule = null;

	public Kite(MapleCharacter player, int itemid, String message){
		this.playerName = player.getName();
		this.itemid = itemid;
		this.message = message;
		this.map = new WeakReference<MapleMap>(player.getMap());
		this.setPosition(player.getMap().calcPointBelow(player.getPosition()));
		this.schedule = TimerManager.getInstance().schedule("kitedestroy", new Runnable(){

			@Override
			public void run(){
				destroyKite();
			}
		}, 1000 * 60 * 60 * 4);
	}

	public int getItemID(){
		return itemid;
	}

	public String getMessage(){
		return message;
	}

	public String getPlayerName(){
		return playerName;
	}

	public void destroyKite(){
		if(schedule != null){
			schedule.cancel(false);
		}
		MapleMap map = this.map.get();
		if(map != null){
			map.broadcastMessage(MessageBoxPool.destroyKite(getObjectId(), (byte) 0));
			map.removeMapObject(this);
			this.map.clear();
		}
		schedule = null;
	}

	@Override
	public void sendSpawnData(MapleClient client){
		client.announce(MessageBoxPool.spawnKite(this));
	}

	@Override
	public void sendDestroyData(MapleClient client){
		client.announce(MessageBoxPool.destroyKite(getObjectId(), (byte) 0));
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.KITE;
	}

	@Override
	public Kite clone(){
		return null;
	}
}
