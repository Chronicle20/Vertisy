package client.command.controller;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.ObjectParser;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandRate extends Command{

	public CommandRate(){
		super("SetRate", "Set an rate exp", "!Rate <exp, meso, drop> <amount>", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			Integer amount = ObjectParser.isInt(args[1]);
			if(amount == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			try{
				switch (args[0].toLowerCase()){
					case "exp":
						ChannelServer.getInstance().getWorldInterface().setExpRate(amount, ChannelServer.getInstance().getQuestExpRate(), ChannelServer.getInstance().getDropRate(), ChannelServer.getInstance().getMesoRate());
						return false;
					case "meso":
						ChannelServer.getInstance().getWorldInterface().setExpRate(ChannelServer.getInstance().getExpRate(), ChannelServer.getInstance().getQuestExpRate(), ChannelServer.getInstance().getDropRate(), amount);
						return false;
					case "drop":
						ChannelServer.getInstance().getWorldInterface().setExpRate(ChannelServer.getInstance().getExpRate(), ChannelServer.getInstance().getQuestExpRate(), amount, ChannelServer.getInstance().getMesoRate());
						return false;
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		return false;
	}
}
