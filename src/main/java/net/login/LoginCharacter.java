package net.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleSkinColor;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 13, 2017
 */
public class LoginCharacter extends MapleCharacter{

	public static LoginCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException{
		try{
			LoginCharacter ret = new LoginCharacter();
			ret.client = client;
			ret.id = charid;
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
			ps.setInt(1, charid);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps.close();
				throw new RuntimeException("Loading char failed (not found)");
			}
			// addCharStats
			ret.name = rs.getString("name");
			ret.gender = rs.getInt("gender");
			ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
			ret.face = rs.getInt("face");
			ret.hair = rs.getInt("hair");
			ret.level = rs.getInt("level");
			ret.job = MapleJob.getById(rs.getInt("job"));
			ret.str = rs.getInt("str");
			ret.dex = rs.getInt("dex");
			ret.int_ = rs.getInt("int");
			ret.luk = rs.getInt("luk");
			ret.hp = rs.getInt("hp");
			ret.maxhp = rs.getInt("maxhp");
			ret.mp = rs.getInt("mp");
			ret.maxmp = rs.getInt("maxmp");
			ret.remainingAp = rs.getInt("ap");
			String[] skillPoints = rs.getString("sp").split(",");
			for(int i = 0; i < skillPoints.length; i++){
				ret.remainingSp[i] = Integer.parseInt(skillPoints[i]);
			}
			ret.exp.set(rs.getInt("exp"));
			ret.fame = rs.getInt("fame");
			ret.gachaexp.set(rs.getInt("gachaexp"));
			ret.mapid = rs.getInt("map");
			ret.initialSpawnPoint = rs.getInt("spawnpoint");
			// addCharLook
			ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equipslots"));
			ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("useslots"));
			ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setupslots"));
			ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etcslots"));
			for(Pair<Item, MapleInventoryType> item : ItemFactory.INVENTORY.loadItems(ret.id, true)){
				ret.getInventory(item.getRight()).addFromDB(item.getLeft());
			}
			// bottom of addCharEntry
			ret.gmLevel = rs.getInt("gm");
			ret.rank = rs.getInt("rank");
			ret.rankMove = rs.getInt("rankMove");
			ret.jobRank = rs.getInt("jobRank");
			ret.jobRankMove = rs.getInt("jobRankMove");
			return ret;
		}catch(SQLException | RuntimeException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return null;
	}
}
