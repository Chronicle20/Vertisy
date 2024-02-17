package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author kevintjuh93
 */
public class AdminChatHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!c.getPlayer().isGM()){// if ( (signed int)CWvsContext::GetAdminLevel((void *)v294) > 2 )
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use AdminChatHandler when not a gm");
			return;
		}
		byte mode = slea.readByte();
		// not saving slides...
		byte[] packet = MaplePacketCreator.serverNotice(slea.readByte(), slea.readMapleAsciiString());// maybe I should make a check for the slea.readByte()... but I just hope gm's don't fuck things up :)
		switch (mode){
			case 0:// /alertall, /noticeall, /slideall
				try{
					ChannelServer.getInstance().getWorldInterface().broadcastPacket(packet);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case 1:// /alertch, /noticech, /slidech
				c.getChannelServer().broadcastPacket(packet);
				break;
			case 2:// /alertm /alertmap, /noticem /noticemap, /slidem /slidemap
				c.getPlayer().getMap().broadcastMessage(packet);
				break;
		}
	}
}
