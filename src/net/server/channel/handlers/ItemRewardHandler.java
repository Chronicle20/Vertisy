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

import java.rmi.RemoteException;
import java.util.List;

import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.ItemInformationProvider;
import server.ItemInformationProvider.RewardItem;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserRemote;

/**
 * @author Jay Estrella/ Modified by kevintjuh93
 */
public final class ItemRewardHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){// if Updated, UseCashItem itemType == 553
		byte slot = (byte) slea.readShort();
		int itemId = slea.readInt(); // will load from xml I don't care.
		Item itemSel = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if(c.getPlayer().getInventory(MapleInventoryType.USE).countById(itemId) < 1){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Doesn't have reward item.");
			return;
		}
		if(itemSel == null || itemSel.getItemId() != itemId){
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Item is null or item ids don't match.");
			return;
		}
		ItemInformationProvider ii = ItemInformationProvider.getInstance();

		Pair<Integer, List<RewardItem>> rewards = ii.getItemData(itemId).rewardItems;
		if(rewards != null){
			for(RewardItem reward : rewards.getRight()){
				if(!c.getPlayer().canHoldItem(new Item(reward.itemid, reward.quantity))){
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					break;
				}
				if(Randomizer.nextInt(rewards.getLeft()) < reward.prob){// Is it even possible to get an item with prob 1?
					if(ItemConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP){
						final Item item = ii.getEquipById(reward.itemid);
						if(reward.period != -1){
							item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
						}
						MapleInventoryManipulator.addFromDrop(c, item, false);
					}else{
						MapleInventoryManipulator.addFromDrop(c, new Item(reward.itemid, reward.quantity), false);
					}
					MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
					if(reward.worldmsg != null){
						String msg = reward.worldmsg;
						msg.replaceAll("/name", c.getPlayer().getName());
						msg.replaceAll("/item", ii.getItemData(reward.itemid).name);
						try{
							ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(6, msg));
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						}
					}
					if(reward.effect != null){
						c.announce(UserLocal.UserEffect.showInfo(reward.effect));
						c.getPlayer().getMap().broadcastMessage(UserRemote.UserEffect.showInfo(c.getPlayer().getId(), reward.effect));
					}
					break;
				}
			}
		}else{
			if(ItemConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP){
				final Item item = ii.getEquipById(itemId);
				MapleInventoryManipulator.addFromDrop(c, item, false);
			}else{
				MapleInventoryManipulator.addFromDrop(c, new Item(itemId, (short) 1), false);
			}
		}
		c.announce(CWvsContext.enableActions());
	}
}
