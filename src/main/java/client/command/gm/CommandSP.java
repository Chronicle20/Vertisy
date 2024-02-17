package client.command.gm;

import client.*;
import client.command.Command;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandSP extends Command{

	public CommandSP(){
		super("SP", "Set sp of your or a specific player", "!SP <amount> <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter target = c.getPlayer();
			Integer amount = ObjectParser.isInt(args[0]);
			if(amount == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}else{
				if(args.length > 1){
					target = c.getChannelServer().getChannelServer().getCharacterByName(args[1]);
					if(target == null){
						c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
						return false;
					}
				}
			}
			target.setRemainingSp(amount);
			target.updateSingleStat(MapleStat.AVAILABLESP, target.getRemainingSp());
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
