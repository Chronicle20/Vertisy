package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.MaplePortal;
import server.maps.MapleMap;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandMap extends Command{

	public CommandMap(){
		super("Map", "Teleport to a specific mapid", "!Map <mapid>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		try{
			MapleMap target = c.getChannelServer().getMap(Integer.parseInt(args[0]));
			if(target == null){
				c.getPlayer().yellowMessage("Map ID " + args[0] + " is invalid.");
				return false;
			}
			if(c.getPlayer().getEventInstance() != null){
				MaplePortal portal = target.getPortal(args.length > 1 ? Integer.parseInt(args[0]) : 0);
				c.getPlayer().changeMap(c.getPlayer().getEventInstance().getMapInstance(target.getId()), portal);
			}else{
				MaplePortal portal = target.getPortal(args.length > 1 ? Integer.parseInt(args[0]) : 0);
				c.getPlayer().changeMap(target, portal);
			}
		}catch(Exception ex){
			c.getPlayer().yellowMessage("Map ID " + args[0] + " is invalid.");
			return false;
		}
		return false;
	}
}
