package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.packets.UserLocal;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 17, 2016
 */
public class CommandSpecialEffect extends Command{

	public CommandSpecialEffect(){
		super("SpecialEffect", "", "", "SE");
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			c.announce(UserLocal.UserEffect.showSpecialEffect(Integer.parseInt(args[0])));
		}
		return false;
	}
}
