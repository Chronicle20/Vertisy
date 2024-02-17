package net.world.family;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 27, 2017
 */
public class FamilyCharacter implements Externalizable{

	public int parentFamilyID, familyID;
	public FamilyCharacter parent;
	public boolean online;
	public short level, job;
	public int characterID, parentCharacterID;
	public int famousPoint, totalFamousPoint, todaySavePoint;
	public int channelID, loginMin;// wtf is loginMin
	public String characterName;
	public long firstUpdateTime;

	public void load(ResultSet rs) throws SQLException{
		//
		int index = 1;
		characterID = rs.getInt(++index);
		familyID = rs.getInt(++index);
		parentFamilyID = rs.getInt(++index);
		famousPoint = rs.getInt(++index);
		totalFamousPoint = rs.getInt(++index);
		todaySavePoint = rs.getInt(++index);
		characterName = rs.getString(++index);
		firstUpdateTime = rs.getLong(++index);
		parentCharacterID = rs.getInt(++index);
	}

	public void save(Connection connection){
		try(PreparedStatement ps = connection.prepareStatement("INSERT INTO family_members" + "(chrid, familyID, parentFamilyID, famousPoint, totalFamousPoint, todaySavePoint, characterName, firstUpdateTime, parentChrID) VALUES" + "(?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + "chrid = VALUES(chrid), familyID = VALUES(familyID), parentFamilyID = VALUES(parentFamilyID), famousPoint = VALUES(famousPoint), totalFamousPoint = VALUES(totalFamousPoint)," + "todaySavePoint = VALUES(todaySavePoint), characterName = VALUES(characterName), firstUpdateTime = VALUES(firstUpdateTime), parentChrID = VALUES(parentChrId)")){
			ps.setInt(1, characterID);
			ps.setInt(2, familyID);
			ps.setInt(3, parentFamilyID);
			ps.setInt(4, famousPoint);
			ps.setInt(5, totalFamousPoint);
			ps.setInt(6, todaySavePoint);
			ps.setString(7, characterName);
			ps.setLong(8, firstUpdateTime);
			ps.setInt(9, parentCharacterID);
			ps.executeUpdate();
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(parentFamilyID);
		out.writeInt(familyID);
		parent.writeExternal(out);
		out.writeBoolean(online);
		out.writeShort(level);
		out.writeShort(job);
		out.writeInt(characterID);
		out.writeInt(famousPoint);
		out.writeInt(totalFamousPoint);
		out.writeInt(todaySavePoint);
		out.writeInt(channelID);
		out.writeInt(loginMin);
		out.writeObject(characterName);
		out.writeLong(firstUpdateTime);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		parentFamilyID = in.readInt();
		familyID = in.readInt();
		parent = new FamilyCharacter();
		parent.readExternal(in);
		online = in.readBoolean();
		level = in.readShort();
		job = in.readShort();
		characterID = in.readInt();
		famousPoint = in.readInt();
		totalFamousPoint = in.readInt();
		todaySavePoint = in.readInt();
		channelID = in.readInt();
		loginMin = in.readInt();
		characterName = (String) in.readObject();
		firstUpdateTime = in.readLong();
	}

	public void setOnline(boolean online){
		this.online = online;
		checkTodaySavePoint();
	}

	public void gainRep(int rep){
		checkTodaySavePoint();
		famousPoint += rep;
		totalFamousPoint += rep;
		todaySavePoint += rep;
	}

	/**
	 * Resets the total rep gained today if it has been a day since last it started recording.
	 */
	public void checkTodaySavePoint(){
		if(firstUpdateTime <= 0) firstUpdateTime = System.currentTimeMillis();
		if((firstUpdateTime >= (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)))){
			firstUpdateTime = System.currentTimeMillis();
			todaySavePoint = 0;
		}
	}
}
