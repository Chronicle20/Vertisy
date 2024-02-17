/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

public final class CharSelectedHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int charId = slea.readInt();
		String macs = slea.readMapleAsciiString();
		c.addMac(macs);
		boolean macBanned = c.hasBannedMac();
		boolean hwidBanned = c.hasBannedHWID();
		if(macBanned || hwidBanned){
			Logger.log(LogType.INFO, LogFile.LOGIN_BAN, null, c.getAccountName() + " tried to login with a banned mac, hwid, or machine id. Mac: %b, Hwid: %b, MachineID: %b", macBanned, hwidBanned);
			c.getSession().close();
			return;
		}
		if(ServerConstants.ENABLE_PIC){
			Logger.log(LogType.INFO, LogFile.LOGIN_BAN, c.getAccountName() + " tried to login without using a pic when pic is enabled.");
			c.getSession().close();
			return;
		}
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