package client.command.normal;

import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.TimerManager;
import tools.ObjectParser;
import tools.StringUtil;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 1, 2017
 */
public class CommandBattleAnalysis extends Command{

	public CommandBattleAnalysis(){
		super("BattleAnalysis", "", "@BattleAnalysis <start/stop> <length>", "BA");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			switch (args[0].toLowerCase()){
				case "start":{
					if(!c.getPlayer().battleAnaylsis.running){
						c.getPlayer().battleAnaylsis.start = System.currentTimeMillis();
						c.getPlayer().battleAnaylsis.running = true;
						if(args.length > 1){
							Integer minutes = ObjectParser.isInt(args[1]);
							if(minutes != null){
								c.getPlayer().removeTimer("battleanalysis");
								c.getPlayer().addTimer("battleanalysis", TimerManager.getInstance().schedule("battleanalysis", ()-> {
									stop(c.getPlayer());
								}, 60 * 1000L));
								c.getPlayer().dropMessage(MessageType.MAPLETIP, "BattleAnalysis is now running for " + args[1] + " minutes.");
							}else c.getPlayer().dropMessage(MessageType.ERROR, "Enter a valid integer for how many minutes you want.");
						}else c.getPlayer().dropMessage(MessageType.MAPLETIP, "BattleAnalysis is now running.");
					}else c.getPlayer().dropMessage(MessageType.ERROR, "You already have a Battle Analysis running.");
					break;
				}
				case "stop":{
					stop(c.getPlayer());
					break;
				}
			}
		}else c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		return false;
	}

	public void stop(MapleCharacter chr){
		chr.battleAnaylsis.end = System.currentTimeMillis();
		chr.battleAnaylsis.running = false;
		chr.dropMessage(MessageType.SYSTEM, "Battle Analysis ran for " + StringUtil.getReadableMillis(chr.battleAnaylsis.start, chr.battleAnaylsis.end));
		long totalDamage = 0;
		for(int skillid : chr.battleAnaylsis.totalDamage.keySet()){
			List<Integer> damages = chr.battleAnaylsis.totalDamage.get(skillid);
			for(int dmg : damages){
				totalDamage += dmg;
			}
		}
		chr.dropMessage(MessageType.SYSTEM, "Total Damage: " + totalDamage);
		chr.dropMessage(MessageType.SYSTEM, "Acquired Exp: " + chr.battleAnaylsis.acquiredExp);
		chr.dropMessage(MessageType.SYSTEM, "Acquired Meso: " + chr.battleAnaylsis.acquiredMeso);
	}
}