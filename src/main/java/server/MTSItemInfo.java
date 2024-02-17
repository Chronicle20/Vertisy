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
package server;

import client.inventory.Item;
import constants.GameConstants;
import constants.ItemConstants;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @author Traitor
 */
public class MTSItemInfo{

	private int price;
	private Item item;
	private String seller;
	private int id;
	public long date;
	public String comment = "";
	public int bidCount, bidRange, bidPrice, minPrice, maxPrice, unitPrice;
	public short status;

	public MTSItemInfo(Item item, int price, int id, int cid, String seller){
		this.item = item;
		this.price = price;
		this.seller = seller;
		this.id = id;
	}

	public Item getItem(){
		return item;
	}

	public int getPrice(){
		return price;
	}

	public int getTaxes(){
		// Before v73
		// return 100 + price / 10;
		return (int) (GameConstants.nCommissionBase + (price * (GameConstants.nCommissionRate / 100)));
	}

	public int getID(){
		return id;
	}

	/**
	 * @return Ending date, or date bought for history.
	 */
	public long getDate(){
		return date;
	}

	public String getSeller(){
		return seller;
	}

	public String getComment(){
		return comment;
	}

	public int getBidCount(){
		return bidCount;
	}

	/**
	 * @return The Bid Increment
	 */
	public int getBidRange(){
		return bidRange;
	}

	/**
	 * @return Current highest bid
	 */
	public int getBidPrice(){
		return bidPrice;
	}

	public int getMinPrice(){
		return minPrice;
	}

	/**
	 * @return The buy now price.
	 */
	public int getMaxPrice(){
		return maxPrice;
	}

	/**
	 * @return Price per item
	 */
	public int getUnitPrice(){
		return unitPrice;
	}

	/**
	 * 0: Sold
	 * 1: Purchased
	 * 2: Bid Lost
	 * 3: Cancelled
	 * 
	 * @return Current history status
	 */
	public short getStatus(){
		return status;
	}

	public void encode(MaplePacketLittleEndianWriter mplew){
		Item ii = getItem().copy();
		if(ItemConstants.isRechargable(getItem().getItemId())){
			ii.setQuantity((short) 1);
		}
		MaplePacketCreator.addItemInfo(mplew, ii, true);
		mplew.writeInt(getID()); // id
		// mplew.writeInt(item.getTaxes()); // this + below = price
		// mplew.writeInt(item.getPrice()); // price
		mplew.writeInt(getPrice());
		mplew.writeInt(getTaxes());
		mplew.writeMapleAsciiString("");
		mplew.writeMapleAsciiString("");
		mplew.writeLong(MaplePacketCreator.getTime(getDate()));
		mplew.writeMapleAsciiString(getSeller()); // account name (what was nexon thinking?)
		mplew.writeMapleAsciiString(getSeller()); // char name
		mplew.writeMapleAsciiString(getComment());
		mplew.writeInt(getBidCount());
		mplew.writeInt(getBidRange());
		mplew.writeInt(getBidPrice());
		mplew.writeInt(getMinPrice());
		mplew.writeInt(getMaxPrice());
		// mplew.writeInt(getItem().getQuantity() / (getPrice() + getTaxes()));
		mplew.writeInt(getUnitPrice());
		mplew.writeShort(getStatus());
	}
}
