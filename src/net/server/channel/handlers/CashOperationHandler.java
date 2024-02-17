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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleRing;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.*;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.cashshop.*;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CCashShop;
import tools.packets.CCashShop.CashItemReq;
import tools.packets.CWvsContext;

public final class CashOperationHandler extends AbstractMaplePacketHandler{

	private void addPurchase(CashItemData cItem){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO bestitems(sn, gender, category, purchases) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE purchases=purchases+" + cItem.nCount)){
			ps.setInt(1, cItem.sn);
			ps.setInt(2, cItem.nCommodityGender);
			ps.setInt(3, CashItemFactory.get_category_from_SN(cItem.sn));
			ps.setInt(4, cItem.nCount);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		CashItemFactory.addBestItem(cItem);
		LimitedGood good = CashItemFactory.getGoodFromSN(cItem.sn);
		if(good == null) return;
		good.nRemainCount -= cItem.nCount;
		try{
			ChannelServer.getInstance().getWorldInterface().updateLimitedGood(cItem.sn, good.nRemainCount);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE limitedgoods SET remainCount = ? WHERE id = ?")){
			ps.setInt(1, good.nRemainCount);
			ps.setInt(2, good.id);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){// http://i.imgur.com/NLSOIfi.png
		MapleCharacter chr = c.getPlayer();
		CashShop cs = chr.getCashShop();
		if(!cs.isOpened()){
			c.announce(CWvsContext.enableActions());
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to send CashOperation packet while not in CashShop");
			return;
		}
		final int action = slea.readByte();
		if(action == CashItemReq.Buy || action == CashItemReq.BuyPackage){
			slea.readByte();
			final int useNX = slea.readInt();
			final int snCS = slea.readInt();
			CashItemData cItem = CashItemFactory.getItem(snCS);
			if(cItem == null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to buy null item on sn " + snCS + " for " + useNX + " nx type");
				return;
			}
			if(!canBuy(cItem, cs.getCash(useNX))){
				c.getPlayer().getClient().announce(MaplePacketCreator.serverNotice(1, "You may not purchase this item."));
				c.announce(CCashShop.showCash(c.getPlayer()));
				return;
			}
			/*if(!cItem.bOnSale || cs.getCash(useNX) < cItem.nPrice){
				c.getPlayer().getClient().announce(MaplePacketCreator.serverNotice(1, "You may not purchase this item."));
				c.announce(MaplePacketCreator.showCash(c.getPlayer()));
				return;
			}*/
			if(action == CashItemReq.Buy){ // Item
				Item item = cItem.toItem();
				cs.addToInventory(item);
				c.announce(CCashShop.CashItemResult.showBoughtCashItem(item, c.getAccID()));
			}else{ // Package
				List<Item> cashPackage = CashItemFactory.getPackage(cItem.nItemid);
				for(Item item : cashPackage){
					cs.addToInventory(item);
				}
				c.announce(CCashShop.CashItemResult.showBoughtCashPackage(cashPackage, c.getAccID()));
			}
			Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem));
			cs.gainCash(useNX, -getAdjustedPrice(cItem));
			c.announce(CCashShop.showCash(chr));
			addPurchase(cItem);
		}else if(action == CashItemReq.Gift){// fuck genders
			// 47 21 42 01 [D9 A0 98 00] 00 [06 00 41 6C 66 72 65 64] [02 00 61 0A]
			slea.readInt(); // Birthday
			CashItemData cItem = CashItemFactory.getItem(slea.readInt());
			slea.readByte();
			Map<String, String> recipient = MapleCharacter.getCharacterFromDatabase(slea.readMapleAsciiString());
			String message = slea.readMapleAsciiString();
			if(!canBuy(cItem, cs.getCash(4))){
				c.getPlayer().getClient().announce(MaplePacketCreator.serverNotice(1, "You may not purchase this item."));
				c.announce(CCashShop.showCash(c.getPlayer()));
				return;
			}
			if(message.length() < 1 || message.length() > 73){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to gift with an invalid length message");
				return;
			}
			if(recipient == null){
				c.announce(CCashShop.CashItemResult.showCashShopMessage((byte) 0xA9));
				return;
			}else if(recipient.get("accountid").equals(String.valueOf(c.getAccID()))){
				c.announce(CCashShop.CashItemResult.showCashShopMessage((byte) 0xA8));
				return;
			}
			cs.gift(Integer.parseInt(recipient.get("id")), chr.getName(), message, cItem.sn);
			c.announce(CCashShop.CashItemResult.showGiftSucceed(recipient.get("name"), cItem));
			Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " gifted " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to " + recipient.get("name"));
			cs.gainCash(4, -getAdjustedPrice(cItem));
			c.announce(CCashShop.showCash(chr));
			try{
				chr.sendNote(recipient.get("name"), chr.getName() + " has sent you a gift! Go check out the Cash Shop.", (byte) 0); // fame or not
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
			MapleCharacter receiver = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient.get("name"));
			if(receiver != null){
				receiver.showNote();
			}
			addPurchase(cItem);
		}else if(action == CashItemReq.SetWish){ // Modify wish list
			cs.clearWishList();
			for(byte i = 0; i < 10; i++){
				int sn = slea.readInt();
				CashItemData cItem = CashItemFactory.getItem(sn);
				if(cItem != null && cItem.bOnSale && sn != 0){
					if(!canBuy(cItem)){
						c.getPlayer().getClient().announce(MaplePacketCreator.serverNotice(1, "You may not add this item to your wishlist."));
						c.announce(CCashShop.showCash(c.getPlayer()));
						return;
					}
					cs.addToWishList(sn);
				}
			}
			c.announce(CCashShop.CashItemResult.showWishList(chr, true));
		}else if(action == CCashShop.CashItemReq.IncSlotCount){ // Increase Inventory Slots
			/*slea.skip(1);
			int cash = slea.readInt();
			byte mode = slea.readByte();
			if(mode == 0){
				byte type = slea.readByte();
				if(cs.getCash(cash) < 4000)return; 
				if(chr.gainSlots(type, 4, false)){
					c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
					cs.gainCash(cash, -4000);
					c.announce(MaplePacketCreator.showCash(chr));
				}
			}else{
				CashModifiedData cItem = CashItemFactory.getItem(slea.readInt());
				int type = (cItem.nItemid - 9110000) / 1000;
				if(!canBuy(cItem, cs.getCash(cash)))return; 
				if(chr.gainSlots(type, 8, false)){
					c.announce(MaplePacketCreator.showBoughtInventorySlots(type, chr.getSlots(type)));
					cs.gainCash(cash, -cItem.nPrice);
					c.announce(MaplePacketCreator.showCash(chr));
				}
			}*/
			c.getPlayer().dropMessage(MessageType.POPUP, "Currently unavailable. Please try again another time.");
			c.announce(CWvsContext.enableActions());
			c.announce(CCashShop.showCash(c.getPlayer()));
			return;
		}else if(action == CCashShop.CashItemReq.IncTrunkCount){ // Increase Storage Slots
			slea.skip(1);
			int cash = slea.readInt();
			byte mode = slea.readByte();
			if(mode == 0){
				if(cs.getCash(cash) < 4000) return;
				if(chr.getStorage().gainSlots(4)){
					c.announce(CCashShop.CashItemResult.showBoughtStorageSlots(chr.getStorage().getSlots()));
					cs.gainCash(cash, -4000);
					c.announce(CCashShop.showCash(chr));
				}
			}else{
				CashItemData cItem = CashItemFactory.getItem(slea.readInt());
				if(!canBuy(cItem, cs.getCash(cash))) return;
				if(chr.getStorage().gainSlots(8)){
					c.announce(CCashShop.CashItemResult.showBoughtStorageSlots(chr.getStorage().getSlots()));
					Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to increase trunk slots");
					cs.gainCash(cash, -getAdjustedPrice(cItem));
					c.announce(CCashShop.showCash(chr));
				}
			}
		}else if(action == CCashShop.CashItemReq.IncCharSlotCount){ // Increase Character Slots
			slea.skip(1);
			int cash = slea.readInt();
			CashItemData cItem = CashItemFactory.getItem(slea.readInt());
			if(!canBuy(cItem, cs.getCash(cash))) return;
			if(c.gainCharacterSlot()){
				c.announce(CCashShop.CashItemResult.showBoughtCharacterSlot(c.getCharacterSlots()));
				Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + cItem.nItemid + " with sn " + cItem.sn + " for " + getAdjustedPrice(cItem) + " to increase character slots");
				cs.gainCash(cash, -getAdjustedPrice(cItem));
				c.announce(CCashShop.showCash(chr));
			}
		}else if(action == CCashShop.CashItemReq.MoveLtoS){ // Take from Cash Inventory
			Item item = cs.findByCashId(slea.readInt());
			if(item == null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an invalid item from Locker to Inventory");
				return;
			}
			if(chr.getInventory(ItemInformationProvider.getInstance().getInventoryType(item.getItemId())).addItem(item) != -1){
				cs.removeFromInventory(item);
				ItemFactory.updateItemOwner(chr, item, c.getPlayer().getCashShop().getFactory());
				c.announce(CCashShop.CashItemResult.takeFromCashInventory(item));
				if(item instanceof Equip){
					Equip equip = (Equip) item;
					if(equip.getRingId() >= 0){
						MapleRing ring = MapleRing.loadFromDb(equip.getRingId());
						if(ring.getItemId() > 1112012){
							chr.addFriendshipRing(ring);
						}else{
							chr.addCrushRing(ring);
						}
					}
				}
			}
		}else if(action == CCashShop.CashItemReq.MoveStoL){ // Put into Cash Inventory
			int cashId = slea.readInt();
			slea.skip(4);
			MapleInventory mi = chr.getInventory(MapleInventoryType.getByType(slea.readByte()));
			Item item = mi.findByCashId(cashId);
			if(item == null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an invalid item from Inventory to Locker");
				return;
			}
			ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
			if(!data.isCash){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to move an non-cash item from Inventory to Locker");
				return;
			}
			cs.addToInventory(item);
			mi.removeSlot(item.getPosition());
			ItemFactory.updateItemOwner(chr, item, c.getPlayer().getCashShop().getFactory());
			c.announce(CCashShop.CashItemResult.putIntoCashInventory(item, c.getAccID()));
		}else if(action == CashItemReq.Couple){ // crush ring (action 28)
			slea.readInt();// Birthday
			// if (checkBirthday(c, birthday)) { //We're using a default birthday, so why restrict rings to only people who know of it?
			int toCharge = slea.readInt();
			int SN = slea.readInt();
			String recipient = slea.readMapleAsciiString();
			String text = slea.readMapleAsciiString();
			CashItemData ring = CashItemFactory.getItem(SN);
			MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
			if(partner == null){
				chr.getClient().announce(MaplePacketCreator.serverNotice(1, "The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel."));
			}else{
				/*  if (partner.getGender() == chr.getGender()) {
				 chr.dropMessage("You and your partner are the same gender, please buy a friendship ring.");
				 return;
				 }*/
				if(ring.toItem() instanceof Equip){
					Equip item = (Equip) ring.toItem();
					int ringid = MapleRing.createRing(ring.nItemid, chr, partner);
					item.setRingId(ringid);
					cs.addToInventory(item);
					c.announce(CCashShop.CashItemResult.showBoughtCashItem(item, c.getAccID()));
					cs.gift(partner.getId(), chr.getName(), text, item.getOldSN(), (ringid + 1));
					Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + item.getItemId() + " with sn " + SN + " for " + getAdjustedPrice(ring) + ". Couple ring with " + recipient + " text: " + text);
					cs.gainCash(toCharge, -getAdjustedPrice(ring));
					chr.addCrushRing(MapleRing.loadFromDb(ringid));
					try{
						chr.sendNote(partner.getName(), text, (byte) 1);
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					}
					partner.showNote();
					addPurchase(ring);
				}
			}
			/* } else {
			 chr.dropMessage("The birthday you entered was incorrect.");
			 }*/
			c.announce(CCashShop.showCash(c.getPlayer()));
		}else if(action == CashItemReq.BuyNormal){ // everything is 1 meso...
			int itemId = CashItemFactory.getItem(slea.readInt()).nItemid;
			if(chr.getMeso() > 0){
				if(itemId == 4031180 || itemId == 4031192 || itemId == 4031191){
					chr.gainMeso(-1, false);
					MapleInventoryManipulator.addFromDrop(c, new Item(itemId, (short) 1), false);
					c.announce(CCashShop.CashItemResult.showBoughtQuestItem(itemId));
				}
			}
			c.announce(CCashShop.showCash(c.getPlayer()));
		}else if(action == CashItemReq.FriendShip){ // Friendship :3
			slea.readInt(); // Birthday
			// if (checkBirthday(c, birthday)) {
			int payment = slea.readByte();
			slea.skip(3); // 0s
			int snID = slea.readInt();
			CashItemData ring = CashItemFactory.getItem(snID);
			String sentTo = slea.readMapleAsciiString();
			int available = slea.readShort() - 1;
			String text = slea.readAsciiString(available);
			slea.readByte();
			MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(sentTo);
			if(partner == null){
				chr.dropMessage("The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel.");
			}else{
				// Need to check to make sure its actually an equip and the right SN...
				if(ring.toItem() instanceof Equip){
					Equip item = (Equip) ring.toItem();
					int ringid = MapleRing.createRing(ring.nItemid, chr, partner);
					item.setRingId(ringid);
					cs.addToInventory(item);
					c.announce(CCashShop.CashItemResult.showBoughtCashItem(item, c.getAccID()));
					cs.gift(partner.getId(), chr.getName(), text, item.getOldSN(), (ringid + 1));
					Logger.log(LogType.INFO, LogFile.BUY_CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " bought " + item.getItemId() + " with sn " + snID + " for " + getAdjustedPrice(ring) + ". Friendship ring with " + sentTo + " text: " + text);
					cs.gainCash(payment, -getAdjustedPrice(ring));
					chr.addFriendshipRing(MapleRing.loadFromDb(ringid));
					try{
						chr.sendNote(partner.getName(), text, (byte) 1);
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					}
					partner.showNote();
					addPurchase(ring);
				}
			}
			c.announce(CCashShop.showCash(c.getPlayer()));
		}
	}

	public static boolean canBuy(CashItemData item, int cash){
		LimitedGood lg = CashItemFactory.getGoodFromSN(item.sn);
		if(lg != null){
			if(lg.nRemainCount < item.nCount) return false;
			if(lg.getState() != 0) return false;
			int stockState = lg.getStockState(item);
			if(stockState == StockState.NoStock || stockState == StockState.NotAvailableTime) return false;
		}
		return item != null && item.bOnSale && getAdjustedPrice(item) <= cash && !blocked(item.nItemid);
	}

	private static int getAdjustedPrice(CashItemData item){
		double price = item.nPrice;
		for(CategoryDiscount cd : CashItemFactory.categoryDiscount){
			if(cd.aCategory == CashItemFactory.get_category_from_SN(item.sn)){
				if(cd.nCategorySub == CashItemFactory.get_categorysub_from_SN(item.sn)){
					price -= price * (cd.nDiscountRate / (double) 100);
				}
			}
		}
		return (int) price;
	}

	@Deprecated
	public static boolean canBuy(CashItemData item){
		return item != null && item.bOnSale && !blocked(item.nItemid);
	}

	public static boolean blocked(int id){
		if(id >= 5211000 && id <= 5211018 || id >= 5211037 && id <= 5211049) return true;// exp
		if(id >= 5360000 && id <= 5360042) return true;// drop
		switch (id){ // All 2x exp cards
			case 5211000:
			case 5211004:
			case 5211005:
			case 5211006:
			case 5211007:
			case 5211008:
			case 5211009:
			case 5211010:
			case 5211011:
			case 5211012:
			case 5211013:
			case 5211014:
			case 5211015:
			case 5211016:
			case 5211017:
			case 5211018:
			case 5211037:
			case 5211038:
			case 5211039:
			case 5211040:
			case 5211041:
			case 5211042:
			case 5211043:
			case 5211044:
			case 5211045:
			case 5211049:
			case 5220000:// Gachapon Ticket
			case 5220010:// slot machines, just incase.
			case 5220020:// Net cafe, just incase.
			case 5451000:// Remote Gachapon
			case 5431000:// Maple Life(A-Type)
			case 5432000:// Maple Life (B-Type)
			case 5510000:// Wheel of Destiny
			case 5130000:// Safety Charm
				return true;
			default:
				return false;
		}
	}
}
