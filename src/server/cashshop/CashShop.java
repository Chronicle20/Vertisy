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
package server.cashshop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import server.ItemInformationProvider;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/*
 * @author Flav
 */
public class CashShop{

	private int accountId, characterId, nxCredit, maplePoint, nxPrepaid, ironManNX;
	private boolean ironMan;
	private int opened;
	private ItemFactory factory;
	private List<Item> inventory = new ArrayList<>();
	private List<Integer> wishList = new ArrayList<>();
	private int notes = 0;

	public CashShop(int accountId, int characterId, int jobType, boolean ironMan) throws SQLException{
		this.accountId = accountId;
		this.characterId = characterId;
		this.ironMan = ironMan;
		this.factory = ItemFactory.CASH_EXPLORER;
		/*if(jobType == 0){
			factory = ItemFactory.CASH_EXPLORER;
		}else if(jobType == 1){
			factory = ItemFactory.CASH_CYGNUS;
		}else if(jobType == 2){
			factory = ItemFactory.CASH_ARAN;
		}*/
		if(characterId == -1){
			this.maplePoint = Integer.MAX_VALUE;
			return;
		}
		Connection con = DatabaseConnection.getConnection();
		if(ironMan){
			try(PreparedStatement ps = con.prepareStatement("SELECT ironManNX FROM characters WHERE id = ?")){
				ps.setInt(1, characterId);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()){
						ironManNX = rs.getInt("ironManNX");
					}
				}
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		try(PreparedStatement ps = con.prepareStatement("SELECT `nxCredit`, `maplePoint`, `nxPrepaid` FROM `accounts` WHERE `id` = ?")){
			ps.setInt(1, accountId);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					this.nxCredit = rs.getInt("nxCredit");
					this.maplePoint = rs.getInt("maplePoint");
					this.nxPrepaid = rs.getInt("nxPrepaid");
				}
			}
			for(Pair<Item, MapleInventoryType> item : factory.loadItems(accountId, false)){
				inventory.add(item.getLeft());
			}
			try(PreparedStatement ps2 = con.prepareStatement("SELECT `sn` FROM `wishlists` WHERE `charid` = ?")){
				ps2.setInt(1, characterId);
				try(ResultSet rs = ps2.executeQuery()){
					while(rs.next()){
						wishList.add(rs.getInt("sn"));
					}
				}
			}
		}
	}

	public int getCash(int type){
		if(ironMan) return ironManNX;
		switch (type){
			case 1:
				return nxCredit;
			case 2:
				return maplePoint;
			case 4:
				return nxPrepaid;
		}
		return 0;
	}

	public void gainCash(int type, int cash){
		if(ironMan){
			ironManNX += cash;
		}else{
			switch (type){
				case 1:
					nxCredit += cash;
					break;
				case 2:
					maplePoint += cash;
					break;
				case 4:
					nxPrepaid += cash;
					break;
			}
		}
	}

	public boolean isOpened(){
		return opened != 0;
	}

	public int getOpenType(){
		return opened;
	}

	public void open(int type){
		opened = type;
	}

	public List<Item> getInventory(){
		return inventory;
	}

	public Item findByCashId(int cashId){
		boolean isRing = false;
		Equip equip = null;
		for(Item item : inventory){
			if(item.getType() == 1){
				equip = (Equip) item;
				isRing = equip.getRingId() > -1;
			}
			if((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) return item;
		}
		return null;
	}

	public void addToInventory(Item item){
		inventory.add(item);
	}

	public void removeFromInventory(Item item){
		inventory.remove(item);
	}

	public List<Integer> getWishList(){
		return wishList;
	}

	public void clearWishList(){
		wishList.clear();
	}

	public void addToWishList(int sn){
		wishList.add(sn);
	}

	public void gift(int recipient, String from, String message, int sn){
		gift(recipient, from, message, sn, -1);
	}

	public void gift(int recipient, String from, String message, int sn, int ringid){
		PreparedStatement ps = null;
		try{
			ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO `gifts` VALUES (DEFAULT, ?, ?, ?, ?, ?)");
			ps.setInt(1, recipient);
			ps.setString(2, from);
			ps.setString(3, message);
			ps.setInt(4, sn);
			ps.setInt(5, ringid);
			ps.executeUpdate();
		}catch(SQLException sqle){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public List<Pair<Item, String>> loadGifts(){
		List<Pair<Item, String>> gifts = new ArrayList<>();
		Connection con = DatabaseConnection.getConnection();
		try{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM `gifts` WHERE `to` = ?");
			ps.setInt(1, characterId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				notes++;
				CashItemData cItem = CashItemFactory.getItem(rs.getInt("sn"));
				Item item = cItem.toItem();
				Equip equip = null;
				item.setGiftFrom(rs.getString("from"));
				if(item.getType() == MapleInventoryType.EQUIP.getType()){
					equip = (Equip) item;
					equip.setRingId(rs.getInt("ringid"));
					gifts.add(new Pair<Item, String>(equip, rs.getString("message")));
				}else{
					gifts.add(new Pair<>(item, rs.getString("message")));
				}
				if(CashItemFactory.isPackage(cItem.nItemid)){ // Packages never contains a ring
					for(Item packageItem : CashItemFactory.getPackage(cItem.nItemid)){
						packageItem.setGiftFrom(rs.getString("from"));
						addToInventory(packageItem);
					}
				}else{
					addToInventory(equip == null ? item : equip);
				}
			}
			rs.close();
			ps.close();
			ps = con.prepareStatement("DELETE FROM `gifts` WHERE `to` = ?");
			ps.setInt(1, characterId);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException sqle){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, sqle);
		}
		return gifts;
	}

	public int getAvailableNotes(){
		return notes;
	}

	public void decreaseNotes(){
		notes--;
	}

	public void save(Connection con) throws SQLException{
		if(ironMan){
			try(PreparedStatement ps = con.prepareStatement("UPDATE characters SET ironManNX = ? WHERE id = ?")){
				ps.setInt(1, ironManNX);
				ps.setInt(2, characterId);
				ps.executeUpdate();
			}
		}
		try(PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `nxCredit` = ?, `maplePoint` = ?, `nxPrepaid` = ? WHERE `id` = ?")){
			ps.setInt(1, nxCredit);
			ps.setInt(2, maplePoint);
			ps.setInt(3, nxPrepaid);
			ps.setInt(4, accountId);
			ps.executeUpdate();
		}
		List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();
		for(Item item : inventory){
			itemsWithType.add(new Pair<>(item, ItemInformationProvider.getInstance().getInventoryType(item.getItemId())));
		}
		factory.saveItems(itemsWithType, accountId, con);
		try(PreparedStatement ps = con.prepareStatement("DELETE FROM `wishlists` WHERE `charid` = ?")){
			ps.setInt(1, characterId);
			ps.executeUpdate();
		}
		try(PreparedStatement ps = con.prepareStatement("INSERT INTO `wishlists` VALUES (DEFAULT, ?, ?)")){
			ps.setInt(1, characterId);
			for(int sn : wishList){
				ps.setInt(2, sn);
				ps.executeUpdate();
			}
		}
	}

	public ItemFactory getFactory(){
		return factory;
	}
}
