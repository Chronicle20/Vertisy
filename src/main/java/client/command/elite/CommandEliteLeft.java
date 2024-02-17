package client.command.elite;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 14, 2016
 */
public class CommandEliteLeft extends Command{

	public CommandEliteLeft(){
		super("EliteLeft", "", "", null);
		setGMLevel(PlayerGMRank.ELITE);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "You have " + c.getEliteTimeLeft() + " of Elite.");
		return false;
	}
}
