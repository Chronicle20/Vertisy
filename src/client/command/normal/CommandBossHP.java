package client.command.normal;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.life.MapleMonster;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandBossHP extends Command{

	public CommandBossHP(){
		super("BossHP", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		for(MapleMonster monster : c.getPlayer().getMap().getMonsters()){
			if(monster != null && monster.isBoss() && monster.getHp() > 0){
				long percent = monster.getHp() * 100L / monster.getMaxHp();
				String bar = "[";
				for(int i = 0; i < 100; i++){
					bar += i < percent ? "|" : ".";
				}
				bar += "]";
				c.getPlayer().yellowMessage(monster.getName() + " has " + percent + "% HP left.");
				c.getPlayer().yellowMessage("HP: " + bar);
			}
		}
		return false;
	}
}
