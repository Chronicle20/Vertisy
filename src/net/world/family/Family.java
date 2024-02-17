package net.world.family;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mysql.jdbc.Statement;

import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 27, 2017
 */
public class Family implements Externalizable{

	public int world;
	public int familyID;
	public int bossID;
	public String familyName;
	public Map<Integer, Integer> mStatistic = new HashMap<>();
	public Map<Integer, Integer> mPrivilege = new HashMap<>();
	public Map<Integer, Integer> mPrivilegeUse = new HashMap<>();
	public Map<Integer, FamilyCharacter> members = new HashMap<>();

	public void create(){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO families(bossID, familyName, world) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setInt(1, bossID);
			ps.setString(2, familyName);
			ps.setInt(3, world);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys()){
				if(rs.next()) familyID = rs.getInt(1);
			}
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void load(ResultSet rs) throws SQLException{
		// familyid, world, bossID, familyName, statistic, privilege, privilegeUse
		familyID = rs.getInt(1);
		world = rs.getInt(2);
		bossID = rs.getInt(3);
		familyName = rs.getString(4);
		//
		//
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM family_members WHERE familyID = ?")){
			ps.setInt(1, familyID);
			try(ResultSet rs2 = ps.executeQuery()){
				while(rs2.next()){
					FamilyCharacter fc = new FamilyCharacter();
					fc.load(rs2);
					members.put(fc.characterID, fc);
				}
			}
		}
		members.values().forEach(fc-> fc.parent = members.get(fc.parentCharacterID));
	}

	public void save(){
		Connection connection = DatabaseConnection.getConnection();
		try(PreparedStatement ps = connection.prepareStatement("UPDATE families SET bossID = ?, familyName = ?, world = ?, statistic = ?, privilege = ?, privilegeUse = ? WHERE familyID = ?")){
			ps.setInt(1, bossID);
			ps.setString(2, familyName);
			ps.setInt(3, world);
			ps.setString(4, "");
			ps.setString(5, "");
			ps.setString(6, "");
			ps.setInt(7, familyID);
			ps.executeUpdate();
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		members.values().forEach(fc-> fc.save(connection));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(world);
		out.writeInt(familyID);
		out.writeInt(bossID);
		out.writeObject(familyName);
		out.writeInt(members.size());
		for(Entry<Integer, FamilyCharacter> member : members.entrySet()){
			out.writeInt(member.getKey());
			member.getValue().writeExternal(out);
		}
		out.writeInt(mStatistic.size());
		for(Entry<Integer, Integer> statistic : mStatistic.entrySet()){
			out.writeInt(statistic.getKey());
			out.writeInt(statistic.getValue());
		}
		out.writeInt(mPrivilege.size());
		for(Entry<Integer, Integer> privilege : mPrivilege.entrySet()){
			out.writeInt(privilege.getKey());
			out.writeInt(privilege.getValue());
		}
		out.writeInt(mPrivilegeUse.size());
		for(Entry<Integer, Integer> privilegeUse : mPrivilegeUse.entrySet()){
			out.writeInt(privilegeUse.getKey());
			out.writeInt(privilegeUse.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		world = in.readInt();
		familyID = in.readInt();
		bossID = in.readInt();
		familyName = (String) in.readObject();
		int size = in.readInt();
		for(int i = 0; i < size; i++){
			int id = in.readInt();
			FamilyCharacter member = new FamilyCharacter();
			member.readExternal(in);
			members.put(id, member);
		}
		size = in.readInt();
		for(int i = 0; i < size; i++){
			mStatistic.put(in.readInt(), in.readInt());
		}
		size = in.readInt();
		for(int i = 0; i < size; i++){
			mPrivilege.put(in.readInt(), in.readInt());
		}
		size = in.readInt();
		for(int i = 0; i < size; i++){
			mPrivilegeUse.put(in.readInt(), in.readInt());
		}
	}
}
