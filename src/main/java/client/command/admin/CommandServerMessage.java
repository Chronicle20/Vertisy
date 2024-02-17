package client.command.admin;

import java.rmi.RemoteException;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandServerMessage extends Command{

	public CommandServerMessage(){
		super("ServerMessage", "", "!ServerMessage", "sm");
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		try{
			ChannelServer.getInstance().getWorldInterface().setServerMessage(StringUtil.joinStringFrom(args, 0));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		return false;
	}
}
