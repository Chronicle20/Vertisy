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
import client.autoban.AutobanFactory;
import client.inventory.*;
import constants.FeatureSettings;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.MapleStorage;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @author Matze
 */
public final class StorageHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		byte mode = slea.readByte();
		if(chr.isIronMan()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		if(!FeatureSettings.STORAGE){
			chr.dropMessage(MessageType.POPUP, FeatureSettings.STORAGE_DISABLED);
			c.announce(CWvsContext.enableActions());
			return;
		}
		final MapleStorage storage = chr.getStorage();
		if(mode == 4){ // take out
			byte type = slea.readByte();
			byte slot = slea.readByte();
			if(slot < 0 || slot > storage.getSlots()){ // removal starts at zero
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
				c.disconnect(true, false);
				return;
			}
			slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
			Item item = storage.getItem(slot);
			if(item != null){
				ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
				if(data.pickupRestricted && chr.getItemQuantity(item.getItemId(), true) > 0){
					c.announce(MaplePacketCreator.getStorageError((byte) 0x0C));
					return;
				}
				if(chr.getMap().getId() == 910000000){
					if(chr.getMeso() < 1000){
						c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
						return;
					}else{
						chr.gainMeso(-1000, false);
					}
				}
				if(c.getPlayer().canHoldItem(item)){
					item = storage.takeOut(slot);// actually the same but idc
					Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + " took out " + item.getQuantity() + " " + data.name + " (" + item.getItemId() + ")");
					if((item.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA){
						item.setFlag((byte) (item.getFlag() ^ ItemConstants.KARMA)); // items with scissors of karma used on them are reset once traded
					}else if(item.getType() == 2 && (item.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES){
						item.setFlag((byte) (item.getFlag() ^ ItemConstants.SPIKES));
					}
					MapleInventoryManipulator.addFromDrop(c, item, false);
					ItemFactory.updateItemOwner(c.getPlayer(), item, ItemFactory.INVENTORY);
					storage.sendTakenOut(c, ii.getInventoryType(item.getItemId()));
				}else{
					c.announce(MaplePacketCreator.getStorageError((byte) 0x0A));
				}
			}
		}else if(mode == 5){ // store
			short slot = slea.readShort();
			int itemId = slea.readInt();
			short quantity = slea.readShort();
			MapleInventoryType slotType = ii.getInventoryType(itemId);
			MapleInventory Inv = chr.getInventory(slotType);
			if(slot < 1 || slot > Inv.getSlotLimit()){ // player inv starts at one
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
				c.disconnect(true, false);
				return;
			}
			if(quantity < 1 || chr.getItemQuantity(itemId, false) < quantity) return;
			MapleInventoryType type = ii.getInventoryType(itemId);
			Item itemOrg = chr.getInventory(type).getItem(slot);
			if(itemOrg != null){
				if(!storage.hasRoom(c, itemOrg)){
					c.announce(MaplePacketCreator.getStorageError((byte) 0x11));
					return;
				}
			}
			short meso = (short) (chr.getMap().getId() == 910000000 ? -500 : -100);
			if(chr.getMeso() < meso){
				c.announce(MaplePacketCreator.getStorageError((byte) 0x0B));
			}else if(itemOrg != null){
				Item item = itemOrg.copy();
				if(item.getItemId() == itemId && (item.getQuantity() >= quantity || ItemConstants.isRechargable(itemId))){
					if(ItemConstants.isRechargable(itemId)){
						quantity = item.getQuantity();
					}
					chr.gainMeso(meso, false, true, false);
					item.setQuantity(quantity);
					if(item.getQuantity() == itemOrg.getQuantity()){
						item.nSN = itemOrg.nSN;
						ItemFactory.updateItemOwner(c.getPlayer(), item, ItemFactory.STORAGE);
					}else itemOrg.addDBFlag(ItemDB.UPDATE);
					MapleInventoryManipulator.removeItem(c, type, slot, quantity, false, false);
					storage.store(c, item);
					storage.sendStored(c, ii.getInventoryType(itemId));
					String itemName = ItemInformationProvider.getInstance().getItemData(item.getItemId()).name;
					Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
				}
			}
		}else if(mode == 7){ // meso
			int meso = slea.readInt();
			int storageMesos = storage.getMeso();
			int playerMesos = chr.getMeso();
			if((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)){
				if(meso < 0 && (storageMesos - meso) < 0){
					meso = -2147483648 + storageMesos;
					if(meso < playerMesos) return;
				}else if(meso > 0 && (playerMesos + meso) < 0){
					meso = 2147483647 - playerMesos;
					if(meso > storageMesos) return;
				}
				storage.setMeso(storageMesos - meso);
				chr.gainMeso(meso, false, true, false);
				Logger.log(LogType.INFO, LogFile.STORAGE, c.getAccountName(), c.getPlayer().getName() + (meso > 0 ? " took out " : " stored ") + Math.abs(meso) + " mesos\r\n");
			}else{
				return;
			}
			storage.sendMeso(c);
		}else if(mode == 8){// close
			storage.close();
		}
	}
}