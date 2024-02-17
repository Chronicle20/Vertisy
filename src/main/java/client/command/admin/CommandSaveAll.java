package client.command.admin;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import net.server.channel.Channel;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 11, 2016
 */
public class CommandSaveAll extends Command{

	public CommandSaveAll(){
		super("SaveAll", "", "", null);
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		for(Channel ch : ChannelServer.getInstance().getChannels()){
			for(MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()){
				chr.saveToDB();
			}
		}
		c.getPlayer().message("All players saved.");
		return false;
	}
}
