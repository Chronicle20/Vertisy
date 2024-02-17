package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.life.MapleLifeFactory;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandHorntail extends Command{

	public CommandHorntail(){
		super("Horntail", "Spawn a horntail", "!Horntail", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8810018), c.getPlayer().getPosition());
		for(int i = 8810002; i < 8810010; i++){
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(i), c.getPlayer().getPosition());
		}
		return false;
	}
}
