package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import constants.ServerConstants;
import server.TimerManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandResetTimerManager extends Command{

	public CommandResetTimerManager(){
		super("ResetTimerManager", "", "!ResetTimerManager", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(5, "[ADMIN] Resetting TimerManager....");
		TimerManager.getInstance().stop();
		c.getPlayer().dropMessage(5, "[ADMIN] Timer stopped. Restarting...");
		TimerManager.getInstance().start();
		c.getPlayer().dropMessage(5, "[ADMIN] Timer started... now starting core threads...");
		TimerManager.getInstance().register("RankingWorker", new net.server.RankingWorker(), ServerConstants.RANKING_INTERVAL);
		c.getPlayer().dropMessage(5, "[ADMIN] RankingWorker started.");
		/*for(Channel ch : Server.getInstance().getAllChannels()){
			ch.restartSpawnThread();
			c.getPlayer().dropMessage(5, "[ADMIN] CH" + ch.getId() + " respawn thread started.");
			ch.reloadEventScriptManager();
			c.getPlayer().dropMessage(5, "[ADMIN] Event scripts reloaded and threads commenced.");
			ch.broadcastPacket(MaplePacketCreator.serverNotice(0, "Attention. All server timers have been reset. If you are in a time-sensitive situation, please take note " + "that the current timer will not function. All timers that appear after this notice will work."));
		}*/
		return false;
	}
}
