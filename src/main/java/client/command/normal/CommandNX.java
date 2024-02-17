package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 22, 2016
 */
public class CommandNX extends Command{

	public CommandNX(){
		super("NX", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "NX: " + c.getPlayer().getCashShop().getCash(4));
		return false;
	}
}
