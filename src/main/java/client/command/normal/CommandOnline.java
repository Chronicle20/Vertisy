package client.command.normal;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import net.server.channel.Channel;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 11, 2016
 */
public class CommandOnline extends Command{

	public CommandOnline(){
		super("Online", "Get a list of online players.", "!Online", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(c.getPlayer().isGM()){
			for(Channel ch : ChannelServer.getInstance().getChannels()){
				c.getPlayer().yellowMessage("Players in Channel " + ch.getId() + ":");
				for(MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()){
					if(chr.getGMLevel() <= c.getPlayer().getGMLevel()){
						c.getPlayer().message(" >> " + MapleCharacter.makeMapleReadable(chr.getName()) + " is at " + chr.getMap().getMapData().getMapName() + ".");
					}
				}
			}
			c.getPlayer().yellowMessage("Players in CashShop:");
			for(MapleCharacter chr : ChannelServer.getInstance().getMTSCharacters()){
				if(chr.getGMLevel() <= c.getPlayer().getGMLevel()){
					c.getPlayer().message(" >> " + MapleCharacter.makeMapleReadable(chr.getName()));
				}
			}
			c.getPlayer().yellowMessage("Players in TempStorage:");
			for(MapleCharacter chr : ChannelServer.getInstance().getTempStorage().getAllCharacters()){
				if(chr.getGMLevel() <= c.getPlayer().getGMLevel()){
					c.getPlayer().message(" >> " + MapleCharacter.makeMapleReadable(chr.getName()));
				}
			}
		}
		int[] players = new int[1];
		ChannelServer.getInstance().getChannels().forEach(ch-> players[0] += ch.getPlayerStorage().getSize());
		players[0] += ChannelServer.getInstance().getMTSCharacters().size();
		players[0] += ChannelServer.getInstance().getTempStorage().getSize();
		StringBuilder chs = new StringBuilder();
		for(int chid : ChannelServer.getInstance().getChannelIDs()){
			chs.append((chid + 1) + ", ");
		}
		if(chs.toString().contains(",")) chs.setLength(chs.length() - ", ".length());
		c.getPlayer().message("Players Online in channels " + chs.toString() + " : " + players[0]);
		return false;
	}
}
