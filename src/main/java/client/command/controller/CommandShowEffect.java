package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.MaplePacketCreator;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 26, 2017
 */
public class CommandShowEffect extends Command{

	public CommandShowEffect(){
		super("ShowEffect", "", "!ShowEffect <effect>", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length != 0){
			c.announce(MaplePacketCreator.showEffect(args[0]));
		}
		return false;
	}
}
