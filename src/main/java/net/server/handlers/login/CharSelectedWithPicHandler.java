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
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public class CharSelectedWithPicHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		String pic = slea.readMapleAsciiString();
		int charId = slea.readInt();
		String macs = slea.readMapleAsciiString();
		String hwid = slea.readMapleAsciiString();
		c.addMac(macs);
		c.addHwid(hwid);
		boolean macBanned = c.hasBannedMac();
		boolean hwidBanned = c.hasBannedHWID();
		if(macBanned || hwidBanned){
			Logger.log(LogType.INFO, LogFile.LOGIN_BAN, null, c.getAccountName() + " tried to login with a banned mac, hwid, or machine id. Mac: %b, Hwid: %b, MachineID: %b", macBanned, hwidBanned);
			c.getSession().close();
			return;
		}
		if(c.checkPic(pic)){
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
		}else{
			c.announce(MaplePacketCreator.wrongPic());
		}
	}
}
