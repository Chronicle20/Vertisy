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
package net.server.channel.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.MapleClient;
import client.RSSkill;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MakerItemFactory;
import server.MakerItemFactory.GemCreateEntry;
import server.MakerItemFactory.MakerItemCreateEntry;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserEffectType;
import tools.packets.field.userpool.UserRemote;

/**
 * @author iPoopMagic (David)
 */
public final class MakerSkillHandler extends AbstractMaplePacketHandler{

	private final ItemInformationProvider ii = ItemInformationProvider.getInstance();

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		final int type = slea.readInt();
		switch (type){
			case 1:{ // Making Items
				final int itemId = slea.readInt();
				boolean success = false;
				if(itemId == 4031980 || itemId == 4032334 || itemId == 4032312 || itemId == 2041058 || itemId == 2040727 || // Other items that fall in "0" category
				        (itemId >= 4001174 && itemId <= 4001186) || (itemId >= 4250000 && itemId <= 4251402)){ // Gems
					final GemCreateEntry gem = MakerItemFactory.getInstance().getGemCreateInfo(itemId);
					// TODO: Handle all hacks here
					final int randGem = itemId;
					if(itemId >= 4250000 && itemId <= 4251402){
						getRandomGemReward(gem.getRandomReward());
					}
					final int quantity = getQuantity(randGem, itemId);
					if(canCreate(c, gem) && !c.getPlayer().getInventory(ii.getInventoryType(itemId)).isFull()){
						c.getPlayer().gainMeso(-gem.getCost(), false);
						for(Pair<Integer, Integer> p : gem.getReqItems()){
							int requiredItem = p.getLeft();
							int requiredQty = p.getRight();
							MapleInventoryManipulator.removeById(c, ii.getInventoryType(requiredItem), requiredItem, requiredQty, true, false);
						}
						MapleInventoryManipulator.addFromDrop(c, new Item(randGem, (short) quantity), false);
						c.getPlayer().gainRSSkillExp(RSSkill.Crafting, quantity);
						showMakerSuccess(c);
						success = true;
					}
					c.announce(MaplePacketCreator.makerResult(success, randGem, (short) quantity, gem.getCost(), gem.getReqItems(), false, 0, 0, null));
				}else{
					final boolean catalyst = slea.readByte() > 0;
					final int amtINCStatGem = slea.readInt(); // Amount of increasing (or random...) stat gems
					final MakerItemCreateEntry item = MakerItemFactory.getInstance().getMakerItemCreateInfo(itemId);
					final List<Integer> INCStatGems = new ArrayList<>();
					// TODO: Handle all hacks here
					if(canCreate(c, item) && !c.getPlayer().getInventory(ii.getInventoryType(itemId)).isFull()){
						c.getPlayer().gainMeso(-item.getCost(), false);
						final Equip toGive = (Equip) ii.getEquipById(itemId);
						short newSlot = c.getPlayer().getInventory(ii.getInventoryType(toGive.getItemId())).addItem(toGive);
						if(newSlot == -1){
							c.announce(MaplePacketCreator.getInventoryFull());
							c.announce(CWvsContext.OnMessage.getShowInventoryFull());
							return;
						}
						if(catalyst || amtINCStatGem > 0){
							if(c.getPlayer().haveItem(item.getCatalyst())){
								ii.randomizeStats(toGive);
								MapleInventoryManipulator.removeById(c, ii.getInventoryType(item.getCatalyst()), item.getCatalyst(), 1, true, false);
								c.getPlayer().gainRSSkillExp(RSSkill.Crafting, 1);
							}
							for(int i = 0; i < amtINCStatGem; i++){
								final int INCStatGem = slea.readInt(); // Gem Item ID
								if(c.getPlayer().haveItem(INCStatGem) && INCStatGem / 10000 == 425){
									final ItemData stats = ii.getItemData(INCStatGem);
									if(stats != null && stats.exists){
										addGemStats(stats, toGive);
										MapleInventoryManipulator.removeById(c, ii.getInventoryType(INCStatGem), INCStatGem, 1, true, false);
										INCStatGems.add(INCStatGem);
										c.getPlayer().gainRSSkillExp(RSSkill.Crafting, 1);
									}
								}
							}
						}
						for(Pair<Integer, Integer> p : item.getReqItems()){
							int requiredItem = p.getLeft();
							int quantity = p.getRight();
							MapleInventoryManipulator.removeById(c, ii.getInventoryType(requiredItem), requiredItem, quantity, true, false);
							c.getPlayer().gainRSSkillExp(RSSkill.Crafting, 1);
						}
						c.getPlayer().gainRSSkillExp(RSSkill.Crafting, 1);
						c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, toGive))));
						showMakerSuccess(c);
						success = true;
					}
					c.announce(MaplePacketCreator.makerResult(success, itemId, item.getQuantity(), item.getCost(), item.getReqItems(), catalyst, item.getCatalyst(), amtINCStatGem, INCStatGems));
				}
				break;
			}
			case 3:{ // Monster Crystals
				final int itemId = slea.readInt(); // ETC Drop Item
				ItemData data = ItemInformationProvider.getInstance().getItemData(itemId);
				if(data.lv != 0){
					if(c.getPlayer().getItemQuantity(itemId, false) > 99){
						MapleInventoryManipulator.removeById(c, ii.getInventoryType(itemId), itemId, 100, true, false);
						MapleInventoryManipulator.addFromDrop(c, new Item(crystalToGive(data.lv), (short) 1), false);
						c.getPlayer().gainRSSkillExp(RSSkill.Crafting, 1);
						showMakerSuccess(c);
						c.announce(MaplePacketCreator.makerResultCrystal(crystalToGive(data.lv), itemId));
					}
				}
				break;
			}
			case 4:{ // Disassembling
				final int itemId = slea.readInt(); // Equip ID
				slea.skip(4);
				final byte slot = (byte) slea.readInt();
				final Item toUse = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
				if(toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1) return;
				ItemData data = ii.getItemData(itemId);
				if(!data.isDropRestricted()){
					final int crystalToGive = crystalToGive(data.reqLevel);
					final int cost = mesoToUse(data.reqLevel);
					int quantity = (((itemId >= 1302000 && itemId <= 1492023) || (itemId >= 1050000 && itemId < 1060000)) ? Randomizer.rand(5, 11) : Randomizer.rand(3, 7));
					c.getPlayer().gainMeso(-cost, false);
					MapleInventoryManipulator.removeItem(c, MapleInventoryType.EQUIP, slot, (byte) 1, true, false);
					MapleInventoryManipulator.addFromDrop(c, new Item(crystalToGive, (short) quantity), false);
					c.getPlayer().gainRSSkillExp(RSSkill.Crafting, quantity);
					showMakerSuccess(c);
					List<Pair<Integer, Integer>> itemsGained = new ArrayList<>();
					itemsGained.add(new Pair<>(crystalToGive, quantity));
					c.announce(MaplePacketCreator.makerResultDesynth(itemId, cost, itemsGained));
				}
				break;
			}
		}
	}

	private boolean canCreate(MapleClient c, MakerItemCreateEntry recipe){
		// Check for recipe, required recipe items, mesos, player level, and skill level
		int job = c.getPlayer().getJob().getId();// Fuck math
		return hasItems(c, recipe) && c.getPlayer().getMeso() >= recipe.getCost() && c.getPlayer().getLevel() >= recipe.getReqLevel() && c.getPlayer().getSkillLevel(job < 1000 ? 1007 : job < 2000 ? 10001007 : 20001007) >= recipe.getReqSkillLevel();
	}

	private boolean hasItems(MapleClient c, MakerItemCreateEntry recipe){
		for(Pair<Integer, Integer> p : recipe.getReqItems()){
			int itemId = p.getLeft();
			if(c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < p.getRight()) return false;
		}
		return true;
	}

	private boolean canCreate(MapleClient c, GemCreateEntry recipe){
		// Check for recipe, required recipe items, mesos, player level, and skill level
		int job = c.getPlayer().getJob().getId();// Fuck math
		return hasItems(c, recipe) && c.getPlayer().getMeso() >= recipe.getCost() && c.getPlayer().getLevel() >= recipe.getReqLevel() && c.getPlayer().getSkillLevel(job < 1000 ? 1007 : job < 2000 ? 10001007 : 20001007) >= recipe.getReqSkillLevel();
	}

	private boolean hasItems(MapleClient c, GemCreateEntry recipe){
		for(Pair<Integer, Integer> p : recipe.getReqItems()){
			int itemId = p.getLeft();
			if(c.getPlayer().getInventory(ii.getInventoryType(itemId)).countById(itemId) < p.getRight()) return false;
		}
		return true;
	}

	private static int crystalToGive(final int level){
		int crystalID = 4260000;
		if(level >= 31 && level <= 50){
			crystalID = 4260000; // Basic 1
		}else if(level >= 51 && level <= 60){
			crystalID = 4260001; // Basic 2
		}else if(level >= 61 && level <= 70){
			crystalID = 4260002; // Basic 3
		}else if(level >= 71 && level <= 80){
			crystalID = 4260003; // Intermediate 1
		}else if(level >= 81 && level <= 90){
			crystalID = 4260004; // Intermediate 2
		}else if(level >= 91 && level <= 100){
			crystalID = 4260005; // Intermediate 3
		}else if(level >= 101 && level <= 110){
			crystalID = 4260006; // Advanced 1
		}else if(level >= 111 && level <= 120){
			crystalID = 4260007; // Advanced 2
		}else if(level >= 121){
			crystalID = 4260008; // Advanced 3
		}else{
			throw new RuntimeException("Invalid CrystalID Level: " + level);
		}
		return crystalID;
	}

	private static int mesoToUse(final int level){
		/* I think Nexon hardcoded this, it ain't sending the packet*/
		return 1;
	}

	private static int getRandomGemReward(final List<Triple<Integer, Integer, Integer>> rewards){
		int itemid;
		final List<Integer> items = new ArrayList<>();
		for(final Triple<Integer, Integer, Integer> t : rewards){
			itemid = t.getLeft();
			// Add as many itemIDs as the chance value
			for(int i = 0; i < t.getRight(); i++){
				items.add(itemid);
			}
		}
		return items.get(Randomizer.nextInt(items.size()));
	}

	private static int getQuantity(final int gem, final int gemToMake){
		int qty = 1;
		if(gem == gemToMake || gem == gemToMake + 1){ // If you get the same gem or the next one up
			qty = 1;
		}else if(gem == gemToMake - 1){ // If what's chosen is one level down
			qty = 9;
		}
		return qty;
	}

	private static void addGemStats(final ItemData stats, final Equip item){
		int pad = stats.incPAD;
		if(stats.incPAD != 0){
			item.setWatk((short) (item.getWatk() + pad));
		}
		int mad = stats.incMAD;
		if(mad != 0){
			item.setMatk((short) (item.getMatk() + mad));
		}
		int acc = stats.incAcc;
		if(acc != 0){
			item.setAcc((short) (item.getAcc() + acc));
		}
		int eva = stats.incEVA;
		if(eva != 0){
			item.setAvoid((short) (item.getAvoid() + eva));
		}
		int speed = stats.incSpeed;
		if(speed != 0){
			item.setSpeed((short) (item.getSpeed() + speed));
		}
		int jump = stats.incJump;
		if(jump != 0){
			item.setJump((short) (item.getJump() + jump));
		}
		int maxHP = stats.incMaxHP;
		if(maxHP != 0){
			item.setHp((short) (item.getHp() + maxHP));
		}
		int maxMP = stats.incMaxMP;
		if(maxMP != 0){
			item.setMp((short) (item.getMp() + maxMP));
		}
		int str = stats.incStr;
		if(str != 0){
			item.setStr((short) (item.getStr() + str));
		}
		int dex = stats.incDex;
		if(dex != 0){
			item.setDex((short) (item.getDex() + dex));
		}
		int int_ = stats.incInt;
		if(int_ != 0){
			item.setInt((short) (item.getInt() + int_));
		}
		int luk = stats.incLuk;
		if(luk != 0){
			item.setLuk((short) (item.getLuk() + luk));
		}
		int level = stats.incReqLevel;
		if(level != 0){
			ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
			int reqLevel = data.reqLevel;
			item.setLevel((byte) (reqLevel + level)); // level is negative
		}
		int randop = stats.randOption; // Black Crystals randomize W.ATT and M.ATT
		if(randop != 0){
			final int ma = item.getMatk(), wa = item.getWatk();
			if(wa > 0){
				item.setWatk((short) (Randomizer.nextBoolean() ? (wa + randop) : (wa - randop)));
			}
			if(ma > 0){
				item.setMatk((short) (Randomizer.nextBoolean() ? (ma + randop) : (ma - randop)));
			}
		}
		int randStat = stats.randStat; // Dark Crystals randomize Ability Stats
		if(randStat != 0){
			final int str1 = item.getStr(), dex1 = item.getDex(), luk1 = item.getLuk(), int_1 = item.getInt();
			if(str1 > 0){
				item.setStr((short) (Randomizer.nextBoolean() ? (str1 + randStat) : (str1 - randStat)));
			}
			if(dex1 > 0){
				item.setDex((short) (Randomizer.nextBoolean() ? (dex1 + randStat) : (dex1 - randStat)));
			}
			if(int_1 > 0){
				item.setInt((short) (Randomizer.nextBoolean() ? (int_1 + randStat) : (int_1 - randStat)));
			}
			if(luk1 > 0){
				item.setLuk((short) (Randomizer.nextBoolean() ? (luk1 + randStat) : (luk1 - randStat)));
			}
		}
	}

	private void showMakerSuccess(MapleClient c){
		c.announce(UserLocal.UserEffect.showSpecialEffect(UserEffectType.ItemMaker));
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), UserRemote.UserEffect.showForeignEffect(c.getPlayer().getId(), UserEffectType.ItemMaker));
	}
}
