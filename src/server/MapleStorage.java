/*
 * - * This file is part of the OdinMS Maple Story Server
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
package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Matze
 */
public class MapleStorage{

	private int id;
	private List<Item> items;
	private int meso;
	private byte slots;
	private Map<MapleInventoryType, List<Item>> typeItems = new HashMap<>();

	private MapleStorage(int id, byte slots, int meso){
		this.id = id;
		this.slots = slots;
		this.items = new LinkedList<>();
		this.meso = meso;
	}

	private static MapleStorage create(int id, int world){
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO storages (accountid, world, slots, meso) VALUES (?, ?, 4, 0)")){
				ps.setInt(1, id);
				ps.setInt(2, world);
				ps.executeUpdate();
			}
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return loadOrCreateFromDB(id, world);
	}

	public static MapleStorage loadOrCreateFromDB(int id, int world){
		MapleStorage ret = null;
		int storeId;
		try{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT storageid, slots, meso FROM storages WHERE accountid = ? AND world = ?");
			ps.setInt(1, id);
			ps.setInt(2, world);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps.close();
				return create(id, world);
			}else{
				storeId = rs.getInt("storageid");
				ret = new MapleStorage(storeId, (byte) rs.getInt("slots"), rs.getInt("meso"));
				rs.close();
				ps.close();
				for(Pair<Item, MapleInventoryType> item : ItemFactory.STORAGE.loadItems(ret.id, false)){
					ret.items.add(item.getLeft());
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return ret;
	}

	public byte getSlots(){
		return slots;
	}

	public boolean gainSlots(int slots){
		slots += this.slots;
		if(slots <= 48){
			this.slots = (byte) slots;
			return true;
		}
		return false;
	}

	public void setSlots(byte set){
		this.slots = set;
	}

	public void saveToDB(Connection con){
		try{
			try(PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")){
				ps.setInt(1, slots);
				ps.setInt(2, meso);
				ps.setInt(3, id);
				ps.executeUpdate();
			}
			List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
			for(Item item : items){
				itemsWithType.add(new Pair<>(item, ItemInformationProvider.getInstance().getInventoryType(item.getItemId())));
			}
			ItemFactory.STORAGE.saveItems(itemsWithType, id, con);
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public Item getItem(byte slot){
		return items.get(slot);
	}

	public Item takeOut(byte slot){
		Item ret = items.remove(slot);
		MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(ret.getItemId());
		typeItems.put(type, new ArrayList<>(filterItems(type)));
		return ret;
	}

	public void store(MapleClient c, Item item){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		MapleInventoryType type = ii.getInventoryType(item.getItemId());
		items.add(item);
		/*short quantity = item.getQuantity();
		short max = ii.getItemData(item.getItemId()).getSlotMax(c);
		
		if(quantity > 0){// update other items
			if(!ItemConstants.isThrowingStar(item.getItemId())){
				for(Item exist : items){
					if(exist.getItemId() == item.getItemId()){
						if(exist.getQuantity() < max){
							short qLeft = (short) (max - exist.getQuantity());
							if(qLeft > 0){// can add items
								short add = 0;
								if(quantity + exist.getQuantity() <= max){
									add = quantity;
								}else{
									add = qLeft < quantity ? qLeft : quantity;
								}
								quantity -= add;
								exist.setQuantity((short) (exist.getQuantity() + add));
							}
						}
					}
				}
			}
		}
		// If we still have extra quantity, create new items
		while(quantity > 0){
			Item nItem = item.copy();
			nItem.setQuantity((short) Math.min(quantity, max));
			quantity -= nItem.getQuantity();
			items.add(nItem);
		}*/
		typeItems.put(type, new ArrayList<>(filterItems(type)));
	}

	public List<Item> getItems(){
		return Collections.unmodifiableList(items);
	}

	private List<Item> filterItems(MapleInventoryType type){
		List<Item> ret = new LinkedList<>();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		for(Item item : items){
			if(ii.getInventoryType(item.getItemId()) == type){
				ret.add(item);
			}
		}
		return ret;
	}

	public byte getSlot(MapleInventoryType type, byte slot){
		byte ret = 0;
		for(Item item : items){
			if(item == typeItems.get(type).get(slot)) return ret;
			ret++;
		}
		return -1;
	}

	public void sendStorage(MapleClient c, int npcId){
		final ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Collections.sort(items, new Comparator<Item>(){

			@Override
			public int compare(Item o1, Item o2){
				if(ii.getInventoryType(o1.getItemId()).getType() < ii.getInventoryType(o2.getItemId()).getType()) return -1;
				else if(ii.getInventoryType(o1.getItemId()) == ii.getInventoryType(o2.getItemId())) return 0;
				return 1;
			}
		});
		for(MapleInventoryType type : MapleInventoryType.values()){
			typeItems.put(type, new ArrayList<>(items));
		}
		c.announce(MaplePacketCreator.getStorage(npcId, slots, items, meso));
	}

	public void sendStored(MapleClient c, MapleInventoryType type){
		c.announce(MaplePacketCreator.storeStorage(slots, type, typeItems.get(type)));
	}

	public void sendTakenOut(MapleClient c, MapleInventoryType type){
		c.announce(MaplePacketCreator.takeOutStorage(slots, type, typeItems.get(type)));
	}

	public int getMeso(){
		return meso;
	}

	public void setMeso(int meso){
		if(meso < 0){ throw new RuntimeException(); }
		this.meso = meso;
	}

	public void sendMeso(MapleClient c){
		c.announce(MaplePacketCreator.mesoStorage(slots, meso));
	}

	public boolean isFull(){
		return items.size() >= slots;
	}

	public boolean hasRoom(MapleClient c, Item item){
		int itemid = item.getItemId();
		int quantity = item.getQuantity();
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		double max = ii.getItemData(itemid).getSlotMax(c);// making it a double for math later
		if(isFull() && max == 1) return false;// if the inventory is full, and the maxstack is 1, we can't add it
		boolean rechargeable = ItemConstants.isRechargable(itemid);
		if(isFull() && rechargeable) return false;// bullet/star - Same as ^. stars/bullets don't stack
		int newQ = quantity;
		for(Item i : listById(itemid)){// check to see if any room exists
			if(i.softEquals(item)){
				int qLeft = (int) (max - i.getQuantity());
				newQ -= qLeft;
			}
		}
		if(newQ <= 0) return true;// if the current items can store all the quantity
		double required = newQ / max;
		double slotsAvailable = getNumFreeSlot();
		if(slotsAvailable >= required) return true;
		return false;
	}

	public short getNumFreeSlot(){
		if(isFull()) return 0;
		return (short) (slots - items.size());
	}

	public List<Item> listById(int itemId){
		List<Item> ret = new ArrayList<>();
		for(Item item : items){
			if(item.getItemId() == itemId){
				ret.add(item);
			}
		}
		if(ret.size() > 1){
			Collections.sort(ret);
		}
		return ret;
	}

	public void close(){
		typeItems.clear();
	}
}
