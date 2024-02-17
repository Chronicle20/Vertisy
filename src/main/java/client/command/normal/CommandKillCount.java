package client.command.normal;

import java.util.Calendar;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 29, 2016
 */
public class CommandKillCount extends Command{

	public CommandKillCount(){
		super("KillCount", "", "", "killcounter, mounsterskilled, kills");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		long killCount = 0;
		for(long kills : c.getPlayer().monsterKillTotal.values()){
			killCount += kills;
		}
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "Monsters Killed: " + killCount);
		killCount = 0;
		Calendar cal = Calendar.getInstance();
		String yearMonth = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONDAY);
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "This month and higher: " + c.getPlayer().monsterKillHigher.get(yearMonth));
		return false;
	}
}
