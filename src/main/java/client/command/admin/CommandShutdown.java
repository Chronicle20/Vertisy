package client.command.admin;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import client.command.Commands.ShutDownTask;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandShutdown extends Command{

	public CommandShutdown(){
		super("Shutdown", "", "!Shutdown <minutes>", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		int time = 0;
		if(args.length > 0){
			Integer newTime = ObjectParser.isInt(args[0]);
			if(newTime == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}else{
				time = newTime;
			}
		}
		c.getPlayer().dropMessage("Server will shutdown in " + time + " minute" + (time > 1 ? "s" : "") + ".");
		(new ShutDownTask()).startShutDown(time);
		return false;
	}
}
