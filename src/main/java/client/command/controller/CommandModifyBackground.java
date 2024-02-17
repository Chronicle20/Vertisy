package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Apr 7, 2016
 */
public class CommandModifyBackground extends Command{

	public CommandModifyBackground(){
		super("ModifyBackground", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length >= 2){
			if(args.length == 3){
				c.getPlayer().getMap().sendBackgroundEffect(c.getPlayer(), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]));
			}else{
				c.getPlayer().getMap().sendBackgroundEffect(c.getPlayer(), Integer.parseInt(args[0]), -1, Boolean.parseBoolean(args[1]));
			}
		}else{// Night
			for(int i = 0; i < 10; i++)
				c.getPlayer().getMap().sendBackgroundEffect(c.getPlayer(), 1, i, Boolean.parseBoolean(args[0]));
		}
		return false;
	}
}
