package client.command.gm;

import client.*;
import client.command.Command;
import net.channel.ChannelServer;
import tools.Pair;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 5, 2017
 */
public class CommandCheckStats extends Command{

	public CommandCheckStats(){
		super("CheckStats", "Display server-side calculated stats", "!CheckStats <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
			if(victim != null){
				victim.recalcLocalStats();
				c.getPlayer().dropMessage(MessageType.ERROR, "Str: " + victim.localstr + " Dex: " + victim.localdex + " Int: " + victim.localint + " Luk: " + victim.localluk);
				c.getPlayer().dropMessage(MessageType.ERROR, "Max HP: " + victim.localmaxhp + " Max MP: " + victim.localmaxmp + " Acc: " + victim.playerStat.accuracy);
				c.getPlayer().dropMessage(MessageType.ERROR, "Crit Dmg: " + victim.playerStat.critDamage);
				StringBuilder buffstats = new StringBuilder("");
				for(Pair<MapleBuffStat, BuffDataHolder> p : victim.getAllStatups()){
					buffstats.append(p.left.name());
					buffstats.append(" - " + p.right.getSourceID() + ":" + p.right.getSourceLevel() + ", ");
				}
				if(buffstats.toString().contains(",")) buffstats.setLength(buffstats.length() - 2);
				c.getPlayer().dropMessage(MessageType.ERROR, "Buffs: " + buffstats);
			}
		}
		return false;
	}
}
