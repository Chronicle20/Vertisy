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
package net.server.channel.handlers;

import java.awt.Point;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.field.userpool.UserCommon;

/**
 * @author BubblesDev
 */
public final class InnerPortalHandler extends AbstractMaplePacketHandler{

	/* 65 00 
	 * 00 
	 * 04 00 68 70 30 31 - hp01 - used portal name
	 * 87 FE - x
	 * 12 01 - y
	 * A9 16 - target x
	 * C6 01 - target y
	 */
	/* 65 00 
	 * 00 
	 * 06 00 68 70 30 31 5F 31 
	 * A9 16 
	 * C6 01 
	 * 87 FE 
	 * 11 01
	 */
	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		slea.readByte();
		String portalName = slea.readMapleAsciiString();
		Point portalPos = slea.readPos();
		Point targetPos = slea.readPos();
		if(c.getPlayer().getMap().getPortal(portalName) == null){
			AutobanFactory.WZ_EDIT.alert(c.getPlayer(), "Used inner portal: " + portalName + " in " + c.getPlayer().getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
			return;
		}
		boolean foundPortal = false;
		for(MaplePortal portal : c.getPlayer().getMap().getPortals()){
			if(portal.getType() == 1 || portal.getType() == 2 || portal.getType() == 10 || portal.getType() == 20){
				if(portal.getPosition().equals(portalPos) || portal.getPosition().equals(targetPos)) foundPortal = true;
			}
		}
		if(!foundPortal){
			AutobanFactory.WZ_EDIT.alert(c.getPlayer(), "Used inner portal: " + portalName + " in " + c.getPlayer().getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
		}
		if(c.getPlayer().passenger != -1){
			c.announce(UserCommon.followCharacter(c.getPlayer().passenger, c.getPlayer().getId()));
		}
	}
}