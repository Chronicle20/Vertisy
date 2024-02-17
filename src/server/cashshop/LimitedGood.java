package server.cashshop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;

import tools.ObjectParser;
import tools.data.output.LittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 12, 2017
 */
public class LimitedGood{

	public int id;
	public int nItemID;
	public int[] nSN = new int[10];
	public int nOriginCount, nRemainCount;
	public int dwConditionFlag;
	public int nDateStart, nDateEnd;
	public int nHourStart, nHourEnd;
	public boolean[] abWeek = new boolean[7];

	public void encode(LittleEndianWriter lew){
		lew.writeInt(nItemID);
		for(int sn : nSN){
			lew.writeInt(sn);
		}
		lew.writeInt(getState());
		lew.writeInt(nOriginCount);
		lew.writeInt(nRemainCount);
		lew.writeInt(dwConditionFlag);
		lew.writeInt(nDateStart);
		lew.writeInt(nDateEnd);
		lew.writeInt(nHourStart);
		lew.writeInt(nHourEnd);
		for(boolean week : abWeek){
			lew.writeInt(week ? 1 : 0);
		}
		/*
		 struct CS_LIMITGOODS
		{
		int nItemID;
		int nSN[10];
		int nState;
		int nOriginCount;
		int nRemainCount;
		unsigned int dwConditionFlag;
		int nDateStart;
		int nDateEnd;
		int nHourStart;
		int nHourEnd;
		int abWeek[7];
		};
		 */
	}

	public void load(ResultSet rs) throws SQLException{
		id = rs.getInt("id");
		nItemID = rs.getInt("itemid");
		Arrays.fill(nSN, 0);
		String sn = rs.getString("sn");
		if(sn != null && sn.length() > 0){
			int in = 0;
			for(String s : sn.split(",")){
				if(s != null && s.length() > 0){
					nSN[in++] = ObjectParser.isInt(s);
				}
			}
		}
		nOriginCount = rs.getInt("originCount");
		nRemainCount = rs.getInt("remainCount");
		dwConditionFlag = rs.getInt("conditionFlag");
		nDateStart = rs.getInt("dateStart");
		nDateEnd = rs.getInt("dateEnd");
		nHourStart = rs.getInt("hourStart");
		nHourEnd = rs.getInt("hourEnd");
		Arrays.fill(abWeek, true);
		String week = rs.getString("week");
		if(week != null && week.length() > 0){
			int in = 0;
			for(String s : sn.split(",")){
				if(s != null && s.length() > 0){
					abWeek[in++] = Boolean.valueOf(s);
				}
			}
		}
	}

	public int getState(){
		Calendar calendar = Calendar.getInstance();
		if(nRemainCount <= 0) return LimitGoodsState.CountLimited;
		if(calendar.get(Calendar.HOUR_OF_DAY) < nHourStart || calendar.get(Calendar.HOUR_OF_DAY) > nHourEnd) return LimitGoodsState.HourLimited;
		if(calendar.get(Calendar.DAY_OF_MONTH) < nDateStart || calendar.get(Calendar.DAY_OF_MONTH) > nDateEnd) return LimitGoodsState.DateLimited;
		if(!abWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]) return LimitGoodsState.WeekLimited;
		return 0;
	}

	public int getStockState(CashItemData data){
		if(nRemainCount >= Integer.MAX_VALUE) return StockState.Infinite;
		if(nRemainCount < data.nCount) return StockState.NoStock;
		Calendar calendar = Calendar.getInstance();
		if(calendar.get(Calendar.HOUR_OF_DAY) < nHourStart || calendar.get(Calendar.HOUR_OF_DAY) > nHourEnd) return StockState.NotAvailableTime;
		if(calendar.get(Calendar.DAY_OF_MONTH) < nDateStart || calendar.get(Calendar.DAY_OF_MONTH) > nDateEnd) return StockState.NotAvailableTime;
		if(!abWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]) return StockState.NotAvailableTime;
		return StockState.Enough;
	}
}
