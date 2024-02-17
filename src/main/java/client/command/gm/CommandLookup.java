package client.command.gm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import tools.DatabaseConnection;
import tools.MessageBuilder;
import tools.ObjectParser;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 19, 2016
 */
public class CommandLookup extends Command{

	public CommandLookup(){
		super("Lookup", "Look up other accounts with an ip, mac, or hwid", "!Lookup <ip, macs, machineid, hwid, account, id> <data>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MessageBuilder builder = new MessageBuilder();
			String mode = args[0].toLowerCase();
			if(!mode.equalsIgnoreCase("ip") && !mode.equalsIgnoreCase("macs") && !mode.equalsIgnoreCase("hwid") && !mode.equalsIgnoreCase("id") && !mode.equalsIgnoreCase("account") && !mode.equalsIgnoreCase("machineid")){
				c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
				return false;
			}
			if(mode.equalsIgnoreCase("id")){
				builder.appendContent("Searching for characters under account id " + args[1] + "\n");
			}else builder.appendContent("Searching for " + mode + ": " + StringUtil.joinStringFrom(args, 1) + "\n");
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, id FROM accounts WHERE " + mode.toLowerCase() + " = ?")){
				ps.setString(1, StringUtil.joinStringFrom(args, 1));
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()){
						if(mode.equalsIgnoreCase("id") || mode.equalsIgnoreCase("account")){
							Integer accountid = ObjectParser.isInt(args[1]);
							if(mode.equalsIgnoreCase("account")){
								builder.appendContent("Grabbing account id from name...\n");
								try(PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM accounts WHERE name = ?")){
									ps2.setString(1, args[1]);
									try(ResultSet rs2 = ps2.executeQuery()){
										if(rs2.next()){
											accountid = rs2.getInt(1);
											builder.appendContent("Grabbed accountid " + accountid + ". Doing a character lookup.\n");
										}else{
											builder.appendContent("No account under the name " + args[1] + "\n");
										}
									}
								}
							}
							if(accountid != null){
								StringBuilder sb = new StringBuilder();
								try(PreparedStatement ps2 = DatabaseConnection.getConnection().prepareStatement("SELECT name, level, world FROM characters WHERE accountid = ? AND deleted = 0")){
									ps2.setInt(1, accountid);
									try(ResultSet rs2 = ps2.executeQuery()){
										while(rs2.next()){
											sb.append(rs2.getString(1) + " (" + rs2.getInt(2) + ", " + rs2.getInt(3) + ")");
											sb.append(" - ");
										}
									}
								}
								sb.setLength(sb.length() - " - ".length());
								builder.appendContent(sb.toString());
							}
						}else{
							builder.appendContent(rs.getString("name") + "\n");
						}
					}
				}
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
			c.getPlayer().dropMessage(MessageType.SYSTEM, builder);
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
