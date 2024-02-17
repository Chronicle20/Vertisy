package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandRuntime extends Command{

	public CommandRuntime(){
		super("Runtime", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		Runtime runtime = Runtime.getRuntime();
		c.getPlayer().dropMessage(6, "Free Memory: " + (runtime.freeMemory() / 1000000) + " MB.");
		return false;
	}
}
