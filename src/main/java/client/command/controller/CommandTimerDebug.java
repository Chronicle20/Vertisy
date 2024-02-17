package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.TimerManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandTimerDebug extends Command{

	public CommandTimerDebug(){
		super("TimerDebug", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		TimerManager tMan = TimerManager.getInstance();
		c.getPlayer().dropMessage(6, "Total Task: " + tMan.getTaskCount() + " Current Task: " + tMan.getQueuedTasks() + " Active Task: " + tMan.getActiveCount() + " Completed Task: " + tMan.getCompletedTaskCount());
		return false;
	}
}
