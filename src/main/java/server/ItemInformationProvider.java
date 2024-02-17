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
package server;

import java.io.File;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleCharacter;
import client.MapleJob;
import client.autoban.AutobanFactory;
import client.inventory.*;
import constants.EquipSlot;
import constants.ItemConstants;
import constants.ServerConstants;
import net.channel.ChannelServer;
import provider.*;
import server.item.Potential;
import server.life.MonsterDropEntry.DropCategory;
import tools.*;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Arnah
 */
public class ItemInformationProvider{

	private static ItemInformationProvider instance = null;
	protected MapleDataProvider itemData;
	protected MapleDataProvider equipData;
	protected MapleDataProvider stringData;
	protected MapleData cashStringData;
	protected MapleData consumeStringData;
	protected MapleData eqpStringData;
	protected MapleData etcStringData;
	protected MapleData insStringData;
	protected MapleData petStringData;
	protected Map<Integer, Integer> monsterBookID = new HashMap<>();
	protected Map<Integer, ItemData> itemDataCache = new HashMap<>();
	public Map<Short, Potential> potentials = new HashMap<>();

	public ItemData getItemData(int itemid){
		String wzPath = "";
		if(itemid == 0 || (itemid >= 20000 && itemid < 40000)){
			ItemData data = new ItemData();
			data.exists = false;
			return data;
		}
		switch (itemid / 10000){
			case 115:// Shoulder
			case 116:// Pocket
			case 117:// Crusader codex
			case 118:// Badge
			case 119:// Emblems
			case 120:// Totems
			case 161:// Engines(mech)
			case 162:// More mech shit
			case 163:// More mech shit
			case 164:// more mech shit
			case 165:// more mech shit
			case 182:// A singular 'Pet Label Ring'
			case 183:// A singular 'Pet Quote Ring'
			case 184:// This shit is in korean
			case 185:// more korean
			case 186:// more
			case 187:// even more
			case 188:// still going
			case 189:// maybe last one
			case 193:// Idk wtf these are.. mounts? google gives mobs, or mysims links.
			case 198:// Chairs in the equip tab.
			case 199:// Mount items?
				wzPath = "Equip";
				break;
			case 100:
				wzPath = "Cap";
				break;
			case 101:
			case 102:
			case 103:
			case 112:
			case 113:
			case 114:
				wzPath = "Accessory";
				break;
			case 104:
				wzPath = "Coat";
				break;
			case 105:
				wzPath = "Longcoat";
				break;
			case 106:
				wzPath = "Pants";
				break;
			case 107:
				wzPath = "Shoes";
				break;
			case 108:
				wzPath = "Glove";
				break;
			case 109:
				wzPath = "Shield";
				break;
			case 110:
				wzPath = "Cape";
				break;
			case 111:
				wzPath = "Ring";
				break;
			case 121:// wtf
			case 122:
			case 123:
			case 124:
			case 125:
			case 126:
			case 130:
			case 131:
			case 132:
			case 133:
			case 134:
			case 135:
			case 136:
			case 137:
			case 138:
			case 139:// Barehand
			case 140:
			case 141:
			case 142:
			case 143:
			case 144:
			case 145:
			case 146:
			case 147:
			case 148:
			case 149:
			case 150:
			case 151:
			case 152:
			case 153:
			case 154:
			case 155:
			case 156:
			case 157:
			case 158:
			case 160:
			case 169:
			case 170:
				wzPath = "Weapon";
				break;
			case 166:
				wzPath = "Android";
				break;
			case 167:
				wzPath = "Heart";
				break;
			case 168:
				wzPath = "Bit";
				break;
			case 180:
			case 181:
				wzPath = "PetEquip";
				break;
			case 190:
			case 191:
				wzPath = "TamingMob";
				break;
			case 194:
			case 195:
			case 196:
			case 197:
				wzPath = "Dragon";
				break;
			case 301:
			case 304:// Extractors
			case 306:// Neb
			case 308:// x slot bag shit
			case 309:// bit cases
			case 310:// Some powder that you can equip on your 'ship'
			case 311:// Kid Ghost emblem
			case 312:// 'Evan Emoji'
			case 360:// Evo monster core
			case 370:// Title things
			case 380:// Monster cards?
			case 399:
				wzPath = "Install";
				break;
			case 200:
			case 201:
			case 202:
			case 203:
			case 204:
			case 205:
			case 206:
			case 207:
			case 208:
			case 210:
			case 212:
			case 216:
			case 219:
			case 221:
			case 224:
			case 226:
			case 227:
			case 228:
			case 229:
			case 231:
			case 232:
			case 233:
			case 234:
			case 236:
			case 237:
			case 238:
			case 239:
			case 243:
			case 244:
			case 245:
			case 246:
			case 247:
			case 249:
				wzPath = "Consume";
				break;
			case 400:
			case 401:
			case 402:
			case 403:
			case 405:
			case 408:
			case 413:
			case 414:
			case 416:
			case 417:
			case 421:
			case 422:
			case 425:
			case 426:
			case 428:
			case 429:
			case 430:
			case 431:
				wzPath = "Etc";
				break;
			case 500:
				wzPath = "Pet";
				break;
			case 501:
			case 502:
			case 503:
			case 504:
			case 505:
			case 506:
			case 507:
			case 508:
			case 509:
			case 510:
			case 511:
			case 512:
			case 513:
			case 514:
			case 515:
			case 516:
			case 517:
			case 518:
			case 519:
			case 520:
			case 521:
			case 522:
			case 523:
			case 524:
			case 525:
			case 528:
			case 529:
			case 530:
			case 533:
			case 536:
			case 537:
			case 538:
			case 539:
			case 540:
			case 542:
			case 543:
			case 545:
			case 546:
			case 547:
			case 549:
			case 550:
			case 551:
			case 552:
			case 553:
			case 555:
			case 557:
			case 559:
			case 561:
			case 562:
			case 564:
			case 599:
				wzPath = "Cash";
				break;
			case 900:
			case 910:
			case 911:
				wzPath = "Special";
				break;
			default:
				wzPath = "Unknown";
				break;
		}
		return getItemData(wzPath, itemid);// TODO:
	}

	public List<ItemData> getItemData(){
		return new ArrayList<>(itemDataCache.values());
	}

	public ItemData getItemData(String wzPath, int itemId){
		ItemData data = itemDataCache.get(itemId);
		if(data == null){
			data = new ItemData();
			if(!ServerConstants.WZ_LOADING){
				if(data.doesBinExist(wzPath, itemId)){
					data.loadFromBin(wzPath, itemId);
				}else data.exists = false;
			}else data.loadFromXML(wzPath, itemId);
			itemDataCache.put(itemId, data);
			return data;
		}else return data;
	}

	private ItemInformationProvider(){
		monsterBookID.clear();
		loadCardIdData();
		if(ServerConstants.WZ_LOADING){
			itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
			equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
			stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
			cashStringData = stringData.getData("Cash.img");
			consumeStringData = stringData.getData("Consume.img");
			eqpStringData = stringData.getData("Eqp.img");
			etcStringData = stringData.getData("Etc.img");
			insStringData = stringData.getData("Ins.img");
			petStringData = stringData.getData("Pet.img");
			for(MapleData potID : itemData.getData("ItemOption.img")){
				Potential pot = new Potential();
				pot.load(potID);
				potentials.put(pot.id, pot);
			}
			if(ServerConstants.BIN_DUMPING){
				try{
					File itemOptionDat = new File(System.getProperty("wzpath") + "/bin/Items/ItemOption.bin");
					if(!itemOptionDat.exists()){
						itemOptionDat.getParentFile().mkdirs();
						itemOptionDat.createNewFile();
						MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
						for(Potential pot : potentials.values()){
							pot.save(mplew);
						}
						mplew.saveToFile(itemOptionDat);
					}
				}catch(Exception ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
		}else{
			try{
				File itemOptionDat = new File(System.getProperty("wzpath") + "/bin/Items/ItemOption.bin");
				if(itemOptionDat.exists()){
					byte[] in = Files.readAllBytes(itemOptionDat.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					while(glea.available() > 0){
						Potential pot = new Potential();
						pot.load(glea);
						potentials.put(pot.id, pot);
					}
					glea = null;
					babs = null;
					in = null;
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
	}

	public void reload(){
		monsterBookID.clear();
		loadCardIdData();
		if(ServerConstants.WZ_LOADING){
			itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
			equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
			stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
			cashStringData = stringData.getData("Cash.img");
			consumeStringData = stringData.getData("Consume.img");
			eqpStringData = stringData.getData("Eqp.img");
			etcStringData = stringData.getData("Etc.img");
			insStringData = stringData.getData("Ins.img");
			petStringData = stringData.getData("Pet.img");
			for(MapleData potID : itemData.getData("ItemOption.img")){
				Potential pot = new Potential();
				pot.load(potID);
				potentials.put(pot.id, pot);
			}
		}else{
			try{
				File itemOptionDat = new File(System.getProperty("wzpath") + "/bin/Items/ItemOption.bin");
				if(itemOptionDat.exists()){
					byte[] in = Files.readAllBytes(itemOptionDat.toPath());
					ByteArrayByteStream babs = new ByteArrayByteStream(in);
					GenericLittleEndianAccessor glea = new GenericLittleEndianAccessor(babs);
					while(glea.available() > 0){
						Potential pot = new Potential();
						pot.load(glea);
						potentials.put(pot.id, pot);
					}
					glea = null;
					babs = null;
					in = null;
				}
			}catch(Exception ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		itemDataCache.clear();
	}

	public static ItemInformationProvider getInstance(){
		if(instance == null){
			instance = new ItemInformationProvider();
		}
		return instance;
	}

	public void loadAllItems(){
		if(ServerConstants.WZ_LOADING){
			if(ServerConstants.BIN_DUMPING){
				for(MapleDataDirectoryEntry topDir : equipData.getRoot().getSubdirectories()){// character.wz
					if(topDir.getName().equals("Face") || topDir.getName().equals("Hair")) continue;
					for(MapleDataFileEntry iFile : topDir.getFiles()){
						Integer itemid = ObjectParser.isInt(iFile.getName().replaceAll(".img", ""));
						if(itemid == null){
							continue;
						}
						getItemData(topDir.getName(), itemid).saveToBin();// loads it
					}
				}
				for(MapleDataDirectoryEntry topDir : itemData.getRoot().getSubdirectories()){// item.wz
					for(MapleDataFileEntry iFile : topDir.getFiles()){
						if(topDir.getName().equals("Pet")){
							Integer itemid = ObjectParser.isInt(iFile.getName().replaceAll(".img", ""));
							if(itemid == null){
								System.out.println(topDir.getName() + "/" + iFile.getName());
								continue;
							}
							getItemData(topDir.getName(), itemid).saveToBin();
						}else{
							if(!iFile.getName().equals("MaplePoint.img")){
								MapleData data = itemData.getData(topDir.getName() + "/" + iFile.getName());
								for(MapleData d : data.getChildren()){
									Integer itemid = ObjectParser.isInt(d.getName());
									if(itemid == null){
										System.out.println(topDir.getName() + "/" + iFile.getName() + "/" + d.getName());
										continue;
									}
									getItemData(topDir.getName(), itemid).saveToBin();
								}
							}
						}
					}
				}
			}
		}else{
			// Map<Integer, Integer> prices = new HashMap<>();
			File binItems = new File(System.getProperty("wzpath") + "/bin/Items/");
			if(!binItems.exists()) return;
			for(File dir : binItems.listFiles()){
				if(dir.isFile()) continue;
				for(File item : dir.listFiles()){
					Integer itemid = ObjectParser.isInt(item.getName().substring(0, item.getName().length() - 4));
					getItemData(dir.getName(), itemid);
					// Integer itemid = ObjectParser.isInt(item.getName().substring(0, item.getName().length() - 4));
					/*ItemData data = getItemData(dir.getName(), itemid);*/// load em all.
					// int price = (int) data.price;
					// price *= data.slotMax;
					// prices.put(data.itemid, price);
				}
			}
			/*Map<Integer, Integer> sorted = MapUtil.sortByValue(prices);
			for(int item : sorted.keySet()){
				System.out.println(item + ": " + sorted.get(item));
			}*/
		}
	}

	public MapleInventoryType getInventoryType(int itemId){
		final byte type = (byte) (itemId / 1000000);
		if(type < 1 || type > 5) return MapleInventoryType.UNDEFINED;
		return MapleInventoryType.getByType(type);
	}

	public void getAllItemNames(){
		if(ServerConstants.WZ_LOADING && ServerConstants.BIN_DUMPING){
			MapleData itemsData;
			itemsData = stringData.getData("Cash.img");
			for(MapleData itemFolder : itemsData.getChildren()){
				getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
			}
			itemsData = stringData.getData("Consume.img");
			for(MapleData itemFolder : itemsData.getChildren()){
				getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
			}
			itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
			for(MapleData eqpType : itemsData.getChildren()){
				if(eqpType.getName().equalsIgnoreCase("Face") || eqpType.getName().equalsIgnoreCase("Hair")) continue;
				for(MapleData itemFolder : eqpType.getChildren()){
					getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
				}
			}
			itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
			for(MapleData itemFolder : itemsData.getChildren()){
				getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
			}
			itemsData = stringData.getData("Ins.img");
			for(MapleData itemFolder : itemsData.getChildren()){
				getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
			}
			itemsData = stringData.getData("Pet.img");
			for(MapleData itemFolder : itemsData.getChildren()){
				getItemData(Integer.parseInt(itemFolder.getName())).saveToBin();
			}
		}
	}

	public MapleData getStringData(int itemId){
		String cat = "null";
		MapleData theData;
		if(itemId >= 5010000){
			theData = cashStringData;
		}else if(itemId >= 2000000 && itemId < 3000000){
			theData = consumeStringData;
		}else if((itemId >= 1010000 && itemId < 1040000) || (itemId >= 1122000 && itemId < 1123000) || (itemId >= 1142000 && itemId < 1143000)){
			theData = eqpStringData;
			cat = "Eqp/Accessory";
		}else if(itemId >= 1000000 && itemId < 1010000){
			theData = eqpStringData;
			cat = "Eqp/Cap";
		}else if(itemId >= 1102000 && itemId < 1103000){
			theData = eqpStringData;
			cat = "Eqp/Cape";
		}else if(itemId >= 1040000 && itemId < 1050000){
			theData = eqpStringData;
			cat = "Eqp/Coat";
		}else if(itemId >= 20000 && itemId < 22000){
			theData = eqpStringData;
			cat = "Eqp/Face";
		}else if(itemId >= 1080000 && itemId < 1090000){
			theData = eqpStringData;
			cat = "Eqp/Glove";
		}else if(itemId >= 30000 && itemId < 32000){
			theData = eqpStringData;
			cat = "Eqp/Hair";
		}else if(itemId >= 1050000 && itemId < 1060000){
			theData = eqpStringData;
			cat = "Eqp/Longcoat";
		}else if(itemId >= 1060000 && itemId < 1070000){
			theData = eqpStringData;
			cat = "Eqp/Pants";
		}else if(itemId >= 1802000 && itemId < 1810000){
			theData = eqpStringData;
			cat = "Eqp/PetEquip";
		}else if(itemId >= 1112000 && itemId < 1120000){
			theData = eqpStringData;
			cat = "Eqp/Ring";
		}else if(itemId >= 1092000 && itemId < 1100000){
			theData = eqpStringData;
			cat = "Eqp/Shield";
		}else if(itemId >= 1070000 && itemId < 1080000){
			theData = eqpStringData;
			cat = "Eqp/Shoes";
		}else if(itemId >= 1900000 && itemId < 2000000){
			theData = eqpStringData;
			cat = "Eqp/Taming";
		}else if(itemId >= 1300000 && itemId < 1800000){
			theData = eqpStringData;
			cat = "Eqp/Weapon";
		}else if(itemId >= 4000000 && itemId < 5000000){
			theData = etcStringData;
			cat = "Etc";
		}else if(itemId >= 3000000 && itemId < 4000000){
			theData = insStringData;
		}else if(itemId >= 5000000 && itemId < 5010000){
			theData = petStringData;
		}else{
			return null;
		}
		if(cat.equalsIgnoreCase("null")){
			return theData.getChildByPath(String.valueOf(itemId));
		}else{
			return theData.getChildByPath(cat + "/" + itemId);
		}
	}

	public MapleData getItemMapleData(int itemId){
		MapleData ret = null;
		String idStr = "0" + String.valueOf(itemId);
		if(itemData == null) return null;
		MapleDataDirectoryEntry root = itemData.getRoot();
		for(MapleDataDirectoryEntry topDir : root.getSubdirectories()){
			for(MapleDataFileEntry iFile : topDir.getFiles()){
				if(idStr.length() >= 4 && iFile.getName().equals(idStr.substring(0, 4) + ".img")){
					ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
					if(ret == null) return null;
					ret = ret.getChildByPath(idStr);
					return ret;
				}else if(idStr.length() > 0 && iFile.getName().equals(idStr.substring(1) + ".img")) return itemData.getData(topDir.getName() + "/" + iFile.getName());
			}
		}
		root = equipData.getRoot();
		for(MapleDataDirectoryEntry topDir : root.getSubdirectories()){
			for(MapleDataFileEntry iFile : topDir.getFiles()){
				if(iFile.getName().equals(idStr + ".img")) return equipData.getData(topDir.getName() + "/" + iFile.getName());
			}
		}
		return ret;
	}

	public MapleWeaponType getWeaponType(int itemId){
		int cat = (itemId / 10000) % 100;
		MapleWeaponType[] type = {MapleWeaponType.SWORD1H, MapleWeaponType.GENERAL1H_SWING, MapleWeaponType.GENERAL1H_SWING, MapleWeaponType.DAGGER_OTHER, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.WAND, MapleWeaponType.STAFF, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.SWORD2H, MapleWeaponType.GENERAL2H_SWING, MapleWeaponType.GENERAL2H_SWING, MapleWeaponType.SPEAR_STAB, MapleWeaponType.POLE_ARM_SWING, MapleWeaponType.BOW, MapleWeaponType.CROSSBOW, MapleWeaponType.CLAW, MapleWeaponType.KNUCKLE, MapleWeaponType.GUN};
		if(cat < 30 || cat > 49) return MapleWeaponType.NOT_A_WEAPON;
		return type[cat - 30];
	}

	private boolean isCleanSlate(int scrollId){
		return scrollId > 2048999 && scrollId < 2049004;
	}

	private boolean isFlaggedScroll(int scrollId){
		return scrollId == 2040727 || scrollId == 2041058;
	}

	public Item scrollEquipWithId(Item equip, int scrollId, boolean usingWhiteScroll, boolean isGM){
		if(equip instanceof Equip){
			Equip nEquip = (Equip) equip;
			ItemData stats = getItemData(scrollId);
			ItemData eqstats = getItemData(equip.getItemId());
			if(((nEquip.getUpgradeSlots() > 0 || isCleanSlate(scrollId) || isFlaggedScroll(scrollId)) && Math.ceil(Math.random() * 100.0) <= stats.success) || isGM){
				short flag = nEquip.getFlag();
				switch (scrollId){
					case 2040727:
						flag |= ItemConstants.SPIKES;
						nEquip.setFlag((byte) flag);
						return equip;
					case 2041058:
						flag |= ItemConstants.COLD;
						nEquip.setFlag((byte) flag);
						return equip;
					case 2049000:
					case 2049001:
					case 2049002:
					case 2049003:
						if(nEquip.getLevel() + nEquip.getUpgradeSlots() < eqstats.tuc + nEquip.getVicious()){
							nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + 1));
						}
						break;
					case 2049100:
					case 2049101:
					case 2049102:
					case 2049113:
					case 2049114:
						if(scrollId == 2049113 && (nEquip.getItemId() != 1003027 && nEquip.getItemId() != 1302131)){
							// Normal Witch Scrolls only for Hat and Broomstick
							break;
						}
						if(scrollId == 2049114 && (nEquip.getItemId() != 1132014 && nEquip.getItemId() != 1132015 && nEquip.getItemId() != 1132016)){
							// Witch Belt Scrolls only for Witch Belts
							break;
						}
						int inc = 1;
						if(Randomizer.nextInt(2) == 0){
							inc = -1;
						}
						if(nEquip.getStr() > 0){
							nEquip.setStr((short) Math.max(0, (nEquip.getStr() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getDex() > 0){
							nEquip.setDex((short) Math.max(0, (nEquip.getDex() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getInt() > 0){
							nEquip.setInt((short) Math.max(0, (nEquip.getInt() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getLuk() > 0){
							nEquip.setLuk((short) Math.max(0, (nEquip.getLuk() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getWatk() > 0){
							nEquip.setWatk((short) Math.max(0, (nEquip.getWatk() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getWdef() > 0){
							nEquip.setWdef((short) Math.max(0, (nEquip.getWdef() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getMatk() > 0){
							nEquip.setMatk((short) Math.max(0, (nEquip.getMatk() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getMdef() > 0){
							nEquip.setMdef((short) Math.max(0, (nEquip.getMdef() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getAcc() > 0){
							nEquip.setAcc((short) Math.max(0, (nEquip.getAcc() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getAvoid() > 0){
							nEquip.setAvoid((short) Math.max(0, (nEquip.getAvoid() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getSpeed() > 0){
							nEquip.setSpeed((short) Math.max(0, (nEquip.getSpeed() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getJump() > 0){
							nEquip.setJump((short) Math.max(0, (nEquip.getJump() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getHp() > 0){
							nEquip.setHp((short) Math.max(0, (nEquip.getHp() + Randomizer.nextInt(6) * inc)));
						}
						if(nEquip.getMp() > 0){
							nEquip.setMp((short) Math.max(0, (nEquip.getMp() + Randomizer.nextInt(6) * inc)));
						}
						break;
					default:
						nEquip.setStr((short) (nEquip.getStr() + stats.incStr));
						nEquip.setDex((short) (nEquip.getDex() + stats.incDex));
						nEquip.setInt((short) (nEquip.getInt() + stats.incInt));
						nEquip.setLuk((short) (nEquip.getLuk() + stats.incLuk));
						nEquip.setWatk((short) (nEquip.getWatk() + stats.incPAD));
						nEquip.setWdef((short) (nEquip.getWdef() + stats.incPDD));
						nEquip.setMatk((short) (nEquip.getMatk() + stats.incMAD));
						nEquip.setMdef((short) (nEquip.getMdef() + stats.incMDD));
						nEquip.setAcc((short) (nEquip.getAcc() + stats.incAcc));
						nEquip.setAvoid((short) (nEquip.getAvoid() + stats.incEVA));
						nEquip.setSpeed((short) (nEquip.getSpeed() + stats.incSpeed));
						nEquip.setJump((short) (nEquip.getJump() + stats.incJump));
						nEquip.setHp((short) (nEquip.getHp() + stats.incMHP));
						nEquip.setMp((short) (nEquip.getMp() + stats.incMMP));
						break;
				}
				if(!isCleanSlate(scrollId) && !isFlaggedScroll(scrollId)){
					if(!isGM){
						nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
					}
					nEquip.setLevel((byte) (nEquip.getLevel() + 1));
				}
			}else{
				if(!usingWhiteScroll && !isCleanSlate(scrollId) && !isFlaggedScroll(scrollId) && !isGM){
					nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
				}
				if(Randomizer.nextInt(99) < stats.cursed) return null;
			}
		}
		return equip;
	}

	public Equip getEquipById(int equipId){
		return getEquipById(equipId, -1);
	}

	Equip getEquipById(int equipId, int ringId){
		Equip nEquip;
		nEquip = new Equip(equipId, (byte) 0, ringId);
		nEquip.setQuantity((short) 1);
		ItemData data = this.getItemData(equipId);
		if(data.isDropRestricted()){
			byte flag = nEquip.getFlag();
			flag |= ItemConstants.UNTRADEABLE;
			nEquip.setFlag(flag);
		}
		if(data.fs > 0){
			byte flag = nEquip.getFlag();
			flag |= ItemConstants.SPIKES;
			nEquip.setFlag(flag);
		}
		nEquip.setStr(data.incStr);
		nEquip.setDex(data.incDex);
		nEquip.setInt(data.incInt);
		nEquip.setLuk(data.incLuk);
		nEquip.setWatk(data.incPAD);
		nEquip.setWdef(data.incPDD);
		nEquip.setMatk(data.incMAD);
		nEquip.setMdef(data.incMDD);
		nEquip.setAcc(data.incAcc);
		nEquip.setAvoid(data.incEVA);
		nEquip.setSpeed(data.incSpeed);
		nEquip.setJump(data.incJump);
		nEquip.setHp(data.incMHP);
		nEquip.setMp(data.incMMP);
		nEquip.setUpgradeSlots(data.tuc);
		return nEquip.copy();
	}

	public static short getRandStat(short defaultValue, int maxRange){
		if(defaultValue == 0){ return 0; }
		int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
		return (short) ((defaultValue - lMaxRange) + Math.floor(Randomizer.nextDouble() * (lMaxRange * 2 + 1)));
	}

	public Equip randomizeStats(Equip equip){
		equip.setStr(getRandStat(equip.getStr(), 5));
		equip.setDex(getRandStat(equip.getDex(), 5));
		equip.setInt(getRandStat(equip.getInt(), 5));
		equip.setLuk(getRandStat(equip.getLuk(), 5));
		equip.setMatk(getRandStat(equip.getMatk(), 5));
		equip.setWatk(getRandStat(equip.getWatk(), 5));
		equip.setAcc(getRandStat(equip.getAcc(), 5));
		equip.setAvoid(getRandStat(equip.getAvoid(), 5));
		equip.setJump(getRandStat(equip.getJump(), 5));
		equip.setSpeed(getRandStat(equip.getSpeed(), 5));
		equip.setWdef(getRandStat(equip.getWdef(), 10));
		equip.setMdef(getRandStat(equip.getMdef(), 10));
		equip.setHp(getRandStat(equip.getHp(), 10));
		equip.setMp(getRandStat(equip.getMp(), 10));
		return equip;
	}

	// only used in PQs for now.
	public static short randomizeItemQty(int itemId, short qty, double deviationPercentage, boolean includeLowerBound){
		MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(itemId);
		// we dont allow scrolls to be more than 1
		if(type.equals(MapleInventoryType.USE) && DropCategory.getDefaultCategory(itemId).equals(DropCategory.SCROLL)){ return 1; }
		short lowerBound = includeLowerBound ? (short) Math.floor(qty * (1 - deviationPercentage)) : qty;
		short upperBound = (short) Math.ceil(qty * (1 + deviationPercentage));
		return (short) Randomizer.rand(lowerBound, upperBound);
	}

	public Equip makeRare(Equip equip, double increase){
		equip.setStr((short) (equip.getStr() * increase));
		equip.setDex((short) (equip.getDex() * increase));
		equip.setInt((short) (equip.getInt() * increase));
		equip.setLuk((short) (equip.getLuk() * increase));
		equip.setMatk((short) (equip.getMatk() * increase));
		equip.setWatk((short) (equip.getWatk() * increase));
		equip.setAcc((short) (equip.getAcc() * increase));
		equip.setAvoid((short) (equip.getAvoid() * increase));
		equip.setJump((short) (equip.getJump() * increase));
		equip.setSpeed((short) (equip.getSpeed() * increase));
		equip.setWdef((short) (equip.getWdef() * increase));
		equip.setMdef((short) (equip.getMdef() * increase));
		equip.setHp((short) (equip.getHp() * increase));
		equip.setMp((short) (equip.getMp() * increase));
		return equip;
	}

	public boolean hyperUpgradeItem(Equip equip){
		// Dans shit is wrong for v90
		return false;
	}

	private void loadCardIdData(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = DatabaseConnection.getConnection().prepareStatement("SELECT cardid, mobid FROM monstercarddata");
			rs = ps.executeQuery();
			while(rs.next()){
				monsterBookID.put(rs.getInt(1), rs.getInt(2));
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}finally{
			try{
				if(rs != null){
					rs.close();
				}
				if(ps != null){
					ps.close();
				}
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}
	}

	public int getCardMobId(int id){
		return monsterBookID.get(id);
	}

	public final boolean isTwoHanded(int itemId){
		switch (getWeaponType(itemId)){
			case GENERAL2H_SWING:
			case BOW:
			case CLAW:
			case CROSSBOW:
			case POLE_ARM_SWING:
			case SPEAR_STAB:
			case SWORD2H:
			case GUN:
			case KNUCKLE:
				return true;
			default:
				return false;
		}
	}

	public boolean isItemValid(int itemid){
		return getItemData(itemid).exists;
	}

	public Collection<Item> canWearEquipment(MapleCharacter chr, Collection<Item> items){
		MapleInventory inv = chr.getInventory(MapleInventoryType.EQUIPPED);
		if(inv.checked()) return items;
		Collection<Item> itemz = new LinkedList<>();
		if(chr.getJob() == MapleJob.SUPERGM || chr.getJob() == MapleJob.GM){
			for(Item item : items){
				Equip equip = (Equip) item;
				equip.wear(true);
				itemz.add(item);
			}
			return itemz;
		}
		boolean highfivestamp = false;
		/* Removed because players shouldn't even get this, and gm's should just be gm job.
		 try {
		 for (Pair<Item, MapleInventoryType> ii : ItemFactory.INVENTORY.loadItems(chr.getId(), false)) {
		 if (ii.getRight() == MapleInventoryType.CASH) {
		 if (ii.getLeft().getItemId() == 5590000) {
		 highfivestamp = true;
		 }
		 }
		 }
		 } catch (SQLException ex) {
		 }*/
		int tdex = chr.getDex(), tstr = chr.getStr(), tint = chr.getInt(), tluk = chr.getLuk(), fame = chr.getFame();
		if(chr.getJob() != MapleJob.SUPERGM || chr.getJob() != MapleJob.GM){
			for(Item item : inv.list()){
				Equip equip = (Equip) item;
				tdex += equip.getDex();
				tstr += equip.getStr();
				tluk += equip.getLuk();
				tint += equip.getInt();
			}
		}
		for(Item item : items){
			Equip equip = (Equip) item;
			ItemData data = getItemData(equip.getItemId());
			if(data.reqLevel > 0){
				int reqLevel = data.reqLevel;
				if(highfivestamp){
					reqLevel -= 5;
					if(reqLevel < 0){
						reqLevel = 0;
					}
				}
				if(reqLevel > chr.getLevel()){
					continue;
				}
			}
			/*
			 int reqJob = getEquipStats(equip.getItemId()).get("reqJob");
			 if (reqJob != 0) {
			 Really hard check, and not really needed in this one
			 Gm's should just be GM job, and players cannot change jobs.
			 }*/
			if(data.reqDex > tdex){
				continue;
			}else if(data.reqStr > tstr){
				continue;
			}else if(data.reqLuk > tluk){
				continue;
			}else if(data.reqInt > tint){
				continue;
			}
			int reqPOP = data.reqPop;
			if(reqPOP > 0){
				if(data.reqPop > fame){
					continue;
				}
			}
			equip.wear(true);
			itemz.add(equip);
		}
		inv.checked(true);
		return itemz;
	}

	public boolean canWearEquipment(MapleCharacter chr, Equip equip, short dst){
		int id = equip.getItemId();
		ItemData itemData = getItemData(id);// item you are equipping.
		String islot = itemData.islot;
		boolean invalidCashItem = false;
		if(itemData.isCash && dst == -111){
			Item cur = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (dst + 100));
			if(cur != null && !itemData.weaponTypes.contains(ItemConstants.get_weapon_type(cur.getItemId()))){
				invalidCashItem = true;
			}
		}
		if(!EquipSlot.getFromTextSlot(islot).isAllowed(dst, itemData) || invalidCashItem){
			equip.wear(false);
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(MaplePacketCreator.sendYellowTip("[WARNING]: " + chr.getName() + " tried to equip " + itemData.name + " into slot " + dst + "."));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
			AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to forcibly equip an item. Item: " + itemData.name + " into " + dst + " slot. invalidCashItem: " + invalidCashItem + " islot: " + islot);
			return false;
		}
		if(chr.getJob() == MapleJob.SUPERGM || chr.getJob() == MapleJob.GM){
			equip.wear(true);
			return true;
		}
		boolean highfivestamp = false;
		/* Removed check above for message ><
		 try {
		 for (Pair<Item, MapleInventoryType> ii : ItemFactory.INVENTORY.loadItems(chr.getId(), false)) {
		 if (ii.getRight() == MapleInventoryType.CASH) {
		 if (ii.getLeft().getItemId() == 5590000) {
		 highfivestamp = true;
		 }
		 }
		 }
		 } catch (SQLException ex) {
		 }*/
		int reqLevel = itemData.reqLevel;
		if(highfivestamp){
			reqLevel -= 5;
		}
		int i = 0; // lol xD
		// Removed job check. Shouldn't really be needed.
		if(reqLevel > chr.getLevel()){
			i++;
		}else if(itemData.reqDex > chr.getTotalDex()){
			i++;
		}else if(itemData.reqStr > chr.getTotalStr()){
			i++;
		}else if(itemData.reqLuk > chr.getTotalLuk()){
			i++;
		}else if(itemData.reqInt > chr.getTotalInt()){
			i++;
		}else if(itemData.rcount > chr.getReincarnations()){
			i++;
		}
		int reqPOP = itemData.reqPop;
		if(reqPOP > 0){
			if(itemData.reqPop > chr.getFame()){
				i++;
			}
		}
		if(i > 0){
			equip.wear(false);
			return false;
		}
		equip.wear(true);
		return true;
	}

	public ArrayList<Pair<Integer, String>> getItemDataByName(String name){
		name = name.toLowerCase();
		ArrayList<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		for(int itemid : this.itemDataCache.keySet()){
			String n = itemDataCache.get(itemid).name.toLowerCase();
			if(n.contains(name)){
				ret.add(new Pair<>(itemid, n));
			}
		}
		return ret;
	}

	public int getItemIDFromString(String name){
		for(int itemid : itemDataCache.keySet()){
			if(getItemData(itemid).name.equalsIgnoreCase(name)) return itemid;
		}
		return 0;
	}

	public static class scriptedItem{

		public boolean runOnPickup;
		public int npc;
		public String script;

		public scriptedItem(int npc, String script, boolean rop){
			this.npc = npc;
			this.script = script;
			this.runOnPickup = rop;
		}

		public int getNpc(){
			return npc;
		}

		public String getScript(){
			return script;
		}

		public boolean runOnPickup(){
			return runOnPickup;
		}
	}

	public static final class RewardItem{

		public int itemid, period;
		public short prob, quantity;
		public String effect = "", worldmsg = "";

		public void load(GenericLittleEndianAccessor glea){
			itemid = glea.readInt();
			period = glea.readInt();
			prob = glea.readShort();
			quantity = glea.readShort();
			effect = glea.readMapleAsciiString();
			worldmsg = glea.readMapleAsciiString();
		}

		public void save(MaplePacketLittleEndianWriter mplew){
			mplew.writeInt(itemid);
			mplew.writeInt(period);
			mplew.writeShort(prob);
			mplew.writeShort(quantity);
			mplew.writeMapleAsciiString(effect);
			mplew.writeMapleAsciiString(worldmsg);
		}
	}
}
