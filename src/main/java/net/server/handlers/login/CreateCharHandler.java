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
package net.server.handlers.login;

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleSkinColor;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.ItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public final class CreateCharHandler extends AbstractMaplePacketHandler{

	public static void loadCreateCharItems(){
		// PremiumCharMale/PremiumCharFemale = Explorer/Cygnus - 0/1
		// OrientCharMale/OrientCharFemale = Aran - 2
		// EvanCharMale/EvanCharFemale = Evan - 3
		// 0 = Face
		// 1 = Hair
		// 2 = Hair Color
		// 3 = Skin
		// 4 = Top
		// 5 = Pants
		// 6 = Boots
		// 7 = Weapon
		MapleDataProvider etc = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
		MapleData charInfo = etc.getData("MakeCharInfo.img");
		for(MapleData type : charInfo.getChildren()){
			JobType jobType = null;
			for(JobType jType : JobType.values()){
				if(type.getName().startsWith(jType.wzPrefix)) jobType = jType;
			}
			if(jobType != null){
				CharData charData = new CharData();
				charData.load(type);
				List<CharData> datas = data.get(jobType);
				if(datas == null) datas = new ArrayList<>();
				datas.add(charData);
				data.put(jobType, datas);
			}else if(type.getName().equals("Info")){
				for(MapleData type2 : type.getChildren()){
					for(JobType jType : JobType.values()){
						if(type2.getName().startsWith(jType.wzPrefix)) jobType = jType;
					}
					if(jobType != null){
						CharData charData = new CharData();
						charData.load(type2);
						List<CharData> datas = data.get(jobType);
						if(datas == null) datas = new ArrayList<>();
						datas.add(charData);
						data.put(jobType, datas);
					}
				}
			}
		}
	}

	private static Map<JobType, List<CharData>> data = new HashMap<>();

	public enum JobType{
		KOC("PremiumChar"),
		EXPLORER("Char"),
		ARAN("OrientChar"),
		EVAN("EvanChar"),;

		public String wzPrefix;

		private JobType(String wzPrefix){
			this.wzPrefix = wzPrefix;
		}
	}

	public static class CharData{

		public byte gender = -1;
		public List<Integer> face = new ArrayList<>();// 0 = Face
		public List<Integer> hair = new ArrayList<>();// 1 = Hair
		public List<Integer> hairColor = new ArrayList<>();// 2 = Hair Color
		public List<Integer> skin = new ArrayList<>();// 3 = Skin
		public List<Integer> top = new ArrayList<>();// 4 = Top
		public List<Integer> bottom = new ArrayList<>();// 5 = Pants
		public List<Integer> shoes = new ArrayList<>();// 6 = Boots
		public List<Integer> weapon = new ArrayList<>();// 7 = Weapon

		public void load(MapleData data){
			if(data.getName().contains("Male")) gender = 0;
			else if(data.getName().contains("Female")) gender = 1;
			for(MapleData dataType : data.getChildren()){
				for(MapleData value : dataType.getChildren()){
					switch (dataType.getName()){
						case "0":
							face.add(MapleDataTool.getInt(value));
							break;
						case "1":
							hair.add(MapleDataTool.getInt(value));
							break;
						case "2":
							hairColor.add(MapleDataTool.getInt(value));
							break;
						case "3":
							skin.add(MapleDataTool.getInt(value));
							break;
						case "4":
							top.add(MapleDataTool.getInt(value));
							break;
						case "5":
							bottom.add(MapleDataTool.getInt(value));
							break;
						case "6":
							shoes.add(MapleDataTool.getInt(value));
							break;
						case "7":
							weapon.add(MapleDataTool.getInt(value));
							break;
					}
				}
			}
		}
	}

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		String name = slea.readMapleAsciiString();
		if(!MapleCharacter.canCreateChar(name)) return;
		MapleCharacter newchar = MapleCharacter.getDefault(c);
		newchar.setWorld(c.getWorld());
		int job = slea.readInt();
		short nSubJob = slea.readShort();
		int face = slea.readInt();
		int hair = slea.readInt();
		int hairColor = slea.readInt();
		int skincolor = slea.readInt();
		newchar.setSkinColor(MapleSkinColor.getById(skincolor));
		int top = slea.readInt();
		int bottom = slea.readInt();
		int shoes = slea.readInt();
		int weapon = slea.readInt();
		newchar.setGender(slea.readByte());
		newchar.setName(name);
		newchar.setHair(hair + hairColor);
		newchar.setFace(face);
		newchar.setSubJob(nSubJob);
		if(job == 0){ // Knights of Cygnus
			newchar.setJob(MapleJob.NOBLESSE);
			newchar.setMapId(130030000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (short) 0, (short) 1));
		}else if(job == 1){ // Adventurer
			newchar.setJob(MapleJob.BEGINNER);
			newchar.setMapId(/*specialJobType == 2 ? 3000600 : */10000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (short) 0, (short) 1));
		}else if(job == 2){ // Aran
			newchar.setJob(MapleJob.LEGEND);
			newchar.setMapId(914000000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (short) 0, (short) 1));
		}else if(job == 3){
			newchar.setJob(MapleJob.EVAN);
			newchar.setMapId(900010000);
			newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161052, (short) 0, (short) 1));
		}else{
			Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, c.getAccountName() + " did invalid job " + job);
			c.announce(MaplePacketCreator.deleteCharResponse(0, 9));
			return;
		}
		JobType jobType = JobType.values()[job];
		boolean illegal = false;
		CharData charData = data.get(jobType).stream().filter(data-> data.gender == newchar.getGender()).findFirst().get();
		if(!charData.face.contains(face)) illegal = true;
		if(!charData.hair.contains(hair)) illegal = true;
		if(!charData.hairColor.contains(hairColor)) illegal = true;
		if(!charData.skin.contains(skincolor)) illegal = true;
		if(!charData.top.contains(top)) illegal = true;
		if(!charData.bottom.contains(bottom)) illegal = true;
		if(!charData.shoes.contains(shoes)) illegal = true;
		if(!charData.weapon.contains(weapon)) illegal = true;
		int[] data = new int[]{job, nSubJob, face, hair, hairColor, skincolor, top, bottom, shoes, weapon, newchar.getGender()};
		if(illegal){
			AutobanFactory.PACKET_EDIT.alert(newchar, c.getAccountName() + " tried to packet edit in character creation with data: " + Arrays.toString(data));
			c.disconnect(true, false);
			return;
		}
		MapleInventory equipped = newchar.getInventory(MapleInventoryType.EQUIPPED);
		Item eq_top = ItemInformationProvider.getInstance().getEquipById(top);
		eq_top.setPosition((byte) -5);
		equipped.addFromDB(eq_top);
		Item eq_bottom = ItemInformationProvider.getInstance().getEquipById(bottom);
		eq_bottom.setPosition((byte) -6);
		equipped.addFromDB(eq_bottom);
		Item eq_shoes = ItemInformationProvider.getInstance().getEquipById(shoes);
		eq_shoes.setPosition((byte) -7);
		equipped.addFromDB(eq_shoes);
		Item eq_weapon = ItemInformationProvider.getInstance().getEquipById(weapon);
		eq_weapon.setPosition((byte) -11);
		equipped.addFromDB(eq_weapon.copy());
		if(!newchar.insertNewChar()){
			c.announce(MaplePacketCreator.deleteCharResponse(0, 9));
			return;
		}
		c.announce(MaplePacketCreator.addNewCharEntry(newchar));
		try{
			LoginServer.getInstance().getCenterInterface().broadcastGMPacket(MaplePacketCreator.sendYellowTip("[NEW CHAR]: " + c.getAccountName() + " has created a new character with IGN " + name));
		}catch(RemoteException | NullPointerException e){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
		}
	}
}