package client.command.gm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.DatabaseConnection;
import tools.ObjectParser;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 24, 2016
 */
public class CommandUnban extends Command{

	public CommandUnban(){
		super("Unban", "Unban a target", "!Unban <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			String target = args[0];
			Integer accID = ObjectParser.isInt(target);
			String ip = null;
			String hwid = null;
			String macs = null;
			try{
				if(accID == null) accID = MapleCharacter.getAccIdByName(target);
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = ?")){
					ps.setInt(1, accID);
					ps.executeUpdate();
					Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Removed account ban: " + accID + " - " + target);
				}
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT ip, macs, hwid FROM accounts WHERE id = ?")){
					ps.setInt(1, accID);
					try(ResultSet rs = ps.executeQuery()){
						if(rs.next()){
							ip = rs.getString(1);
							macs = rs.getString(2);
							hwid = rs.getString(3);
						}
					}
				}
				if(c.getPlayer().isSuperGM()){
					try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM ipbans WHERE ip = ?")){
						ps.setString(1, "/" + ip);
						ps.executeUpdate();
						Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Deleted ip ban: " + ip);
					}
				}
				if(c.getPlayer().isAdmin()){
					try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM hwidbans WHERE hwid = ?")){
						ps.setString(1, hwid);
						ps.executeUpdate();
						Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Deleted hwid ban: " + hwid);
					}
				}
				if(c.getPlayer().isController()){
					for(String mac : macs.split(",")){
						try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM macbans WHERE mac = ?")){
							ps.setString(1, mac);
							ps.executeUpdate();
							Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Deleted mac ban: " + mac);
						}
					}
				}
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				c.getPlayer().message("Failed to unban " + args[0]);
				return false;
			}
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
