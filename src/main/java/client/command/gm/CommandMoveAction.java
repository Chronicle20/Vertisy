package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 21, 2017
 */
public class CommandMoveAction extends Command{

	public CommandMoveAction(){
		super("MoveAction", "Modify your move action(stance). -1 to disable", "!MoveAction <target> <action>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter target = c.getPlayer();
			Byte moveAction = null;
			if(args.length > 1){
				target = ChannelServer.getInstance().getCharacterByName(args[0]);
				moveAction = ObjectParser.isByte(args[1]);
			}else moveAction = ObjectParser.isByte(args[0]);
			if(target != null){
				target.bMoveAction = moveAction;
				c.getPlayer().dropMessage(MessageType.MAPLETIP, "Move Action for " + target.getName() + " is now: " + moveAction);
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Invalid target.");
			}
		}
		return false;
	}
}
