package server.events.gm;

import client.MapleCharacter;
import net.channel.ChannelServer;
import server.maps.MapleMap;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 25, 2017
 */
public abstract class Event{

	private int channel, map;

	public Event(int channel, int map){
		this.channel = channel;
		this.map = map;
	}

	public int getChannel(){
		return channel;
	}

	public MapleMap getMap(){
		return ChannelServer.getInstance().getChannel(channel).getMap(map);
	}

	public abstract void start();

	public abstract void end();

	public abstract boolean enter(MapleCharacter chr);

	public int getLimit(){
		return 0;// Temp
	}
}
