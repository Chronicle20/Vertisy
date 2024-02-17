package server.cashshop;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 13, 2017
 */
public class BestItem implements Comparable<BestItem>{

	public int sn, nCount, nCommodityGender;

	public void load(ResultSet rs) throws SQLException{
		sn = rs.getInt("sn");
		nCommodityGender = rs.getInt("gender");
		nCount = rs.getInt("purchases");
	}

	@Override
	public int compareTo(BestItem o){
		return o.nCount - nCount;
	}
}
