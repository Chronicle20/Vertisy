package client.command.gm;

import java.util.Arrays;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.events.gm.Event;
import server.events.gm.Events;
import tools.ObjectParser;
import tools.Randomizer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 26, 2017
 */
public class CommandStartEvent extends Command{

	public CommandStartEvent(){
		super("StartEvent", "", "!StartEvent <event>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		try{
			Events type = Events.valueOf(args[0].toUpperCase());
			if(type.getMaps().length > 1 && args.length == 1){
				c.getPlayer().dropMessage(MessageType.ERROR, "Please enter one of the maps: " + Arrays.toString(type.getMaps()));
				return false;
			}
			Integer mapid = ObjectParser.isInt(args[1]);
			if(mapid == null) mapid = type.getMaps()[Randomizer.nextInt(type.getMaps().length)];
			try{
				Event event = type.getEventClass().getConstructor(int.class, int.class).newInstance(c.getChannel(), (int) mapid);
				c.getChannelInstance().setEvent(event);
				c.getPlayer().dropMessage(MessageType.SYSTEM, "Set event to " + type.name() + " in channel: " + c.getChannel());
				c.getPlayer().dropMessage(MessageType.SYSTEM, "Type !stopevent to stop the event at any time.");
				event.start();
			}catch(Exception ex){
				c.getPlayer().dropMessage(MessageType.ERROR, "Failed to start the event.");
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				return false;
			}
		}catch(Exception ex){
			c.getPlayer().dropMessage(MessageType.ERROR, "Unknown event: " + args[0]);
			return false;
		}
		return false;
	}
}
