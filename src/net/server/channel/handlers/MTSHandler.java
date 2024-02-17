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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import server.MTSItemInfo;
import server.MapleInventoryManipulator;
import tools.*;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public final class MTSHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!c.getPlayer().getCashShop().isOpened()) return;
		// System.out.println(slea.toString());
		if(slea.available() > 0){
			byte op = slea.readByte();
			if(op == 2){ // put item up for sale
				byte itemtype = slea.readByte();
				int itemid = slea.readInt();
				slea.readShort();
				slea.skip(7);
				short stars = 1;
				if(itemtype == 1){
					slea.skip(32);
				}else{
					stars = slea.readShort();
				}
				slea.readMapleAsciiString(); // another useless thing (owner)
				if(itemtype == 1){
					slea.skip(32);
				}else{
					slea.readShort();
				}
				short slot;
				short quantity;
				if(itemtype != 1){
					if(itemid / 10000 == 207 || itemid / 10000 == 233){
						slea.skip(8);
					}
					slot = (short) slea.readInt();
				}else{
					slot = (short) slea.readInt();
				}
				if(itemtype != 1){
					if(itemid / 10000 == 207 || itemid / 10000 == 233){
						quantity = stars;
						slea.skip(4);
					}else{
						quantity = (short) slea.readInt();
					}
				}else{
					quantity = (byte) slea.readInt();
				}
				int price = slea.readInt();
				if(itemtype == 1){
					quantity = 1;
				}
				if(quantity < 0 || price < 110 || c.getPlayer().getItemQuantity(itemid, false) < quantity) return;
				MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(itemid);
				Item i = c.getPlayer().getInventory(type).getItem(slot).copy();
				if(i != null && c.getPlayer().getMeso() >= 5000){
					Connection con = DatabaseConnection.getConnection();
					try{
						/*try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE seller = ?")){
							ps.setInt(1, c.getPlayer().getId());
							try(ResultSet rs = ps.executeQuery()){
								if(rs.next()){
									if(rs.getInt(1) > 10){ // They have more than 10 items up for sale already!
										c.getPlayer().dropMessage(1, "You already have 10 items up for sale already!");
										c.announce(getMTS(c.getPlayer(), 1, 0, 0));
										c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
										c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
										return;
									}
								}
							}
						}*/
						if(i.getType() == 2){
							Item item = (Item) i;
							try(PreparedStatement ps = con.prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, seller, price, owner, sellername, sell_ends) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")){
								ps.setInt(1, 1);
								ps.setInt(2, (int) type.getType());
								ps.setInt(3, item.getItemId());
								ps.setInt(4, quantity);
								ps.setInt(5, c.getPlayer().getId());
								ps.setInt(6, price);
								ps.setString(7, item.getOwner());
								ps.setString(8, c.getPlayer().getName());
								ps.setLong(9, addTime(Calendar.getInstance().getTimeInMillis(), 7, 0));
								ps.executeUpdate();
							}
						}else{
							Equip equip = (Equip) i;
							int pos = 0;// 31
							try(PreparedStatement ps = con.prepareStatement("INSERT INTO mts_items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
								ps.setInt(++pos, 1);
								ps.setByte(++pos, type.getType());
								ps.setInt(++pos, equip.getItemId());
								ps.setInt(++pos, quantity);
								ps.setInt(++pos, c.getPlayer().getId());
								ps.setInt(++pos, price);
								ps.setInt(++pos, 0);
								ps.setInt(++pos, 0);
								ps.setInt(++pos, equip.getUpgradeSlots());
								ps.setInt(++pos, equip.getLevel());
								ps.setInt(++pos, equip.getStr());
								ps.setInt(++pos, equip.getDex());
								ps.setInt(++pos, equip.getInt());
								ps.setInt(++pos, equip.getLuk());
								ps.setInt(++pos, equip.getHp());
								ps.setInt(++pos, equip.getMp());
								ps.setInt(++pos, equip.getWatk());
								ps.setInt(++pos, equip.getMatk());
								ps.setInt(++pos, equip.getWdef());
								ps.setInt(++pos, equip.getMdef());
								ps.setInt(++pos, equip.getAcc());
								ps.setInt(++pos, equip.getAvoid());
								ps.setInt(++pos, equip.getHands());
								ps.setInt(++pos, equip.getSpeed());
								ps.setInt(++pos, equip.getJump());
								ps.setInt(++pos, equip.getVicious());
								ps.setInt(++pos, equip.getFlag());
								ps.setString(++pos, equip.getOwner());
								ps.setString(++pos, c.getPlayer().getName());
								ps.setLong(++pos, addTime(Calendar.getInstance().getTimeInMillis(), 7, 0));
								ps.setInt(++pos, 0);// transfer
								ps.setShort(++pos, (short) 0);
								ps.setInt(++pos, -1);
								ps.setInt(++pos, -1);
								ps.setString(++pos, "");
								ps.setInt(++pos, 1);
								ps.setFloat(++pos, equip.getItemExp());
								ps.setByte(++pos, equip.getItemLevel());
								ps.setLong(++pos, equip.getLockExpiration());
								ps.setInt(++pos, equip.getRingId());
								ps.setBoolean(++pos, equip.hasLearnedSkills());
								ps.setString(++pos, "");
								ps.executeUpdate();
							}
						}
						MapleInventoryManipulator.removeItem(c, type, slot, quantity, true, false);
						c.getPlayer().gainMeso(-GameConstants.nRegisterFeeMeso, false);
						c.announce(MaplePacketCreator.MTSConfirmSell());
						c.announce(getMTS(c.getPlayer(), 1, 0, 0));
						c.announce(MaplePacketCreator.enableCSUse());
						c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
						c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error saving item.");
					}
				}
			}else if(op == 3){ // send offer for wanted item
				// 04 - inv type
				// 01 - slot
				// 00 00 00 02
				// 13 09 3D 00 - itemid
				// 00 00 80 05 BB 46 E6 17 02 64 00
				// 05 00 41 72 6E 61 68 - seller name
				// 00 00
				// 0C 00 00 00 - mts_items id
				/*byte inv = slea.readByte();
				byte slot = slea.readByte();
				slea.readInt();
				int itemid = slea.readInt();
				slea.skip(11);
				slea.readMapleAsciiString();// seller name
				slea.readShort();
				int id = slea.readInt();*/
				c.announce(MaplePacketCreator.enableCSUse());
			}else if(op == 4){ // list wanted item
				int itemid = slea.readInt();
				int price = slea.readInt();// listing price(price + tax)
				int quantity = slea.readInt();//
				slea.readShort();
				String note = slea.readMapleAsciiString();
				MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(itemid);
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO mts_items(tab, type, itemid, quantity, seller, sellername, price, note, sell_ends)  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")){
					ps.setInt(1, 2);
					ps.setInt(2, type.getType());
					ps.setInt(3, itemid);
					ps.setInt(4, quantity);
					ps.setInt(5, c.getPlayer().getId());
					ps.setString(6, c.getPlayer().getName());
					ps.setInt(7, price);
					ps.setString(8, note);
					ps.setLong(9, addTime(Calendar.getInstance().getTimeInMillis(), 7, 0));
					ps.executeUpdate();
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error saving wanted item.");
				}
				try{
					c.announce(getMTS(c.getPlayer(), 2, 0, 0));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
				}
				c.announce(MaplePacketCreator.enableCSUse());
			}else if(op == 5){ // change page
				int tab = slea.readInt();
				int type = slea.readInt();
				int page = slea.readInt();
				byte sortType = slea.readByte();
				byte sortColumn = slea.readByte();
				int searchOption = slea.readInt();
				String searchCondition = slea.readMapleAsciiString();
				c.getPlayer().changePage(page);
				if(tab == 4 && type == 0){// cart
					try{
						c.announce(getCart(c.getPlayer(), c.getPlayer().getId()));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error getting cart.");
						return;
					}
				}else if(tab == 4 && type == 2){
					try{
						c.announce(getHistory(c.getPlayer(), page));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error grabbing history.");
						return;
					}
				}else if(tab == 4 && type == 3){
					try{
						c.announce(getAuctionItems(c.getPlayer(), c.getPlayer().getId(), page));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error getting auction items.");
						return;
					}
				}else if(tab == c.getPlayer().getCurrentTab() && type == c.getPlayer().getCurrentType() && c.getPlayer().getSearch() != null){
					try{
						c.getPlayer().changeCI(searchOption);
						c.getPlayer().setSearch(searchCondition);
						c.announce(getMTSSearch(c.getPlayer(), tab, type, searchOption, searchCondition, page));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving search data.");
						return;
					}
				}else{
					try{
						c.getPlayer().setSearch(null);
						c.announce(getMTS(c.getPlayer(), tab, type, page));
					}catch(SQLException e){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
						c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
						return;
					}
				}
				c.getPlayer().changeTab(tab);
				c.getPlayer().changeType(type);
				c.getPlayer().changeSortType(sortType);
				c.getPlayer().changeSortColumn(sortColumn);
				c.announce(MaplePacketCreator.enableCSUse());
				try{
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 6){ // search
				int tab = slea.readInt();
				int type = slea.readInt();
				slea.readInt();
				int searchOption = slea.readInt();
				String search = slea.readMapleAsciiString();
				c.getPlayer().setSearch(search);
				c.getPlayer().changeTab(tab);
				c.getPlayer().changeType(type);
				c.getPlayer().changeCI(searchOption);
				c.announce(MaplePacketCreator.enableCSUse());
				c.announce(CWvsContext.enableActions());
				try{
					c.announce(getMTSSearch(c.getPlayer(), tab, type, searchOption, search, c.getPlayer().getCurrentPage()));
					c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 7){ // cancel sale
				int id = slea.readInt(); // id of the item
				Connection con = DatabaseConnection.getConnection();
				try{
					try(PreparedStatement ps = con.prepareStatement("UPDATE mts_items SET transfer = 1 WHERE id = ? AND seller = ?")){
						ps.setInt(1, id);
						ps.setInt(2, c.getPlayer().getId());
						ps.executeUpdate();
					}
					try(PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?")){
						ps.setInt(1, id);
						ps.executeUpdate();
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error updating mts data.");
					return;
				}
				c.announce(MaplePacketCreator.enableCSUse());
				try{
					c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 8){ // transfer item from transfer inv.
				int id = slea.readInt(); // id of the item
				Connection con = DatabaseConnection.getConnection();
				try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 1  AND id= ? ORDER BY id DESC")){
					ps.setInt(1, c.getPlayer().getId());
					ps.setInt(2, id);
					try(ResultSet rs = ps.executeQuery()){
						if(rs.next()){
							Item i;
							if(rs.getInt("type") != 1){
								Item ii = new Item(rs);
								ii.setPosition(c.getPlayer().getInventory(ItemInformationProvider.getInstance().getInventoryType(rs.getInt("itemid"))).getNextFreeSlot());
								i = ii.copy();
							}else{
								Equip equip = new Equip(rs);
								equip.setPosition(c.getPlayer().getInventory(ItemInformationProvider.getInstance().getInventoryType(rs.getInt("itemid"))).getNextFreeSlot());
								i = equip.copy();
							}
							try(PreparedStatement pse = con.prepareStatement("DELETE FROM mts_items WHERE id = ? AND seller = ? AND transfer = 1")){
								pse.setInt(1, id);
								pse.setInt(2, c.getPlayer().getId());
								pse.executeUpdate();
							}
							MapleInventoryManipulator.addFromDrop(c, i, false);
							c.announce(MaplePacketCreator.enableCSUse());
							c.announce(getCart(c.getPlayer(), c.getPlayer().getId()));
							c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
							c.announce(MaplePacketCreator.MTSConfirmTransfer(i.getQuantity(), i.getPosition()));
							c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
						}
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 9){ // add to cart
				int id = slea.readInt(); // id of the item
				Connection con = DatabaseConnection.getConnection();
				try(PreparedStatement ps1 = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? AND seller <> ?")){
					ps1.setInt(1, id);// Previene que agregues al cart tus propios items
					ps1.setInt(2, c.getPlayer().getId());
					try(ResultSet rs1 = ps1.executeQuery()){
						if(rs1.next()){
							PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_cart WHERE cid = ? AND itemid = ?");
							ps.setInt(1, c.getPlayer().getId());
							ps.setInt(2, id);
							try(ResultSet rs = ps.executeQuery()){
								if(!rs.next()){
									try(PreparedStatement pse = con.prepareStatement("INSERT INTO mts_cart (cid, itemid) VALUES (?, ?)")){
										pse.setInt(1, c.getPlayer().getId());
										pse.setInt(2, id);
										pse.executeUpdate();
									}
								}
							}
						}
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
				try{
					c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
					c.announce(MaplePacketCreator.enableCSUse());
					c.announce(CWvsContext.enableActions());
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 10){ // delete from cart
				int id = slea.readInt(); // id of the item
				Connection con = DatabaseConnection.getConnection();
				try{
					try(PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ? AND cid = ?")){
						ps.setInt(1, id);
						ps.setInt(2, c.getPlayer().getId());
						ps.executeUpdate();
					}
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error updating cart.");
					return;
				}
				try{
					c.announce(getCart(c.getPlayer(), c.getPlayer().getId()));
					c.announce(MaplePacketCreator.enableCSUse());
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.getPlayer().dropMessage(MessageType.POPUP, "Error retrieving mts data.");
					return;
				}
			}else if(op == 18){ // put item up for auction
				/**
				 * Sword: 1 quantity
				 * 01 - item type? 1 == equip
				 * F0 DD 13 00 - itemid
				 * 00
				 * 00 80 05 BB 46 E6 17 02 - ?
				 * 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				 * 05 00 41 72 6E 61 68 - Equip owner
				 * 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 FF FF FF FF
				 * 02 00 00 00 - slot
				 * 01 00 00 00 - quantity
				 * E8 03 00 00 - starting bid
				 * 39 30 00 00 - direct purchase
				 * 18 - sale duration
				 * 01 - fee type
				 * F4 01 00 00 - bid increment
				 */
				/**
				 * Snail Shell: Slot 3, 150 units, direct purchase: 4444, bid increment: 10, starting bid: 1000, time(hours): 24
				 * 02- item type
				 * 13 09 3D 00 - item id
				 * 00
				 * 00 80 05 BB 46 E6 17 02
				 * C8 00 05 00 41 72 6E 61 68 00 00
				 * 03 00 00 00 - slot
				 * 96 00 00 00 - quantity
				 * E8 03 00 00 - starting bid
				 * 5C 11 00 00 - direct purchase
				 * 18 - sale duration
				 * 01 - fee type
				 * 0A 00 00 00 - bid increment
				 */
				if(true){
					c.getPlayer().dropMessage(MessageType.POPUP, "Auctions have been temporary disabled.");
					c.announce(CWvsContext.enableActions());
					return;// need to remove nx when adding a bid, then give nx back if you don't win
				}
				byte itemType = slea.readByte();// 1 == equip, 2 == bundle
				int itemid = slea.readInt();
				slea.readByte();
				slea.readLong();
				if(itemType == 2){
					slea.skip(11);
				}else{// equip
					slea.skip(32);
					slea.readMapleAsciiString();// equip owner?
					slea.skip(32);
				}
				int slot = slea.readInt();
				int quantity = slea.readInt();
				int startingBid = slea.readInt();
				int directPurchase = slea.readInt();
				byte duration = slea.readByte();
				slea.readByte();// fee type
				int bidIncrement = slea.readInt();
				MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(itemid);
				Item item = c.getPlayer().getInventory(type).getItem((short) slot);
				short quant = (short) Math.min(quantity, c.getPlayer().getItemQuantity(itemid, false));
				try{
					if(item != null && item.getItemId() == itemid){
						if(itemType == 2){
							try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO mts_items (tab, type, itemid, quantity, seller, price, owner, sellername, sell_ends, bid_incre, buy_now) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
								ps.setInt(1, 3);
								ps.setInt(2, (int) type.getType());
								ps.setInt(3, item.getItemId());
								ps.setInt(4, quant);
								ps.setInt(5, c.getPlayer().getId());
								ps.setInt(6, startingBid);
								ps.setString(7, item.getOwner());
								ps.setString(8, c.getPlayer().getName());
								ps.setLong(9, addTime(Calendar.getInstance().getTimeInMillis(), 0, duration));
								ps.setInt(10, bidIncrement);
								ps.setInt(11, directPurchase);
								ps.executeUpdate();
							}
						}else{
							Equip equip = (Equip) item;
							int pos = 0;// 31
							try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO mts_items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
								ps.setInt(++pos, 3);
								ps.setByte(++pos, type.getType());
								ps.setInt(++pos, equip.getItemId());
								ps.setInt(++pos, quant);
								ps.setInt(++pos, c.getPlayer().getId());
								ps.setInt(++pos, startingBid);
								ps.setInt(++pos, bidIncrement);
								ps.setInt(++pos, directPurchase);
								ps.setInt(++pos, equip.getUpgradeSlots());
								ps.setInt(++pos, equip.getLevel());
								ps.setInt(++pos, equip.getStr());
								ps.setInt(++pos, equip.getDex());
								ps.setInt(++pos, equip.getInt());
								ps.setInt(++pos, equip.getLuk());
								ps.setInt(++pos, equip.getHp());
								ps.setInt(++pos, equip.getMp());
								ps.setInt(++pos, equip.getWatk());
								ps.setInt(++pos, equip.getMatk());
								ps.setInt(++pos, equip.getWdef());
								ps.setInt(++pos, equip.getMdef());
								ps.setInt(++pos, equip.getAcc());
								ps.setInt(++pos, equip.getAvoid());
								ps.setInt(++pos, equip.getHands());
								ps.setInt(++pos, equip.getSpeed());
								ps.setInt(++pos, equip.getJump());
								ps.setInt(++pos, equip.getVicious());
								ps.setInt(++pos, equip.getFlag());
								ps.setString(++pos, equip.getOwner());
								ps.setString(++pos, c.getPlayer().getName());
								ps.setLong(++pos, addTime(Calendar.getInstance().getTimeInMillis(), 0, duration));
								ps.setInt(++pos, 0);// transfer
								ps.setShort(++pos, (short) 0);
								ps.setInt(++pos, -1);
								ps.setInt(++pos, -1);
								ps.setString(++pos, "");
								ps.setInt(++pos, 1);
								ps.setFloat(++pos, equip.getItemExp());
								ps.setByte(++pos, equip.getItemLevel());
								ps.setLong(++pos, equip.getLockExpiration());
								ps.setInt(++pos, equip.getRingId());
								ps.setBoolean(++pos, equip.hasLearnedSkills());
								ps.setString(++pos, "");
								ps.executeUpdate();
							}
						}
						MapleInventoryManipulator.removeItem(c, type, slot, quant, true, false);
						c.announce(MaplePacketCreator.MTSConfirmSell());
					}
				}catch(SQLException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					c.announce(MaplePacketCreator.MTSOperation(ITCResCode.RegAuction_Failed));
				}
				try{
					c.announce(MaplePacketCreator.MTSOperation(ITCResCode.RegAuction_Done));
					c.announce(getMTS(c.getPlayer(), 3, 0, 0));
					c.announce(MaplePacketCreator.enableCSUse());
					c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
					c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
				}catch(SQLException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}else if(op == 19){ // bidding on an auction
				int id = slea.readInt();// id
				int bidPrice = slea.readInt();// my bid price
				int bidRange = slea.readInt();// my bid range
				int total = bidPrice + bidRange;
				if(c.getPlayer().getCashShop().getCash(GameConstants.MAIN_NX_TYPE) >= total){
					Connection con = DatabaseConnection.getConnection();
					try(PreparedStatement ps = con.prepareStatement("SELECT sell_ends from mts_items WHERE id = ?")){
						ps.setInt(1, id);
						try(ResultSet rs = ps.executeQuery()){
							if(rs.next()){
								if(rs.getLong("sell_ends") <= Calendar.getInstance().getTimeInMillis()){// its over
									c.getPlayer().dropMessage(MessageType.POPUP, "The auction has ended.");
									c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
									c.announce(MaplePacketCreator.enableCSUse());
									c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
									c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
								}
							}else{// no auction under id
								c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BidAuction_Failed));
								c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
								c.announce(MaplePacketCreator.enableCSUse());
								c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
								c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
								return;
							}
						}
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
						c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BidAuction_Failed));
					}
					try(PreparedStatement ps = con.prepareStatement("INSERT INTO mts_bids VALUES(DEFAULT, ?, ?, ?, ?)")){// TODO: It would be better to just update 3 values in mts_items
						ps.setInt(1, id);
						ps.setInt(2, c.getPlayer().getId());
						ps.setInt(3, bidPrice);
						ps.setInt(4, bidRange);
						ps.executeUpdate();
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
						c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BidAuction_Failed));
					}
					try{
						c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BidAuction_Done));
						c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
						c.announce(MaplePacketCreator.enableCSUse());
						c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
						c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
						c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BidAuction_Failed));
					}
				}else{
					c.getPlayer().dropMessage(MessageType.POPUP, "You do not have enough " + GameConstants.MAIN_NX_NAME + ".");
				}
			}else if(op == 16 || op == 17 || op == 20){ // buy && buy from cart, and buy from auction
				int id = slea.readInt(); // id of the item
				Connection con = DatabaseConnection.getConnection();
				try{
					con.setAutoCommit(false);
					try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE id = ? ORDER BY id DESC")){
						ps.setInt(1, id);
						try(ResultSet rs = ps.executeQuery()){
							if(rs.next()){
								int price = rs.getInt("price") + GameConstants.nCommissionBase + (int) (rs.getInt("price") * (GameConstants.nCommissionRate / 100)); // taxes
								if(c.getPlayer().getCashShop().getCash(4) >= price){ // FIX
									try(PreparedStatement pse = con.prepareStatement("UPDATE mts_items SET seller = ?, transfer = 1 WHERE id = ? AND transfer = 0 AND seller != ?")){
										pse.setInt(1, c.getPlayer().getId());
										pse.setInt(2, id);
										pse.setInt(3, c.getPlayer().getId());
										if(pse.executeUpdate() == 0){// check if we updated anything, if not it could of already been sold/removed
											// c.getPlayer().dropMessage(MessageType.POPUP, "Failed to purchase the item. Please try again.");
											c.announce(MaplePacketCreator.MTSOperation(ITCResCode.BuyItem_Failed));
											c.announce(MaplePacketCreator.enableCSUse());
											c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
											c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
											c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
											c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
											c.announce(CWvsContext.enableActions());
											return;
										}else{
											if(rs.getInt("type") == 1){
												insertIntoHistory(con, c.getPlayer().getId(), new Equip(rs), rs.getInt("tab"), price, rs.getString("sellername"), rs.getInt("seller"), (short) 1);
												insertIntoHistory(con, rs.getInt("seller"), new Equip(rs), rs.getInt("tab"), price, c.getPlayer().getName(), c.getPlayer().getId(), (short) 0);
											}else{
												insertIntoHistory(con, c.getPlayer().getId(), new Item(rs), rs.getInt("tab"), price, rs.getString("sellername"), rs.getInt("seller"), (short) 1);
												insertIntoHistory(con, rs.getInt("seller"), new Item(rs), rs.getInt("tab"), price, c.getPlayer().getName(), c.getPlayer().getId(), (short) 0);
											}
										}
									}
									try(PreparedStatement pse = con.prepareStatement("DELETE FROM mts_cart WHERE itemid = ?")){
										pse.setInt(1, id);
										pse.executeUpdate();
									}
									try(PreparedStatement pse = con.prepareStatement("INSERT INTO server_queue(serverType, characterr, type, value) VALUES(?, ?, ?, ?)")){
										pse.setString(1, "ChannelServer");
										pse.setInt(2, rs.getInt("seller"));
										pse.setString(3, "GIVE_NX");
										pse.setInt(4, rs.getInt("price"));
										pse.executeUpdate();
									}
									/*MapleCharacter victim = null;// TODO: ChannelServer.g.getCharacterById(rs.getInt("seller"));
									if(victim != null){
										victim.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, rs.getInt("price"));
										if(victim.getCashShop().isOpened()) victim.announce(MaplePacketCreator.showMTSCash(victim));
									}else{
										System.out.println("Seller is offline");
										try(PreparedStatement pse = con.prepareStatement("SELECT accountid FROM characters WHERE id = ?")){
											pse.setInt(1, rs.getInt("seller"));
											try(ResultSet rse = pse.executeQuery()){
												if(rse.next()){
													try(PreparedStatement psee = con.prepareStatement("UPDATE accounts SET nxPrepaid = nxPrepaid + ? WHERE id = ?")){
														psee.setInt(1, rs.getInt("price"));
														psee.setInt(2, rse.getInt("accountid"));
														psee.executeUpdate();
													}
												}
											}
										}
									}*/
									c.getPlayer().getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, -price);
									c.announce(MaplePacketCreator.enableCSUse());
									c.announce(getMTS(c.getPlayer(), c.getPlayer().getCurrentTab(), c.getPlayer().getCurrentType(), c.getPlayer().getCurrentPage()));
									c.announce(MaplePacketCreator.MTSConfirmBuy());
									c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
									c.announce(MaplePacketCreator.transferInventory(getTransfer(c.getPlayer().getId())));
									c.announce(MaplePacketCreator.notYetSoldInv(getNotYetSold(c.getPlayer().getId())));
									c.announce(CWvsContext.enableActions());
								}else{
									c.announce(MaplePacketCreator.MTSFailBuy());
								}
							}
						}
					}
					con.commit();
					con.setAutoCommit(true);
				}catch(SQLException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
					c.announce(MaplePacketCreator.MTSFailBuy());
					try{
						con.rollback();
						con.setAutoCommit(true);
					}catch(SQLException ex2){}
				}
			}else{
				System.out.println("Unhandled OP(MTS): " + op + " Packet: " + slea.toString());
			}
		}else{
			c.announce(MaplePacketCreator.showMTSCash(c.getPlayer()));
		}
	}

	public static List<MTSItemInfo> getNotYetSold(int cid) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND tab = 1 AND transfer = 0 ORDER BY id DESC")){
			ps.setInt(1, cid);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		return items;
	}

	public byte[] getCart(MapleCharacter chr, int cid) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		int pages = 0;
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_cart WHERE cid = ? ORDER BY id DESC")){
			ps.setInt(1, cid);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					try(PreparedStatement pse = con.prepareStatement("SELECT * FROM mts_items WHERE id = ?")){
						pse.setInt(1, rs.getInt("itemid"));
						try(ResultSet rse = pse.executeQuery()){
							if(rse.next()){
								items.add(createItem(rse));
							}
						}
					}
				}
			}
		}
		try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_cart WHERE cid = ?")){
			ps.setInt(1, cid);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					pages = rs.getInt(1) / 16;
					if(rs.getInt(1) % 16 > 0){
						pages += 1;
					}
				}
			}
		}
		return MaplePacketCreator.sendMTS(items, 4, 0, 0, pages, chr.getCurrentSortType(), chr.getCurrentSortColumn());
	}

	public static List<MTSItemInfo> getTransfer(int cid) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE transfer = 1 AND seller = ? ORDER BY id DESC")){
			ps.setInt(1, cid);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		return items;
	}

	public static byte[] getMTS(MapleCharacter chr, int tab, int type, int page) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		int pages = 0;
		try(PreparedStatement ps = (type != 0 ? con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16") : con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16"))){
			ps.setInt(1, tab);
			if(type != 0){
				ps.setInt(2, type);
				ps.setInt(3, page * 16);
			}else{
				ps.setInt(2, page * 16);
			}
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + (type != 0 ? "AND type = ? " : " ") + "AND transfer = 0")){
			ps.setInt(1, tab);
			if(type != 0){
				ps.setInt(2, type);
			}
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					pages = rs.getInt(1) / 16;
					if(rs.getInt(1) % 16 > 0){
						pages++;
					}
				}
			}
		}
		return MaplePacketCreator.sendMTS(items, tab, type, page, pages, chr.getCurrentSortType(), chr.getCurrentSortColumn()); // resniff
	}

	public static byte[] getAuctionItems(MapleCharacter chr, int seller, int page) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		int pages = 0;
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? AND transfer = 0 AND seller = ? ORDER BY id DESC LIMIT ?, 16")){
			ps.setInt(1, 3);
			ps.setInt(2, seller);
			ps.setInt(3, page * 16);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? AND transfer = 0 AND seller = ?")){
			ps.setInt(1, 3);
			ps.setInt(2, seller);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					pages = rs.getInt(1) / 16;
					if(rs.getInt(1) % 16 > 0){
						pages++;
					}
				}
			}
		}
		return MaplePacketCreator.sendMTS(items, 4, 3, page, pages, chr.getCurrentSortType(), chr.getCurrentSortColumn()); // resniff
	}

	public static byte[] getHistory(MapleCharacter chr, int page) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		int pages = 0;
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_history WHERE tab = ? AND chrid = ? ORDER BY id DESC LIMIT ?, 16")){
			ps.setInt(1, 1);
			ps.setInt(2, chr.getId());
			ps.setInt(3, page * 16);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_history WHERE tab = ? AND chrid = ?")){
			ps.setInt(1, 1);
			ps.setInt(2, chr.getId());
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					pages = rs.getInt(1) / 16;
					if(rs.getInt(1) % 16 > 0){
						pages++;
					}
				}
			}
		}
		return MaplePacketCreator.sendMTS(items, 4, 2, page, pages, chr.getCurrentSortType(), chr.getCurrentSortColumn()); // resniff
	}

	public static void insertIntoHistory(Connection con, int chrid, Item item, int tab, int price, String sellerName, int sellerID, short status) throws SQLException{
		MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(item.getItemId());
		if(item.getType() == 2){
			try(PreparedStatement ps = con.prepareStatement("INSERT INTO mts_history (tab, type, chrid, itemid, quantity, price, owner, seller, sellername, date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
				ps.setInt(1, tab);
				ps.setInt(2, (int) type.getType());
				ps.setInt(3, chrid);
				ps.setInt(4, item.getItemId());
				ps.setInt(5, item.getQuantity());
				ps.setInt(6, price);
				ps.setString(7, item.getOwner());
				ps.setInt(8, sellerID);
				ps.setString(9, sellerName);
				ps.setLong(10, Calendar.getInstance().getTimeInMillis());
				ps.setShort(11, status);
				ps.executeUpdate();
			}
		}else{
			Equip equip = (Equip) item;
			int pos = 0;
			try(PreparedStatement ps = con.prepareStatement("INSERT INTO mts_history VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")){
				ps.setInt(++pos, tab);
				ps.setByte(++pos, type.getType());
				ps.setInt(++pos, chrid);
				ps.setInt(++pos, equip.getItemId());
				ps.setInt(++pos, equip.getQuantity());
				ps.setInt(++pos, equip.getUpgradeSlots());
				ps.setInt(++pos, equip.getLevel());
				ps.setInt(++pos, equip.getStr());
				ps.setInt(++pos, equip.getDex());
				ps.setInt(++pos, equip.getInt());
				ps.setInt(++pos, equip.getLuk());
				ps.setInt(++pos, equip.getHp());
				ps.setInt(++pos, equip.getMp());
				ps.setInt(++pos, equip.getWatk());
				ps.setInt(++pos, equip.getMatk());
				ps.setInt(++pos, equip.getWdef());
				ps.setInt(++pos, equip.getMdef());
				ps.setInt(++pos, equip.getAcc());
				ps.setInt(++pos, equip.getAvoid());
				ps.setInt(++pos, equip.getHands());
				ps.setInt(++pos, equip.getSpeed());
				ps.setInt(++pos, equip.getJump());
				ps.setInt(++pos, equip.getVicious());
				ps.setInt(++pos, equip.getFlag());
				ps.setString(++pos, equip.getOwner());
				ps.setInt(++pos, 0);
				ps.setShort(++pos, (short) 0);
				ps.setLong(++pos, equip.getExpiration());
				ps.setString(++pos, equip.getGiftFrom());
				ps.setInt(++pos, equip.getPerBundle());
				ps.setFloat(++pos, equip.getItemExp());
				ps.setByte(++pos, equip.getItemLevel());
				ps.setLong(++pos, equip.getLockExpiration());
				ps.setInt(++pos, equip.getRingId());
				ps.setBoolean(++pos, equip.hasLearnedSkills());
				ps.setLong(++pos, Calendar.getInstance().getTimeInMillis());
				ps.setInt(++pos, sellerID);
				ps.setString(++pos, sellerName);
				ps.setInt(++pos, price);
				ps.setShort(++pos, status);
				ps.executeUpdate();
			}
		}
	}

	public byte[] getMTSSearch(MapleCharacter chr, int tab, int type, int cOi, String search, int page) throws SQLException{
		List<MTSItemInfo> items = new ArrayList<>();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		String listaitems = "";
		if(cOi != 0){
			List<String> retItems = new ArrayList<>();
			for(Pair<Integer, String> itemPair : ii.getItemDataByName(search)){
				if(itemPair.getRight().toLowerCase().contains(search.toLowerCase())){
					retItems.add(" itemid=" + itemPair.getLeft() + " OR ");
				}
			}
			listaitems += " AND (";
			if(retItems != null && retItems.size() > 0){
				for(String singleRetItem : retItems){
					listaitems += singleRetItem;
				}
				listaitems += " itemid=0 )";
			}
		}else{
			listaitems = " AND sellername LIKE CONCAT('%','" + search + "', '%')";
		}
		Connection con = DatabaseConnection.getConnection();
		int pages = 0;
		try(PreparedStatement ps = (type != 0 ? con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND type = ? AND transfer = 0 ORDER BY id DESC LIMIT ?, 16") : con.prepareStatement("SELECT * FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0 ORDER BY id DESC LIMIT ?, 16"))){
			ps.setInt(1, tab);
			if(type != 0){
				ps.setInt(2, type);
				ps.setInt(3, page * 16);
			}else{
				ps.setInt(2, page * 16);
			}
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()){
					items.add(createItem(rs));
				}
			}
		}
		if(type == 0){
			try(PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items WHERE tab = ? " + listaitems + " AND transfer = 0")){
				ps.setInt(1, tab);
				if(type != 0){
					ps.setInt(2, type);
				}
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()){
						pages = rs.getInt(1) / 16;
						if(rs.getInt(1) % 16 > 0){
							pages++;
						}
					}
				}
			}
		}
		return MaplePacketCreator.sendMTS(items, tab, type, page, pages, chr.getCurrentSortType(), chr.getCurrentSortColumn());
	}

	public static MTSItemInfo createItem(ResultSet rs) throws SQLException{
		MTSItemInfo item = null;
		if(rs.getInt("type") != 1){
			item = new MTSItemInfo(new Item(rs), rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"));
		}else{
			item = new MTSItemInfo((Item) new Equip(rs), rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"));
		}
		item.comment = SQLUtil.hasColumn(rs, "note") ? rs.getString("note") : "";
		int highestBid = 0;
		int bids = 0;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_bids WHERE mtsItemID = ?")){
			ps.setInt(1, rs.getInt("id"));
			try(ResultSet rss = ps.executeQuery()){
				while(rss.next()){
					bids++;
					highestBid = Math.max(highestBid, rss.getInt("bidRange") + rss.getInt("bid"));
				}
			}
		}
		if(SQLUtil.hasColumn(rs, "bid_incre")){
			item.bidRange = rs.getInt("bid_incre");
			highestBid = Math.max(rs.getInt("price"), highestBid);
			item.bidCount = bids;
			item.bidPrice = highestBid;
			item.minPrice = highestBid;
			item.maxPrice = rs.getInt("buy_now");
		}
		item.date = SQLUtil.hasColumn(rs, "sell_ends") ? rs.getLong("sell_ends") : rs.getLong("date");
		if(SQLUtil.hasColumn(rs, "status")) item.status = rs.getShort("status");
		return item;
	}

	public long addTime(long baseTime, long days, long hours){
		long time = baseTime;
		// hours += days * 24L;
		long minutes = hours * 60L;
		minutes += 5;
		long seconds = minutes * 60L;
		long ms = seconds * 1000L;
		time += ms;
		return time;
	}
}
