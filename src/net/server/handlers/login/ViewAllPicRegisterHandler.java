package net.server.handlers.login;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import client.MapleClient;
import client.MessageType;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class ViewAllPicRegisterHandler extends AbstractMaplePacketHandler{ // Gey class name lol

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		slea.readByte();
		int charId = slea.readInt();
		c.setWorld(slea.readInt()); // world
		try{
			int channel = Randomizer.rand(0, LoginServer.getInstance().getCenterInterface().getChannelSize(c.getWorld()));
			c.setChannel(channel);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			return;
		}
		String mac = slea.readMapleAsciiString();
		slea.readMapleAsciiString();// ?? hwid?
		c.addMac(mac);
		boolean macBanned = c.hasBannedMac();
		boolean hwidBanned = c.hasBannedHWID();
		if(macBanned || hwidBanned){
			Logger.log(LogType.INFO, LogFile.LOGIN_BAN, null, c.getAccountName() + " tried to login with a banned mac, hwid, or machine id. Mac: %b, Hwid: %b, MachineID: %b", macBanned, hwidBanned);
			c.getSession().close();
			return;
		}
		String pic = slea.readMapleAsciiString();
		c.setPic(pic);
		c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
		try{
			String sock = LoginServer.getInstance().getCenterInterface().getIP(c.getWorld(), c.getChannel());
			if(sock != null){
				String[] socket = sock.split(":");
				c.announce(MaplePacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
			}else{
				c.announce(MaplePacketCreator.serverNotice(MessageType.POPUP.getValue(), ServerConstants.CENTER_SERVER_ERROR));
				c.announce(CWvsContext.enableActions());
			}
		}catch(UnknownHostException | RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
			c.announce(MaplePacketCreator.serverNotice(MessageType.POPUP.getValue(), ServerConstants.CENTER_SERVER_ERROR));
			c.announce(CWvsContext.enableActions());
		}
	}
}
