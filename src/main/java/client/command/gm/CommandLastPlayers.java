package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 23, 2016
 */
public class CommandLastPlayers extends Command{

	public CommandLastPlayers(){
		super("LastPlayers", "Get a list of the last players in the map.", "!LastPlayers", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		StringBuilder sb = new StringBuilder("Players: ");
		for(String player : c.getPlayer().getMap().getLastPlayers()){
			sb.append(player);
			sb.append(", ");
		}
		sb.setLength(sb.length() - ", ".length());
		c.getPlayer().dropMessage(MessageType.SYSTEM, sb.toString());
		return false;
	}
}
