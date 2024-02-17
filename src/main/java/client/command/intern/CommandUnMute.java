package client.command.intern;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 3, 2016
 */
public class CommandUnMute extends Command{

	public CommandUnMute(){
		super("UnMute", "", "!UnMute <target>", "unchatban");
		setGMLevel(PlayerGMRank.INTERN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[0]);
			if(victim != null){
				if(victim.getGMLevel() > c.getPlayer().getGMLevel()) return true; // bitchno
				victim.unChatBan();
				c.getPlayer().dropMessage(MessageType.SYSTEM, "You have unmuted " + victim.getName());
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown Player.");
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return true;
	}
}
