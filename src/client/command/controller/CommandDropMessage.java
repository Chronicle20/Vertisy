package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 15, 2016
 */
public class CommandDropMessage extends Command{

	public CommandDropMessage(){
		super("DropMessage", "", "", "DM");
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			c.getPlayer().dropMessage(Integer.parseInt(args[0]), StringUtil.joinStringFrom(args, 1));
		}
		return false;
	}
}