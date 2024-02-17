package client.command.supergm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 21, 2017
 */
public class CommandChair extends Command{

	public CommandChair(){
		super("Chair", "Set you or a target into a chair item", "!Chair <target> <action>", null);
		setGMLevel(PlayerGMRank.SUPERGM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter target = c.getPlayer();
			Integer itemid = null;
			if(args.length > 1){
				target = ChannelServer.getInstance().getCharacterByName(args[0]);
				itemid = ObjectParser.isInt(args[1]);
			}else itemid = ObjectParser.isInt(args[0]);
			if(target != null){
				target.setChair(itemid);
				target.getMap().broadcastMessage(target, MaplePacketCreator.showChair(target.getId(), itemid), true);
				c.getPlayer().dropMessage(MessageType.MAPLETIP, "Chair for " + target.getName() + " is now: " + itemid);
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Invalid target.");
			}
		}
		return false;
	}
}
