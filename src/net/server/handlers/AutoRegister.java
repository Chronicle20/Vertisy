package net.server.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import crypto.BCrypt;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 24, 2016
 */
public class AutoRegister{

	// private static final int ACCOUNTS_PER_IP = 5; // change the value to the amount of accounts you want allowed for each ip
	public static final boolean autoRegister = true; // enable = true or disable = false

	public static boolean getAccountExists(String login){
		boolean accountExists = false;
		Connection con = DatabaseConnection.getConnection();
		try{
			PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
			ps.setString(1, login);
			ResultSet rs = ps.executeQuery();
			if(rs.first()){
				accountExists = true;
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return accountExists;
	}

	public static boolean createAccount(String login, String pwd, String eip){
		String sockAddr = eip;
		Connection con;
		// connect to database or halt
		try{
			con = DatabaseConnection.getConnection();
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			return false;
		}
		try{
			try(PreparedStatement ipc = con.prepareStatement("SELECT ip FROM accounts WHERE ip = ?")){
				ipc.setString(1, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
				try(ResultSet rs = ipc.executeQuery()){
					if(rs.first() == false || rs.last() == true/* && rs.getRow() < ACCOUNTS_PER_IP*/){
						try{
							try(PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, ip, sessionID) VALUES (?, ?, ?, ?, ?, ?, ?)")){
								ps.setString(1, login);
								ps.setString(2, BCrypt.hashpw(pwd, BCrypt.gensalt()));
								ps.setString(3, "no@email.provided");
								ps.setString(4, "2008-04-07");
								ps.setString(5, "00-00-00-00-00-00");
								ps.setString(6, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
								ps.setInt(7, 0);
								ps.executeUpdate();
							}
							return true;
						}catch(SQLException ex){
							Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
							return false;
						}
					}
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return false;
	}
}
