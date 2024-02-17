package client.command.controller;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.maps.MapleMapData;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 12, 2017
 */
public class CommandMapDataInfo extends Command{

	public CommandMapDataInfo(){
		super("MapDataInfo", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleMapData data = c.getPlayer().getMap().getMapData();
		c.getPlayer().dropMessage(MessageType.ERROR, "Map Objects: " + data.getMapObjects().size());
		int npcs = 0;
		for(MapleMapObject mmo : data.getMapObjects()){
			if(mmo.getType().equals(MapleMapObjectType.NPC)){
				npcs++;
			}
		}
		c.getPlayer().dropMessage(MessageType.ERROR, "NPCs: " + npcs);
		int monsters = 0;
		for(MapleMapObject mmo : data.getMapObjects()){
			if(mmo.getType().equals(MapleMapObjectType.MONSTER)){
				monsters++;
			}
		}
		c.getPlayer().dropMessage(MessageType.ERROR, "Monsters: " + monsters);
		return false;
	}
}
