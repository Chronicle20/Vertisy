package client.command.normal;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.UserLocal.FollowCharacterFailType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 27, 2017
 */
public class CommandFollow extends Command{

	public CommandFollow(){
		super("Follow", "", "!Follow <target>", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			if(c.getPlayer().driver != -1){
				c.announce(UserLocal.followCharacterFailed(FollowCharacterFailType.IS_FOLLOWING, c.getPlayer().driver));
				return true;
			}
			MapleCharacter target = c.getPlayer().getMap().getCharacterByName(args[0]);
			if(target == null){
				c.announce(UserLocal.followCharacterFailed(FollowCharacterFailType.CANT_ACCEPT, 0));
				return true;
			}
			target.requestedFollow.add(c.getPlayer().getId());
			target.getClient().announce(CWvsContext.SetPassengerRequest(c.getPlayer().getId()));
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
