package client.command.intern;

import java.util.List;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 26, 2017
 */
public class CommandBack extends Command{

	public CommandBack(){
		super("Back", "", "!Back", null);
		setGMLevel(PlayerGMRank.INTERN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		List<Integer> lastMaps = c.getPlayer().getPreviousMaps();
		if(lastMaps.isEmpty()){
			c.getPlayer().dropMessage(MessageType.ERROR, "No previous maps are saved.");
			return false;
		}
		int lastMap = 0;
		for(int mapid : lastMaps){
			lastMap = mapid;
		}
		if(lastMap != 0){
			lastMaps.remove(lastMap);
			c.getPlayer().changeMap(lastMap);
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, "Unable to find a map to warp back to.");
		}
		return false;
	}
}
