package client.command.intern;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.UUID;

import client.*;
import client.command.Command;
import constants.ServerConstants;
import net.channel.ChannelServer;
import scripting.event.EventInstanceManager;
import server.maps.MapleMap;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 1, 2016
 */
public class CommandWarp extends Command{

	public CommandWarp(){
		super("Warp", "", "!Warp <target>", "warpto");
		setGMLevel(PlayerGMRank.INTERN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter player = c.getPlayer();
		if(args.length > 0){
			try{
				CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(args[0]);
				if(location == null){
					player.dropMessage(MessageType.ERROR, "Unknown player.");
				}else{
					changeMap(player, location.channel, location.instanceMap, location.mapid, location.position, location.eventManager, location.eventInstance);
				}
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			}
		}else{
			player.dropMessage(MessageType.ERROR, getUsage());
		}
		return true;
	}

	public static void changeMap(MapleCharacter player, int channel, UUID instanceID, int mapid, Point position, String eventManager, String eventInstance){
		if(player.getEventInstance() != null){
			player.getEventInstance().unregisterPlayer(player);
		}
		if(player.getClient().getChannel() != channel){
			player.getClient().changeChannel(channel);
		}else{
			// Attempt to join the victims warp instance.
			if(eventInstance != null){
				EventInstanceManager eim = player.getClient().getChannelServer().getEventSM().getEventManager(eventManager).getInstance(eventInstance);
				eim.registerPlayer(player);
				player.addPreviousMap(player.getMapId());
				player.changeMap(eim.getMapInstance(mapid), eim.getMapInstance(mapid).findClosestPortal(position));
			}else{// If victim isn't in an event instance, just warp them.
				player.addPreviousMap(player.getMapId());
				MapleMap target = player.getClient().getChannelServer().getMap(mapid);
				player.changeMap(target.getId(), target.findClosestPortal(position));
			}
		}
	}
}