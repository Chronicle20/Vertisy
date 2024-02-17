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

import java.awt.Point;
import java.util.*;

import client.MapleBuffStat;
import client.MapleClient;
import client.inventory.*;
import constants.ItemConstants;
import tools.ExceptionUtil;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserRemote;

/**
 * @author Matze
 */
public class MapleInventoryManipulator{

	/**
	 * Use {@link MapleCharacter.canHoldItem(item)} To check if the item can be held before using this method.
	 * 
	 * @return False if 1 or mores items failed to add, or no room was available.
	 */
	public static boolean addFromDrop(MapleClient c, Item item, boolean show){
		return addFromDrop(c, item, show, true);
	}

	/**
	 * Use {@link MapleCharacter.canHoldItem(item)} To check if the item can be held before using this method.
	 * 
	 * @return False if 1 or mores items failed to add, or no room was available.
	 */
	public static boolean addFromDrop(MapleClient c, Item item, boolean show, boolean log){
		/*if(!c.getPlayer().canHoldItem(item)){
			c.announce(MaplePacketCreator.getInventoryFull());
			return false;
		}*/
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		ItemData data = ii.getItemData(item.getItemId());
		MapleInventoryType type = ii.getInventoryType(item.getItemId());
		if(data.pickupRestricted && c.getPlayer().getItemQuantity(item.getItemId(), true) > 0){
			c.announce(MaplePacketCreator.getInventoryFull());
			c.announce(CWvsContext.OnMessage.showItemUnavailable());
			return false;
		}
		if(item.hasDBFlag(ItemDB.DELETE)){
			item.removeDBFlag(ItemDB.DELETE);
			item.addDBFlag(ItemDB.INSERT);
		}
		short quantity = item.getQuantity();
		short max = data.getSlotMax(null);
		if(type.equals(MapleInventoryType.EQUIP)){
			if(!(item instanceof Equip)){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, "Trying to add inventorytype equip with non-equip instanceof item.\r\n" + ExceptionUtil.buildException());
				return false;
			}
			short newSlot = c.getPlayer().getInventory(type).addItem(item);
			if(newSlot == -1){
				c.announce(MaplePacketCreator.getInventoryFull());
				c.announce(CWvsContext.OnMessage.getShowInventoryFull());
				return false;
			}
			c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, item))));
			if(show) c.announce(UserLocal.UserEffect.getShowItemGain(item.getItemId(), item.getQuantity()));
		}else{
			if(quantity > 0){// update other items
				if(!ItemConstants.isRechargable(item.getItemId())){
					for(Item exist : c.getPlayer().getInventory(type).listById(item.getItemId())){
						if(exist.getQuantity() < max){
							// quantity: 100
							// max: 2000
							// current: 1955
							short qLeft = (short) (max - exist.getQuantity());
							// 45
							if(qLeft > 0){// can add items
								short add = 0;
								if(quantity + exist.getQuantity() <= max){
									add = quantity;
								}else{
									// 45 < 100: add 45
									//
									add = qLeft < quantity ? qLeft : quantity;
								}
								quantity -= add;
								exist.setQuantity((short) (exist.getQuantity() + add));
								c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, exist))));
								if(show) c.announce(UserLocal.UserEffect.getShowItemGain(item.getItemId(), add));
							}
						}
					}
				}
			}else if(quantity == 0 && ItemConstants.isRechargable(item.getItemId())){
				Item nItem = item.copy();
				nItem.nSN = item.nSN;
				nItem.setQuantity((short) 0);
				quantity -= nItem.getQuantity();
				short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
				if(newSlot == -1){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return false;
				}
				c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
				if(show) c.announce(UserLocal.UserEffect.getShowItemGain(item.getItemId(), nItem.getQuantity()));
			}
			if(quantity == 0) ItemFactory.deleteItem(item);
			// If we still have extra quantity, create new items
			int sn = item.nSN;
			while(quantity > 0){
				Item nItem = item.copy();
				if(sn != -1){
					nItem.nSN = sn;
					sn = -1;
				}
				short fixedQuantity = (short) Math.min(quantity, max);
				if(ItemConstants.isRechargable(nItem.getItemId())) fixedQuantity = quantity;
				quantity -= fixedQuantity;
				nItem.setQuantity(fixedQuantity);
				short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
				if(newSlot == -1){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return false;
				}
				c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
				if(show) c.announce(UserLocal.UserEffect.getShowItemGain(item.getItemId(), nItem.getQuantity()));
			}
			if(sn != -1) ItemFactory.deleteItem(item);
		}
		return true;
		/*
		short quantity = item.getQuantity();
		if(!type.equals(MapleInventoryType.EQUIP)){
			short slotMax = ii.getSlotMax(c, item.getItemId());
			List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
			if(!ItemConstants.isRechargable(item.getItemId())){
				if(existing.size() > 0){ // first update all existing slots to slotMax
					Iterator<Item> i = existing.iterator();
					while(quantity > 0){
						if(i.hasNext()){
							Item eItem = (Item) i.next();
							short oldQ = eItem.getQuantity();
							if(oldQ < slotMax && item.getOwner().equals(eItem.getOwner())){
								short newQ = (short) Math.min(oldQ + quantity, slotMax);
								quantity -= (newQ - oldQ);
								eItem.setQuantity(newQ);
								c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, eItem))));
							}
						}else{
							break;
						}
					}
				}
				while(quantity > 0){
					short newQ = (short) Math.min(quantity, slotMax);
					quantity -= newQ;
					Item nItem = new Item(item.getItemId(), (short) 0, newQ);
					nItem.setExpiration(item.getExpiration());
					nItem.setOwner(item.getOwner());
					nItem.setFlag(item.getFlag());
					short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
					if(newSlot == -1){
						c.announce(MaplePacketCreator.getInventoryFull());
						c.announce(MaplePacketCreator.getShowInventoryFull());
						item.setQuantity((short) (quantity + newQ));
						return false;
					}
					c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
				}
			}else{
				Item nItem = new Item(item.getItemId(), (short) 0, quantity);
				short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
				if(newSlot == -1){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(MaplePacketCreator.getShowInventoryFull());
					return false;
				}
				c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, nItem))));
				c.announce(CWvsContext.enableActions());
			}
		}else if(quantity == 1){
			short newSlot = c.getPlayer().getInventory(type).addItem(item);
			if(newSlot == -1){
				c.announce(MaplePacketCreator.getInventoryFull());
				c.announce(MaplePacketCreator.getShowInventoryFull());
				return false;
			}
			c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, item))));
		}else{
			return false;
		}
		if(show){
			c.announce(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
		}
		return true;*/
	}

	/**
	 * Removes the specified amount of quantity of an item.
	 * Will check the slot defined first, then if the required amount of quantity to remove is still greater than zero, move onto checking the entire inventory.
	 * 
	 * @param slot Slot to check first.
	 */
	public static void removeItem(MapleClient c, MapleInventoryType type, int slot, int quantity, boolean deleteIfZero, boolean fromDrop){// I really need to remove this fromDrop parameter
		removeItem(c, type, slot, quantity, deleteIfZero, fromDrop, true);
	}

	/**
	 * Removes the specified amount of quantity of an item.
	 * Will check the slot defined first, then if the required amount of quantity to remove is still greater than zero, move onto checking the entire inventory.
	 * 
	 * @param slot Slot to check first.
	 */
	public static void removeItem(MapleClient c, MapleInventoryType type, int slot, int quantity, boolean deleteIfZero, boolean fromDrop, boolean consumeIfZero){
		if(c == null || c.getPlayer() == null || c.getPlayer().getInventory(type) == null) return;
		short quantityLeft = (short) quantity;
		Item item = c.getPlayer().getInventory(type).getItem((short) slot);
		if(item != null){
			short removeAmount = (short) Math.min(item.getQuantity(), quantity);
			quantityLeft -= removeAmount;
			if(ItemConstants.isRechargable(item.getItemId())) quantityLeft = 0;
			c.getPlayer().getInventory(type).removeItem((short) slot, removeAmount, !consumeIfZero);
			if(item.getQuantity() == 0 && consumeIfZero){
				if(deleteIfZero) ItemFactory.deleteItem(item);
				c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(3, item))));
			}else{
				c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(1, item))));
			}
			if(quantityLeft > 0){
				for(Item i : c.getPlayer().getInventory(type).listById(item.getItemId())){
					if(i.getQuantity() >= quantityLeft){// only remove the amount we have left, not all of it.
						c.getPlayer().getInventory(type).removeItem(i.getPosition(), quantityLeft, !consumeIfZero);
						if(item.getQuantity() == 0 && consumeIfZero){
							if(deleteIfZero) ItemFactory.deleteItem(item);
							c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(3, item))));
						}else{
							c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(1, item))));
						}
						break;
					}else{// Item doesn't have all the quantity left, so just remove the quantity from that item and go to the next.
						quantityLeft -= i.getQuantity();
						c.getPlayer().getInventory(type).removeItem(i.getPosition(), i.getQuantity(), !consumeIfZero);
						if(item.getQuantity() == 0 && consumeIfZero){
							if(deleteIfZero) ItemFactory.deleteItem(item);
							c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(3, item))));
						}else{
							c.announce(MaplePacketCreator.modifyInventory(fromDrop, Collections.singletonList(new ModifyInventory(1, item))));
						}
						if(quantityLeft <= 0) break;
					}
				}
			}
			if(quantityLeft > 0){
				Logger.log(LogType.WARNING, LogFile.ANTICHEAT, c.getPlayer().getName() + " - Not enough of item: " + item.getItemId() + " Leftover: " + quantityLeft + "\r\n" + ExceptionUtil.buildException());
			}
		}
	}

	public static void removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean deleteIfZero, boolean fromDrop){
		if(c == null || c.getPlayer() == null || c.getPlayer().getInventory(type) == null) return;
		short removeQuantity = (short) quantity;
		MapleInventory inv = c.getPlayer().getInventory(type);
		int slotLimit = type == MapleInventoryType.EQUIPPED ? 128 : inv.getSlotLimit();
		for(short i = 0; i <= slotLimit; i++){
			Item item = inv.getItem((short) (type == MapleInventoryType.EQUIPPED ? -i : i));
			if(item != null){
				if(item.getItemId() == itemId || item.getCashId() == itemId){
					if(removeQuantity <= item.getQuantity()){
						removeItem(c, type, item.getPosition(), removeQuantity, deleteIfZero, fromDrop);
						removeQuantity = 0;
						break;
					}else{
						removeQuantity -= item.getQuantity();
						removeItem(c, type, item.getPosition(), item.getQuantity(), deleteIfZero, fromDrop);
					}
				}
			}
		}
		if(removeQuantity > 0){
			Logger.log(LogType.INFO, LogFile.ANTICHEAT, "Not enough of item: " + itemId + ", Quantity (After Quantity/Over Current Quantity): " + (quantity - removeQuantity) + "/" + quantity + "\r\n" + ExceptionUtil.buildException());
		}
	}

	public static void removeStarById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consumeIfZero){
		int removeQuantity = quantity;
		MapleInventory inv = c.getPlayer().getInventory(type);
		int slotLimit = type == MapleInventoryType.EQUIPPED ? 128 : inv.getSlotLimit();
		for(short i = 0; i <= slotLimit; i++){
			Item item = inv.getItem((short) (type == MapleInventoryType.EQUIPPED ? -i : i));
			if(item != null){
				if(item.getItemId() == itemId || item.getCashId() == itemId){
					if(item.getQuantity() == 0 || removeQuantity <= 0) continue;
					if(item.getQuantity() >= removeQuantity){// remove that quantity yo
						removeItem(c, type, item.getPosition(), (short) removeQuantity, fromDrop, false, consumeIfZero);
						removeQuantity = 0;
						break;
					}else if(removeQuantity >= item.getQuantity()){
						if(getAmountOfStarsById(c, itemId) >= removeQuantity){// We have extra stars in other slots. Only remove amount in that slot and continue
							removeQuantity -= item.getQuantity();
							removeItem(c, type, item.getPosition(), item.getQuantity(), fromDrop, false, consumeIfZero);
						}else{// We have no more stars, just take them all.
							removeItem(c, type, item.getPosition(), item.getQuantity(), fromDrop, false, consumeIfZero);
							removeQuantity = 0;
							break;
						}
					}
				}
			}
		}
	}

	public static int getAmountOfStarsById(MapleClient c, int starid){
		int totalStars = 0;
		for(Item item : c.getPlayer().getInventory(MapleInventoryType.USE).listById(starid)){
			totalStars += item.getQuantity();
		}
		return totalStars;
	}

	public static void move(MapleClient c, MapleInventoryType type, short src, short dst){
		if(src < 0 || dst < 0) return;
		if(dst > c.getPlayer().getInventory(type).getSlotLimit()) return;
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Item source = c.getPlayer().getInventory(type).getItem(src);
		Item initialTarget = c.getPlayer().getInventory(type).getItem(dst);
		if(source == null) return;
		short olddstQ = -1;
		if(initialTarget != null){
			olddstQ = initialTarget.getQuantity();
		}
		short oldsrcQ = source.getQuantity();
		short slotMax = ii.getItemData(source.getItemId()).getSlotMax(c);
		c.getPlayer().getInventory(type).move(src, dst, slotMax);
		final List<ModifyInventory> mods = new ArrayList<>();
		if(!type.equals(MapleInventoryType.EQUIP) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && !ItemConstants.isRechargable(source.getItemId()) && initialTarget.getExpiration() == source.getExpiration()){
			if((olddstQ + oldsrcQ) > slotMax){
				mods.add(new ModifyInventory(1, source));
				mods.add(new ModifyInventory(1, initialTarget));
			}else{
				mods.add(new ModifyInventory(3, source));
				mods.add(new ModifyInventory(1, initialTarget));
			}
		}else{
			mods.add(new ModifyInventory(2, source, src));
		}
		c.announce(MaplePacketCreator.modifyInventory(true, mods));
	}

	public static void equip(MapleClient c, short src, short dst){
		Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
		if(source == null || !ItemInformationProvider.getInstance().canWearEquipment(c.getPlayer(), source, dst)){
			c.announce(CWvsContext.enableActions());
			return;
		}else if((((source.getItemId() >= 1902000 && source.getItemId() <= 1902002) || source.getItemId() == 1912000) && c.getPlayer().isCygnus()) || ((source.getItemId() >= 1902005 && source.getItemId() <= 1902007) || source.getItemId() == 1912005) && !c.getPlayer().isCygnus()){// Adventurer taming equipment
			return;
		}
		boolean itemChanged = false;
		ItemData data = ItemInformationProvider.getInstance().getItemData(source.getItemId());
		if(data.equipTradeBlock){
			source.setFlag((byte) ItemConstants.UNTRADEABLE);
			itemChanged = true;
		}
		if(source.getRingId() > -1){
			c.getPlayer().getRingById(source.getRingId()).equip();
		}
		if(dst == -6){ // unequip the overall
			Item top = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -5);
			if(top != null && isOverall(top.getItemId())){
				if(c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -5, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		}else if(dst == -5){
			final Item bottom = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -6);
			if(bottom != null && isOverall(source.getItemId())){
				if(c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -6, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		}else if(dst == -10){// check if weapon is two-handed
			Item weapon = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
			if(weapon != null && ItemInformationProvider.getInstance().isTwoHanded(weapon.getItemId())){
				if(c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -11, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		}else if(dst == -11){
			Item shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
			if(shield != null && ItemInformationProvider.getInstance().isTwoHanded(source.getItemId())){
				if(c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				unequip(c, (byte) -10, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
			}
		}
		if(dst == -18){
			if(c.getPlayer().getMount() != null){
				c.getPlayer().getMount().setItemId(source.getItemId());
			}
		}
		if(source.getItemId() == 1122017){
			c.getPlayer().equipPendantOfSpirit();
		}
		// 1112413, 1112414, 1112405 (Lilin's Ring)
		source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
		Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(src);
		if(target != null){
			c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
		}
		final List<ModifyInventory> mods = new ArrayList<>();
		if(itemChanged){
			mods.add(new ModifyInventory(3, source));
			mods.add(new ModifyInventory(0, source.copy()));// to prevent crashes
		}
		source.setPosition(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
		if(target != null){
			target.setPosition(src);
			c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(target);
		}
		if(c.getPlayer().getBuffedValue(MapleBuffStat.BOOSTER) != null && isWeapon(source.getItemId())){
			c.getPlayer().cancelBuffStats(MapleBuffStat.BOOSTER);
		}
		mods.add(new ModifyInventory(2, source, src));
		c.announce(MaplePacketCreator.modifyInventory(true, mods));
		c.getPlayer().equipChanged();
	}

	public static void unequip(MapleClient c, short src, short dst){
		Equip source = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
		Equip target = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
		if(dst < 0) return;
		if(source == null) return;
		if(target != null && src <= 0){
			c.announce(MaplePacketCreator.getInventoryFull());
			return;
		}
		if(source.getItemId() == 1122017){
			c.getPlayer().unequipPendantOfSpirit();
		}
		if(source.getRingId() > -1){
			c.getPlayer().getRingById(source.getRingId()).unequip();
		}
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
		if(target != null){
			c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
		}
		source.setPosition(dst);
		c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
		if(target != null){
			target.setPosition(src);
			c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
		}
		c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(2, source, src))));
		c.getPlayer().equipChanged();
	}

	public static void drop(MapleClient c, MapleInventoryType type, short src, short quantity){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		if(src < 0){
			type = MapleInventoryType.EQUIPPED;
		}
		Item source = c.getPlayer().getInventory(type).getItem(src);
		if(c.getPlayer().getTrade() != null || c.getPlayer().getMiniGame() != null || source == null){ // Only check needed would prob be merchants (to see if the player is in one)
			return;
		}
		int itemId = source.getItemId();
		if(itemId >= 5000000 && itemId <= 5000100) return;
		if(type == MapleInventoryType.EQUIPPED && itemId == 1122017){
			c.getPlayer().unequipPendantOfSpirit();
		}
		if(c.getPlayer().getItemEffect() == itemId && source.getQuantity() == 1){
			c.getPlayer().setItemEffect(0);
			c.getPlayer().getMap().broadcastMessage(UserRemote.itemEffect(c.getPlayer().getId(), 0));
		}else if(itemId == 5370000 || itemId == 5370001){
			if(c.getPlayer().getItemQuantity(itemId, false) == 1){
				c.getPlayer().setChalkboard(null);
			}
		}
		if((!ItemConstants.isRechargable(itemId) && c.getPlayer().getItemQuantity(itemId, true) < quantity) || quantity < 0 || source == null) return;
		Point dropPos = new Point(c.getPlayer().getPosition());
		if(quantity < source.getQuantity() && !ItemConstants.isRechargable(itemId)){
			// split stack
			Item target = source.copy();
			if(source.getQuantity() != quantity){
				target.setQuantity(quantity);
			}
			source.setQuantity((short) (source.getQuantity() - quantity));
			target.addDBFlag(ItemDB.DELETE);
			c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(1, source))));
			boolean weddingRing = source.getItemId() == 1112803 || source.getItemId() == 1112806 || source.getItemId() == 1112807 || source.getItemId() == 1112809;
			ItemData data = ii.getItemData(target.getItemId());
			if(weddingRing){
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
			}else if(c.getPlayer().getMap().getMapData().getEverlast()){
				if(data.isDropRestricted() || data.isCash){
					c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
				}else{
					c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, false);
				}
			}else if(data.isDropRestricted() || data.isCash){
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos);
			}else{
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
			}
		}else{
			// full stack
			ItemFactory.clearItemOwner(source, ItemFactory.INVENTORY);
			source.addDBFlag(ItemDB.DELETE);
			c.getPlayer().getInventory(type).removeSlot(src);
			c.announce(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(3, source))));
			if(src < 0){
				c.getPlayer().equipChanged();
			}
			ItemData data = ii.getItemData(itemId);
			if(c.getPlayer().getMap().getMapData().getEverlast()){
				if(data.isDropRestricted() || data.isCash){
					c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
					ItemFactory.deleteItem(source);
				}else{
					c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, false);
				}
			}else if(data.isDropRestricted() || data.isCash){
				c.getPlayer().getMap().disappearingItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos);
				ItemFactory.deleteItem(source);
			}else{
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
			}
		}
	}

	private static boolean isOverall(int itemId){
		return itemId / 10000 == 105;
	}

	private static boolean isWeapon(int itemId){
		return itemId >= 1302000 && itemId < 1492024;
	}

	public static boolean hasRoom(MapleClient c, Item item){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		MapleInventory inventory = c.getPlayer().getInventory(ii.getInventoryType(item.getItemId()));
		return inventory.hasRoom(c, item);
	}

	public static List<Item> combineItems(MapleClient c, List<Item> oldItems){
		return combineItems(c, oldItems, false);
	}

	public static List<Item> combineItems(MapleClient c, List<Item> oldItems, boolean ignoreMaxStack){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		Map<Integer, Item> itemCombining = new HashMap<>();
		List<Item> newItems = new ArrayList<>();
		for(int i = 0; i < oldItems.size(); i++){
			Item item = oldItems.get(i);
			int itemid = item.getItemId();
			if(itemCombining.containsKey(itemid)){
				Item item2 = itemCombining.get(itemid);
				int maxStack = ii.getItemData(itemid).getSlotMax(c);
				if(ignoreMaxStack){
					item2.setQuantity((short) (item2.getQuantity() + item.getQuantity()));
					itemCombining.put(itemid, item2);
				}else{
					if(item2.getQuantity() + item.getQuantity() > maxStack){
						itemCombining.put(itemid, item);
						newItems.add(item2);
					}else{
						item2.setQuantity((short) (item2.getQuantity() + item.getQuantity()));
						itemCombining.put(itemid, item2);
					}
				}
			}else{
				itemCombining.put(itemid, item);
			}
		}
		return newItems;
	}
}
