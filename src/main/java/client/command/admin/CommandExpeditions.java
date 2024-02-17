package client.command.admin;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import server.expeditions.MapleExpedition;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 24, 2017
 */
public class CommandExpeditions extends Command{

	public CommandExpeditions(){
		super("Expeditions", "", "", "expeds");
		setGMLevel(PlayerGMRank.ADMIN);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		for(Channel ch : ChannelServer.getInstance().getChannels()){
			if(ch.getExpeditions().size() == 0){
				c.getPlayer().yellowMessage("No Expeditions in Channel " + ch.getId());
				continue;
			}
			c.getPlayer().yellowMessage("Expeditions in Channel " + ch.getId());
			int id = 0;
			for(MapleExpedition exped : ch.getExpeditions()){
				id++;
				c.getPlayer().yellowMessage("> Expedition " + id);
				c.getPlayer().yellowMessage(">> Type: " + exped.getType().toString());
				c.getPlayer().yellowMessage(">> Status: " + (exped.isRegistering() ? "REGISTERING" : "UNDERWAY"));
				c.getPlayer().yellowMessage(">> Size: " + exped.getMembers().size());
				c.getPlayer().yellowMessage(">> Leader: " + exped.getLeader().getName());
				int memId = 2;
				for(MapleCharacter member : exped.getMembers()){
					if(exped.isLeader(member)){
						continue;
					}
					c.getPlayer().yellowMessage(">>> Member " + memId + ": " + member.getName());
					memId++;
				}
			}
		}
		return false;
	}
}
