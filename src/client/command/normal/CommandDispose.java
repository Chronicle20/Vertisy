package client.command.normal;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandDispose extends Command{

	public CommandDispose(){
		super("Dispose", "", "", "Save");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dispose();
		c.getPlayer().message("You've been disposed.");
		c.getPlayer().recalcLocalStats();
		return false;
	}
}
