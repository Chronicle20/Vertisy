package server.maps.objects.miniroom;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 14, 2017
 */
public class MRLeave{

	public static final int
	// miniroom
	MRLeave_UserRequest = 0x0, MRLeave_WrongPosition = 0x1, MRLeave_Closed = 0x2, MRLeave_HostOut = 0x3, MRLeave_Booked = 0x4, MRLeave_Kicked = 0x5, MRLeave_OpenTimeOver = 0x6,
	        // trade
	        TRLeave_TradeDone = 0x7, TRLeave_TradeFail = 0x8, TRLeave_TradeFail_OnlyItem = 0x9, TRLeave_TradeFail_Expired = 0xA, TRLeave_TradeFail_Denied = 0xB, TRLeave_FieldError = 0xC, TRLeave_ItemCRCFailed = 0xD,
	        // PersonalShop
	        PSLeave_NoMoreItem = 0xE, PSLeave_KickedTimeOver = 0xF,
	        // entrusted shop
	        ESLeave_Open = 0x10, ESLeave_StartManage = 0x11, ESLeave_ClosedTimeOver = 0x12, ESLeave_EndManage = 0x13, ESLeave_DestoryByAdmin = 0x14, MGLeave_UserRequest = 0x15;
}
