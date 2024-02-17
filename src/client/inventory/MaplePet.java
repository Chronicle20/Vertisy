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
package client.inventory;

import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import client.MapleCharacter;
import constants.ExpTable;
import server.ItemData;
import server.ItemInformationProvider;
import server.movement.Elem;
import server.movement.MovePath;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.ObjectParser;
import tools.Randomizer;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserRemote;

/**
 * @author Matze
 */
public class MaplePet extends Item{

	private String name;
	private int uniqueid;
	private int closeness = 0;
	private byte level = 1;
	private int fullness = 100;
	private int Fh;
	private Point pos;
	private int stance;
	private boolean summoned;
	private int nRemainLife;
	private List<Integer> aExceptionList = new ArrayList<>();

	private MaplePet(int id, short position, int uniqueid){
		super(id, position, (short) 1);
		this.uniqueid = uniqueid;
	}

	public static MaplePet loadFromDb(int itemid, short position, int petid){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, level, closeness, fullness, summoned, remainLife, excluded FROM pets WHERE petid = ?")){ // Get pet details..
			MaplePet ret = new MaplePet(itemid, position, petid);
			ps.setInt(1, petid);
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					ret.setName(rs.getString("name"));
					ret.setCloseness(Math.min(rs.getInt("closeness"), 30000));
					ret.setLevel((byte) Math.min(rs.getByte("level"), 30));
					ret.setFullness(Math.min(rs.getInt("fullness"), 100));
					ret.setSummoned(rs.getInt("summoned") == 1);
					ret.setRemainLife(rs.getInt("remainLife"));
					String[] exc = rs.getString("excluded").split(",");
					for(String id : exc){
						if(id.length() > 0){
							ret.addItemException(ObjectParser.isInt(id));
						}
					}
				}
			}
			return ret;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return null;
		}
	}

	public void saveToDb(){
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, summoned = ?, remainLife = ?, excluded = ? WHERE petid = ?")){
			ps.setString(1, getName());
			ps.setInt(2, getLevel());
			ps.setInt(3, getCloseness());
			ps.setInt(4, getFullness());
			ps.setInt(5, isSummoned() ? 1 : 0);
			ps.setInt(6, getRemainLife());
			StringBuilder exc = new StringBuilder();
			for(int itemid : aExceptionList){
				exc.append(itemid);
				exc.append(",");
			}
			if(exc.toString().contains(",")) exc.setLength(exc.length() - 1);
			ps.setString(7, exc.toString());
			ps.setInt(8, getUniqueId());
			ps.executeUpdate();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public static int createPet(int itemid){
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, remainLife, level, closeness, fullness, summoned) VALUES (?, ?, 1, 0, 100, 0)", Statement.RETURN_GENERATED_KEYS);
			ItemData data = ItemInformationProvider.getInstance().getItemData(itemid);
			String name = data.name;
			ps.setString(1, name);
			ps.setInt(2, data.limitedLife);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int ret = -1;
			if(rs.next()){
				ret = rs.getInt(1);
			}
			rs.close();
			ps.close();
			return ret;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return -1;
		}
	}

	public static int createPet(int itemid, byte level, int closeness, int fullness){
		try{
			PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, level, closeness, fullness, remainLife, summoned) VALUES (?, ?, ?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
			ItemData data = ItemInformationProvider.getInstance().getItemData(itemid);
			ps.setString(1, data.name);
			ps.setByte(2, level);
			ps.setInt(3, closeness);
			ps.setInt(4, fullness);
			ps.setInt(5, data.limitedLife);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			int ret = -1;
			if(rs.next()){
				ret = rs.getInt(1);
				rs.close();
				ps.close();
			}
			return ret;
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return -1;
		}
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public int getUniqueId(){
		return uniqueid;
	}

	public void setUniqueId(int id){
		this.uniqueid = id;
	}

	public int getCloseness(){
		return closeness;
	}

	public void setCloseness(int closeness){
		this.closeness = closeness;
	}

	public void gainCloseness(MapleCharacter chr, int x){
		int oldCloseness = this.closeness;
		this.closeness += x;
		this.closeness = Math.min(30000, closeness);
		if(closeness >= ExpTable.getClosenessNeededForLevel(getLevel())){
			setLevel((byte) (getLevel() + 1));
			chr.getClient().announce(UserLocal.UserEffect.showOwnPetLevelUp(chr.getPetIndex(this)));
			chr.getMap().broadcastMessage(UserRemote.UserEffect.showPetLevelUp(chr, chr.getPetIndex(this)));
		}
		if(oldCloseness != closeness) saveToDb();
	}

	public void feed(MapleCharacter chr){
		boolean gainCloseness = false;
		if(Randomizer.nextInt(100) + 1 > 50){
			gainCloseness = true;
		}
		if(getFullness() < 100){
			int newFullness = getFullness() + 30;
			if(newFullness > 100){
				newFullness = 100;
			}
			setFullness(newFullness);
			if(gainCloseness && getCloseness() < 30000){
				int newCloseness = getCloseness() + 1;
				if(newCloseness > 30000){
					newCloseness = 30000;
				}
				setCloseness(newCloseness);
				if(newCloseness >= ExpTable.getClosenessNeededForLevel(getLevel())){
					setLevel((byte) (getLevel() + 1));
					chr.getClient().announce(UserLocal.UserEffect.showOwnPetLevelUp(chr.getPetIndex(this)));
					chr.getMap().broadcastMessage(UserRemote.UserEffect.showPetLevelUp(chr, chr.getPetIndex(this)));
				}
			}
			chr.getMap().broadcastMessage(MaplePacketCreator.commandResponse(chr.getId(), chr.getPetIndex(this), 0, true));
			chr.getClient().announce(CWvsContext.enableActions());
		}else{
			if(gainCloseness){
				int newCloseness = getCloseness() - 1;
				if(newCloseness < 0){
					newCloseness = 0;
				}
				setCloseness(newCloseness);
				if(getLevel() > 1 && newCloseness < ExpTable.getClosenessNeededForLevel(getLevel())){
					setLevel((byte) (getLevel() - 1));
				}
			}
			chr.getMap().broadcastMessage(MaplePacketCreator.commandResponse(chr.getId(), chr.getPetIndex(this), 0, false));
			chr.getClient().announce(CWvsContext.enableActions());
		}
	}

	public byte getLevel(){
		return level;
	}

	public void setLevel(byte level){
		this.level = level;
	}

	public int getFullness(){
		return fullness;
	}

	public void setFullness(int fullness){
		this.fullness = fullness;
	}

	public int getFh(){
		return Fh;
	}

	public void setFh(int Fh){
		this.Fh = Fh;
	}

	public Point getPos(){
		return pos;
	}

	public void setPos(Point pos){
		this.pos = pos;
	}

	public int getStance(){
		return stance;
	}

	public void setStance(int stance){
		this.stance = stance;
	}

	public boolean isSummoned(){
		return summoned;
	}

	public void setSummoned(boolean yes){
		this.summoned = yes;
	}

	public boolean canConsume(int itemId){
		for(int petId : ItemInformationProvider.getInstance().getItemData(itemId).petCanConsume){
			if(petId == this.getItemId()) return true;
		}
		return false;
	}

	public int getRemainLife(){
		return nRemainLife;
	}

	public void setRemainLife(int nRemainLife){
		this.nRemainLife = nRemainLife;
	}

	public List<Integer> getExceptionList(){
		return aExceptionList;
	}

	public void addItemException(int x){
		if(!aExceptionList.contains(x)) aExceptionList.add(x);
	}

	public void updatePosition(MovePath path){
		for(Elem elem : path.lElem){
			if(elem.x != 0 || elem.y != 0){
				setPos(new Point(elem.x, elem.y));
			}
			setStance(elem.bMoveAction);
		}
	}
}