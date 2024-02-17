package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import constants.ServerConstants;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster (some modifications to this beautiful code)
 */
public class DatabaseConnection{

	public static final int RETURN_GENERATED_KEYS = 1;
	private static ThreadLocal<Connection> con = new ThreadLocalConnection();

	public static Connection getConnection(){
		Connection c = con.get();
		if(c == null){
			con.remove();
			c = con.get();
		}
		try{
			c.getMetaData();
			if(!c.isValid(28800)){
				con.remove();
				c = con.get();
			}
		}catch(Exception ex){
			try{
				if(c != null) c.close();
			}catch(SQLException ex1){}
			con.remove();
			c = con.get();
		}
		return c;
	}

	private static class ThreadLocalConnection extends ThreadLocal<Connection>{

		@Override
		protected Connection initialValue(){
			try{
				Class.forName("com.mysql.jdbc.Driver"); // touch the mysql driver
			}catch(ClassNotFoundException e){
				System.out.println("[SEVERE] SQL Driver Not Found. Consider death by clams.");
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				return null;
			}
			try{
				return DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
			}catch(SQLException e){
				System.out.println("[SEVERE] Unable to make database connection.");
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				return null;
			}
		}
	}
}
