package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 8, 2017
 */
public class CommandArnahIP extends Command{

	public CommandArnahIP(){
		super("ArnahIP", "", "", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		// GameConstants.ARNAH_IP = args[0];
		return false;
	}
}