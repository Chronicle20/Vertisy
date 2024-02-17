package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 14, 2016
 */
public class CommandHide extends Command{

	public CommandHide(){
		super("Hide", "Toggle being hidden", "!Hide", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().toggleHide(false);
			// c.getPlayer().dropMessage(MessageType.SYSTEM, c.getPlayer().isHidden() ? "You are now hidden." : "You are now visible.");
		}else{
			if(args[0].equalsIgnoreCase("arnah")){
				c.getPlayer().dropMessage(MessageType.POPUP, "How about you don't");
				return false;
			}
			MapleCharacter chr = ChannelServer.getInstance().getCharacterByName(args[0]);
			if(chr != null){
				chr.toggleHide(false);
				c.getPlayer().dropMessage(MessageType.SYSTEM, chr.isHidden() ? chr.getName() + " is now hidden." : chr.getName() + " is not visible");
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Unable to find player " + args[0]);
			}
		}
		return false;
	}
}
