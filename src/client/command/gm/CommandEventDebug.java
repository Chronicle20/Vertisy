package client.command.gm;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import scripting.event.EventManager;
import scripting.event.EventScriptManager.EventEntry;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 1, 2017
 */
public class CommandEventDebug extends Command{

	public CommandEventDebug(){
		super("EventDebug", "", "!EventDebug", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(c.getPlayer().getEventInstance() != null){
			c.getPlayer().dropMessage(MessageType.ERROR, "Event Instance: " + c.getPlayer().getEventInstance().getName());
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, "Not currently in an event instance.");
		}
		for(EventEntry entry : c.getChannelInstance().getEventSM().getEventEntryList()){
			EventManager em = entry.em;
			if(em.getInstances().size() > 0){
				c.getPlayer().dropMessage(MessageType.ERROR, em.getName() + " has " + em.getInstances().size() + " instances.");
			}
		}
		return false;
	}
}
