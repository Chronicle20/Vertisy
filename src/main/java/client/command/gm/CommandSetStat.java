package client.command.gm;

import client.MapleClient;
import client.MapleStat;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 28, 2016
 */
public class CommandSetStat extends Command{

	public CommandSetStat(){
		super("SetStat", "Set the value of a specific stat.", "!SetStat <stat> <amount>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			Integer amount = ObjectParser.isInt(args[1]);
			if(amount == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			MapleStat stat = MapleStat.getByString(args[0].toUpperCase());
			if(stat == null){
				StringBuilder sb = new StringBuilder("Available stats: ");
				for(MapleStat s : MapleStat.values()){
					sb.append(s.name() + ", ");
				}
				sb.setLength(sb.length() - ", ".length());
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			c.getPlayer().setStat(stat, amount);
			c.getPlayer().updateSingleStat(stat, amount);
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
