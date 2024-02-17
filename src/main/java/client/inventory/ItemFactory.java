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
package client.inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import client.MapleCharacter;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Flav
 */
public enum ItemFactory{
	INVENTORY(1, false),
	STORAGE(2, true),
	CASH_EXPLORER(3, true),
	CASH_CYGNUS(4, false),
	CASH_ARAN(5, false),
	MERCHANT(6, false);

	private int value;
	private boolean account;
	private static ReentrantLock lock = new ReentrantLock(true);

	private ItemFactory(int value, boolean account){
		this.value = value;
		this.account = account;
	}

	public int getValue(){
		return value;
	}

	public List<Pair<Item, MapleInventoryType>> loadItems(int id, boolean login) throws SQLException{
		List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			StringBuilder query = new StringBuilder();
			query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`sn`) WHERE `type` = ? AND `");
			query.append(account ? "accountid" : "characterid").append("` = ?");
			if(login){
				query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
			}
			ps = DatabaseConnection.getConnection().prepareStatement(query.toString());
			ps.setInt(1, value);
			ps.setInt(2, id);
			rs = ps.executeQuery();
			while(rs.next()){
				MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
				if(mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)){
					items.add(new Pair<Item, MapleInventoryType>(new Equip(rs), mit));
				}else{
					items.add(new Pair<>(new Item(rs), mit));
				}
			}
			rs.close();
			ps.close();
		}finally{
			if(rs != null){
				rs.close();
			}
			if(ps != null){
				ps.close();
			}
		}
		return items;
	}

	public Map<MapleInventoryType, List<Item>> loadItems(int id) throws SQLException{
		Map<MapleInventoryType, List<Item>> ret = new HashMap<>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			StringBuilder query = new StringBuilder();
			query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`sn`) WHERE `type` = ? AND `");
			query.append(account ? "accountid" : "characterid").append("` = ?");
			/*if(login){
				query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
			}*/
			ps = DatabaseConnection.getConnection().prepareStatement(query.toString());
			ps.setInt(1, value);
			ps.setInt(2, id);
			rs = ps.executeQuery();
			while(rs.next()){
				MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
				List<Item> items = ret.get(mit);
				if(items == null) items = new ArrayList<>();
				if(mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)){
					items.add(new Equip(rs));
				}else{
					items.add(new Item(rs));
				}
				ret.put(mit, items);
			}
			rs.close();
			ps.close();
		}finally{
			if(rs != null){
				rs.close();
			}
			if(ps != null){
				ps.close();
			}
		}
		return ret;
	}

	public static void deleteItem(Item item){
		if(item.nSN < 0) return;
		// System.out.println("Deleting item with sn: " + item.nSN);
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement delete = con.prepareStatement("DELETE FROM inventoryitems WHERE sn = ?")){
			delete.setInt(1, item.nSN);
			delete.executeUpdate();
			if(item.isEquip()){
				try(PreparedStatement deletee = con.prepareStatement("DELETE FROM inventoryequipment WHERE sn = ?")){
					deletee.setInt(1, item.nSN);
					deletee.executeUpdate();
				}
			}
			item.nSN = -1;
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public static void clearItemOwner(Item item, ItemFactory factory){
		Connection con = DatabaseConnection.getConnection();
		if(item.nSN >= 0){
			try(PreparedStatement ps = con.prepareStatement("UPDATE inventoryitems SET accountid = ?, characterid = ?, type = ? WHERE sn = ?")){
				ps.setString(1, factory.account ? String.valueOf(-1) : null);
				ps.setString(2, factory.account ? null : String.valueOf(-1));
				ps.setInt(3, factory.value);
				ps.setInt(4, item.nSN);
				ps.executeUpdate();
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public static void updateItemOwner(MapleCharacter chr, Item item, ItemFactory factory){
		Connection con = DatabaseConnection.getConnection();
		if(item.nSN >= 0){
			try(PreparedStatement ps = con.prepareStatement("UPDATE inventoryitems SET accountid = ?, characterid = ?, type = ? WHERE sn = ?")){
				ps.setString(1, factory.account ? String.valueOf(chr.getAccountID()) : null);
				ps.setString(2, factory.account ? null : String.valueOf(chr.getId()));
				ps.setInt(3, factory.value);
				ps.setInt(4, item.nSN);
				ps.executeUpdate();
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	@SuppressWarnings("resource")
	public synchronized void saveItems(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException{
		lock.lock();
		try{
			try(PreparedStatement insertDefault = con.prepareStatement("INSERT INTO inventoryitems(sn, type, characterid, accountid, itemid, inventorytype, position, quantity, owner, petid, flag, expiration, lockExpiration, giftFrom, bundles)" + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE sn = VALUES(sn), type = VALUES(type), characterid = VALUES(characterid), accountid = VALUES(accountid), itemid = VALUES(itemid), inventorytype = VALUES(inventorytype), " + "position = VALUES(position), quantity = VALUES(quantity), owner = VALUES(owner), petid = VALUES(petid), flag = VALUES(flag), expiration = VALUES(expiration), lockExpiration = VALUES(lockExpiration), " + "giftFrom = VALUES(giftFrom), bundles = VALUES(bundles)", Statement.RETURN_GENERATED_KEYS)){
				try(PreparedStatement insertSN = con.prepareStatement("INSERT INTO inventoryitems(sn, type, characterid, accountid, itemid, inventorytype, position, quantity, owner, petid, flag, expiration, lockExpiration, giftFrom, bundles)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE sn = VALUES(sn), type = VALUES(type), characterid = VALUES(characterid), accountid = VALUES(accountid), itemid = VALUES(itemid), inventorytype = VALUES(inventorytype), " + "position = VALUES(position), quantity = VALUES(quantity), owner = VALUES(owner), petid = VALUES(petid), flag = VALUES(flag), expiration = VALUES(expiration), lockExpiration = VALUES(lockExpiration), " + "giftFrom = VALUES(giftFrom), bundles = VALUES(bundles)", Statement.RETURN_GENERATED_KEYS)){
					try(PreparedStatement delete = con.prepareStatement("DELETE FROM inventoryitems WHERE sn = ?")){
						if(!items.isEmpty()){
							for(Pair<Item, MapleInventoryType> pair : items){
								Item item = pair.getLeft();
								PreparedStatement insert = item.nSN >= 0 ? insertSN : insertDefault;
								MapleInventoryType mit = pair.getRight();
								if(item.hasDBFlag(ItemDB.INSERT) || item.hasDBFlag(ItemDB.UPDATE)){
									// System.out.println("Inserting or updating: " + item.nSN + " flag: " + item.dbFlag);
									int pos = 0;
									if(item.nSN >= 0) insert.setInt(++pos, item.nSN);
									insert.setInt(++pos, value);
									insert.setString(++pos, account ? null : String.valueOf(id));
									insert.setString(++pos, account ? String.valueOf(id) : null);
									insert.setInt(++pos, item.getItemId());
									insert.setInt(++pos, mit.getType());
									insert.setShort(++pos, item.getPosition());
									insert.setInt(++pos, item.getQuantity());
									insert.setString(++pos, item.getOwner());
									insert.setInt(++pos, item.getPetId());
									insert.setInt(++pos, item.getFlag());
									insert.setLong(++pos, item.getExpiration());
									insert.setLong(++pos, item.getLockExpiration());
									insert.setString(++pos, item.getGiftFrom());
									insert.setShort(++pos, item.getPerBundle());
									item.clearDBFlag();
									insert.executeUpdate();
									try(ResultSet rs = insert.getGeneratedKeys()){
										if(rs.next()) item.nSN = rs.getInt(1);
									}
									try(PreparedStatement inserte = con.prepareStatement("INSERT INTO inventoryequipment(sn, upgradeslots, level, str, `dex`, `int`, `luk`, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious, " + "itemlevel, itemexp, ringid, learnedSkills, durability, grade, chuc, option1, option2, option3) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " + "ON DUPLICATE KEY UPDATE sn = VALUES(sn), upgradeslots = VALUES(upgradeslots), level = VALUES(level), " + "str = VALUES(str), dex = VALUES(dex), `int` = VALUES(`int`), luk = VALUES(luk), hp = VALUES(hp), mp = VALUES(mp), watk = VALUES(watk), matk = VALUES(matk), wdef = VALUES(wdef), " + "mdef = VALUES(mdef), acc = VALUES(acc), avoid = VALUES(avoid), hands = VALUES(hands), speed = VALUES(speed), jump = VALUES(jump), vicious = VALUES(vicious), " + "itemlevel = VALUES(itemlevel), itemexp = VALUES(itemexp), ringid = VALUES(ringid), learnedSkills = VALUES(learnedSkills), durability = VALUES(durability), " + "grade = VALUES(grade), chuc = VALUES(chuc), option1 = VALUES(option1), option2 = VALUES(option2), option3 = VALUES(option3)")){
										if(item.isEquip()){
											inserte.setInt(1, item.nSN);
											Equip equip = (Equip) item;
											inserte.setInt(2, equip.getUpgradeSlots());
											inserte.setInt(3, equip.getLevel());
											inserte.setInt(4, equip.getStr());
											inserte.setInt(5, equip.getDex());
											inserte.setInt(6, equip.getInt());
											inserte.setInt(7, equip.getLuk());
											inserte.setInt(8, equip.getHp());
											inserte.setInt(9, equip.getMp());
											inserte.setInt(10, equip.getWatk());
											inserte.setInt(11, equip.getMatk());
											inserte.setInt(12, equip.getWdef());
											inserte.setInt(13, equip.getMdef());
											inserte.setInt(14, equip.getAcc());
											inserte.setInt(15, equip.getAvoid());
											inserte.setInt(16, equip.getHands());
											inserte.setInt(17, equip.getSpeed());
											inserte.setInt(18, equip.getJump());
											inserte.setInt(19, equip.getVicious());
											inserte.setInt(20, equip.getItemLevel());
											inserte.setFloat(21, equip.getItemExp());
											inserte.setInt(22, equip.getRingId());
											inserte.setBoolean(23, equip.hasLearnedSkills());
											inserte.setInt(24, equip.getDurability());
											inserte.setByte(25, equip.getGrade());
											inserte.setByte(26, equip.getChuc());
											inserte.setShort(27, equip.getOption1());
											inserte.setShort(28, equip.getOption2());
											inserte.setShort(29, equip.getOption3());
											inserte.executeUpdate();
										}
									}
								}else if(item.hasDBFlag(ItemDB.DELETE)){
									delete.setInt(1, item.nSN);
									delete.executeUpdate();
									if(item.isEquip()){
										try(PreparedStatement deletee = con.prepareStatement("DELETE FROM inventoryequipment WHERE sn = ?")){
											deletee.setInt(1, item.nSN);
											deletee.executeUpdate();
										}
									}
								}
							}
						}
					}
				}
			}
		}finally{
			lock.unlock();
		}
	}
}