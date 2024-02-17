package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import constants.ServerConstants;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 20, 2017
 */
public class CommandUptime extends Command{

	public CommandUptime(){
		super("Uptime", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dropMessage(MessageType.MAPLETIP, "Uptime: " + StringUtil.getReadableMillis(ServerConstants.startup, System.currentTimeMillis()));
		return false;
	}
}
