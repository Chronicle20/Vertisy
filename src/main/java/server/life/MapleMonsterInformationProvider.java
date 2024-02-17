/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.life;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import constants.ServerConstants;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.DatabaseConnection;
import tools.Pair;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class MapleMonsterInformationProvider{

	private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
	private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<>();
	private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();
	private final List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();

	protected MapleMonsterInformationProvider(){
		retrieveGlobal();
	}

	public static MapleMonsterInformationProvider getInstance(){
		return instance;
	}

	public final List<MonsterGlobalDropEntry> getGlobalDrop(){
		return globaldrops;
	}

	private void retrieveGlobal(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			final Connection con = DatabaseConnection.getConnection();
			ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
			rs = ps.executeQuery();
			while(rs.next()){
				globaldrops.add(new MonsterGlobalDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("continent"), rs.getByte("dropType"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
				if(rs != null){
					rs.close();
				}
			}catch(SQLException ignore){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ignore);
			}
		}
	}

	public final List<MonsterDropEntry> retrieveDrop(final int monsterId){
		if(drops.containsKey(monsterId)) return drops.get(monsterId);
		final List<MonsterDropEntry> ret = new LinkedList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
			ps.setInt(1, monsterId);
			rs = ps.executeQuery();
			while(rs.next()){
				ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getByte("dropType"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
			}
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			return ret;
		}finally{
			try{
				if(ps != null){
					ps.close();
				}
				if(rs != null){
					rs.close();
				}
			}catch(SQLException ignore){
				return ret;
			}
		}
		drops.put(monsterId, ret);
		return ret;
	}

	public void loadData(){
		File stringMobBin = new File(System.getProperty("wzpath") + "/bin/Life/MobNames.bin");
		if(ServerConstants.WZ_LOADING){
			MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
			MapleData data = dataProvider.getData("Mob.img");
			for(MapleData mobIdData : data.getChildren()){
				int mobIdFromData = Integer.parseInt(mobIdData.getName());
				String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
				mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
			}
			if(!stringMobBin.exists() && ServerConstants.BIN_DUMPING){
				try{
					stringMobBin.createNewFile();
					MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
					mplew.writeInt(mobPairList.size());
					for(Pair<Integer, String> p : mobPairList){
						mplew.writeInt(p.left);
						mplew.writeMapleAsciiString(p.right);
					}
					mplew.saveToFile(stringMobBin);
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}else{
			try{
				byte[] in = Files.readAllBytes(stringMobBin.toPath());
				ByteArrayByteStream babs = new ByteArrayByteStream(in);
				GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
				int size = glea.readInt();
				for(int i = 0; i < size; i++){
					mobPairList.add(new Pair<Integer, String>(glea.readInt(), glea.readMapleAsciiString()));
				}
				glea = null;
				babs = null;
				in = null;
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public ArrayList<Pair<Integer, String>> getMobsIDsFromName(String search){
		ArrayList<Pair<Integer, String>> retMobs = new ArrayList<Pair<Integer, String>>();
		for(Pair<Integer, String> mobPair : mobPairList){
			if(mobPair.getRight().toLowerCase().contains(search.toLowerCase())){
				retMobs.add(mobPair);
			}
		}
		return retMobs;
	}

	public static String getMobNameFromID(int id){
		try{
			return MapleLifeFactory.getMonster(id).getName();
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e, "Mob: " + id);
			return null; // nonexistant mob
		}
	}

	public final void clearDrops(){
		drops.clear();
		globaldrops.clear();
		retrieveGlobal();
	}
}