package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.maps.MapleMapEffect;
import tools.MaplePacketCreator;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 20, 2016
 */
public class CommandNight extends Command{

	public CommandNight(){
		super("Night", "Completely disables night overlay.", "", "nightoverlay");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(!c.isNightOverlayEnabled()){
			c.setProgressValue("nightoverlay", true);
			c.getPlayer().dropMessage(MessageType.SYSTEM, "Night overlay enabled.");
			MapleMapEffect effect = c.getPlayer().getMap().getMapEffect();
			if(effect != null) c.announce(effect.makeStartData());
		}else{
			c.setProgressValue("nightoverlay", false);
			c.getPlayer().dropMessage(MessageType.SYSTEM, "Night overlay disabled.");
			c.announce(MaplePacketCreator.removeMapEffect());
		}
		return false;
	}
}
