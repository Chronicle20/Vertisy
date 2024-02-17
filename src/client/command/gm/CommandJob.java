package client.command.gm;

import client.*;
import client.command.Command;
import tools.ObjectParser;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandJob extends Command{

	public CommandJob(){
		super("Job", "Change your job", "!Job <jobid>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter target = c.getPlayer();
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		Integer jobid = null;
		if(args.length > 1){
			jobid = ObjectParser.isInt(args[1]);
			MapleCharacter victim = c.getChannelServer().getChannelServer().getCharacterByName(args[0]);
			if(victim == null){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
				return false;
			}else{
				target = victim;
			}
		}else{
			jobid = ObjectParser.isInt(args[0]);
		}
		if(jobid == null){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		target.changeJob(MapleJob.getById(jobid));
		target.equipChanged();
		return false;
	}
}
