package client.command.gm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.DatabaseConnection;
import tools.MessageBuilder;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 17, 2016
 */
public class CommandWhois extends Command{

	public CommandWhois(){
		super("Whois", "Get Client and Character info about a target", "!Whois <target>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length > 0){
			MessageBuilder builder = new MessageBuilder();
			MapleCharacter mc = ChannelServer.getInstance().getCharacterByName(args[0]);
			Integer accountid = null, characterid = null;
			String accountName = null;
			String ip = null;
			String macs = null;
			String hwid = null;
			if(mc == null){
				try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT accountid, id FROM characters WHERE name = ?")){
					ps.setString(1, args[0]);
					try(ResultSet rs = ps.executeQuery()){
						if(rs.next()){
							accountid = rs.getInt("accountid");
							characterid = rs.getInt("id");
						}
					}
				}catch(SQLException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
				if(accountid != null){
					try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, ip, macs, hwid, machineid FROM accounts WHERE id = ?")){
						ps.setInt(1, accountid);
						try(ResultSet rs = ps.executeQuery()){
							if(rs.next()){
								accountName = rs.getString(1);
								ip = rs.getString(3);
								macs = rs.getString(4);
								hwid = rs.getString(5);
							}
						}
					}catch(SQLException ex){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
					}
				}else{
					c.getPlayer().dropMessage(MessageType.ERROR, "Unknown player.");
					return false;
				}
			}else{
				accountid = mc.getAccountID();
				accountName = mc.getClient().getAccountName();
				characterid = mc.getId();
				ip = mc.getClient().getSession().remoteAddress().toString();
				macs = mc.getClient().toStringMac();
				hwid = mc.getClient().toStringHwid();
			}
			builder.appendContent("Account Name: " + accountName + "\n");
			builder.appendContent("Account ID: " + accountid + "\n");
			builder.appendContent("Character ID: " + characterid + "\n");
			if(c.getPlayer().isSuperGM()){
				builder.appendContent("IP: " + ip + "\n");
				if(c.getPlayer().isAdmin()) builder.appendContent("HWID: " + hwid + "\n");
				if(c.getPlayer().isController()){
					builder.appendContent("Macs: " + macs + "\n");
					// builder.appendContent("Machine ID: " + machineID + "\n");
				}
			}
			c.getPlayer().dropMessage(MessageType.SYSTEM, builder);
		}else{
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
		}
		return false;
	}
}
