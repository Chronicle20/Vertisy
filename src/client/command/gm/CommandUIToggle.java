package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 17, 2017
 */
public class CommandUIToggle extends Command{

	public CommandUIToggle(){
		super("UIToggle", "Toggle seeing UI in F1 fly", "!UIToggle", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().uiToggle = !c.getPlayer().uiToggle;
		c.getPlayer().dropMessage(MessageType.SYSTEM, "In F1 Fly the UI will now be " + (c.getPlayer().uiToggle ? "hidden" : "shown") + ".");
		return false;
	}
}