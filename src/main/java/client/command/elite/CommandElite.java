package client.command.elite;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import scripting.npc.NPCScriptManager;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 15, 2017
 */
public class CommandElite extends Command{

	public CommandElite(){
		super("Elite", "", "", null);
		setGMLevel(PlayerGMRank.ELITE);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().dispose();
		NPCScriptManager.getInstance().start(c, 9250052, "TradeButton", c.getPlayer());
		return false;
	}
}
