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

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.FamilyPackets;
import tools.packets.FamilyPackets.FamilyRes;

/**
 * @author Jay Estrella
 */
public final class FamilyAddHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!FeatureSettings.FAMILY){
			c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.FAMILY_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		System.out.println("Family add:" + slea.toString());
		String toAdd = slea.readMapleAsciiString();
		MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
		if(addChr != null){
			if((c.getPlayer().getFamilyId() != -1 && addChr.getFamilyId() != -1) && addChr.getFamilyId() == c.getPlayer().getFamilyId()){
				c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_SameFamily, 0));
				return;
			}
			if(addChr.getMapId() != c.getPlayer().getMapId()){
				c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_NotSameMap, 0));
				return;
			}
			if(addChr.getLevel() < 10){
				c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_MinLevel, 0));
				return;
			}
			if(addChr.getLevel() > c.getPlayer().getLevel()){
				c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_UnderLevel, 0));
				return;
			}
			if(c.getPlayer().getLevel() - addChr.getLevel() > 20){
				c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_OverLevel, 0));
				return;
			}
			addChr.getClient().announce(FamilyPackets.sendFamilyInvite(c.getPlayer().getId(), c.getPlayer().getName()));
			c.getPlayer().dropMessage("The invite has been sent.");
		}else{
			c.announce(FamilyPackets.sendFamilyMessage(FamilyRes.FamilyRes_Fail_WrongName, 0));
		}
		c.announce(CWvsContext.enableActions());
	}
}
