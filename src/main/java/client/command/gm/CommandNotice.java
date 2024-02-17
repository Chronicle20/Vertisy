package client.command.gm;

import java.rmi.RemoteException;

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
public class CommandNotice extends Command{

	public CommandNotice(){
		super("Notice", "Broadcast a message", "!Notice <map, world, server/global> <message>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		String where = args[0];
		switch (where.toLowerCase()){
			case "map":
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 0)));
				break;
			case "world":
				// Integer worldid = ObjectParser.isInt(args[1]);
				// if(worldid != null) Server.getInstance().getWorld(worldid).broadcastMessage(MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 1)));
				// else Server.getInstance().getWorlds().forEach(w-> w.broadcastMessage(MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 0))));
				break;
			case "server":
			case "global":// TODO: Make it actually global
				try{
					ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 1)));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			default:
				try{
					ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(args, 0)));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
		}
		return false;
	}
}
