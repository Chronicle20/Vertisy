package client.command.gm;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandCheckDmg extends Command{

	public CommandCheckDmg(){
		super("CheckDmg", "", "!CheckDmg", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
		victim.recalcLocalStats();
		int maxBase = victim.calculateMaxBaseDamage(victim.getTotalWatk());
		Integer watkBuff = victim.getBuffedValue(MapleBuffStat.WATK);
		Integer matkBuff = victim.getBuffedValue(MapleBuffStat.MATK);
		Integer blessing = victim.getSkillLevel(10000000 * victim.getJobType() + 12);
		if(watkBuff == null){
			watkBuff = 0;
		}
		if(matkBuff == null){
			matkBuff = 0;
		}
		c.getPlayer().dropMessage(5, "Cur Str: " + victim.getTotalStr() + " Cur Dex: " + victim.getTotalDex() + " Cur Int: " + victim.getTotalInt() + " Cur Luk: " + victim.getTotalLuk());
		c.getPlayer().dropMessage(5, "Cur WATK: " + victim.getTotalWatk() + " Cur MATK: " + victim.getTotalMagic());
		c.getPlayer().dropMessage(5, "Cur WATK Buff: " + watkBuff + " Cur MATK Buff: " + matkBuff + " Cur Blessing Level: " + blessing);
		c.getPlayer().dropMessage(5, victim.getName() + "'s maximum base damage (before skills) is " + maxBase);
		return false;
	}
}
