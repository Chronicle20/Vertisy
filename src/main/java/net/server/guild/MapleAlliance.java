/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.guild;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author XoticStory.
 */
public class MapleAlliance implements Externalizable{

	private static final long serialVersionUID = -8167271378203902215L;
	private int[] guilds = new int[5];
	private int allianceId = -1;
	private byte capacity = 5;
	private String name;
	private String notice = "";
	private String rankTitles[] = new String[5];
	private Map<Integer, String> invitations = new HashMap<>();// leader id, guild name

	public MapleAlliance(){
		super();
	}

	public MapleAlliance(String name, int id, int guild1, int guild2){
		this.name = name;
		allianceId = id;
		int[] guild = new int[capacity];// {guild1, guild2, -1, -1, -1};
		Arrays.fill(guild, -1);
		guild[0] = guild1;
		guild[1] = guild2;
		String[] ranks = {"Master", "Jr.Master", "Member", "Member", "Member"};
		for(int i = 0; i < capacity; i++){
			guilds[i] = guild[i];
		}
		for(int i = 0; i < 5; i++){
			rankTitles[i] = ranks[i];
		}
	}

	public static MapleAlliance loadAlliance(int id){
		if(id <= 0) return null;
		MapleAlliance alliance = new MapleAlliance(null, -1, -1, -1);
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM alliance WHERE id = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()){
				rs.close();
				ps.close();
				return null;
			}
			alliance.allianceId = id;
			alliance.capacity = rs.getByte("capacity");
			alliance.name = rs.getString("name");
			alliance.notice = rs.getString("notice");
			for(int i = 1; i <= 5; i++){
				alliance.rankTitles[i - 1] = rs.getString("rank_title" + i);
			}
			for(int i = 1; i <= alliance.guilds.length; i++){
				alliance.guilds[i - 1] = rs.getInt("guild" + i);
			}
			ps.close();
			rs.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return alliance;
	}

	public void saveToDB(){
		StringBuilder sb = new StringBuilder();
		sb.append("capacity = ?, ");
		sb.append("notice = ?, ");
		for(int i = 1; i <= rankTitles.length; i++){
			sb.append("rank_title").append(i).append(" = ?, ");
		}
		for(int i = 1; i <= guilds.length; i++){
			sb.append("guild").append(i).append(" = ?, ");
		}
		sb.setLength(sb.length() - ", ".length());
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE `alliance` SET " + sb.toString() + " WHERE id = ?")){
			ps.setInt(1, this.capacity);
			ps.setString(2, this.notice);
			for(int i = 0; i < rankTitles.length; i++){
				ps.setString(i + 3, rankTitles[i]);
			}
			for(int i = 0; i < guilds.length; i++){
				ps.setInt(i + 8, guilds[i]);
			}
			ps.setInt(13, this.allianceId);
			ps.executeUpdate();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public boolean addRemGuildFromDB(int gid, boolean add){
		Connection con = DatabaseConnection.getConnection();
		boolean ret = false;
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM alliance WHERE id = ?")){
			ps.setInt(1, this.allianceId);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					int avail = -1;
					for(int i = 1; i <= 5; i++){
						int guildId = rs.getInt("guild" + i);
						if(add){
							if(guildId == -1){
								avail = i;
								break;
							}
						}else if(guildId == gid){
							avail = i;
							break;
						}
					}
					if(avail != -1){ // empty slot
						try(PreparedStatement ps2 = con.prepareStatement("UPDATE alliance SET guild" + avail + " = ? WHERE id = ?")){
							if(add){
								ps2.setInt(1, gid);
							}else{
								ps2.setInt(1, -1);
							}
							ps2.setInt(2, this.allianceId);
							ps2.executeUpdate();
							ret = true;
						}
					}
				}
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		return ret;
	}

	public boolean removeGuild(int gid){
		synchronized(guilds){
			int gIndex = getGuildIndex(gid);
			if(gIndex != -1){
				guilds[gIndex] = -1;
			}
			return addRemGuildFromDB(gid, false);
		}
	}

	public boolean addGuild(int gid){
		synchronized(guilds){
			if(getGuildIndex(gid) == -1){
				int emptyIndex = getGuildIndex(-1);
				if(emptyIndex != -1){
					guilds[emptyIndex] = gid;
					return addRemGuildFromDB(gid, true);
				}
			}
		}
		return false;
	}

	private int getGuildIndex(int gid){
		for(int i = 0; i < guilds.length; i++){
			if(guilds[i] == gid) return i;
		}
		return -1;
	}

	public void setRankTitle(String[] ranks){
		rankTitles = ranks;
	}

	public void setNotice(String notice){
		this.notice = notice;
	}

	public int getId(){
		return allianceId;
	}

	public String getName(){
		return name;
	}

	public String getRankTitle(int rank){
		return rankTitles[rank - 1];
	}

	public String getAllianceNotice(){
		return notice;
	}

	public List<Integer> getGuilds(){
		List<Integer> guilds_ = new LinkedList<Integer>();
		for(int guild : guilds){
			if(guild != -1){
				guilds_.add(guild);
			}
		}
		return guilds_;
	}

	public String getNotice(){
		return notice;
	}

	public int getCapacity(){
		return capacity;
	}

	public void addInvitation(int leaderid, String guildName){
		invitations.put(leaderid, guildName);
	}

	public boolean isInvited(int leaderid, String guildName){
		if(!invitations.containsKey(leaderid)) return false;
		return invitations.get(leaderid).equalsIgnoreCase(guildName);
	}

	public void removeInvited(int id){
		invitations.remove(id);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(allianceId);
		out.writeByte(capacity);
		out.writeObject(name);
		out.writeObject(notice);
		for(int i = 0; i < guilds.length; i++){
			out.writeInt(guilds[i]);
		}
		for(int i = 0; i < rankTitles.length; i++){
			out.writeObject(rankTitles[i]);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		allianceId = in.readInt();
		capacity = in.readByte();
		name = (String) in.readObject();
		notice = (String) in.readObject();
		for(int i = 0; i < guilds.length; i++){
			guilds[i] = in.readInt();
		}
		for(int i = 0; i < rankTitles.length; i++){
			rankTitles[i] = (String) in.readObject();
		}
	}
}
