package tools;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 30, 2016
 */
public class SQLUtil{

	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for(int x = 1; x <= columns; x++){
			if(columnName.equals(rsmd.getColumnName(x))) return true;
		}
		return false;
	}
}
