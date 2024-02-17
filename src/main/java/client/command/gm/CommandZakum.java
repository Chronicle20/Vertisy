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
public class CommandZakum extends Command{

	public CommandZakum(){
		super("Zakum", "Spawn a Zakum", "!Zakum", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		c.getPlayer().getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), c.getPlayer().getPosition());
		for(int x = 8800003; x < 8800011; x++){
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(x), c.getPlayer().getPosition());
		}
		return false;
	}
}
