package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.MaplePacketCreator;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 11, 2017
 */
public class CommandChatMsg extends Command{

	public CommandChatMsg(){
		super("ChatMsg", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			c.announce(MaplePacketCreator.onChatMsg(Integer.parseInt(args[0]), StringUtil.joinStringFrom(args, 1)));
		}
		return false;
	}
}