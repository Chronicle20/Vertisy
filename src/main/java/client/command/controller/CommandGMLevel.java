package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 8, 2016
 */
public class CommandGMLevel extends Command{

	public CommandGMLevel(){
		super("GmLevel", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 1){
			c.setGMLevel(Integer.parseInt(args[0]));
		}
		return false;
	}
}
