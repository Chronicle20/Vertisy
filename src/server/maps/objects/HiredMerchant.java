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
package server.maps.objects;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.mysql.jdbc.Statement;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MaplePlayerShopItem;
import server.TimerManager;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.field.MiniRoomBase;

/**
 * @author XoticStory
 */
public class HiredMerchant extends PlayerShop{

	private int ownerId, itemId, mesos = 0;
	private int channel;
	private long start;
	private String ownerName = "";
	private String description = "";
	private MapleCharacter[] visitors = new MapleCharacter[3];
	private List<MaplePlayerShopItem> items = new LinkedList<>();
	private List<Pair<String, Byte>> messages = new LinkedList<>();
	private List<SoldItem> sold = new LinkedList<>();
	private boolean open;
	public ScheduledFuture<?> schedule = null;
	private MapleMap map;
	private boolean doSave = true;

	public HiredMerchant(final MapleCharacter owner, int itemId, String desc, long endTime){
		this.setPosition(owner.getPosition());
		this.start = System.currentTimeMillis();
		this.ownerId = owner.getId();
		this.channel = owner.getClient().getChannel();
		this.itemId = itemId;
		this.ownerName = owner.getName();
		this.description = desc;
		this.map = owner.getMap();
		this.schedule = TimerManager.getInstance().scheduleAtTimestamp("merchant-kill", new Runnable(){

			@Override
			public void run(){
				forceClose();
				ChannelServer.getInstance().getChannel(channel).removeHiredMerchant(ownerId);
			}
		}, endTime > 0 ? endTime : Long.MAX_VALUE);
	}

	public void broadcastToVisitors(final byte[] packet){
		for(MapleCharacter visitor : visitors){
			if(visitor != null){
				visitor.getClient().announce(packet);
			}
		}
	}

	public void addVisitor(MapleCharacter visitor){
		int i = this.getFreeSlot();
		if(i > -1){
			// if(items.size() == 0 && sold.size() == 0){
			// doSave = false;
			// forceClose();
			// Server.getInstance().getChannel(world, channel).removeHiredMerchant(ownerId);
			// }else{
			visitors[i] = visitor;
			broadcastToVisitors(MiniRoomBase.EntrustedShop.hiredMerchantVisitorAdd(visitor, i + 1));
			// }
		}
	}

	public void removeVisitor(MapleCharacter visitor){
		int slot = getVisitorSlot(visitor);
		if(slot < 0){ // Not found
			return;
		}
		if(visitors[slot] != null && visitors[slot].getId() == visitor.getId()){
			visitors[slot] = null;
			if(slot != -1){
				broadcastToVisitors(MiniRoomBase.EntrustedShop.hiredMerchantVisitorLeave(slot + 1));
			}
		}
	}

	public int getVisitorSlot(MapleCharacter visitor){
		for(int i = 0; i < 3; i++){
			if(visitors[i] != null && visitors[i].getId() == visitor.getId()) return i;
		}
		return -1; // Actually 0 because of the +1's.
	}

	public void removeAllVisitors(String message){
		for(int i = 0; i < 3; i++){
			if(visitors[i] != null){
				visitors[i].setHiredMerchant(null);
				visitors[i].getClient().announce(MiniRoomBase.EntrustedShop.leaveHiredMerchant(i + 1, 0x11));
				if(message.length() > 0){
					visitors[i].dropMessage(1, message);
				}
				visitors[i] = null;
			}
		}
	}

	public void buy(MapleClient c, int item, short quantity){
		MaplePlayerShopItem pItem = items.get(item);
		/*
		 * Nexon makes you enter the amount you want, then divides that by perBundle, then sends that as 'quantity' which is how many bundles you are buying.
		 * The price is for each bundle, so price * quantity.
		 * 
		 */
		// System.out.println(String.format("Bundles: %d, PerBundle: %d", pItem.getBundles(), pItem.getPerBundle()));
		synchronized(items){
			Item newItem = pItem.getItem().copy();
			newItem.setQuantity((short) (pItem.getPerBundle() * quantity));
			newItem.setPerBundle((short) 1);
			if((newItem.getFlag() & ItemConstants.KARMA) == ItemConstants.KARMA){
				newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.KARMA));
			}
			if(newItem.getType() == 2 && (newItem.getFlag() & ItemConstants.SPIKES) == ItemConstants.SPIKES){
				newItem.setFlag((byte) (newItem.getFlag() ^ ItemConstants.SPIKES));
			}
			if(quantity < 1 || pItem.getItem().getPerBundle() < 1 || !pItem.isExist() || pItem.getBundles() < quantity){
				c.announce(CWvsContext.enableActions());
				return;
			}else if(newItem.getType() == 1 && newItem.getQuantity() > 1){
				c.announce(CWvsContext.enableActions());
				return;
			}else if(!pItem.isExist()){
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(newItem.getQuantity() == pItem.getPerBundle() * pItem.getBundles()) newItem.nSN = pItem.getItem().nSN;
			int price = pItem.getPrice() * quantity;
			if(c.getPlayer().getMeso() >= price){
				if(c.getPlayer().canHoldItem(newItem)){
					MapleInventoryManipulator.addFromDrop(c, newItem, true);
					ItemFactory.updateItemOwner(c.getPlayer(), newItem, ItemFactory.INVENTORY);
					c.getPlayer().gainMeso(-price, false);
					sold.add(new SoldItem(c.getPlayer().getName(), pItem.getItem().getItemId(), quantity, price));
					pItem.setBundles((short) (pItem.getBundles() - quantity));
					if(pItem.getBundles() < 1){
						pItem.setDoesExist(false);
					}
					try{
						ChannelServer.getInstance().getWorldInterface().addMerchantMesos(ownerId, price);
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
				}else{
					c.getPlayer().dropMessage(1, "Your inventory is full. Please clean a slot before buying this item.");
					return;
				}
			}else{
				c.getPlayer().dropMessage(1, "You do not have enough mesos.");
				return;
			}
			try{
				this.saveItems();
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Failed to save shop: " + ownerName);
			}
		}
	}

	public void forceClose(){
		if(map == null) return;
		if(schedule != null){
			schedule.cancel(false);
		}
		try{
			saveItems();
			items.clear();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		// Server.getInstance().getChannel(world, channel).removeHiredMerchant(ownerId);
		map.broadcastMessage(MiniRoomBase.EntrustedShop.destroyHiredMerchant(getOwnerId()));
		map.removeMapObject(this);
		try{
			ChannelServer.getInstance().getWorldInterface().closeMerchant(ownerName, ownerId);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		map = null;
		schedule = null;
	}

	public void closeShop(MapleClient c, boolean timeout){
		if(map == null) return;
		map.removeMapObject(this);
		map.broadcastMessage(MiniRoomBase.EntrustedShop.destroyHiredMerchant(ownerId));
		c.getChannelServer().removeHiredMerchant(ownerId);
		try{
			// MapleCharacter player = c.getWorldServer().getCharacterById(ownerId);
			if(c.getPlayer() != null){// why did it grab the chr via worldserver?
				c.getPlayer().setHasMerchant(false);
			}else{
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", Statement.RETURN_GENERATED_KEYS)){
					ps.setInt(1, ownerId);
					ps.executeUpdate();
				}
			}
			if(check(c.getPlayer(), getItems()) && !timeout){
				for(MaplePlayerShopItem mpsi : getItems()){
					if(mpsi.isExist() && (mpsi.getItem().getType() == MapleInventoryType.EQUIP.getType())){
						MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), false);
						ItemFactory.updateItemOwner(c.getPlayer(), mpsi.getItem(), ItemFactory.INVENTORY);
					}else if(mpsi.isExist()){
						Item item = mpsi.getItem().copy();
						item.nSN = mpsi.getItem().nSN;
						item.setPerBundle((short) 1);
						item.setQuantity((short) (mpsi.getItem().getPerBundle() * mpsi.getItem().getQuantity()));
						MapleInventoryManipulator.addFromDrop(c, item, false);
						ItemFactory.updateItemOwner(c.getPlayer(), item, ItemFactory.INVENTORY);
					}
				}
				items.clear();
				this.saveItems();
			}else{
				try{
					this.saveItems();
				}catch(Exception e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
				items.clear();
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Error closing shop: " + ownerName);
		}
		if(schedule != null) schedule.cancel(false);
	}

	public String getOwnerName(){
		return ownerName;
	}

	public void clearItems(){
		items.clear();
	}

	public int getOwnerId(){
		return ownerId;
	}

	public String getDescription(){
		return description;
	}

	public MapleCharacter[] getVisitors(){
		return visitors;
	}

	public List<MaplePlayerShopItem> getItems(){
		return Collections.unmodifiableList(items);
	}

	public void addItem(MaplePlayerShopItem item){
		items.add(item);
		/*try{
			this.saveItems(false);
		}catch(SQLException ex){}*/
	}

	public void removeFromSlot(int slot){
		items.remove(slot);
		/*try{
		this.saveItems(false);
		}catch(SQLException ex){}*/
	}

	public int getFreeSlot(){
		for(int i = 0; i < 3; i++){
			if(visitors[i] == null) return i;
		}
		return -1;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public boolean isOpen(){
		return open;
	}

	public void setOpen(boolean set){
		this.open = set;
	}

	public int getItemId(){
		return itemId;
	}

	public boolean isOwner(MapleCharacter chr){
		return chr.getId() == ownerId;
	}

	public void saveItems() throws SQLException{
		if(!doSave) return;
		List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
		for(MaplePlayerShopItem pItems : items){
			Item newItem = pItems.getItem();
			if(pItems.isExist()){
				itemsWithType.add(new Pair<>(newItem, MapleInventoryType.getByType(newItem.getType())));
			}
		}
		if(items.size() == 0 && sold.size() == 0){
			doSave = false;
			forceClose();
			ChannelServer.getInstance().getChannel(channel).removeHiredMerchant(ownerId);
			itemsWithType.clear();
		}
		ItemFactory.MERCHANT.saveItems(itemsWithType, this.ownerId, DatabaseConnection.getConnection());
	}

	private static boolean check(MapleCharacter chr, List<MaplePlayerShopItem> items){
		List<Item> itemList = new ArrayList<>();
		for(MaplePlayerShopItem mpsi : items){
			Item copy = mpsi.getItem().copy();
			copy.setQuantity((short) (copy.getPerBundle() * copy.getQuantity()));
			copy.setPerBundle((short) 1);
			itemList.add(copy);
		}
		return chr.canHoldItems(itemList);
		/*byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0;
		List<MapleInventoryType> li = new LinkedList<>();
		for(MaplePlayerShopItem item : items){
			final MapleInventoryType invtype = ItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId());
			if(!li.contains(invtype)){
				li.add(invtype);
			}
			if(invtype == MapleInventoryType.EQUIP){
				eq++;
			}else if(invtype == MapleInventoryType.USE){
				use++;
			}else if(invtype == MapleInventoryType.SETUP){
				setup++;
			}else if(invtype == MapleInventoryType.ETC){
				etc++;
			}else if(invtype == MapleInventoryType.CASH){
				cash++;
			}
		}
		for(MapleInventoryType mit : li){
			if(mit == MapleInventoryType.EQUIP){
				if(chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() <= eq) return false;
			}else if(mit == MapleInventoryType.USE){
				if(chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() <= use) return false;
			}else if(mit == MapleInventoryType.SETUP){
				if(chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() <= setup) return false;
			}else if(mit == MapleInventoryType.ETC){
				if(chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() <= etc) return false;
			}else if(mit == MapleInventoryType.CASH){
				if(chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() <= cash) return false;
			}
		}
		return true;*/
	}

	public int getChannel(){
		return channel;
	}

	public int getTimeLeft(){
		return (int) ((System.currentTimeMillis() - start) / 1000L);
	}

	public List<Pair<String, Byte>> getMessages(){
		return messages;
	}

	public int getMapId(){
		return map.getId();
	}

	public List<SoldItem> getSold(){
		return sold;
	}

	public int getMesos(){
		return mesos;
	}

	@Override
	public void sendDestroyData(MapleClient client){}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.HIRED_MERCHANT;
	}

	@Override
	public void sendSpawnData(MapleClient client){
		client.announce(MiniRoomBase.EntrustedShop.spawnHiredMerchant(this));
	}

	public class SoldItem{

		int itemid, mesos;
		short quantity;
		String buyer;

		public SoldItem(String buyer, int itemid, short quantity, int mesos){
			this.buyer = buyer;
			this.itemid = itemid;
			this.quantity = quantity;
			this.mesos = mesos;
		}

		public String getBuyer(){
			return buyer;
		}

		public int getItemId(){
			return itemid;
		}

		public short getQuantity(){
			return quantity;
		}

		public int getMesos(){
			return mesos;
		}
	}

	public HiredMerchant clone(){
		return null;
	}
}
