package client.command.gm;

import java.rmi.RemoteException;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 18, 2016
 */
public class CommandIgnore extends Command{

	public CommandIgnore(){
		super("Ignore", "Ignore someone from Ingame Logging", "!Ignore <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		String target = args[0].toLowerCase();
		boolean ignored = MapleLogger.ignored.contains(target);
		if(ignored){
			MapleLogger.ignored.remove(target);
		}else{
			MapleLogger.ignored.add(target);
		}
		c.getPlayer().dropMessage(MessageType.SYSTEM, target + " is " + (!ignored ? "now being ignored." : "no longer being ignored."));
		String message = c.getPlayer().getName() + (!ignored ? " has started ignoring " : " has stopped ignoring ") + target + ".";
		try{
			ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(MaplePacketCreator.serverNotice(5, message));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		return false;
	}
}
