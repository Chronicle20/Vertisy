package client.command.controller;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.MapleLogger;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandMonitorPackets extends Command{

	public CommandMonitorPackets(){
		super("MonitorPackets", "Monitor packets of a specific player", "!MonitorPackets <target>", null);
		setGMLevel(PlayerGMRank.CONTROLLER);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		if(args.length > 1){
			boolean monitored = MapleLogger.monitored.contains(args[0]);
			if(monitored){
				MapleLogger.monitored.remove(args[0]);
			}else{
				MapleLogger.monitored.add(args[0]);
			}
			c.getPlayer().dropMessage(5, args[0] + " is " + (!monitored ? "now being monitored." : "no longer being monitored."));
		}else{
			MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
			if(victim == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Player not found!");
				return false;
			}
			boolean monitored = MapleLogger.monitored.contains(victim.getName());
			if(monitored){
				MapleLogger.monitored.remove(victim.getName());
			}else{
				MapleLogger.monitored.add(victim.getName());
			}
			c.getPlayer().dropMessage(5, victim.getName() + " is " + (!monitored ? "now being monitored." : "no longer being monitored."));
		}
		return false;
	}
}
