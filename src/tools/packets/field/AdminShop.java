package tools.packets.field;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 24, 2017
 */
public class AdminShop{

	public static class Request{

		public static final int OpenShop = 0x0, Trade = 0x1, Close = 0x2, WishItem = 0x3;
	}

	public static class Result{

		public static final int Trade = 0x4, SoldOut = 0x5;
	}

	public static class ResultFail{

		public static final int None = 0, WrongSN = 1, WrongLevel = 2, WrongPeriod = 3, NotEnoughMeso = 4, TooMuchMeso = 5, SoldOut = 6, SoldOutForTheDay = 7, SoldOutForThePerson = 8, OverCount = 9, TradeBlocked = 10, Unknown = 11;
	}

	public static class CommodityState{

		public static final int OnSale = 0x0, SoldOut = 0x1, SoldOutForTheDay = 0x2, SoldOutForThePerson = 0x3;
	}

	/**
	 * 1, 2, 3: 'That's not something I barter. I'll show you the list again for you to choose from.'<br>
	 * 4: 'Are you begging?'<br>
	 * 5: 'Why do you carry around so much Mesos? Come back after emptying your pockets.'<br>
	 * 6: 'Taking your precious time, huh? I don't barter this item anymore.'<br>
	 * 7: 'I'm done bartering this item for the day. Come back tomorrow.'<br>
	 * 8: 'Why are you obsessing over this item? I won't barter anymore.'<br>
	 * 9: 'You're greedy. How about you lower the quantity?'<br>
	 * 10: 'Items or mesos cannot be moved.\r\nPlease contact customer support.'<br>
	 * 11: 'I'm a little preoccupied right now, so come back later.'<br>
	 */
	public static byte[] result(int mode){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADMIN_SHOP_RESULT.getValue());
		mplew.write(4);// ? always a 4
		mplew.write(mode);
		return mplew.getPacket();
	}

	// Apparently the list of items is per gm, they can customize.
	// SN is used for recv packet.
	public static byte[] commodity(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADMIN_SHOP_COMMODITY.getValue());
		mplew.writeInt(2084001);// dwNpctemplateID
		mplew.writeShort(2);// nCommodityCount
		for(int i = 0; i < 2; i++){
			mplew.writeInt(i * 10);// nSN
			mplew.writeInt(2000000);// nItemID
			mplew.writeInt(100);// nPrice
			mplew.write(CommodityState.OnSale);// nSaleState, probably CommodityState
			mplew.writeShort(200);// nMaxPerSlot
		}
		mplew.write(0);// bAskItemWishList
		return mplew.getPacket();
	}
}
