package client.command.intern;

import java.rmi.RemoteException;

import client.*;
import client.command.Command;
import constants.ServerConstants;
import net.channel.ChannelServer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 1, 2016
 */
public class CommandWarpHere extends Command{

	public CommandWarpHere(){
		super("WarpHere", "", "!WarpHere <target>", null);
		setGMLevel(PlayerGMRank.INTERN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		MapleCharacter player = c.getPlayer();
		if(args.length > 0){
			try{
				if(!ChannelServer.getInstance().getWorldInterface().changeMap(args[0], new CharacterLocation(player))){
					player.dropMessage(MessageType.ERROR, "Unknown player.");
				}
				/*if(location == null){
					player.dropMessage(MessageType.ERROR, "Unknown player.");
				}else{
					
					CommandWarp.changeMap(player, location.channel, location.instanceMap, location.mapid, location.position, location.eventManager, location.eventInstance);
				}*/
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				player.dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			}
			/*MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(args[0]);
			if(victim == null){// If victim isn't on current channel, loop all channels on current world.
				for(Channel ch : Server.getInstance().getChannelsFromWorld(c.getWorld())){
					victim = ch.getPlayerStorage().getCharacterByName(args[0]);
					if(victim != null){
						break;// We found the person, no need to continue the loop.
					}
				}
			}
			if(victim != null){
				if(victim.getEventInstance() != null){
					victim.getEventInstance().unregisterPlayer(victim);
				}
				// Attempt to join the warpers instance.
				if(player.getEventInstance() != null){
					if(player.getClient().getChannel() == victim.getClient().getChannel()){// just in case.. you never know...
						player.getEventInstance().registerPlayer(victim);
						victim.addPreviousMap(victim.getMapId());
						victim.changeMap(player.getEventInstance().getMapInstance(player.getMapId()), player.getMap().findClosestPortal(player.getPosition()));
					}else{
						player.dropMessage(MessageType.ERROR, "Target isn't on your channel, not able to warp into event instance.");
					}
				}else{// If victim isn't in an event instance, just warp them.
					victim.addPreviousMap(victim.getMapId());
					if(player.getMap().getInstanceID() != null){
						victim.changeMap(player.getMap(), player.getMap().findClosestPortal(player.getPosition()));
					}else{
						victim.changeMap(player.getMapId(), player.getMap().findClosestPortal(player.getPosition()));
					}
			
				}
				if(player.getClient().getChannel() != victim.getClient().getChannel()){// And then change channel if needed.
					victim.dropMessage(MessageType.ERROR, "Changing channel, please wait a moment.");
					victim.getClient().changeChannel(player.getClient().getChannel());
				}
			}else{
				player.dropMessage(MessageType.ERROR, "Unknown player.");
			}*/
		}else{
			player.dropMessage(MessageType.ERROR, getUsage());
		}
		return true;
	}
}