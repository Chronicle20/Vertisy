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
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

/**
 * @author Matze
 */
public final class ChangeChannelHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		int channel = slea.readByte();
		c.getPlayer().getAutobanManager().setTimestamp(6, slea.readInt(), 2);
		if(c.getChannel() == channel){
			AutobanFactory.GENERAL.alert(c.getPlayer(), "CCing to same channel.");
			c.disconnect(false, false);
			return;
			// } else if (c.getChannel() == 6 && c.getPlayer().isDonor() == 0 && !c.getPlayer().isGM()) {
			// c.getPlayer().dropMessage(1, "You may not enter the donor channel.");
			// return;
		}else if(c.getPlayer().getCashShop().isOpened() || c.getPlayer().getMiniGame() != null || c.getPlayer().getPlayerShop() != null) return;
		if(!FeatureSettings.CC){
			c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.CC_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		c.changeChannel(channel);
	}
}