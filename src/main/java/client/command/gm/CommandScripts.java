package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 22, 2016
 */
public class CommandScripts extends Command{

	public CommandScripts(){
		super("Scripts", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().toggleScriptDebug();
		c.getPlayer().dropMessage(MessageType.SYSTEM, "Script Debug: " + (c.getPlayer().getScriptDebug() ? "Enabled." : "Disabled."));
		return false;
	}
}
