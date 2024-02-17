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
package client.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Item implements Comparable<Item>{

	public int nSN = -1;
	protected int id;
	private int cashId;
	private int oldSN;
	protected short position;
	protected short quantity;
	private int petid = -1;
	private MaplePet pet = null;
	protected String owner = "";
	protected List<String> log;
	private byte flag;
	protected long expiration = -1;
	protected long lockExpiration = -1;
	protected String giftFrom = "";
	protected short perBundle;
	public int dbFlag;

	public Item(int id, short quantity){
		this.id = id;
		this.position = -1;
		this.quantity = quantity;
		this.log = new LinkedList<>();
		this.flag = 0;
		this.perBundle = 1;
		this.addDBFlag(ItemDB.INSERT);
	}

	public Item(int id, short position, short quantity){
		this.id = id;
		this.position = position;
		this.quantity = quantity;
		this.log = new LinkedList<>();
		this.flag = 0;
		this.perBundle = 1;
		this.addDBFlag(ItemDB.INSERT);
	}

	public Item(int id, short position, short quantity, int petid){
		this.id = id;
		this.position = position;
		this.quantity = quantity;
		this.petid = petid;
		if(petid > -1) this.pet = MaplePet.loadFromDb(id, position, petid);
		this.flag = 0;
		this.log = new LinkedList<>();
		this.perBundle = 1;
		this.addDBFlag(ItemDB.INSERT);
	}

	public Item(ResultSet rs) throws SQLException{
		nSN = rs.getInt("sn");
		id = rs.getInt("itemid");
		setPosition(rs.getShort("position"));
		setQuantity(rs.getShort("quantity"));
		setPetId(rs.getInt("petid"));
		if(petid > -1) this.pet = MaplePet.loadFromDb(id, position, petid);
		setOwner(rs.getString("owner"));
		setExpiration(rs.getLong("expiration"));
		setLockExpiration(rs.getLong("lockExpiration"));
		setGiftFrom(rs.getString("giftFrom"));
		setFlag(rs.getByte("flag"));
		setPerBundle(rs.getShort("bundles"));
		this.dbFlag = 0;
		this.log = new LinkedList<>();
	}

	public Item copy(){
		return copy(false);
	}

	public Item copy(boolean copySN){
		Item ret = new Item(id, position, quantity, petid);
		ret.owner = owner;
		ret.expiration = expiration;
		ret.lockExpiration = lockExpiration;
		ret.giftFrom = giftFrom;
		ret.flag = flag;
		ret.perBundle = perBundle;
		ret.log = new LinkedList<>(log);
		ret.dbFlag = dbFlag;
		if(copySN) ret.nSN = nSN;
		return ret;
	}

	public void setPosition(short position){
		this.position = position;
		if(pet != null) pet.setPosition(position);
		addDBFlag(ItemDB.UPDATE);
	}

	public void setQuantity(short quantity){
		this.quantity = quantity;
		addDBFlag(ItemDB.UPDATE);
	}

	public int getItemId(){
		return id;
	}

	public int getCashId(){
		if(cashId == 0){
			cashId = new Random().nextInt(Integer.MAX_VALUE) + 1;
		}
		return cashId;
	}

	public short getPosition(){
		return position;
	}

	public short getQuantity(){
		return quantity;
	}

	public byte getType(){
		if(getPetId() > -1) return 3;
		return 2;
	}

	public String getOwner(){
		return owner;
	}

	public void setOwner(String owner){
		this.owner = owner;
		addDBFlag(ItemDB.UPDATE);
	}

	public int getPetId(){
		return petid;
	}

	public void setPetId(int id){
		this.petid = id;
		addDBFlag(ItemDB.UPDATE);
	}

	@Override
	public int compareTo(Item other){
		if(this.id < other.getItemId()){
			return -1;
		}else if(this.id > other.getItemId()) return 1;
		return 0;
	}

	public List<String> getLog(){
		return Collections.unmodifiableList(log);
	}

	public void addLog(String s){
		log.add(s);
		addDBFlag(ItemDB.UPDATE);
	}

	public byte getFlag(){
		return flag;
	}

	public void setFlag(byte b){
		this.flag = b;
		addDBFlag(ItemDB.UPDATE);
	}

	public long getExpiration(){
		return expiration;
	}

	public void setExpiration(long expire){
		this.expiration = expire;
		addDBFlag(ItemDB.UPDATE);
	}

	public long getLockExpiration(){
		return lockExpiration;
	}

	public void setLockExpiration(long expire){
		this.lockExpiration = expire;
		addDBFlag(ItemDB.UPDATE);
	}

	public int getOldSN(){
		return oldSN;
	}

	public void setOldSN(int sn){
		this.oldSN = sn;
	}

	public String getGiftFrom(){
		return giftFrom;
	}

	public void setGiftFrom(String giftFrom){
		this.giftFrom = giftFrom;
		addDBFlag(ItemDB.UPDATE);
	}

	public MaplePet getPet(){
		return pet;
	}

	public short getPerBundle(){
		return perBundle;
	}

	public void setPerBundle(short perBundle){
		this.perBundle = perBundle;
		addDBFlag(ItemDB.UPDATE);
	}

	public void addDBFlag(int flag){
		if((this.dbFlag & flag) > 0) return;
		// System.out.println("Adding flag: " + flag);
		this.dbFlag |= flag;
	}

	public void removeDBFlag(int flag){
		if((this.dbFlag & flag) > 0){
			this.dbFlag -= flag;
		}
	}

	public void clearDBFlag(){
		this.dbFlag = 0;
	}

	public boolean hasDBFlag(int flag){
		return (this.dbFlag & flag) > 0;
	}

	public boolean isEquip(){
		return this.getItemId() / 1000000 == 1;
	}

	public boolean equalss(Object item){
		if(item == null) return false;
		if(!(item instanceof Item)) return false;
		Item other = (Item) item;
		if(id != other.id) return false;
		if(cashId != other.cashId) return false;
		if(oldSN != other.oldSN) return false;
		if(owner == null && other.owner == null || !owner.equals(other.owner)) return false;
		if(flag != other.flag) return false;
		if(expiration != other.expiration) return false;
		if(lockExpiration != other.lockExpiration) return false;
		if(!giftFrom.equals(other.giftFrom)) return false;
		return true;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + cashId;
		result = prime * result + (int) (expiration ^ (expiration >>> 32));
		result = prime * result + flag;
		result = prime * result + ((giftFrom == null) ? 0 : giftFrom.hashCode());
		result = prime * result + id;
		result = prime * result + (int) (lockExpiration ^ (lockExpiration >>> 32));
		result = prime * result + oldSN;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + perBundle;
		result = prime * result + petid;
		result = prime * result + position;
		result = prime * result + quantity;
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Item other = (Item) obj;
		if(cashId != other.cashId) return false;
		if(expiration != other.expiration) return false;
		if(flag != other.flag) return false;
		if(giftFrom == null){
			if(other.giftFrom != null) return false;
		}else if(!giftFrom.equals(other.giftFrom)) return false;
		if(id != other.id) return false;
		if(lockExpiration != other.lockExpiration) return false;
		if(oldSN != other.oldSN) return false;
		if(owner == null){
			if(other.owner != null) return false;
		}else if(!owner.equals(other.owner)) return false;
		if(perBundle != other.perBundle) return false;
		if(petid != other.petid) return false;
		if(position != other.position) return false;
		if(quantity != other.quantity) return false;
		return true;
	}

	public boolean softEquals(Item other){
		if(expiration != other.expiration) return false;
		if(flag != other.flag) return false;
		if(giftFrom == null){
			if(other.giftFrom != null) return false;
		}else if(!giftFrom.equals(other.giftFrom)) return false;
		if(id != other.id) return false;
		if(lockExpiration != other.lockExpiration) return false;
		if(oldSN != other.oldSN) return false;
		if(owner == null){
			if(other.owner != null) return false;
		}else if(!owner.equals(other.owner)) return false;
		if(perBundle != other.perBundle) return false;
		return true;
	}

	@Override
	public String toString(){
		return "Item [nSN=" + nSN + ", id=" + id + ", cashId=" + cashId + ", oldSN=" + oldSN + ", position=" + position + ", quantity=" + quantity + ", petid=" + petid + ", pet=" + pet + ", owner=" + owner + ", log=" + log + ", flag=" + flag + ", expiration=" + expiration + ", lockExpiration=" + lockExpiration + ", giftFrom=" + giftFrom + ", perBundle=" + perBundle + ", dbFlag=" + dbFlag + "]";
	}
}
