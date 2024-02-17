package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import server.life.MapleLifeFactory;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandBomb extends Command{

	public CommandBomb(){
		super("Bomb", "Place a bomb on a target", "!Bomb <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			if(args[0].equalsIgnoreCase("arnah")){
				c.getPlayer().dropMessage(MessageType.POPUP, "How about you don't");
				return false;
			}
			MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
			if(victim != null) victim.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300166), victim.getPosition());
		}else{
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300166), c.getPlayer().getPosition());
		}
		return false;
	}
}
