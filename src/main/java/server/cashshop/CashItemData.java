package server.cashshop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import provider.MapleData;
import provider.MapleDataTool;
import server.ItemInformationProvider;
import tools.ObjectParser;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 13, 2017
 */
public class CashItemData{

	public int sn, modifier;
	public byte nPriority, nCommodityGender, nClass, nLimit;
	public boolean bBonus, bForPremiumUser, bOnSale;
	public short nCount = 1, nPeriod, nReqPOP, nReqLEV, nPbCash, nPbPoint, nPbGift;
	public int nItemid, nPrice, nMaplePoint, nMeso;
	public List<Integer> packageSN = new ArrayList<>();

	public void load(MapleData data){
		switch (data.getName()){
			case "SN":
				sn = MapleDataTool.getInt(data);
				break;
			case "ItemId":
				nItemid = MapleDataTool.getInt(data);
				break;
			case "Count":
				nCount = MapleDataTool.getShort(data);
				break;
			case "Price":
				nPrice = MapleDataTool.getInt(data);
				break;
			case "Period":
				nPeriod = MapleDataTool.getShort(data);
				break;
			case "Priority":
				nPriority = (byte) MapleDataTool.getInt(data);
				break;
			case "Gender":
				nCommodityGender = (byte) MapleDataTool.getInt(data);
				break;
			case "OnSale":
				bOnSale = MapleDataTool.getInt(data) == 1;
				break;
			case "Class":
				nClass = (byte) MapleDataTool.getInt(data);
				break;
			case "PbCash":
				nPbCash = MapleDataTool.getShort(data);
				break;
			case "PbPoint":
				nPbPoint = MapleDataTool.getShort(data);
				break;
			case "PbGift":
				nPbGift = MapleDataTool.getShort(data);
				break;
			case "Limit":
				nLimit = (byte) MapleDataTool.getInt(data);
				break;
			case "Bonus":
				bBonus = MapleDataTool.getInt(data) == 1;
				break;
			default:
				System.out.println("Unhandled CashItem data: " + data.getName());
				break;
		}
	}

	public void load(LittleEndianAccessor lea){
		sn = lea.readInt();
		nPriority = lea.readByte();
		nCommodityGender = lea.readByte();
		nClass = lea.readByte();
		nLimit = lea.readByte();
		bBonus = lea.readBoolean();
		bForPremiumUser = lea.readBoolean();
		bOnSale = lea.readBoolean();
		nCount = lea.readShort();
		nPeriod = lea.readShort();
		nReqPOP = lea.readShort();
		nReqLEV = lea.readShort();
		nPbCash = lea.readShort();
		nPbPoint = lea.readShort();
		nPbGift = lea.readShort();
		nItemid = lea.readInt();
		nPrice = lea.readInt();
		nMaplePoint = lea.readInt();
		nMeso = lea.readInt();
		byte size = lea.readByte();
		for(int i = 0; i < size; i++){
			packageSN.add(lea.readInt());
		}
	}

	public void saveToBin(LittleEndianWriter lew){
		lew.writeInt(sn);
		lew.write(nPriority);
		lew.write(nCommodityGender);
		lew.write(nClass);
		lew.write(nLimit);
		lew.writeBoolean(bBonus);
		lew.writeBoolean(bForPremiumUser);
		lew.writeBoolean(bOnSale);
		lew.writeShort(nCount);
		lew.writeShort(nPeriod);
		lew.writeShort(nReqPOP);
		lew.writeShort(nReqLEV);
		lew.writeShort(nPbCash);
		lew.writeShort(nPbPoint);
		lew.writeShort(nPbGift);
		lew.writeInt(nItemid);
		lew.writeInt(nPrice);
		lew.writeInt(nMaplePoint);
		lew.writeInt(nMeso);
		lew.write(packageSN.size());
		for(int sn : packageSN){
			lew.writeInt(sn);
		}
	}

	public void load(ResultSet rs) throws SQLException{
		sn = rs.getInt("sn");
		nPriority = rs.getByte("priority");
		nCommodityGender = rs.getByte("gender");
		nClass = rs.getByte("class");
		nLimit = rs.getByte("limit");
		bBonus = rs.getBoolean("bonus");
		bForPremiumUser = rs.getBoolean("forPremiumUser");
		bOnSale = rs.getBoolean("onSale");
		nCount = rs.getShort("count");
		nPeriod = rs.getShort("period");
		nReqPOP = rs.getShort("reqPOP");
		nReqLEV = rs.getShort("reqLEV");
		nPbCash = rs.getShort("pbCash");
		nPbPoint = rs.getShort("pbPoint");
		nPbGift = rs.getShort("pbGift");
		nItemid = rs.getInt("itemid");
		nPrice = rs.getInt("price");
		nMaplePoint = rs.getInt("maplePoint");
		nMeso = rs.getInt("meso");
		String packageData = rs.getString("packageSN");
		if(packageData != null && packageData.length() > 0){
			for(String s : packageData.split(",")){
				if(s != null && s.length() > 0) packageSN.add(ObjectParser.isInt(s));
			}
		}
		CashItemData ci = CashItemFactory.getItem(sn);
		if(ci != null){
			if(ci.nPriority != nPriority) modifier |= CashModification.PRIORITY;
			if(ci.nCommodityGender != nCommodityGender) modifier |= CashModification.COMMODITYGENDER;
			if(ci.nClass != nClass) modifier |= CashModification.CLASS;
			if(ci.nLimit != nLimit) modifier |= CashModification.LIMIT;
			if(ci.bBonus != bBonus) modifier |= CashModification.BONUS;
			if(ci.bForPremiumUser != bForPremiumUser) modifier |= CashModification.FORPREMIUMUSER;
			if(ci.bOnSale != bOnSale) modifier |= CashModification.ONSALE;
			if(ci.nCount != nCount) modifier |= CashModification.COUNT;
			if(ci.nPeriod != nPeriod) modifier |= CashModification.PERIOD;
			if(ci.nReqPOP != nReqPOP) modifier |= CashModification.REQPOP;
			if(ci.nReqLEV != nReqLEV) modifier |= CashModification.REQLEV;
			if(ci.nPbCash != nPbCash) modifier |= CashModification.PBCASH;
			if(ci.nPbPoint != nPbPoint) modifier |= CashModification.PBPOINT;
			if(ci.nPbGift != nPbGift) modifier |= CashModification.PBGIFT;
			if(ci.nItemid != nItemid) modifier |= CashModification.ITEMID;
			if(ci.nPrice != nPrice) modifier |= CashModification.PRICE;
			if(ci.nMaplePoint != nMaplePoint) modifier |= CashModification.MAPLEPOINT;
			if(ci.nMeso != nMeso) modifier |= CashModification.MESO;
			if(ci.packageSN.size() != packageSN.size()) modifier |= CashModification.PACKAGESN;
			else{
				for(int i = 0; i < ci.packageSN.size(); i++){
					if(ci.packageSN.get(i) != packageSN.get(i)){
						modifier |= CashModification.PACKAGESN;
						break;
					}
				}
			}
		}else{
			// List<Item> packages = CashItemFactory.getPackage(nItemid);
			// if()
		}
	}

	public Item toItem(){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Item item;
		int petid = -1;
		if(ItemConstants.isPet(nItemid)){
			petid = MaplePet.createPet(nItemid);
		}
		if(ii.getInventoryType(nItemid).equals(MapleInventoryType.EQUIP)){
			item = ii.getEquipById(nItemid);
		}else{
			item = new Item(nItemid, (byte) 0, nCount, petid);
		}
		if(ItemConstants.EXPIRING_ITEMS){
			if(nItemid == 5211048 || nItemid == 5360042){ // 4 Hour 2X coupons, the period is 1, but we don't want them to last a day.
				item.setExpiration(System.currentTimeMillis() + (1000L * 60 * 60 * 4));
			}else{
				item.setExpiration(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * nPeriod));
			}
		}
		item.setOldSN(sn);
		return item;
	}

	public void encode(LittleEndianWriter lew){
		lew.writeInt(sn);
		// CS_COMMODITY::DecodeModifiedData
		lew.writeInt(modifier);
		if((modifier & CashModification.ITEMID) > 0) lew.writeInt(nItemid);
		if((modifier & CashModification.COUNT) > 0) lew.writeShort(nCount);
		if((modifier & CashModification.PRIORITY) > 0) lew.write(nPriority);
		if((modifier & CashModification.PRICE) > 0) lew.writeInt(nPrice);
		if((modifier & CashModification.BONUS) > 0) lew.writeBoolean(bBonus);
		if((modifier & CashModification.PERIOD) > 0) lew.writeShort(nPeriod);
		if((modifier & CashModification.REQPOP) > 0) lew.writeShort(nReqPOP);
		if((modifier & CashModification.REQLEV) > 0) lew.writeShort(nReqLEV);
		if((modifier & CashModification.MAPLEPOINT) > 0) lew.writeInt(nMaplePoint);
		if((modifier & CashModification.MESO) > 0) lew.writeInt(nMeso);
		if((modifier & CashModification.FORPREMIUMUSER) > 0) lew.writeBoolean(bForPremiumUser);
		if((modifier & CashModification.COMMODITYGENDER) > 0) lew.write(nCommodityGender);
		if((modifier & CashModification.ONSALE) > 0) lew.writeBoolean(bOnSale);
		if((modifier & CashModification.CLASS) > 0) lew.write(nClass);
		if((modifier & CashModification.LIMIT) > 0) lew.write(nLimit);
		if((modifier & CashModification.PBCASH) > 0) lew.writeShort(nPbCash);
		if((modifier & CashModification.PBPOINT) > 0) lew.writeShort(nPbPoint);
		if((modifier & CashModification.PBGIFT) > 0) lew.writeShort(nPbGift);
		if((modifier & CashModification.PACKAGESN) > 0){
			lew.write(packageSN.size());
			for(int sn : packageSN){
				lew.writeInt(sn);
			}
		}
	}
}