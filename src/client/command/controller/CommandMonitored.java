package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.MapleLogger;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandMonitored extends Command{

	public CommandMonitored(){
		super("Monitored", "All players monitored", "!Monitored", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		for(String ign : MapleLogger.monitored){
			c.getPlayer().dropMessage(5, ign + " is being monitored.");
		}
		return false;
	}
}
