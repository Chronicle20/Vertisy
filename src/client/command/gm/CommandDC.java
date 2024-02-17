package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import server.maps.MapleMap;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandDC extends Command{

	public CommandDC(){
		super("DC", "Disconnect a specific player", "!DC <target>", "dci,dcm");
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length == 0){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		MapleCharacter victim = ChannelServer.getInstance().getCharacterByName(args[0]);
		if(victim == null){
			victim = ChannelServer.getInstance().getMTSCharacterByName(args[0]);
			if(victim != null){
				if(commandLabel.equalsIgnoreCase("dc")){
					c.getPlayer().dropMessage(MessageType.ERROR, "The player is in the MTS/CS. Use the bypass !dci to remove them.");
					return false;
				}else{
					victim.getClient().disconnectFully();
					ChannelServer.getInstance().removeMTSPlayer(victim.getId());
					return false;
				}
			}
			victim = ChannelServer.getInstance().getPlayerFromTempStorage(args[0]);
			if(victim != null){
				if(commandLabel.equalsIgnoreCase("dc")){
					c.getPlayer().dropMessage(MessageType.ERROR, "The player is in the Temp Storage. Use the bypass !dci to remove them.");
					return false;
				}else{
					victim.getClient().disconnectFully();
					ChannelServer.getInstance().removePlayerFromTempStorage(victim.getId());
					return false;
				}
			}
			if(commandLabel.equalsIgnoreCase("dc")){
				c.getPlayer().dropMessage(MessageType.ERROR, "Unable to find player " + args[0] + " If you would like to dc any player under that name stuck in a map use the command !dcm");
				return false;
			}else if(commandLabel.equalsIgnoreCase("dcm")){
				for(Channel ch : ChannelServer.getInstance().getChannels()){
					for(MapleMap map : ch.getMaps().values()){
						for(MapleCharacter mc : map.getAllPlayers()){
							if(mc.getName().equalsIgnoreCase(args[0])){
								map.removePlayer(mc);
								mc.getClient().disconnectFully();
								c.getPlayer().dropMessage(MessageType.SYSTEM, "Removed a player under the name " + args[0] + " in map: " + map.getId());
								return false;
							}
						}
					}
					MapleCharacter target = ch.getPlayerStorage().getCharacterByName(args[0]);
					if(target != null){
						target.getClient().disconnectFully();
						ch.getPlayerStorage().removePlayer(target.getId());
						c.getPlayer().dropMessage(MessageType.SYSTEM, "Removed a player under the name " + args[0]);
						return false;
					}
				}
				c.getPlayer().dropMessage(MessageType.SYSTEM, "Failed to find a player under the name " + args[0] + " in a map.");
				return false;
			}
		}
		if(c.getPlayer().getGMLevel() < victim.getGMLevel()) return false;
		victim.getClient().disconnect(false, false);
		return false;
	}
}
