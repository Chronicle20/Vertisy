package client.command.gm;

import client.*;
import client.command.Command;
import server.MapleCharacterInfo;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandHair extends Command{

	public CommandHair(){
		super("Hair", "Change your hair", "!hair <hair> <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter target = c.getPlayer();
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		Integer hair = ObjectParser.isInt(args[0]);
		if(hair == null){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		if(MapleCharacterInfo.getInstance().getHairs().get(hair) == null){
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown hair");
			return false;
		}
		if(args.length == 2){
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[1]);
			if(victim == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
				return false;
			}else{
				target = victim;
			}
		}
		target.updateSingleStat(MapleStat.HAIR, hair);
		target.setHair(hair);
		target.equipChanged();
		return false;
	}
}
