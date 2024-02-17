package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandChat extends Command{

	public CommandChat(){
		super("Chat", "Toggle white text", "!Chat", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().toggleWhiteChat();
		c.getPlayer().dropMessage(5, "Your chat is now " + (c.getPlayer().getWhiteChat() ? "white" : "normal") + ".");
		return false;
	}
}
