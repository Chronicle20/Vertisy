package tools;

/**
 * All Maple Trade System Operation Codes.
 *
 * @author Eric
 */
public enum ITCResCode{
    // ITCReq
	GetMaplePoint(0x0),
	CharacterModifiedNFlush(0x1),
	RegisterSaleEntry(0x2),
	SaleCurrentItemToWish(0x3),
	RegisterBuyOrder(0x4),
	GetITCList(0x5),
	GetSearchITCList(0x6),
	CancelSaleItem(0x7),
	MoveITCPurchaseItemLtoS(0x8),
	SetZzim(0x9),
	DeleteZzim(0xA),
	LoadWishSaleList(0xB),
	BuyWish(0xC),
	CancelWish(0xD),
	BuyWishChargeCash(0xE),
	BuyWishCancel(0xF),
	BuyItem(0x10),
	BuyZzimItem(0x11),
	RegAuction(0x12),
	BidAuction(0x13),
	BuyAuctionImm(0x14),
    // ITCRes
	GetITCList_Done(0x15),
	GetITCList_Failed(0x16),
	GetSearchITCList_Done(0x17),
	GetSearchITCList_Failed(0x18),
	GetMaplePoint_Done(0x19),
	GetMaplePoint_Failed(0x1A),
	CharacterModifiedNFlush_Done(0x1B),
	CharacterModifiedNFlush_Failed(0x1C),
	RegisterSaleEntry_Done(0x1D),
	RegisterSaleEntry_Failed(0x1E),
	SaleCurrentItemToWish_Done(0x1F),
	SaleCurrentItemToWish_Failed(0x20),
	GetUserPurchaseItem_Done(0x21),
	GetUserPurchaseItem_Failed(0x22),
	GetUserSaleItem_Done(0x23),
	GetUserSaleItem_Failed(0x24),
	CancelSaleItem_Done(0x25),
	CancelSaleItem_Failed(0x26),
	MoveITCPurchaseItemLtoS_Done(0x27),
	MoveITCPurchaseItemLtoS_Failed(0x28),
	SetZzim_Done(0x29),
	SetZzim_Failed(0x2A),
	DeleteZzim_Done(0x2B),
	DeleteZzim_Failed(0x2C),
	LoadWishSaleList_Done(0x2D),
	LoadWishSaleList_Failed(0x2E),
	BuyWish_Done(0x2F),
	BuyWish_Failed(0x30),
	CancelWish_Done(0x31),
	CancelWish_Failed(0x32),
	BuyItem_Done(0x33),
	BuyItem_Failed(0x34),
	BuyZzimItem_Done(0x35),
	BuyZzimItem_Failed(0x36),
	RegisterBuyOrder_Done(0x37),
	RegisterBuyOrder_Failed(0x38),
	RegAuction_Done(0x39),
	RegAuction_Failed(0x3A),
	BidAuction_Done(0x3B),
	BidAuction_Failed(0x3C),
	GetNotifyCancelWishResult(0x3D),
	GetSuccessBidInfoResult(0x3E);

	private final int res;

	private ITCResCode(int res){
		this.res = res;
	}

	public int getRes(){
		return res;
	}

	public static ITCResCode Find(int nResCode){
		for(ITCResCode enRes : ITCResCode.values()){
			if(enRes.getRes() == nResCode){ return enRes; }
		}
		return null;
	}
}