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

import java.util.Calendar;

import client.MapleClient;
import net.MaplePacketHandler;
import net.server.handlers.AutoRegister;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CLogin;

public final class LoginPasswordHandler implements MaplePacketHandler{

	@Override
	public boolean validateState(MapleClient c){
		return !c.isLoggedIn();
	}

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		String login = slea.readMapleAsciiString();
		String pwd = slea.readMapleAsciiString();
		c.setAccountName(login);
		c.sendServerList(false);
		int loginok = 0;
		if(AutoRegister.autoRegister){
			if(AutoRegister.getAccountExists(login)){
				loginok = c.login(login, pwd);
			}else{
				if(AutoRegister.createAccount(login, pwd, c.getSession().remoteAddress().toString())){
					loginok = c.login(login, pwd);
				}else{
					c.announce(MaplePacketCreator.getLoginFailed(5));
					return;
				}
			}
		}else{
			loginok = c.login(login, pwd);
		}
		if((c.hasBannedIP() || c.hasBannedMac())){
			Logger.log(LogType.INFO, LogFile.LOGIN_BAN, "Someone tried to login to the account " + login + " when the account has a banned ip or mac. New ip: " + c.getSession().remoteAddress().toString());
			c.announce(MaplePacketCreator.getLoginFailed(3));
			if(c.getBanReason() != null && c.getBanReason().length() > 0) c.announce(MaplePacketCreator.serverNotice(1, c.getBanReason()));
			return;
		}
		Calendar tempban = c.getTempBanCalendar();
		if(tempban != null){
			if(tempban.getTimeInMillis() > System.currentTimeMillis()){
				c.announce(MaplePacketCreator.getTempBan(tempban.getTimeInMillis(), c.getGReason()));
				if(c.getBanReason() != null && c.getBanReason().length() > 0) c.announce(MaplePacketCreator.serverNotice(1, c.getBanReason()));
				return;
			}
		}
		if(loginok == 3){
			c.announce(MaplePacketCreator.getPermBan(c.getGReason()));// crashes but idc :D
			if(c.getBanReason() != null && c.getBanReason().length() > 0) c.announce(MaplePacketCreator.serverNotice(1, c.getBanReason()));
			return;
		}else if(loginok != 0){
			c.announce(MaplePacketCreator.getLoginFailed(loginok));
			return;
		}
		if(c.finishLogin() == 0){
			login(c);
		}else{
			c.announce(MaplePacketCreator.getLoginFailed(7));
		}
	}

	private static void login(MapleClient c){
		c.announce(CLogin.getAuthSuccess(c));// why the fk did I do c.getAccountName()?
	}
}
