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

import client.MapleClient;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldInfo;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CharlistRequestHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		c.sendServerList(false);
		slea.readByte();
		int world = slea.readByte();
		c.setWorld(world);
		c.setChannel(slea.readByte());
		WorldInfo worldInfo = WorldConstants.WorldInfo.values()[world];
		if(c.getGMLevel() > 0 || worldInfo.isSelectable()){
			c.sendCharList(world);
		}else{
			c.announce(MaplePacketCreator.serverNotice(1, ServerConstants.WORLD_NAMES[worldInfo.ordinal()] + " is currently unavailable."));
		}
		slea.readInt();// lan ip
	}
}