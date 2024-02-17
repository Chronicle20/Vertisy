package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandClearDrops extends Command{

	public CommandClearDrops(){
		super("ClearDrops", "Clear all drops in the map", "!ClearDrops", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().getMap().clearDrops(c.getPlayer());
		return false;
	}
}
