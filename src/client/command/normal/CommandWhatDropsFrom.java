package client.command.normal;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import scripting.npc.NPCScriptManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandWhatDropsFrom extends Command{

	public CommandWhatDropsFrom(){
		super("WhatDropsFrom", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dispose();
		NPCScriptManager.getInstance().start(c, 9010000, "whatdropsfrom", c.getPlayer());
		return false;
	}
}
