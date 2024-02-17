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

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.maps.FieldLimit;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class EnterMTSHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		try{
			if(!FeatureSettings.MTS){
				/*if (!FieldLimit.CANNOTVIPROCK.check(c.getPlayer().getMap().getFieldLimit())) {
				c.getPlayer().saveLocation("FREE_MARKET");
				c.getPlayer().changeMap(910000000);
				} else {
				c.getPlayer().dropMessage(1, "You can not enter the free market from this location.");
				}*/
				c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.MTS_DISABLED);
				return;
			}
			if(!chr.isAlive() || chr.getHiredMerchant() != null || chr.getTrade() != null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to open MTS with certain UIs open");
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(chr.getLevel() < 10){
				c.announce(MaplePacketCreator.blockedMessage2(5));
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(chr.getCashShop().isOpened()) return;
			if(chr.getEventInstance() != null) return;
			if(FieldLimit.CHANGECHANNEL.check(chr.getMap().getMapData().getFieldType())) return;
			ChannelServer.getInstance().getWorldInterface().addBuffsToStorage(chr.getId(), chr.getAllBuffs());
			chr.cancelAllBuffs();
			chr.cancelExpirationTask();
			if(chr.getBuffedValue(MapleBuffStat.PUPPET) != null){
				chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
			}
			if(chr.getBuffedValue(MapleBuffStat.COMBO) != null){
				chr.cancelEffectFromBuffStat(MapleBuffStat.COMBO);
			}
			c.announce(MaplePacketCreator.openITC(c));
			chr.getCashShop().open(2);
			chr.getMap().removePlayer(chr);
			c.getChannelServer().removePlayer(chr);
			ChannelServer.getInstance().addMTSPlayer(chr);
			c.getPlayer().changeTab(1);// meh
			c.getPlayer().changeType(0);
			c.getPlayer().changePage(0);
			c.getPlayer().setSearch(null);
			c.announce(MaplePacketCreator.enableCSUse());
			c.announce(MaplePacketCreator.MTSWantedListingOver(0, 0));
			c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
			c.announce(MTSHandler.getMTS(c.getPlayer(), 1, 0, 0));
			c.announce(MaplePacketCreator.transferInventory(MTSHandler.getTransfer(chr.getId())));
			c.announce(MaplePacketCreator.notYetSoldInv(MTSHandler.getNotYetSold(chr.getId())));
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return;
		}
	}
}