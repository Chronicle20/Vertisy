package client.command.normal;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.RSSkill;
import client.command.Command;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 18, 2016
 */
public class CommandTrack extends Command{

	public CommandTrack(){
		super("Track", "Track your skill progress easily.", "@Track <skill>", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			RSSkill tracking = null;
			for(RSSkill skill : RSSkill.values()){
				if(skill.name().equalsIgnoreCase(args[0])){
					tracking = skill;
				}
			}
			if(tracking != null){
				if(c.getPlayer().isTracking(tracking)){
					c.getPlayer().stopTracking(tracking);
					c.getPlayer().dropMessage(MessageType.MAPLETIP, "No longer tracking " + tracking.name());
				}else{
					c.getPlayer().startTracking(tracking);
					c.getPlayer().dropMessage(MessageType.MAPLETIP, "Now tracking " + tracking.name());
				}
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "Invalid skill: " + args[0]);
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
