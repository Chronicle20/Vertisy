package client.command.gm;

import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleQuestStatus.Status;
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
public class CommandClearQuest extends Command{

	public CommandClearQuest(){
		super("ClearQuest", "Clear a quest", "!ClearQuest <id>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			Integer id = ObjectParser.isInt(args[0]);
			if(id != null){
				MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(id));
				status.setStatus(Status.NOT_STARTED);
				c.getPlayer().updateQuest(status);
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
