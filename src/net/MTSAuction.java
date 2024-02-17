package net;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import client.inventory.Item;
import net.server.channel.handlers.MTSHandler;
import tools.DatabaseConnection;
import tools.Pair;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Dec 31, 2016
 */
public class MTSAuction implements Runnable{

	private Connection con;

	@Override
	public void run(){
		try{
			// System.out.println("Checking for auction shit");
			con = DatabaseConnection.getConnection();
			con.setAutoCommit(false);
			try(PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = 3 AND sell_ends <= ? AND transfer = 0")){
				ps.setLong(1, Calendar.getInstance().getTimeInMillis());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()){// loop all auctioned items where its over
						int seller = rs.getInt("seller");
						int mtsID = rs.getInt("id");
						int highestBid = -1;
						int highestBidder = -1;
						List<Pair<Integer, Integer>> bids = new ArrayList<>();
						try(PreparedStatement ps2 = con.prepareStatement("SELECT bidderid, bid, bidrange FROM mts_bids WHERE mtsItemID = ?")){
							ps2.setInt(1, mtsID);
							try(ResultSet rs2 = ps2.executeQuery()){
								while(rs2.next()){
									int total = rs2.getInt("bid") + rs2.getInt("bidrange");
									if(total > highestBid){
										highestBid = total;
										highestBidder = rs2.getInt("bidderid");
									}
									bids.add(new Pair<Integer, Integer>(rs2.getInt("bidderid"), total));
								}
							}
						}
						if(highestBidder >= 0 && highestBid >= 0){
							try(PreparedStatement ps2 = con.prepareStatement("UPDATE mts_items SET transfer = 1 AND seller = ? WHERE id = ?")){
								ps2.setInt(1, highestBidder);
								ps2.setInt(2, mtsID);
								if(ps2.executeUpdate() == 1){// successfully gave it to the highest bidder
									try(PreparedStatement up1 = con.prepareStatement("INSERT INTO server_queue(serverType, characterr, type, value) VALUES(?, ?, ?, ?)")){
										up1.setString(1, "ChannelServer");
										up1.setInt(2, highestBidder);
										up1.setString(3, "GIVE_NX");
										up1.setLong(4, -highestBid);
										up1.executeUpdate();
									}
									try(PreparedStatement up2 = con.prepareStatement("INSERT INTO server_queue(serverType, characterr, type, value) VALUES(?, ?, ?, ?)")){
										up2.setString(1, "ChannelServer");
										up2.setInt(2, seller);
										up2.setString(3, "GIVE_NX");
										up2.setLong(4, highestBid);
										up2.executeUpdate();
									}
								}
							}
							Item item = MTSHandler.createItem(rs).getItem();
							for(Pair<Integer, Integer> val : bids){
								int bidderid = val.left;
								int total = val.right;
								short status = (short) (total == highestBid ? 1 : 2);
								MTSHandler.insertIntoHistory(con, bidderid, item, rs.getInt("tab"), total, rs.getString("sellername"), rs.getInt("seller"), status);
							}
							String buyerName = "";
							try(PreparedStatement ps2 = con.prepareStatement("SELECT name FROM characters WHERE id = ?")){
								ps2.setInt(1, highestBidder);
								try(ResultSet rs2 = ps2.executeQuery()){
									if(rs2.next()) buyerName = rs2.getString("name");
								}
							}
							MTSHandler.insertIntoHistory(con, rs.getInt("seller"), item, rs.getInt("tab"), rs.getInt("price"), buyerName, highestBidder, (short) 0);
						}else{
							try(PreparedStatement ps2 = con.prepareStatement("UPDATE mts_items SET transfer = 1 WHERE id = ?")){
								ps2.setInt(1, mtsID);
								ps2.executeUpdate();
							}
						}
					}
				}
			}
			con.commit();
			con.setAutoCommit(true);
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			try{
				con.rollback();
				con.setAutoCommit(true);
			}catch(SQLException ex2){}
		}
	}
}
