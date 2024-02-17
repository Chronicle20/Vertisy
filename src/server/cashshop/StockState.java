package server.cashshop;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 12, 2017
 */
public class StockState{

	// CS_StockState_
	public static final int Infinite = 0xFFFFFFFE, Enough = 0xFFFFFFFF, NoStock = 0x1, // these 2 disable buy button
	        NotAvailableTime = 0x2;
	/*
	 enum $7B52A1EB95419FEA687CD9332E081BF4
	{
	CS_LimitGoodsState_NotLimitGoods = 0xFFFFFFFE,
	CS_LimitGoodsState_Enough = 0xFFFFFFFF,
	CS_LimitGoodsState_NoGoods = 0x1,
	CS_LimitGoodsState_NotAvailableTime = 0x2,
	CS_LimitGoodsState_SearchErr = 0x3,
	};
	 */
}
