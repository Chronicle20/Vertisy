package client.command.gm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 23, 2016
 */
public class CommandBan extends Command{

	public CommandBan(){
		super("Ban", "Ban a target", "!Ban <ign> <reason>", null);
		setGMLevel(PlayerGMRank.GM);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(args.length < 2){
			c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
			return false;
		}
		String ign = args[0];
		String reason = StringUtil.joinStringFrom(args, 1);
		MapleCharacter target = c.getChannelServer().getChannelServer().getCharacterByName(ign);
		if(target != null){
			String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
			String reason1 = c.getPlayer().getName() + " banned " + readableTargetName + " for " + reason;
			if(c.getPlayer().isSuperGM()){
				String ip = target.getClient().getSession().remoteAddress().toString().split(":")[0];
				reason1 += " (IP: " + ip + ") ";
				// Ban ip
				PreparedStatement ps = null;
				try{
					Connection con = DatabaseConnection.getConnection();
					if(ip.matches("/[0-9]{1,3}\\..*")){
						ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
						ps.setString(1, ip);
						ps.executeUpdate();
						ps.close();
					}
				}catch(SQLException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Error occured while banning IP address for " + target.getName() + " Ip: " + ip);
				}
			}
			if(c.getPlayer().isAdmin()) target.getClient().banHwids();
			if(c.getPlayer().isController()){
				target.getClient().banMacs();
			}
			target.ban(reason1);
			final String reasonFinal = reason1;
			c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
			final MapleCharacter rip = target;
			rip.getClient().disconnect(false, false);
			TimerManager.getInstance().schedule("Ban", new Runnable(){

				@Override
				public void run(){
					if(rip != null && rip.getClient() != null){
						rip.getClient().announce(MaplePacketCreator.serverNotice(1, reasonFinal));
					}
				}
			}, 1000); // 1 Seconds
		}else if(MapleCharacter.ban(ign, reason, false)){
			c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
		}else{
			c.announce(MaplePacketCreator.getGMEffect(6, (byte) 1));
		}
		return false;
	}
}
