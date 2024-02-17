package client.command.controller;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 20, 2017
 */
public class CommandStartupChannel extends Command{

	public CommandStartupChannel(){
		super("StartupChannel", "", "!StartupChannel <id>", "startchannel");
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			Integer id = ObjectParser.isInt(args[0]);
			if(id != null){
				ChannelServer.getInstance().loadChannel(id);
				c.getPlayer().dropMessage(MessageType.SYSTEM, "Starting channel: " + id);
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			}
		}
		return false;
	}
}
