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

import java.util.Map;

import client.MapleClient;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldInfo;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CLogin;

public final class ServerlistRequestHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		// Server server = Server.getInstance();
		sendInfo(c);
		// c.announce(MaplePacketCreator.sendRecommended(server.worldRecommendedList()));
	}

	public static void sendInfo(MapleClient c){
		for(WorldInfo worldInfo : WorldConstants.WorldInfo.values()){
			if(worldInfo.isEnabled()){
				if(c != null && ((c.getGMLevel() > 0) || worldInfo.isSelectable())){
					Map<Integer, Integer> load = LoginServer.getInstance().getLoad(worldInfo.ordinal());
					if(load != null){
						c.sendServerList(false);// disable in charlist request handler?
						c.announce(CLogin.getServerList(worldInfo.ordinal(), ServerConstants.WORLD_NAMES[worldInfo.ordinal()], worldInfo.getFlag(), worldInfo.getEventMessage(), load));
					}else{
						c.sendServerList(true);
					}
				}
			}
		}
		c.announce(CLogin.getEndOfServerList());
		// sess.write(MaplePacketCreator.selectWorld(0));// too lazy to make a check lol
	}
}