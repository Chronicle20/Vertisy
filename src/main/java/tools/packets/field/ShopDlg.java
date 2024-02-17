package tools.packets.field;

import java.util.List;

import client.MapleClient;
import net.SendOpcode;
import server.shops.MapleShopItem;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class ShopDlg{

	public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(items.size()); // item count
		for(MapleShopItem item : items){
			item.encode(c, mplew);
		}
		return mplew.getPacket();
	}

	/* 00 = /
	 * 01 = You don't have enough in stock
	 * 02 = You do not have enough mesos
	 * 03 = Please check if your inventory is full or not
	 * 05 = You don't have enough in stock
	 * 06 = Due to an error, the trade did not happen
	 * 07 = Due to an error, the trade did not happen
	 * 08 = /
	 * 09 = You don't have enough in stock
	 * 10 = You do not have enough meso
	 * 0D = You need more items
	 * 14 = You must be under lv.x to purchase this item
	 * 15 = You must be over level x to be purchase this item
	 * 19 = Custom message
	 */
	public static byte[] shopTransaction(byte code){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
		mplew.write(code);
		if(code == ShopRes.ServerMsg){
			mplew.writeBoolean(true);// if false, no msg
			mplew.writeMapleAsciiString("test");
		}else if(code == ShopRes.LimitLevel_Less || code == ShopRes.LimitLevel_More){
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static class ShopReq{

		public static final int Buy = 0x0, Sell = 0x1, Recharge = 0x2, Close = 0x3;
	}

	public static class ShopRes{

		public static final int BuySuccess = 0, BuyNoStock = 1, BuyNoMoney = 2, BuyUnknown = 3, SellSuccess = 4, SellNoStock = 5, SellIncorrectRequest = 6, SellUnkonwn = 7, RechargeSuccess = 8, RechargeNoStock = 9, RechargeNoMoney = 10, RechargeIncorrectRequest = 11, RechargeUnknown = 12, BuyNoToken = 13, LimitLevel_Less = 14, LimitLevel_More = 15, CantBuyAnymore = 16, TradeBlocked = 17, BuyLimit = 18, ServerMsg = 19;
	}
}
