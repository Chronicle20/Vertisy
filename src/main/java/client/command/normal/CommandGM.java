package client.command.normal;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandGM extends Command{

	public CommandGM(){
		super("GM", "", "", null);
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length < 2){
			c.getPlayer().dropMessage(5, "Your message was too short. Please provide as much detail as possible.");
			return false;
		}
		String message = StringUtil.joinStringFrom(args, 0);
		try{
			ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(MaplePacketCreator.sendYellowTip("[GM MESSAGE]:" + MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + ": " + message));
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		Logger.log(LogType.INFO, LogFile.GM_CALL, MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + ": " + message);
		c.getPlayer().dropMessage(5, "Your message '" + message + "' was sent to the GMs.");
		return false;
	}
}
