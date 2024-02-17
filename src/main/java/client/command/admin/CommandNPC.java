package client.command.admin;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.ObjectParser;
import tools.packets.field.NpcPool;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandNPC extends Command{

	public CommandNPC(){
		super("Npc", "", "!Npc <npcid>", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		Integer npcid = ObjectParser.isInt(args[0]);
		if(npcid == null){
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown npc");
			return false;
		}
		MapleNPC npc = MapleLifeFactory.getNPC(npcid);
		if(npc != null){
			npc.setPosition(c.getPlayer().getPosition());
			npc.setCy(c.getPlayer().getPosition().y);
			npc.setRx0(c.getPlayer().getPosition().x + 50);
			npc.setRx1(c.getPlayer().getPosition().x - 50);
			npc.setFh(c.getPlayer().getMap().getMapData().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
			c.getPlayer().getMap().addMapObject(npc);
			c.getPlayer().getMap().broadcastMessage(NpcPool.spawnNPC(npc));
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown npc");
		}
		return false;
	}
}
