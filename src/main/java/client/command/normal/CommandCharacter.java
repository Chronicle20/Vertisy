package client.command.normal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.server.channel.handlers.PlayerLoggedinHandler;
import server.maps.FieldLimit;
import tools.DatabaseConnection;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 12, 2017
 */
public class CommandCharacter extends Command{

	public CommandCharacter(){
		super("Character", "Swap between characters.", "!Character <name>", "chr");
		setGMLevel(PlayerGMRank.NORMAL);
	}

	@Override
	public boolean execute(MapleClient c, String commandLabel, String[] args){
		if(!c.getPlayer().isAlive() || FieldLimit.CHANGECHANNEL.check(c.getPlayer().getMap().getMapData().getFieldLimit()) || c.getPlayer().getEventInstance() != null){
			c.announce(CWvsContext.enableActions());
			return false;
		}
		final boolean admin = c.getPlayer().isAdmin();
		if(args.length > 0){
			String chr = args[0];
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters where name = ? AND deleted = 0 AND gm >= ?" + (admin ? "" : " AND accountid = ?"))){
				ps.setString(1, chr);
				ps.setInt(2, c.getPlayer().getGMLevel());
				if(!admin) ps.setInt(3, c.getAccID());
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()){
						c.disconnect(false, false, c.getPlayer().getCashShop().isOpened());
						c.notDisconnecting();
						c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
						if(admin) c.isFakeLogin = true;
						MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
						mplew.writeInt(rs.getInt(1));
						SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(mplew.getPacket()));
						new PlayerLoggedinHandler().handlePacket(slea, c);
					}
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		return false;
	}
}
