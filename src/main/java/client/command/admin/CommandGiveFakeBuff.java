package client.command.admin;

import java.util.ArrayList;
import java.util.List;

import client.*;
import client.command.Command;
import server.MapleStatEffect;
import tools.MaplePacketCreator;
import tools.ObjectParser;
import tools.Pair;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 4, 2017
 */
public class CommandGiveFakeBuff extends Command{

	public CommandGiveFakeBuff(){
		super("FakeBuff", "", "!FakeBuff <buffstat> <sourceid> <sourcelevel> <duration> <buffstat> <data>", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 1){
			Integer sourceid = ObjectParser.isInt(args[0]);
			Integer sourcelevel = ObjectParser.isInt(args[1]);
			Integer duration = ObjectParser.isInt(args[2]);
			MapleBuffStat buff = MapleBuffStat.valueOf(args[3]);
			Integer data = ObjectParser.isInt(args[4]);
			List<Pair<MapleBuffStat, BuffDataHolder>> localstatups = new ArrayList<>();
			localstatups.add(new Pair<MapleBuffStat, BuffDataHolder>(buff, new BuffDataHolder(sourceid, sourcelevel, data)));
			if(buff != null){
				if(!MapleStatEffect.hasNoIcon(sourceid)){
					byte[] buffdata = MaplePacketCreator.giveBuff(c.getPlayer(), sourceid.intValue(), duration.intValue(), localstatups);
					c.getPlayer().getClient().announce(buffdata);
					c.getPlayer().dropMessage(MessageType.MAPLETIP, buff.name() + " given");
				}
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
