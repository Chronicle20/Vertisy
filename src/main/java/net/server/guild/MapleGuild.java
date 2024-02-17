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
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import client.MapleCharacter;
import client.MapleClient;
import net.world.WorldServer;
import tools.BigBrother;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleGuild implements Externalizable{

	private static final long serialVersionUID = -2614216857405564324L;
	public final static int CREATE_GUILD_COST = 1500000;
	public final static int CHANGE_EMBLEM_COST = 5000000;
	private List<MapleGuildCharacter> members = new ArrayList<>();
	private String rankTitles[] = new String[5]; // 1 = master, 2 = jr, 5 = lowest member
	private String name, notice;
	private int id, gp, logo, logoColor, leader, capacity, logoBG, logoBGColor, allianceId, world;
	private long meso, coins;

	public MapleGuild(){
		super();
	}

	public MapleGuild(int guildid, int world){
		Connection con = DatabaseConnection.getConnection();
		try{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = " + guildid);
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				id = -1;
				ps.close();
				rs.close();
				return;
			}
			id = guildid;
			this.world = world;
			name = rs.getString("name");
			gp = rs.getInt("GP");
			logo = rs.getInt("logo");
			logoColor = rs.getInt("logoColor");
			logoBG = rs.getInt("logoBG");
			logoBGColor = rs.getInt("logoBGColor");
			capacity = rs.getInt("capacity");
			for(int i = 1; i <= 5; i++){
				rankTitles[i - 1] = rs.getString("rank" + i + "title");
			}
			leader = rs.getInt("leader");
			notice = rs.getString("notice");
			allianceId = rs.getInt("allianceId");
			meso = rs.getLong("meso");
			coins = rs.getLong("coins");
			ps.close();
			rs.close();
			ps = con.prepareStatement("SELECT id, name, "/*level, job,*/ + " guildrank, allianceRank, gp FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC");
			ps.setInt(1, guildid);
			rs = ps.executeQuery();
			if (!rs.next()) {
				rs.close();
				ps.close();
				return;
			}
			do{
				members.add(new MapleGuildCharacter(rs.getInt("id"), /*rs.getInt("level")*/ -1, rs.getString("name"), (byte) -1, world, -1/*rs.getInt("job")*/, rs.getInt("guildrank"), guildid, false, rs.getInt("allianceRank"), rs.getInt("gp")));
			}while(rs.next());
			ps.close();
			rs.close();
		}catch(SQLException se){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, se);
		}
	}

	public void writeToDB(boolean bDisband){
		try{
			Connection con = DatabaseConnection.getConnection();
			if(!bDisband){
				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE guilds SET GP = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
				for(int i = 0; i < 5; i++){
					builder.append("rank").append(i + 1).append("title = ?, ");
				}
				builder.append("capacity = ?, notice = ?, meso = ?, coins = ?, world = ? WHERE guildid = ?");
				try(PreparedStatement ps = con.prepareStatement(builder.toString())){
					ps.setInt(1, gp);
					ps.setInt(2, logo);
					ps.setInt(3, logoColor);
					ps.setInt(4, logoBG);
					ps.setInt(5, logoBGColor);
					for(int i = 6; i < 11; i++){
						ps.setString(i, rankTitles[i - 6]);
					}
					ps.setInt(11, capacity);
					ps.setString(12, notice);
					ps.setLong(13, meso);
					ps.setLong(14, coins);
					ps.setInt(15, world);
					ps.setInt(16, this.id);
					ps.execute();
				}
			}else{
				PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?");
				ps.setInt(1, this.id);
				ps.execute();
				ps.close();
				ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
				ps.setInt(1, this.id);
				ps.execute();
				ps.close();
				this.broadcast(MaplePacketCreator.guildDisband(this.id));
			}
		}catch(SQLException se){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, se);
		}
	}

	public int getId(){
		return id;
	}

	public int getLeaderId(){
		return leader;
	}

	public int getGP(){
		return gp;
	}

	public int getLogo(){
		return logo;
	}

	public void setLogo(int l){
		logo = l;
	}

	public int getLogoColor(){
		return logoColor;
	}

	public void setLogoColor(int c){
		logoColor = c;
	}

	public int getLogoBG(){
		return logoBG;
	}

	public void setLogoBG(int bg){
		logoBG = bg;
	}

	public int getLogoBGColor(){
		return logoBGColor;
	}

	public void setLogoBGColor(int c){
		logoBGColor = c;
	}

	public String getNotice(){
		if(notice == null) return "";
		return notice;
	}

	public String getName(){
		return name;
	}

	public java.util.Collection<MapleGuildCharacter> getMembers(){
		return java.util.Collections.unmodifiableCollection(members);
	}

	public int getCapacity(){
		return capacity;
	}

	public void broadcast(final byte[] packet){
		broadcast(packet, -1);
	}

	public void broadcast(final byte[] packet, int dontSendTo){
		List<Integer> sendTo = new ArrayList<>();
		for(MapleGuildCharacter mgc : members){
			if(mgc.getId() != dontSendTo) sendTo.add(mgc.getId());
		}
		try{
			WorldServer.getInstance().broadcastPacket(sendTo, packet);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public void guildMessage(final byte[] serverNotice){
		List<Integer> players = new ArrayList<>();
		members.forEach(mgc-> players.add(mgc.getId()));
		try{
			WorldServer.getInstance().broadcastPacket(players, serverNotice);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public final void setOnline(MapleGuildCharacter mgc, boolean online, int channel){
		boolean bBroadcast = true;
		for(MapleGuildCharacter gc : members){
			if(gc.getId() == mgc.getId()){
				if(gc.isOnline() && online){
					bBroadcast = false;
				}
				gc.setOnline(online);
				gc.setChannel(channel);
				break;
			}
		}
		memberLevelJobUpdate(mgc);
		if(bBroadcast){
			this.broadcast(MaplePacketCreator.guildMemberOnline(id, mgc.getId(), online), mgc.getId());
		}
	}

	public void guildChat(String sourceName, int sourceid, String message){
		BigBrother.guild(this, sourceName, message);
		this.broadcast(MaplePacketCreator.multiChat(sourceName, message, 2), sourceid);
	}

	public String getRankTitle(int rank){
		return rankTitles[rank - 1];
	}

	public static int createGuild(int leaderId, String name){
		try{
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ps.close();
				rs.close();
				return 0;
			}
			ps.close();
			rs.close();
			ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `notice`) VALUES (?, ?, ?)");
			ps.setInt(1, leaderId);
			ps.setString(2, name);
			ps.setString(3, "");
			ps.execute();
			ps.close();
			ps = con.prepareStatement("SELECT guildid FROM guilds WHERE leader = ?");
			ps.setInt(1, leaderId);
			rs = ps.executeQuery();
			rs.next();
			int guildid = rs.getInt("guildid");
			rs.close();
			ps.close();
			return guildid;
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return 0;
		}
	}

	public int addGuildMember(MapleGuildCharacter mgc){
		synchronized(members){
			if(members.size() >= capacity) return 0;
			for(int i = members.size() - 1; i >= 0; i--){
				if(members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(mgc.getName()) < 0){
					members.add(i + 1, mgc);
					break;
				}
			}
		}
		this.broadcast(MaplePacketCreator.newGuildMember(mgc));
		return 1;
	}

	public void leaveGuild(MapleGuildCharacter mgc){
		this.broadcast(MaplePacketCreator.memberLeft(mgc, false));
		synchronized(members){
			members.remove(mgc);
		}
	}

	public void expelMember(MapleGuildCharacter initiator, String name, int cid){
		synchronized(members){
			java.util.Iterator<MapleGuildCharacter> itr = members.iterator();
			MapleGuildCharacter mgc;
			while(itr.hasNext()){
				mgc = itr.next();
				if(mgc.getId() == cid && initiator.getGuildRank() < mgc.getGuildRank()){
					this.broadcast(MaplePacketCreator.memberLeft(mgc, true));
					itr.remove();
					try{
						if(mgc.isOnline()){
							WorldServer.getInstance().removeGP(id, mgc.getId(), mgc.getGP());
							// MapleCharacter mc = Server.getInstance().getWorld(mgc.getWorld()).getCharacterById(mgc.getId());
							removeGP(mgc.getGP());
							// mc.removeGP(mc.getGP());
							WorldServer.getInstance().setGuildAndRank(cid, 0, 5);
						}else{
							int gp = 0;
							try{
								try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)")){
									ps.setString(1, mgc.getName());
									ps.setString(2, initiator.getName());
									ps.setString(3, "You have been expelled from the guild.");
									ps.setLong(4, System.currentTimeMillis());
									ps.executeUpdate();
								}
								try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT gp FROM characters where id = ?")){
									ps.setInt(1, cid);
									try(ResultSet rs = ps.executeQuery()){
										if(rs.next()){
											gp = rs.getInt("gp");
										}
									}
								}
							}catch(SQLException e){
								Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "expelMember - MapleGuild ");
							}
							removeGP(gp);
							WorldServer.getInstance().setOfflineGuildStatus((short) 0, (byte) 5, cid, gp);
						}
					}catch(Exception re){
						Logger.log(LogType.ERROR, LogFile.EXCEPTION, re);
						return;
					}
					return;
				}
			}
			System.out.println("Unable to find member with name " + name + " and id " + cid);
		}
	}

	public void changeRank(int cid, int newRank){
		for(MapleGuildCharacter mgc : members){
			if(cid == mgc.getId()){
				try{
					if(mgc.isOnline()){
						WorldServer.getInstance().setGuildAndRank(cid, this.id, newRank);
					}else{
						WorldServer.getInstance().setOfflineGuildStatus((short) this.id, (byte) newRank, cid, mgc.getGP());
					}
				}catch(Exception re){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, re);
					return;
				}
				mgc.setGuildRank(newRank);
				this.broadcast(MaplePacketCreator.changeRank(mgc));
				return;
			}
		}
	}

	public void setGuildNotice(String notice){
		this.notice = notice;
		writeToDB(false);
		this.broadcast(MaplePacketCreator.guildNotice(this.id, notice));
	}

	public void memberLevelJobUpdate(MapleGuildCharacter mgc){
		for(MapleGuildCharacter member : members){
			if(mgc.getId() == member.getId()){
				member.setName(mgc.getName());
				member.setJobId(mgc.getJobId());
				member.setLevel(mgc.getLevel());
				member.setWorld(mgc.getWorld());
				broadcast(MaplePacketCreator.guildMemberLevelJobUpdate(mgc));
				break;
			}
		}
	}

	@Override
	public boolean equals(Object other){
		if(!(other instanceof MapleGuildCharacter)) return false;
		MapleGuildCharacter o = (MapleGuildCharacter) other;
		return(o.getId() == id && o.getName().equals(name));
	}

	@Override
	public int hashCode(){
		int hash = 3;
		hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 89 * hash + this.id;
		return hash;
	}

	public void changeRankTitle(String[] ranks){
		System.arraycopy(ranks, 0, rankTitles, 0, 5);
		this.broadcast(MaplePacketCreator.rankTitleChange(this.id, ranks));
		this.writeToDB(false);
	}

	public void disbandGuild(){
		this.writeToDB(true);
		try{
			WorldServer.getInstance().setGuildAndRank(members.stream().map(mgc-> mgc.getId()).collect(Collectors.toList()), 0, 5, -1);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public void setGuildEmblem(short bg, byte bgcolor, short logo, byte logocolor){
		this.logoBG = bg;
		this.logoBGColor = bgcolor;
		this.logo = logo;
		this.logoColor = logocolor;
		this.writeToDB(false);
		// this.broadcast(null, -1, BCOp.EMBELMCHANGE);
		try{
			WorldServer.getInstance().changeEmblem(this.id, members.stream().map(mgc-> mgc.getId()).collect(Collectors.toList()), new MapleGuildSummary(this));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public MapleGuildCharacter getMGC(int cid){
		for(MapleGuildCharacter mgc : members){
			if(mgc.getId() == cid) return mgc;
		}
		return null;
	}

	public String increaseCapacity(){
		if(capacity > 99) return "max";
		int cost = getIncreaseGuildCost(getCapacity());
		if(getMeso() < cost) return "Your guild doesn't have enough mesos.";
		capacity += 5;
		this.removeMeso(cost);
		this.writeToDB(false);
		this.broadcast(MaplePacketCreator.guildCapacityChange(this.id, this.capacity));
		return "good";
	}

	public boolean gainGP(int amount){
		return gainGP(-1, amount);
	}

	public boolean gainGP(int chrid, int amount){
		if(gp + amount > 0){
			if(chrid != -1){
				MapleGuildCharacter mgc = getMGC(chrid);
				if(mgc != null){
					mgc.gainGP(amount);
				}
			}
			this.gp += amount;
			this.writeToDB(false);
			this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
			return true;
		}
		return false;
	}

	public void removeGP(int amount){
		this.gp -= amount;
		this.writeToDB(false);
		this.guildMessage(MaplePacketCreator.updateGP(this.id, this.gp));
	}

	public static MapleGuildResponse sendInvite(MapleClient c, String targetName){
		MapleCharacter mc = c.getChannelServer().getChannelServer().getCharacterByName(targetName);
		if(mc == null) return MapleGuildResponse.NOT_IN_CHANNEL;
		if(mc.getGuildId() > 0) return MapleGuildResponse.ALREADY_IN_GUILD;
		mc.getClient().announce(MaplePacketCreator.guildInvite(c.getPlayer().getGuildId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob().getId()));
		return null;
	}

	public static void displayGuildRanks(MapleClient c, int npcid){
		try{
			ResultSet rs;
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `name`, `GP`, `logoBG`, `logoBGColor`, `logo`, `logoColor` FROM guilds WHERE NOT `guildid` = '1' ORDER BY `GP` DESC LIMIT 50")){
				rs = ps.executeQuery();
				c.announce(MaplePacketCreator.showGuildRanks(npcid, rs));
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("failed to display guild ranks. " + e);
		}
	}

	public int getAllianceId(){
		return allianceId;
	}

	public void setAllianceId(int aid){
		this.allianceId = aid;
		try{
			try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE guilds SET allianceId = ? WHERE guildid = ?")){
				ps.setInt(1, aid);
				ps.setInt(2, id);
				ps.executeUpdate();
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public int getIncreaseGuildCost(int size){
		return 500000 * (size - 6) / 6;
	}

	public long getMeso(){
		return meso;
	}

	public void addMeso(long meso){
		Logger.log(LogType.INFO, LogFile.GUILD_FUNDS, name, "Added " + meso + " meso.");
		this.meso += meso;
	}

	public void removeMeso(long meso){
		Logger.log(LogType.INFO, LogFile.GUILD_FUNDS, name, "Removed " + meso + " meso.");
		this.meso -= meso;
	}

	public long getCoins(){
		return coins;
	}

	public void addCoins(long coin){
		Logger.log(LogType.INFO, LogFile.GUILD_FUNDS, name, "Added " + coin + " coins.");
		this.coins += coin;
	}

	public void removeCoins(long coin){
		Logger.log(LogType.INFO, LogFile.GUILD_FUNDS, name, "Removed " + coin + " coins.");
		this.coins -= coin;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeInt(id);
		out.writeObject(name);
		out.writeObject(notice);
		out.writeInt(capacity);
		out.writeShort(logoBG);
		out.writeByte(logoBGColor);
		out.writeShort(logo);
		out.writeByte(logoColor);
		out.writeInt(leader);
		out.writeInt(allianceId);
		out.writeInt(gp);
		out.writeLong(meso);
		out.writeLong(coins);
		for(int i = 0; i < rankTitles.length; i++){
			out.writeObject(rankTitles[i]);
		}
		out.writeByte(members.size());
		for(MapleGuildCharacter mgc : members){
			mgc.writeExternal(out);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		id = in.readInt();
		name = (String) in.readObject();
		notice = (String) in.readObject();
		capacity = in.readInt();
		logoBG = in.readShort();
		logoBGColor = in.readByte();
		logo = in.readShort();
		logoColor = in.readByte();
		leader = in.readInt();
		allianceId = in.readInt();
		gp = in.readInt();
		meso = in.readLong();
		coins = in.readLong();
		for(int i = 0; i < rankTitles.length; i++){
			rankTitles[i] = (String) in.readObject();
		}
		byte loop = in.readByte();
		for(int i = 0; i < loop; i++){
			MapleGuildCharacter mgc = new MapleGuildCharacter();
			mgc.readExternal(in);
			members.add(mgc);
		}
	}
}
