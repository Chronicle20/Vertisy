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

import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import constants.ItemConstants;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.output.LittleEndianWriter;

/**
 * @author Matze
 */
public class MapleShopItem{

	private short buyable;
	private int itemid;
	private int price;
	private byte discountRate;
	private int tokenItemID, tokenPrice;
	private int itemPeriod;
	private int levelLimited;

	public MapleShopItem(ResultSet rs) throws SQLException{
		itemid = rs.getInt("itemid");
		price = rs.getInt("price");
		discountRate = rs.getByte("discountRate");
		tokenItemID = rs.getInt("tokenItemID");
		tokenPrice = rs.getInt("tokenPrice");
		itemPeriod = rs.getInt("itemPeriod");
		levelLimited = rs.getInt("levelLimited");
		buyable = 1000;// ?
		// if(ItemConstants.isRechargable(rs.getInt("itemid"))){
		// }
	}

	public MapleShopItem(short buyable, int itemid){
		this.buyable = buyable;
		this.itemid = itemid;
	}

	public short getBuyable(){
		return buyable;
	}

	public int getItemId(){
		return itemid;
	}

	public int getPrice(){
		return price;
	}

	public int getTokenItemID(){
		return tokenItemID;
	}

	public int getTokenPrice(){
		return tokenPrice;
	}

	public void encode(MapleClient c, LittleEndianWriter lew){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		ItemData data = ii.getItemData(getItemId());
		lew.writeInt(itemid);
		lew.writeInt(price);
		lew.write(discountRate);
		lew.writeInt(tokenItemID);
		lew.writeInt(tokenPrice);
		lew.writeInt(itemPeriod);// Can be used x minutes after purchase
		lew.writeInt(levelLimited);
		if(!ItemConstants.isRechargable(getItemId())){
			lew.writeShort(1); // nQuantity
			lew.writeShort(getBuyable());
		}else{
			lew.writeDouble(data.unitPrice);
			lew.writeShort(data.getSlotMax(c));
		}
	}
	/*
	 * int nMaxPerSlot;
	if (ItemConstants.is_rechargeable_item(pShopItem.nItemID)) {
	oPacket.EncodeBuffer(pShopItem.dUnitPrice, 0x8);
	nMaxPerSlot = SkillInfo.GetBundleItemMaxPerSlot(pShopItem.nItemID, pUser.character);
	} else {
	oPacket.Encode2(pShopItem.nQuantity);
	BundleItem pBundleItem = ItemInfo.GetBundleItem(pShopItem.nItemID);
	if (pBundleItem == null) {
	nMaxPerSlot = 1;
	} else {
	nMaxPerSlot = pBundleItem.nMaxPerSlot;
	}
	}
	oPacket.Encode2(nMaxPerSlot);
	 */
}
