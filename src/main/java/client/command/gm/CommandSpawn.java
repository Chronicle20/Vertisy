package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandSpawn extends Command{

	public CommandSpawn(){
		super("Spawn", "Spawn a monster", "!spawn <mobid> <amount>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			Integer mobid = ObjectParser.isInt(args[0]);
			if(mobid == null){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			Integer amount = 1;
			if(args.length > 1){
				Integer newAmount = ObjectParser.isInt(args[1]);
				if(newAmount != null) amount = newAmount;
			}
			if(MapleLifeFactory.getMonster(mobid) == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown mob.");
				return false;
			}
			for(int i = 0; i < amount; i++){
				MapleMonster mob = MapleLifeFactory.getMonster(mobid);
				// mob.disableDrops();
				c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition(), true);
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
