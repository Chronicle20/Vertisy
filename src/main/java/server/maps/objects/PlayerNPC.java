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
package server.maps.objects;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import server.maps.MapleMap;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class PlayerNPC extends AbstractMapleMapObject{

	/**
	 * Script IDS:
	 * Total: 9901000 - 9901319
	 * RSSkills: 9901000 - 9901030
	 */
	private Map<Short, Integer> equips = new HashMap<Short, Integer>();// Pos, itemid
	public int id, scriptID, face, hair;
	public byte skin, dir, gender;
	public String name = "";
	public int FH, RX0, RX1, x, CY;
	public int mapid;

	/*public PlayerNPC(ResultSet rs){
		try{
			id = rs.getInt("id");
			CY = rs.getInt("cy");
			name = rs.getString("name");
			hair = rs.getInt("hair");
			face = rs.getInt("face");
			skin = rs.getByte("skin");
			dir = rs.getByte("dir");
			gender = rs.getByte("gender");
			FH = rs.getInt("Foothold");
			RX0 = rs.getInt("rx0");
			RX1 = rs.getInt("rx1");
			scriptID = rs.getInt("ScriptId");
			setPosition(new Point(rs.getInt("x"), CY));
			loadEquipsFromDB();
		}catch(SQLException e){Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);}
	}*/
	public boolean createPlayerNPC(int scriptID){
		boolean ret = false;
		this.scriptID = scriptID;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM playernpcs WHERE ScriptId = ?")){
			ps.setInt(1, scriptID);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					id = rs.getInt("id");
					name = rs.getString("name");
					face = rs.getInt("face");
					hair = rs.getInt("hair");
					skin = rs.getByte("skin");
					dir = rs.getByte("dir");
					gender = rs.getByte("gender");
					x = rs.getInt("x");
					CY = rs.getInt("cy");
					mapid = rs.getInt("map");
					FH = rs.getInt("FootHold");
					RX0 = rs.getInt("rx0");
					RX1 = rs.getInt("rx1");
					ret = true;
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return ret;
	}

	public void createPlayerNPC(MapleCharacter chr, int scriptID){
		name = chr.getName();
		face = chr.getFace();
		hair = chr.getHair();
		skin = (byte) chr.getSkinColor().getId();
		dir = (byte) (chr.isFacingLeft() ? 0 : 1);
		gender = (byte) chr.getGender();
		x = chr.getPosition().x;
		CY = chr.getPosition().y;
		mapid = chr.getMapId();
		this.scriptID = scriptID;
		FH = chr.getMap().getMapData().getFootholds().findBelow(chr.getPosition()).getId();
		RX0 = chr.getPosition().x;
		RX1 = chr.getPosition().x;
	}

	public void saveMainStuffToDB(){
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs (name, face, hair, skin, x, cy, map, ScriptId, Foothold, rx0, rx1, dir) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setString(1, name);
			ps.setInt(2, face);
			ps.setInt(3, hair);
			ps.setInt(4, skin);
			ps.setInt(5, x);
			ps.setInt(6, CY);
			ps.setInt(7, mapid);
			ps.setInt(8, scriptID);
			ps.setInt(9, FH);
			ps.setInt(10, RX0);
			ps.setInt(11, RX1);
			ps.setByte(12, dir);
			ps.executeUpdate();
			try(ResultSet rs = ps.getGeneratedKeys()){
				rs.next();
				id = rs.getInt(1);
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void updateMainStuffInDB(){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE playernpcs SET name = ?, hair = ?, face = ?, skin = ?, gender = ? WHERE id = ?")){
			ps.setString(1, name);
			ps.setInt(2, hair);
			ps.setInt(3, face);
			ps.setByte(4, skin);
			ps.setByte(5, gender);
			ps.setInt(6, id);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void loadMainStuffFromDB(int npcid){
		this.id = npcid;
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM playernpcs WHERE id = ?")){
			ps.setInt(1, npcid);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					name = rs.getString("name");
					face = rs.getInt("face");
					hair = rs.getInt("hair");
					skin = rs.getByte("skin");
					dir = rs.getByte("dir");
					gender = rs.getByte("gender");
					x = rs.getInt("x");
					CY = rs.getInt("cy");
					mapid = rs.getInt("map");
					scriptID = rs.getInt("ScriptId");
					FH = rs.getInt("FootHold");
					RX0 = rs.getInt("rx0");
					RX1 = rs.getInt("rx1");
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void loadEquipsFromDB(){
		equips.clear();
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT equippos, equipid FROM playernpcs_equip WHERE NpcId = ?")){
			ps.setInt(1, id);
			try(ResultSet rs2 = ps.executeQuery()){
				while(rs2.next()){
					equips.put(rs2.getShort("equippos"), rs2.getInt("equipid"));
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void saveEquipsToDB(){
		Connection con = DatabaseConnection.getConnection();
		try(PreparedStatement ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE NpcId = ?")){
			ps.setInt(1, id);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		try(PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs_equip (NpcId, equipid, equippos) VALUES (?, ?, ?)")){
			ps.setInt(1, id);
			for(Short pos : equips.keySet()){
				ps.setInt(2, equips.get(pos));
				ps.setInt(3, pos);
				ps.addBatch();
			}
			ps.executeBatch();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public void setEquipsTo(MapleCharacter chr){
		equips.clear();
		for(Item equip : chr.getInventory(MapleInventoryType.EQUIPPED)){
			int position = Math.abs(equip.getPosition());
			if((position < 12 && position > 0) || (position > 100 && position < 112)){
				equips.put(equip.getPosition(), equip.getItemId());
			}
		}
	}

	public void addToMaps(){
		for(Channel channel : ChannelServer.getInstance().getChannels()){
			MapleMap m = channel.getMap(mapid);
			m.broadcastMessage(MaplePacketCreator.spawnPlayerNPC(this));
			m.broadcastMessage(MaplePacketCreator.getPlayerNPC(this));
			m.addMapObject(this);
		}
	}

	public Map<Short, Integer> getEquips(){
		return equips;
	}

	public int getId(){
		return scriptID;
	}

	public int getFH(){
		return FH;
	}

	public int getRX0(){
		return RX0;
	}

	public int getRX1(){
		return RX1;
	}

	public int getCY(){
		return CY;
	}

	public byte getSkin(){
		return skin;
	}

	public void setSkin(byte skin){
		this.skin = skin;
	}

	public byte getDirection(){
		return dir;
	}

	public byte getGender(){
		return gender;
	}

	public void setGender(byte gender){
		this.gender = gender;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public int getFace(){
		return face;
	}

	public void setFace(int face){
		this.face = face;
	}

	public int getHair(){
		return hair;
	}

	public void setHair(int hair){
		this.hair = hair;
	}

	@Override
	public void sendDestroyData(MapleClient client){
		return;
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.PLAYER_NPC;
	}

	@Override
	public void sendSpawnData(MapleClient client){
		client.announce(MaplePacketCreator.spawnPlayerNPC(this));
		client.announce(MaplePacketCreator.getPlayerNPC(this));
	}

	@Override
	public PlayerNPC clone(){
		PlayerNPC npc = new PlayerNPC();
		npc.id = id;
		npc.scriptID = scriptID;
		npc.face = face;
		npc.hair = hair;
		npc.skin = skin;
		npc.dir = dir;
		npc.gender = gender;
		npc.name = name;
		npc.FH = FH;
		npc.RX0 = RX0;
		npc.RX1 = RX1;
		npc.x = x;
		npc.CY = CY;
		npc.mapid = mapid;
		npc.setPosition(getPosition());
		npc.equips = equips;
		return npc;
	}

	@Override
	public void save(MaplePacketLittleEndianWriter mplew){
		super.save(mplew);
		mplew.writeInt(id);
		mplew.writeInt(scriptID);
	}

	@Override
	public void load(LittleEndianAccessor slea){
		super.load(slea);
		id = slea.readInt();
		scriptID = slea.readInt();
		this.createPlayerNPC(scriptID);
		this.loadEquipsFromDB();
	}
}