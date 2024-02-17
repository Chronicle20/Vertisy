package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.quest.MapleQuest;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 2, 2017
 */
public class CommandCompleteQuest extends Command{

	public CommandCompleteQuest(){
		super("CompleteQuest", "Complete a quest", "!CompleteQuest <id>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			Integer id = ObjectParser.isInt(args[0]);
			if(id != null){
				MapleQuest.getInstance(id).forceComplete(c.getPlayer(), c.getPlayer().getQuest(MapleQuest.getInstance(id)).getNpc());
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
