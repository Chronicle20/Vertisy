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

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.reactors.MapleReactor;
import server.reactors.ReactorHitInfo;
import server.reactors.ReactorHitType;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Lerk
 */
public final class ReactorHitHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int oid = slea.readInt();
		/*int bSkill = */slea.readInt(); // FindHitReactor: 0, FindSkillReactor: 1
		int dwHitOption = slea.readInt();// bMoveAction & 1 | 2 * (m_pfh != 0), if on ground, left/right
		int bMoveAction = (dwHitOption & 1);
		int m_pfh = ((dwHitOption >> 1) & 1);
		/*int tActionDelay = */slea.readShort(); // tDelay
		int skillid = slea.readInt();
		ReactorHitInfo info = new ReactorHitInfo();
		info.skillid = skillid;
		info.bMoveAction = bMoveAction;
		info.m_pfh = m_pfh;
		MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
		if(reactor != null && reactor.isAlive()){
			reactor.hitReactor(ReactorHitType.HIT, info, c);
		}else Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, c.getPlayer().getName() + " is trying to hit a null, or dead reactor");
	}
}
