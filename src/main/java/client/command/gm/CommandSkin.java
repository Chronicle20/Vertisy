package client.command.gm;

import client.*;
import client.command.Command;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandSkin extends Command{

	public CommandSkin(){
		super("Skin", "Change your skin", "!skin <skin> <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter target = c.getPlayer();
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		Integer skin = ObjectParser.isInt(args[0]);
		if(skin == null){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		if(MapleSkinColor.getById(skin) == null){
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown skin");
			return false;
		}
		if(args.length == 2){
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[0]);
			if(victim == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
				return false;
			}else{
				target = victim;
			}
		}
		target.updateSingleStat(MapleStat.SKIN, skin);
		target.setSkinColor(MapleSkinColor.getById(skin));
		target.equipChanged();
		return false;
	}
}
