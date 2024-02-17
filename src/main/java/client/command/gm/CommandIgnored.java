package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.MapleLogger;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 18, 2016
 */
public class CommandIgnored extends Command{

	public CommandIgnored(){
		super("Ignored", "Check all people ignored from Ingame Logging", "!Ignored", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		StringBuilder sb = new StringBuilder("Ignored: ");
		for(String ign : MapleLogger.ignored){
			sb.append(ign + ", ");
		}
		if(sb.length() > "Ignored: ".length()){
			sb.setLength(sb.length() - ", ".length());
		}
		c.getPlayer().dropMessage(MessageType.MAPLETIP, sb.toString());
		return false;
	}
}
