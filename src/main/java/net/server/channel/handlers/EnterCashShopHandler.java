/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.channel.handlers;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.maps.FieldLimit;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CCashShop;
import tools.packets.CWvsContext;

/**
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		try{
			MapleCharacter mc = c.getPlayer();
			if(!mc.isAlive() || mc.getHiredMerchant() != null || mc.getTrade() != null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to open cashshop with certain UIs open");
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(mc.getCashShop().isOpened()) return;
			if(mc.getEventInstance() != null) return;
			if(FieldLimit.CHANGECHANNEL.check(mc.getMap().getMapData().getFieldType())) return;
			ChannelServer.getInstance().getWorldInterface().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
			mc.cancelAllBuffs();
			mc.cancelExpirationTask();
			if(mc.getBuffedValue(MapleBuffStat.PUPPET) != null){
				mc.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
			}
			if(mc.getBuffedValue(MapleBuffStat.COMBO) != null){
				mc.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
			}
			c.announce(CCashShop.openCashShop(c));
			mc.getCashShop().open(1);
			mc.getMap().removePlayer(mc);
			c.getChannelServer().removePlayer(mc);
			ChannelServer.getInstance().addMTSPlayer(mc);
			c.announce(CCashShop.CashItemResult.showCashInventory(c));
			c.announce(CCashShop.CashItemResult.showGifts(mc.getCashShop().loadGifts()));
			c.announce(CCashShop.CashItemResult.showWishList(mc, false));
			c.announce(CCashShop.showCash(mc));
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}
}
