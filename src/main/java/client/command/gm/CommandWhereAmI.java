package client.command.gm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.MaplePortal;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.objects.MapleMapObject;
import server.reactors.MapleReactor;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 13, 2016
 */
public class CommandWhereAmI extends Command{

	public CommandWhereAmI(){
		super("Whereami", "Gives information about the map you are currently in.", "!Whereami", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().yellowMessage("Map ID: " + c.getPlayer().getMap().getId());
		c.getPlayer().yellowMessage("Players on this map:");
		for(MapleMapObject mmo : c.getPlayer().getMap().getAllPlayer()){
			MapleCharacter chr = (MapleCharacter) mmo;
			c.getPlayer().dropMessage(5, ">> " + chr.getName());
		}
		c.getPlayer().yellowMessage("NPCs on this map:");
		for(MapleMapObject npcs : c.getPlayer().getMap().getMapObjects()){
			if(npcs == null) continue;
			if(npcs instanceof MapleNPC){
				MapleNPC npc = (MapleNPC) npcs;
				if(npc != null) c.getPlayer().dropMessage(5, ">> " + npc.getName() + " - " + npc.getId());
			}
		}
		c.getPlayer().yellowMessage("Unique Monsters on this map:");
		int monsters = 0;
		Map<Integer, Integer> knownMobs = new HashMap<>();
		Map<Integer, String> idToName = new HashMap<>();
		for(MapleMapObject mobs : c.getPlayer().getMap().getMapObjects()){
			if(mobs instanceof MapleMonster){
				MapleMonster mob = (MapleMonster) mobs;
				if(mob.isAlive()){
					monsters++;
					Integer amount = knownMobs.get(mob.getId());
					if(amount == null) amount = 1;
					else amount++;
					knownMobs.put(mob.getId(), amount);
					idToName.put(mob.getId(), mob.getName());
				}
			}
		}
		for(Entry<Integer, Integer> data : knownMobs.entrySet()){
			c.getPlayer().dropMessage(5, ">> " + idToName.get(data.getKey()) + " - " + data.getKey() + " : " + data.getValue());
		}
		c.getPlayer().yellowMessage("Total monsters: " + monsters);
		c.getPlayer().yellowMessage("Portals on this map:");
		for(MaplePortal portal : c.getPlayer().getMap().getPortals()){
			c.getPlayer().dropMessage(5, ">> " + portal.getName() + " - " + portal.getId() + " - " + portal.getScriptName());
		}
		c.getPlayer().yellowMessage("Reactors on this map:");
		for(MapleReactor reactor : c.getPlayer().getMap().getAllReactor()){
			c.getPlayer().dropMessage(5, ">> " + reactor.getName() + " - " + reactor.getId() + " - " + reactor.getAction() + " State: " + reactor.getCurrState());
		}
		return false;
	}
}
