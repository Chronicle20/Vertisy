package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandPosition extends Command{

	public CommandPosition(){
		super("Position", "Get your current position.", "!Position", "pos");
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		float xpos = c.getPlayer().getPosition().x;
		float ypos = c.getPlayer().getPosition().y;
		float fh = c.getPlayer().getMap().getMapData().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
		c.getPlayer().dropMessage("Position: (" + xpos + ", " + ypos + ")");
		c.getPlayer().dropMessage("Foothold ID: " + fh);
		return false;
	}
}
