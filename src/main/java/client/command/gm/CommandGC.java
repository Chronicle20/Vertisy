package client.command.gm;

import java.rmi.RemoteException;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import constants.ServerConstants;
import net.channel.ChannelServer;
import net.server.guild.MapleGuild;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 20, 2016
 */
public class CommandGC extends Command{

	public CommandGC(){
		super("GC", "Talk in a guild forcefully", "!GC <guild> <message>", "guildchat, gchat");
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			String guildName = args[0];
			try{
				MapleGuild guild = ChannelServer.getInstance().getWorldInterface().getGuild(guildName);
				if(guild != null){
					ChannelServer.getInstance().getWorldInterface().guildChat(guild.getId(), c.getPlayer().getName(), c.getPlayer().getId(), StringUtil.joinStringFrom(args, 1));
				}else{
					c.getPlayer().dropMessage(MessageType.ERROR, "Unknown guild: " + guildName);
				}
			}catch(RemoteException | NullPointerException ex){
				c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		return false;
	}
}
