package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandMuteMap extends Command{

	public CommandMuteMap(){
		super("MuteMap", "Toggle map mute", "!MuteMap", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(c.getPlayer().getMap().isMuted()){
			c.getPlayer().getMap().setMuted(false);
			c.getPlayer().dropMessage(5, "The map you are in has been un-muted.");
		}else{
			c.getPlayer().getMap().setMuted(true);
			c.getPlayer().dropMessage(5, "The map you are in has been muted.");
		}
		return false;
	}
}
