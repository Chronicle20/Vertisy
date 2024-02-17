package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Nov 24, 2017
 */
public class CommandResetSP extends Command{

	public CommandResetSP(){
		super("ResetSP", "Reset your sp and ap", "!ResetSP", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().resetStats();
		return false;
	}
}
