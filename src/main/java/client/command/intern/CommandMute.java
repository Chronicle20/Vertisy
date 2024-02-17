package client.command.intern;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.ObjectParser;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 3, 2016
 */
public class CommandMute extends Command{

	public CommandMute(){
		super("Mute", "", "!Mute <target> <hours> <reason>", "chatban");
		setGMLevel(PlayerGMRank.INTERN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[0]);
			if(victim != null){
				if(victim.getGMLevel() > c.getPlayer().getGMLevel()) return true; // bitchno
				String reason = null;
				String fullReason = null;
				// if(args.length > 1){//time based
				Integer hours = args.length > 1 ? ObjectParser.isInt(args[1]) : null;
				reason = hours != null ? StringUtil.joinStringFrom(args, 2) : StringUtil.joinStringFrom(args, 1);
				if(reason == null || reason.length() == 0){
					c.getPlayer().dropMessage(MessageType.ERROR, "Invalid Reason.");
					return true;
				}
				if(hours != null){
					fullReason = "You have been muted for " + hours + (hours == 1 ? " hour" : " hours") + " for: " + reason + " by: " + c.getPlayer().getName();
					victim.chatBan(fullReason, hours);
					victim.message(fullReason);
					c.getPlayer().dropMessage(MessageType.SYSTEM, "You have muted " + victim.getName() + " for " + hours + (hours == 1 ? " hour" : " hours") + ".");
				}else{
					if(c.getPlayer().isGM()){
						fullReason = "You have been muted permanently for: " + reason + " by: " + c.getPlayer().getName();
						victim.chatBan(fullReason);
						victim.message(fullReason);
						c.getPlayer().dropMessage(MessageType.SYSTEM, "You have muted " + victim.getName() + " permanently.");
					}else{
						c.getPlayer().dropMessage(MessageType.ERROR, "Invalid length.");
					}
				}
				/*}else if(c.getPlayer().isGM()){// perm
					reason = StringUtil.joinStringFrom(args, 1);
					if(reason == null || reason.length() == 0){
						c.getPlayer().dropMessage(MessageType.ERROR, "Invalid Reason.");
						return true;
					}
					fullReason = "You have been muted permanently for: " + reason + " by: " + c.getPlayer().getName();
					victim.chatBan(fullReason);
					victim.message(fullReason);
					c.getPlayer().dropMessage(MessageType.SYSTEM, "You have muted " + victim.getName() + " permanently.");
				}else{
					c.getPlayer().dropMessage(MessageType.ERROR, getUsuage());
				}*/
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown Player.");
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return true;
	}
}
