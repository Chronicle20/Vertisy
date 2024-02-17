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
 * @since Jul 11, 2016
 */
public class CommandBigBrother extends Command{

	public CommandBigBrother(){
		super("BigBrother", "Toggle BigBrother", "!BigBrother", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
			if(victim != null){
				if(c.getPlayer().isBigBrotherMonitoring(victim.getId())){
					c.getPlayer().removeBigBrotherMonitor(victim.getId());
					c.getPlayer().dropMessage(MessageType.SYSTEM, "No longer BigBrother monitoring: " + args[0]);
				}else{
					c.getPlayer().addBigBrotherMonitor(victim.getId());
					c.getPlayer().dropMessage(MessageType.SYSTEM, "Now BigBrother monitoring: " + args[0]);
				}
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player: " + args[0]);
			}
		}else{
			c.getPlayer().toggleBigBrother();
			c.getPlayer().message("You are " + (c.getPlayer().isBigBrother() ? "a" : "not a") + " big brother.");
		}
		return false;
	}
}
