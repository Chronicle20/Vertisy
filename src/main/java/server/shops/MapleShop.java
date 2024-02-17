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
package server.shops;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.field.ShopDlg;

/**
 * @author Matze
 */
public class MapleShop{

	private static final Set<Integer> rechargeableItems = new LinkedHashSet<>();
	private int id;
	private int npcId;
	private List<MapleShopItem> items;
	static{
		for(int i = 2070000; i < 2070017; i++){
			rechargeableItems.add(i);
		}
		rechargeableItems.add(2331000);// Blaze Capsule
		rechargeableItems.add(2332000);// Glaze Capsule
		rechargeableItems.add(2070018);
		rechargeableItems.remove(2070014); // doesn't exist
		for(int i = 2330000; i <= 2330005; i++){
			rechargeableItems.add(i);
		}
	}

	private MapleShop(int id, int npcId){
		this.id = id;
		this.npcId = npcId;
		items = new ArrayList<>();
	}

	private void addItem(MapleShopItem item){
		items.add(item);
	}

	public void sendShop(MapleClient c){
		if(c.getPlayer().getScriptDebug()) c.getPlayer().dropMessage(MessageType.MAPLETIP, "Shop: " + this.id + " with npc: " + this.npcId);
		c.getPlayer().setShop(this);
		c.announce(ShopDlg.getNPCShop(c, getNpcId(), items));
	}

	public void buy(MapleClient c, short slot, int itemId, short quantity){
		MapleShopItem item = findBySlot(slot);
		if(item != null){
			if(item.getItemId() != itemId){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Gave incorrect itemid for slot");
				return;
			}
		}else{
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Trying to buy null item.");
			return;
		}
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		if(item != null && item.getPrice() >= 0){
			if(c.getPlayer().getMeso() >= (long) item.getPrice() * quantity){
				if(c.getPlayer().canHoldItem(new Item(itemId, quantity))){
					Item i = null;
					if(ii.getInventoryType(itemId) == MapleInventoryType.EQUIP){
						i = ii.getEquipById(itemId);
					}else{
						i = new Item(itemId, quantity);
					}
					if(!ItemConstants.isRechargable(itemId)){ // Pets can't be bought from shops
						MapleInventoryManipulator.addFromDrop(c, i, false);
						c.getPlayer().gainMeso(-(item.getPrice() * quantity), false);
					}else{
						short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
						quantity = slotMax;
						i.setQuantity(quantity);
						MapleInventoryManipulator.addFromDrop(c, i, false);
						c.getPlayer().gainMeso(-item.getPrice(), false);
					}
					c.announce(ShopDlg.shopTransaction((byte) 0));
				}else c.announce(ShopDlg.shopTransaction((byte) 3));
			}else c.announce(ShopDlg.shopTransaction((byte) 2));
		}else if(item != null && item.getTokenPrice() > 0){
			if(c.getPlayer().getInventory(ii.getInventoryType(item.getTokenItemID())).countById(item.getTokenItemID()) >= (long) item.getTokenPrice() * quantity){
				if(c.getPlayer().canHoldItem(new Item(itemId, quantity))){
					Item i = null;
					if(ii.getInventoryType(itemId) == MapleInventoryType.EQUIP){
						i = ii.getEquipById(itemId);
					}else{
						i = new Item(itemId, quantity);
					}
					if(!ItemConstants.isRechargable(itemId)){
						MapleInventoryManipulator.addFromDrop(c, i, false);
						MapleInventoryManipulator.removeById(c, ii.getInventoryType(item.getTokenItemID()), item.getTokenItemID(), item.getTokenPrice() * quantity, true, false);
					}else{
						short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
						quantity = slotMax;
						i.setQuantity(quantity);
						MapleInventoryManipulator.addFromDrop(c, i, false);
						MapleInventoryManipulator.removeById(c, ii.getInventoryType(item.getTokenItemID()), item.getTokenItemID(), item.getTokenPrice() * quantity, true, false);
					}
					c.announce(ShopDlg.shopTransaction((byte) 0));
				}else c.announce(ShopDlg.shopTransaction((byte) 3));
			}
		}
	}

	public void sell(MapleClient c, MapleInventoryType type, short slot, short quantity){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Item item = c.getPlayer().getInventory(type).getItem(slot);
		if(item == null){ // Basic check
			return;
		}
		if(ItemConstants.isRechargable(item.getItemId())){
			quantity = item.getQuantity();
		}
		if(quantity < 0) return;
		short iQuant = item.getQuantity();
		if(quantity <= iQuant && iQuant >= 0){
			MapleInventoryManipulator.removeItem(c, type, (byte) slot, quantity, true, false);
			ItemData data = ii.getItemData(item.getItemId());
			double price;
			if(ItemConstants.isRechargable(item.getItemId())){
				price = data.wholePrice / (double) ii.getItemData(item.getItemId()).getSlotMax(c);
			}else{
				price = data.price;
			}
			int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0);
			if(recvMesos >= 0){
				c.getPlayer().gainMeso(recvMesos, false);
			}
			c.announce(ShopDlg.shopTransaction((byte) 0x8));
		}
	}

	public void recharge(MapleClient c, short slot){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if(item == null || !ItemConstants.isRechargable(item.getItemId())) return;
		ItemData itemData = ii.getItemData(item.getItemId());
		short slotMax = ii.getItemData(item.getItemId()).getSlotMax(c);
		if(item.getQuantity() < 0) return;
		if(item.getQuantity() < slotMax){
			int price = (int) Math.round(itemData.unitPrice * (slotMax - item.getQuantity()));
			if(c.getPlayer().getMeso() >= price){
				item.setQuantity(slotMax);
				c.getPlayer().forceUpdateItem(item);
				c.getPlayer().gainMeso(-price, false, true, false);
				c.announce(ShopDlg.shopTransaction((byte) 0x8));
			}else{
				c.announce(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
				c.announce(CWvsContext.enableActions());
			}
		}
	}

	private MapleShopItem findBySlot(short slot){
		return items.get(slot);
	}

	public static MapleShop createFromDB(int id, boolean isShopId){
		MapleShop ret = null;
		int shopId;
		try{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps;
			if(isShopId){
				ps = con.prepareStatement("SELECT * FROM shops WHERE shopid = ?");
			}else{
				ps = con.prepareStatement("SELECT * FROM shops WHERE npcid = ?");
			}
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
				shopId = rs.getInt("shopid");
				ret = new MapleShop(shopId, rs.getInt("npcid"));
				rs.close();
				ps.close();
			}else{
				rs.close();
				ps.close();
				return null;
			}
			ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
			ps.setInt(1, shopId);
			rs = ps.executeQuery();
			// List<Integer> recharges = new ArrayList<>(rechargeableItems);
			while(rs.next()){
				ItemData data = ItemInformationProvider.getInstance().getItemData(rs.getInt("itemid"));
				if(data == null || !data.exists){
					Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, rs.getInt("itemid") + " in shop " + shopId + " is an invalid item.");
					continue;
				}
				MapleShopItem item = new MapleShopItem(rs);
				ret.addItem(item);
				/*if(ItemConstants.isRechargable(rs.getInt("itemid"))){
					MapleShopItem starItem = new MapleShopItem((short) 1, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("pitch"));
					ret.addItem(starItem);
					if(rechargeableItems.contains(starItem.getItemId())){
						recharges.remove(Integer.valueOf(starItem.getItemId()));
					}
				}else{
					ret.addItem(new MapleShopItem((short) 1000, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("pitch")));
				}*/
			}
			re: for(Integer recharge : rechargeableItems){
				for(MapleShopItem item : ret.items){
					if(item.getItemId() == recharge.intValue()) continue re;
				}
				ret.addItem(new MapleShopItem((short) 1000, recharge.intValue()));
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return ret;
	}

	public int getNpcId(){
		return npcId;
	}

	public int getId(){
		return id;
	}
}
