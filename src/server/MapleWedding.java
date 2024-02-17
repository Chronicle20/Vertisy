package server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import net.server.Server;
import scripting.event.EventInstanceManager;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 27, 2016
 */
public class MapleWedding implements Externalizable{

	private int marriageid = -1;
	private int cathedral, premium;
	private int player1, player2;
	/**
	 * -1 = Divorced
	 * 0 = They have a wedding receipt and can invite & can start the wedding whenever.
	 * 1 = Married. Wedding was completed fully
	 */
	private int status;
	/**
	 * The progress in the Wedding.
	 * 1 = Started Wedding
	 * 2 = Started Ceremony
	 * 3 = End Ceremony
	 * 4 = Photos
	 * 5 = Collecting keys & going to the box map to collect random prizes
	 */
	private int state;
	private List<Integer> invited = new LinkedList<>();
	public ScheduledFuture<?> autoTransport;
	public boolean ceremony;
	public int index = -1;
	private EventInstanceManager eim;

	public MapleWedding(int marriageid){
		this.marriageid = marriageid;
		invited = new LinkedList<Integer>();
	}

	public MapleWedding(int player1, int player2){
		this.player1 = player1;
		this.player2 = player2;
		invited = new LinkedList<Integer>();
	}

	public int getMarriageID(){
		return marriageid;
	}

	public boolean isCathedral(){
		return cathedral == 1;
	}

	public void setCathedral(int cathedral){
		this.cathedral = cathedral;
	}

	public boolean isPremium(){
		return premium == 1;
	}

	public void setPremium(int premium){
		this.premium = premium;
	}

	public int getPlayer1(){
		return player1;
	}

	public int getPlayer2(){
		return player2;
	}

	public int getStatus(){
		return status;
	}

	public void setStatus(int status){
		this.status = status;
	}

	public int getState(){
		return state;
	}

	public void setState(int state){
		this.state = state;
	}

	public List<Integer> getInvited(){
		return invited;
	}

	public EventInstanceManager getEIM(){
		return eim;
	}

	public void setEIM(EventInstanceManager eim){
		this.eim = eim;
	}

	public int insertToDB(){
		if(marriageid > 0) return marriageid;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO weddings(id, player1, player2, cathedral, premium, status, invited) VALUES(DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setInt(1, player1);
			ps.setInt(2, player2);
			ps.setInt(3, cathedral);
			ps.setInt(4, premium);
			ps.setInt(5, status);
			String invites = "";
			for(int id : invited){
				invites += id + ",";
			}
			ps.setString(6, invites);
			ps.executeUpdate();
			try(ResultSet keys = ps.getGeneratedKeys()){
				if(keys.next()){
					marriageid = keys.getInt(1);
					if(marriageid > Server.highestWeddingID){
						Server.highestWeddingID = marriageid;
					}
				}else{
					throw new SQLException("Creating DropParty log failed, no ID obtained.");
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return marriageid;
	}

	public void saveToDB(){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE weddings SET player1 = ?, player2 = ?, cathedral = ?, premium = ?, status = ?, invited = ? WHERE id = ?")){
			ps.setInt(1, player1);
			ps.setInt(2, player2);
			ps.setInt(3, cathedral);
			ps.setInt(4, premium);
			ps.setInt(5, status);
			String invites = "";
			for(int id : invited){
				invites += id + ",";
			}
			ps.setString(6, invites);
			ps.setInt(7, marriageid);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void loadFromDB(){
		if(marriageid > Server.highestWeddingID){
			Server.highestWeddingID = marriageid;
		}
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM weddings WHERE id = ?")){
			ps.setInt(1, marriageid);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					player1 = rs.getInt("player1");
					player2 = rs.getInt("player2");
					cathedral = rs.getInt("cathedral");
					premium = rs.getInt("premium");
					status = rs.getInt("status");
					String invites = rs.getString("invited");
					for(String s : invites.split(",")){
						if(s.length() > 0) invited.add(Integer.parseInt(s));
					}
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(marriageid);
		out.writeInt(cathedral);
		out.writeInt(premium);
		out.writeInt(player1);
		out.writeInt(player2);
		out.writeInt(status);
		out.writeInt(state);
		out.writeInt(invited.size());
		for(int invite : invited){
			out.writeInt(invite);
		}
		out.writeBoolean(ceremony);
		out.writeInt(index);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		marriageid = in.readInt();
		cathedral = in.readInt();
		premium = in.readInt();
		player1 = in.readInt();
		player2 = in.readInt();
		status = in.readInt();
		state = in.readInt();
		int size = in.readInt();
		for(int i = 0; i < size; i++){
			invited.add(in.readInt());
		}
		ceremony = in.readBoolean();
		index = in.readInt();
	}
}
