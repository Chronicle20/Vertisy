package tools.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import net.SendOpcode;
import server.cashshop.*;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 30, 2017
 */
public class CCashShop{

	public static class CashItemReq{

		public static final short Buy = 3, Gift = 4, SetWish = 5, IncSlotCount = 6, IncTrunkCount = 7, IncCharSlotCount = 8, MoveLtoS = 14, MoveStoL = 15, Couple = 30, BuyPackage = 31, BuyNormal = 33, FriendShip = 36;
	}

	public static byte[] openCashShop(MapleClient c) throws Exception{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_CASH_SHOP.getValue());
		MaplePacketCreator.addCharacterInfo(mplew, c.getPlayer());
		// start of CCashShop::CCashShop(v5, iPacket); and CCashShop::LoadData(v2, iPacket);
		boolean bCashShopAuthorized = true;
		mplew.writeBoolean(bCashShopAuthorized);
		if(bCashShopAuthorized) mplew.writeMapleAsciiString("Arnah");
		// CWvsContext::SetSaleInfo
		mplew.writeInt(0);// if > 0, decodeBuffer 4 * amount
		List<CashItemData> lsci = CashItemFactory.getModifiedCommodity();
		mplew.writeShort(lsci.size());
		for(CashItemData cmd : lsci){
			cmd.encode(mplew);// CS_COMMODITY::DecodeModifiedData
		}
		mplew.write(CashItemFactory.categoryDiscount.size());
		for(CategoryDiscount cd : CashItemFactory.categoryDiscount){
			cd.encode(mplew);
		}
		// aBest(buffer of 1080)
		for(int j = 0; j < 2; j++){// gender
			int[] aBest = {50200004, 50200069, 50200117, 50100008, 50000047};
			int index = 0;
			List<BestItem> bestItems = new ArrayList<>(CashItemFactory.bestItems.values());
			Collections.sort(bestItems);
			for(BestItem bItem : bestItems){
				if((bItem.nCommodityGender == 2 || bItem.nCommodityGender == j)){
					aBest[index++] = bItem.sn;
				}
				if(index == aBest.length) break;
			}
			for(int i = 0; i < 9; i++){// category
				for(int sn : aBest){
					mplew.writeInt(i);// category
					mplew.writeInt(j);// gender
					mplew.writeInt(sn);// sn
				}
			}
			bestItems.clear();
		}
		// CCashShop::DecodeStock, CStockInfo::EncodeStock
		List<Integer> stockSN = new ArrayList<>();
		for(LimitedGood lg : CashItemFactory.limitedGoods){
			for(int sn : lg.nSN){
				if(sn != 0) stockSN.add(sn);
			}
		}
		mplew.writeShort(stockSN.size());// decodeBuffer 8 * amount
		for(int sn : stockSN){
			mplew.writeInt(sn);// nSN
			LimitedGood lg = CashItemFactory.getGoodFromSN(sn);
			mplew.writeInt(lg.getStockState(CashItemFactory.getItem(sn)));// nStockState,
		}
		// CCashShop::DecodeLimitGoods, CLimitSell::EncodeLimitGoods
		mplew.writeShort(CashItemFactory.limitedGoods.size());// decodeBuffer 104 * amount
		for(LimitedGood lg : CashItemFactory.limitedGoods){
			lg.encode(mplew);
		}
		// CCashShop::DecodeZeroGoods
		mplew.writeShort(0);// this shit broken af decodeBuffer 68 * amount.
		for(int i = 0; i < 0; i++){
			/*
			 struct CS_ZEROGOODS
			{
			int nStartSN;
			int nEndSN;
			int nGoodsCount;
			int nEventSN;
			int nExpireDays;
			unsigned int dwConditionFlag;
			int nDateStart;
			int nDateEnd;
			int nHourStart;
			int nHourEnd;
			int abWeek[7];
			};
			 */
			mplew.writeInt(0);// nStartSN
			mplew.writeInt(0);// nEndSN
			mplew.writeInt(50);// nGoodsCount
			mplew.writeInt(10102346);// nEventSN
			mplew.writeInt(40);// nExpireDays
			mplew.writeInt(0);// dwConditionFlag
			mplew.writeInt(0);// nDateStart
			mplew.writeInt(31);// nDateEnd
			mplew.writeInt(0);// nHourStart
			mplew.writeInt(24);// nHourEnd
			List<Boolean> abWeek = new ArrayList<>(Collections.nCopies(7, true));
			for(boolean week : abWeek){
				mplew.writeInt(week ? 1 : 0);// abWeek
			}
		}
		// end of CCashShop::LoadData
		mplew.write(0);// bEventOn - apparently never used
		mplew.writeInt(75);// nHighestCharacterLevelInThisAccount
		return mplew.getPacket();
	}

	public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId){
		addCashItemInformation(mplew, item, accountId, null);
	}

	public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId, String giftMessage){
		boolean isGift = giftMessage != null;
		boolean isRing = false;
		Equip equip = null;
		if(item.getType() == 1){
			equip = (Equip) item;
			isRing = equip.getRingId() > -1;
		}
		mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
		if(!isGift){
			mplew.writeInt(accountId);
			mplew.writeInt(0);// dwCharacterID
		}
		mplew.writeInt(item.getItemId());
		if(!isGift){
			mplew.writeInt(item.getOldSN());
			mplew.writeShort(item.getQuantity());
		}
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
		if(isGift){
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
			return;
		}
		MaplePacketCreator.addExpirationTime(mplew, item.getExpiration());
		mplew.writeInt(0);// nPaybackRate
		mplew.writeInt(0);// nDiscountRate
		/**
		 * public void Encode(OutPacket oPacket) {
		 * oPacket.EncodeBuffer(liSN, 8);
		 * oPacket.Encode4(dwAccountID);
		 * oPacket.Encode4(dwCharacterID);
		 * oPacket.Encode4(nItemID);
		 * oPacket.Encode4(nCommodityID);
		 * oPacket.Encode2(nNumber);
		 * oPacket.EncodeBuffer(sBuyCharacterID, 13);
		 * oPacket.EncodeBuffer(dateExpire, 8);
		 * oPacket.Encode4(nPaybackRate);
		 * oPacket.Encode4(nDiscountRate);
		 * }
		 */
	}

	public static class CashItemResult{

		// result = CInPacket::Decode1(a2) - 83;
		public static final short LoadLocker_Done = 87, LoadLocker_Failed = 88, LoadGift_Done = 89, LoadGift_Failed = 90, LoadWish_Done = 91, LoadWish_Failed = 92, SetWish_Done = 97, SetWish_Failed = 98, Buy_Done = 99, Buy_Failed = 100, Gift_Done = 106, Gift_Failed = 107, IncSlotCount_Done = 108, IncSlotCount_Failed = 109, IncTrunkCount_Done = 110, IncTrunkCount_Failed = 111, IncCharSlotCount_Done = 112, IncCharSlotCount_Failed = 113, IncBuyCharCount_Done = 114, MoveLtoS_Done = 118, MoveLtoS_Failed = 119, MoveStoL_Done = 120, MoveStoL_Failed = 121, UseCoupon_Done = 184, IncBuyCharCount_Failed = 115, BuyPackage_Done = 151, BuyNormal_Done = 238, BuyNormal_Failed = 239;

		public static byte[] showCashInventory(MapleClient c){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(LoadLocker_Done);
			mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());
			for(Item item : c.getPlayer().getCashShop().getInventory()){
				addCashItemInformation(mplew, item, c.getAccID());
			}
			mplew.writeShort(c.getPlayer().getStorage().getSlots());
			mplew.writeShort(c.getCharacterSlots());
			mplew.writeShort(c.nBuyCharacterCount);// m_nBuyCharacterCount
			mplew.writeShort(0);// m_nCharacterCount
			return mplew.getPacket();
		}

		public static byte[] showWishList(MapleCharacter mc, boolean update){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			if(update){
				mplew.write(SetWish_Done);
			}else{
				mplew.write(LoadWish_Done);
			}
			for(int sn : mc.getCashShop().getWishList()){
				mplew.writeInt(sn);
			}
			for(int i = mc.getCashShop().getWishList().size(); i < 10; i++){
				mplew.writeInt(0);
			}
			return mplew.getPacket();
		}

		public static byte[] showBoughtCashItem(Item item, int accountId){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(Buy_Done);
			addCashItemInformation(mplew, item, accountId);
			return mplew.getPacket();
		}

		/*
		 * 00 = Due to an unknown error, failed
		 * A4 = Due to an unknown error, failed + warpout
		 * A5 = You don't have enough cash.
		 * A6 = long as shet msg
		 * A7 = You have exceeded the allotted limit of price for gifts.
		 * A8 = You cannot send a gift to your own account. Log in on the char and purchase
		 * A9 = Please confirm whether the character's name is correct.
		 * AA = Gender restriction!
		 * //Skipped a few
		 * B0 = Wrong Coupon Code
		 * B1 = Disconnect from CS because of 3 wrong coupon codes < lol
		 * B2 = Expired Coupon
		 * B3 = Coupon has been used already
		 * B4 = Nexon internet cafes? lolfk
		 *
		 * BB = inv full
		 * C2 = not enough mesos? Lol not even 1 mesos xD
		 * C4 = Birthday failure bitch
		 */
		public static byte[] showCashShopMessage(byte message){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(LoadLocker_Failed);// technically each one has its own failed mode.
			mplew.write(message);
			return mplew.getPacket();
		}

		public static byte[] showGifts(List<Pair<Item, String>> gifts){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(LoadGift_Done);
			mplew.writeShort(gifts.size());
			for(Pair<Item, String> gift : gifts){
				addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight());
			}
			return mplew.getPacket();
		}

		public static byte[] showGiftSucceed(String to, CashItemData item){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(Gift_Done);
			mplew.writeMapleAsciiString(to);
			mplew.writeInt(item.nItemid);
			mplew.writeShort(item.nCount);
			mplew.writeInt(item.nPrice);
			return mplew.getPacket();
		}

		public static byte[] showBoughtInventorySlots(int type, short slots){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(IncSlotCount_Done);
			mplew.write(type);
			mplew.writeShort(slots);
			return mplew.getPacket();
		}

		public static byte[] showBoughtStorageSlots(short slots){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(IncTrunkCount_Done);
			mplew.writeShort(slots);
			return mplew.getPacket();
		}

		public static byte[] showBoughtCharacterSlot(short slots){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(IncCharSlotCount_Done);
			mplew.writeShort(slots);
			return mplew.getPacket();
		}

		public static byte[] takeFromCashInventory(Item item){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(MoveLtoS_Done);
			mplew.writeShort(item.getPosition());
			MaplePacketCreator.addItemInfo(mplew, item, true);
			return mplew.getPacket();
		}

		public static byte[] putIntoCashInventory(Item item, int accountId){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(MoveStoL_Done);
			addCashItemInformation(mplew, item, accountId);
			return mplew.getPacket();
		}

		public static byte[] showBoughtCashPackage(List<Item> cashPackage, int accountId){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(BuyPackage_Done);
			mplew.write(cashPackage.size());
			for(Item item : cashPackage){
				addCashItemInformation(mplew, item, accountId);
			}
			mplew.writeShort(0);
			return mplew.getPacket();
		}

		public static byte[] showBoughtQuestItem(int itemId){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.write(BuyNormal_Done);
			mplew.writeInt(1);
			mplew.writeShort(1);
			mplew.write(0x0B);
			mplew.write(0);
			mplew.writeInt(itemId);
			return mplew.getPacket();
		}

		public static byte[] showCouponRedeemedItem(int itemid){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
			mplew.writeShort(UseCoupon_Done); // v72
			mplew.writeInt(0);
			mplew.writeInt(1);
			mplew.writeShort(1);
			mplew.writeShort(0x1A);
			mplew.writeInt(itemid);
			mplew.writeInt(0);
			return mplew.getPacket();
		}
	}

	public static byte[] showCash(MapleCharacter mc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.QUERY_CASH_RESULT.getValue());
		mplew.writeInt(mc.getCashShop().getCash(1));
		mplew.writeInt(mc.getCashShop().getCash(2));
		mplew.writeInt(mc.getCashShop().getCash(4));
		return mplew.getPacket();
	}

	public static byte[] onCashShopGachaponStampResult(boolean bInvFull, int nNumber){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GACHAPON_STAMP_ITEM_RESULT.getValue());
		mplew.writeBoolean(bInvFull);
		mplew.writeInt(nNumber);// "You have acquired %d Gachapon Stamps\r\nby purchasing the Gachapon Ticket.
		return mplew.getPacket();
	}

	public static byte[] onCashItemGachaponResult(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASH_ITEM_GACHAPON_RESULT.getValue());
		// mplew.write(ShopResCode.GashItemGachapon_Failed.getRes());// Please check and see if you have exceeded\r\nthe number of cash items you can have.
		mplew.write(189);
		return mplew.getPacket();
	}

	public static byte[] onCashGachaponOpenResult(int accountid, long sn, int remainingBoxes, Item item, int itemid, int nSelectedItemCount, boolean bJackpot){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASH_ITEM_GACHAPON_RESULT.getValue());
		// mplew.write(ShopResCode.CashItemGachapon_Done.getRes());
		mplew.write(190);
		mplew.writeLong(sn);// sn of the box used
		mplew.writeInt(remainingBoxes);
		addCashItemInformation(mplew, item, accountid);
		mplew.writeInt(itemid);// the itemid of the liSN?
		mplew.write(nSelectedItemCount);// the total count now? o.O
		mplew.writeBoolean(bJackpot);// "CashGachaponJackpot"
		return mplew.getPacket();
	}
}
