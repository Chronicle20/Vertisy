package client.command.normal;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandRates extends Command{

	public CommandRates(){
		super("Rates", "", "", "Rate");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().message("Exp: " + c.getPlayer().getStats().getExpRate() + "x | Bonus Exp: " + c.getPlayer().getStats().getBonusExpBuff() + "% | Quest Exp: " + c.getPlayer().getStats().getQuestExpRate() + "x | Meso: " + c.getPlayer().getStats().getMesoRate() + "x | Drop: " + c.getPlayer().getStats().getDropRate() + "x.");
		if(c.getPlayer().isIntern()){
			c.getPlayer().message("Party Exp Buff: " + c.getPlayer().getStats().getPartyExpBuff() + "%");
		}
		return false;
	}
}
