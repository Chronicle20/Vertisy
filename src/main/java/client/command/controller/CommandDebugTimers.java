package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.TimerManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandDebugTimers extends Command{

	public CommandDebugTimers(){
		super("DebugTimers", "", "!DebugTimers", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(5, "Trying to access Tm...");
		TimerManager tMan = TimerManager.getInstance();
		c.getPlayer().dropMessage(5, "TimerManager accessed. Attempting to get INFO.");
		c.getPlayer().dropMessage(5, "Queued tasks : " + tMan.getQueuedTasks() + ". Active Count : " + tMan.getActiveCount());
		return false;
	}
}
