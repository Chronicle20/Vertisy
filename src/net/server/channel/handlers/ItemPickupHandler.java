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

import java.awt.Point;
import java.util.Arrays;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.RSSkill;
import client.autoban.AutobanFactory;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.server.world.MaplePartyCharacter;
import scripting.item.ItemScriptManager;
import server.ItemData;
import server.ItemInformationProvider;
import server.ItemInformationProvider.scriptedItem;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.field.DropPool;

/**
 * @author Matze
 */
public class ItemPickupHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c){
		slea.readByte();
		slea.readInt();
		Point clientPos = slea.readPos(); // cpos
		int oid = slea.readInt();
		slea.readInt();
		MapleCharacter chr = c.getPlayer();
		MapleMapObject ob = chr.getMap().getMapObject(oid);
		if(ob == null) return;
		if(ob instanceof MapleMapItem){
			MapleMapItem mapitem = (MapleMapItem) ob;
			pickupItem(c, null, ob, mapitem, clientPos, false);
		}
		c.announce(CWvsContext.enableActions());
	}

	static boolean useItem(final MapleClient c, final int id){
		if(id / 1000000 == 2){
			ItemInformationProvider ii = ItemInformationProvider.getInstance();
			if(ii.getItemData(id).consumeOnPickup){
				if(id > 2022430 && id < 2022434){
					for(MapleCharacter mc : c.getPlayer().getMap().getCharacters()){
						if(mc.getParty() == c.getPlayer().getParty()){
							ii.getItemData(id).itemEffect.applyTo(mc);
						}
					}
				}else{
					ii.getItemData(id).itemEffect.applyTo(c.getPlayer());
				}
				return true;
			}
		}
		return false;
	}

	public void pickupItem(MapleClient c, MaplePet pet, MapleMapObject ob, MapleMapItem mapitem, Point pos, boolean itemVac){
		boolean usedPet = pet != null;
		if(!FeatureSettings.LOOTING){
			c.announce(CWvsContext.enableActions());
			return;
		}
		MapleCharacter chr = c.getPlayer();
		if(ob.getVisibleTo() >= 0 && (ob.getVisibleTo() != chr.getId())) return;
		if(mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031866 || mapitem.getMeso() > 0 || (mapitem.getMeso() <= 0 && ItemInformationProvider.getInstance().getItemData(mapitem.getItemId()).consumeOnPickup) || c.getPlayer().canHoldItem(mapitem.getItem())){
			if(chr.getMapId() > 980000000 || (chr.getMapId() > 925020000 && chr.getMapId() < 925033600)){ // MCPQ, Dojo
				// Party Mana Elixirs, Elixirs, and Power Elixirs are already handled..?
				if(mapitem.getItemId() == 2022163 || mapitem.getItemId() == 2022433){ // Party All Cure Potion
					if(chr.isInParty()){
						for(MaplePartyCharacter pchr : chr.getParty().getMembers()){
							if(pchr.isOnline()){
								MapleCharacter pChrI = pchr.getPlayerInChannel();
								if(pChrI != null) pChrI.dispelDebuffs();
							}
						}
					}
				}
			}
			if((chr.getMapId() > 209000000 && chr.getMapId() < 209000016) || (chr.getMapId() >= 990000500 && chr.getMapId() <= 990000502)){// happyville trees and guild PQ
				if(!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == c.getPlayer().getObjectId()){
					if(mapitem.getMeso() > 0){
						chr.gainMeso(mapitem.getMeso(), true, true, false);
						if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
						else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
						chr.getMap().removeMapObject(ob);
						mapitem.setPickedUp(true);
					}else if(c.getPlayer().canHoldItem(mapitem.getItem())){
						MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false);
						if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
						else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
						chr.getMap().removeMapObject(ob);
						mapitem.setPickedUp(true);
					}else return;
				}else{
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				return;
			}
			synchronized(mapitem){
				ItemInformationProvider ii = ItemInformationProvider.getInstance();
				if(mapitem.isPickedUp()){
					c.announce(MaplePacketCreator.getInventoryFull());
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					return;
				}
				// if player drop > 5 second wait before other people can loot(dropper ignores)
				// if mob drop && hasn't been 900ms(item drop animation) > cancel
				// if mob drop && you aren't in a party with killer/aren't killer && hasn't been 10 seconds > cancel
				// if looter || mob killer == ironman && owner != partyid/chrid > cancel
				boolean dropOwner = mapitem.getOwner() == chr.getId() || mapitem.getOwner() == chr.getPartyId();
				long timeSinceDropped = System.currentTimeMillis() - mapitem.getDropTime();
				if(mapitem.isPlayerDrop()){
					if(timeSinceDropped < 5000 && !dropOwner) return;// drop cooldown
				}else{
					if(timeSinceDropped < 900) return;// animation cooldown
					if(!dropOwner && mapitem.getDropType() != 2){
						if(timeSinceDropped < 10000) return;// 10 seconds before other players can steal your loot.
					}
				}
				if((mapitem.isIronMan() || chr.isIronMan()) && !dropOwner) return;// Iron man check
				if(mapitem.getQuest() > 0 && !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId())){
					if(!usedPet) c.announce(CWvsContext.OnMessage.showItemUnavailable());
					return;
				}
				if(usedPet){
					if(mapitem.getDropper() == c.getPlayer()) return;
					//
					if(mapitem.getMeso() <= 0 && chr.getInventory(MapleInventoryType.EQUIPPED).findById(1812001) == null) return;
					else if(mapitem.getMeso() > 0 && chr.getInventory(MapleInventoryType.EQUIPPED).findById(1812000) == null) return;
				}
				if(mapitem.getMeso() <= 0 && mapitem.getItem() != null){
					if(!mapitem.isPlayerDrop()){
						if(chr.canHoldItem(mapitem.getItem())) chr.gainRSSkillExp(RSSkill.Capacity, 1);
					}
					if(mapitem.getItem() != null){
						if(mapitem.getItemId() == 4031530 || mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031531 || mapitem.getItemId() == 4031866){
							int nx = mapitem.getItemId() == 4031530 || mapitem.getItemId() == 4031865 ? 100 : 250;
							nx *= mapitem.getItem().getQuantity();
							chr.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, nx);
							chr.dropMessage(MessageType.TITLE, nx + " NX");
							if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
							else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
							chr.getMap().removeMapObject(ob);
							mapitem.setPickedUp(true);
							return;
						}
						if(!mapitem.isPlayerDrop()){
							if(mapitem.getItemId() == 4000514 || mapitem.getItemId() == 4032473){
								int exp = mapitem.getItemId() == 4000514 ? 4 : 8;
								exp *= mapitem.getItem().getQuantity();
								chr.gainRSSkillExp(RSSkill.Prayer, exp);
								if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
								else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
								chr.getMap().removeMapObject(ob);
								mapitem.setPickedUp(true);
								return;
							}
						}
						/*if(mapitem.getItemId() == 4007014 || mapitem.getItemId() == 4007015 || mapitem.getItemId() == 4007016){
							if(mapitem.getOwnerChrId() == chr.getId()){
								double expMultiplier = mapitem.getItemId() == 4007014 ? 10 : mapitem.getItemId() == 4007015 ? 20 : 30;
								expMultiplier *= mapitem.getItem().getQuantity();
								Optional<SpawnPoint> sp = chr.getMap().getHighestLevelMonster(false);
								if(sp.isPresent()){
									MapleMonster monster = sp.get().getFakeMonster();
									double exp = monster.getExp() * expMultiplier;
									int levelDifference = monster.getLevel() - c.getPlayer().getLevel();
									if(levelDifference < -5 || levelDifference > 5){
										levelDifference = Math.abs(levelDifference) - 5;
										exp /= levelDifference;
									}
									chr.gainExp(new ExpProperty(ExpGainType.GEM).gain((int) exp).show());
								}
								if(usedPet) chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
								else chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
								chr.getMap().removeMapObject(ob);
								mapitem.setPickedUp(true);
								return;
							}else return;
						}*/
						if(!mapitem.getItem().getLog().contains("Legendary") && !mapitem.getItem().getLog().contains("Rare")){
							if(chr.getAutoSell() && chr.getClient().checkEliteStatus()){
								if(!ItemConstants.AUTO_SELL_BLACKLIST.contains(mapitem.getItemId())){
									if(chr.isItemAutoSellable(mapitem.getItemId())){
										int price = ii.getItemData(mapitem.getItemId()).wholePrice;
										price *= 0.85;
										if(chr.getMeso() + price > 0){
											chr.gainMeso(price, true);
											mapitem.setPickedUp(true);
											if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
											else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
											chr.getMap().removeMapObject(ob);
											return;
										}
									}
								}
							}
						}
					}
				}
				if(!itemVac){
					final double distance = pos.distanceSq(mapitem.getPosition());
					if(distance > 5000 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)){
						chr.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
					}else if(distance > 640000.0){
						chr.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
					}
				}
				if(usedPet && chr.getInventory(MapleInventoryType.EQUIPPED).findById(1812007) != null){
					for(int i : pet.getExceptionList()){
						if((mapitem.getItem() != null && mapitem.getItem().getItemId() == i) || (mapitem.getMeso() > 0 && i == 2147483647)) return;
					}
				}
				if(mapitem.getMeso() > 0){
					if(chr.isInParty()){
						int mesosamm = mapitem.getMeso();
						if(mesosamm > 50000 * chr.getStats().getMesoRate()) mesosamm = 50000;
						double partynum = 0;
						for(MaplePartyCharacter partymem : chr.getParty().getMembers()){
							if(partymem.isOnline() && partymem.getMapId() == chr.getMap().getId() && partymem.getChannel() == c.getChannel()){
								partynum++;
							}
						}
						for(MaplePartyCharacter partymem : chr.getParty().getMembers()){
							if(partymem.isOnline() && partymem.getMapId() == chr.getMap().getId()){
								MapleCharacter somecharacter = c.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
								if(somecharacter != null){
									int meso = (int) (mesosamm / partynum);
									chr.battleAnaylsis.addMeso(meso);
									somecharacter.gainMeso(meso, true, true, false);
								}
							}
						}
					}else{
						chr.battleAnaylsis.addMeso(mapitem.getMeso());
						chr.gainMeso(mapitem.getMeso(), true, true, false);
					}
				}else if(mapitem.getItem().getItemId() / 10000 == 243){
					scriptedItem info = ii.getItemData(mapitem.getItem().getItemId()).scriptedItem;
					if(info.runOnPickup()){
						ItemScriptManager ism = ItemScriptManager.getInstance();
						String scriptName = info.getScript();
						if(ism.scriptExists(scriptName)){
							ism.getItemScript(c, scriptName);
						}
					}else{
						if(!c.getPlayer().canHoldItem(mapitem.getItem())) return;
						else{
							MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, false);
							ItemFactory.updateItemOwner(chr, mapitem.getItem(), ItemFactory.INVENTORY);
						}
					}
				}else if(useItem(c, mapitem.getItem().getItemId())){
					if(mapitem.getItem().getItemId() / 10000 == 238){
						chr.getMonsterBook().addCard(c, mapitem.getItem().getItemId());
					}
				}else if(c.getPlayer().canHoldItem(mapitem.getItem())){
					MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, false);
					ItemFactory.updateItemOwner(chr, mapitem.getItem(), ItemFactory.INVENTORY);
				}else if(mapitem.getItem().getItemId() == 4031868){
					chr.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getItemQuantity(4031868, false), false));
				}else return;
				if(mapitem.getMeso() <= 0){
					ItemData data = ItemInformationProvider.getInstance().getItemData(mapitem.getItemId());
					if(data.onlyOnePickup){
						// Loop through drops on the map, and expire all of the other only one pickup items.
						chr.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM)).stream().filter((mmo)-> {
							if(mmo instanceof MapleMapItem){
								if(mmo.getObjectId() != ob.getObjectId() && ItemInformationProvider.getInstance().getItemData(((MapleMapItem) mmo).getItemId()).onlyOnePickup) return true;
							}
							return false;
						}).forEach(mmo-> {
							if(mmo instanceof MapleMapItem){
								((MapleMapItem) mmo).setPickedUp(true);
								chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mmo.getObjectId(), 0, 0));
								chr.getMap().removeMapObject(mmo);
							}
						});
					}
				}
				mapitem.setPickedUp(true);
				if(usedPet) chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), true, chr.getPetIndex(pet)), mapitem.getPosition());
				else chr.getMap().broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
				chr.getMap().removeMapObject(ob);
			}
		}
	}
}
