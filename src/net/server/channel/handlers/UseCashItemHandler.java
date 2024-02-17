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
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import scripting.npc.NPCScriptManager;
import server.*;
import server.ItemInformationProvider.RewardItem;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import server.maps.MapleTVEffect;
import server.maps.objects.HiredMerchant;
import server.maps.objects.Kite;
import server.maps.objects.MapleMapObjectType;
import server.maps.objects.PlayerShop;
import server.shops.MapleShop;
import server.shops.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CUserPool;
import tools.packets.CWvsContext;
import tools.packets.Field;
import tools.packets.UserLocal;
import tools.packets.field.MessageBoxPool;
import tools.packets.field.userpool.UserCommon;
import tools.packets.field.userpool.UserRemote;

public final class UseCashItemHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter player = c.getPlayer();
		if(System.currentTimeMillis() - player.getLastUsedCashItem() < 1000) return;
		player.setLastUsedCashItem(System.currentTimeMillis());
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		slea.readInt();
		short pos = slea.readShort();
		int itemId = slea.readInt();
		int itemType = itemId / 10000;
		Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pos);
		String medal = "";
		Item medalItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
		if(medalItem != null){
			medal = "<" + ii.getItemData(medalItem.getItemId()).name + "> ";
		}
		if(toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1){
			AutobanFactory.PACKET_EDIT.alert(player, "Item id doesn't match, or tried to use an item that doesn't exist");
			c.announce(CWvsContext.enableActions());
			return;
		}
		Logger.log(LogType.INFO, LogFile.CASH_ITEM, player.getName(), "Using item: " + itemId + "(" + itemType + ") at pos: " + pos);
		if(itemType == 505){ // AP/SP reset
			if(itemId > 5050000){
				int SPTo = slea.readInt();
				int SPFrom = slea.readInt();
				Skill skillSPTo = SkillFactory.getSkill(SPTo);
				Skill skillSPFrom = SkillFactory.getSkill(SPFrom);
				byte curLevel = player.getSkillLevel(skillSPTo);
				byte curLevelSPFrom = player.getSkillLevel(skillSPFrom);
				int maxLevel = skillSPTo.getMaxLevel();
				int masterLevel = skillSPTo.getMasterLevel();
				if(masterLevel > 0 && player.getMasterLevel(skillSPTo) <= masterLevel) maxLevel = masterLevel;
				int maxLevelFrom = skillSPFrom.getMaxLevel();
				int masterLevelFrom = skillSPFrom.getMasterLevel();
				if(masterLevelFrom > 0 && player.getMasterLevel(skillSPFrom) <= masterLevelFrom) maxLevelFrom = masterLevelFrom;
				if((curLevel < maxLevel) && curLevelSPFrom > 0){
					player.changeSkillLevel(skillSPFrom, (byte) (curLevelSPFrom - 1), maxLevelFrom, -1);
					player.changeSkillLevel(skillSPTo, (byte) (curLevel + 1), masterLevel, -1);
					if((curLevelSPFrom - 1) == 0){
						boolean updated = false;
						for(SkillMacro macro : player.getMacros()){
							if(macro == null) continue;
							boolean update = false;// cleaner?
							if(macro.skill1 == SPFrom){
								update = true;
								macro.skill1 = 0;
							}
							if(macro.skill2 == SPFrom){
								update = true;
								macro.skill2 = 0;
							}
							if(macro.skill2 == SPFrom){
								update = true;
								macro.skill2 = 0;
							}
							if(update){
								updated = true;
								player.updateMacros(macro.getPosition(), macro);
							}
						}
						if(updated) player.sendMacros();
					}
				}else return;
			}else{
				List<Pair<MapleStat, Integer>> statupdate = new ArrayList<>(2);
				int APTo = slea.readInt();
				int APFrom = slea.readInt();
				switch (APFrom){
					case 64: // str
						if(player.getStr() < 5) return;
						player.addStat(1, -1);
						break;
					case 128: // dex
						if(player.getDex() < 5) return;
						player.addStat(2, -1);
						break;
					case 256: // int
						if(player.getInt() < 5) return;
						player.addStat(3, -1);
						break;
					case 512: // luk
						if(player.getLuk() < 5) return;
						player.addStat(4, -1);
						break;
					case 2048: // HP
						if(player.getHpMpApUsed() == 0){
							player.dropMessage(5, "You must first put a point into HP or MP to take one out.");
							player.announce(CWvsContext.enableActions());
							return;
						}
						int hplose = 0;
						final int jobid = player.getJob().getId();
						if(jobid == 0 || jobid == 1000 || jobid == 2000 || jobid >= 1200 && jobid <= 1211){ // Beginner
							hplose -= 12;
						}else if(jobid >= 100 && jobid <= 132){ // Warrior
							Skill improvinghplose = SkillFactory.getSkill(1000001);
							int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
							hplose -= 24;
							if(improvinghploseLevel >= 1){
								hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
							}
						}else if(jobid >= 200 && jobid <= 232){ // Magician
							hplose -= 10;
						}else if(jobid >= 500 && jobid <= 522){ // Pirate
							Skill improvinghplose = SkillFactory.getSkill(5100000);
							int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
							hplose -= 22;
							if(improvinghploseLevel > 0){
								hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
							}
						}else if(jobid >= 1100 && jobid <= 1111){ // Soul Master
							Skill improvinghplose = SkillFactory.getSkill(11000000);
							int improvinghploseLevel = c.getPlayer().getSkillLevel(improvinghplose);
							hplose -= 27;
							if(improvinghploseLevel >= 1){
								hplose -= improvinghplose.getEffect(improvinghploseLevel).getY();
							}
						}else if((jobid >= 1300 && jobid <= 1311) || (jobid >= 1400 && jobid <= 1411)){ // Wind Breaker and Night Walker
							hplose -= 17;
						}else if(jobid >= 300 && jobid <= 322 || jobid >= 400 && jobid <= 422 || jobid >= 2000 && jobid <= 2112){ // Aran
							hplose -= 20;
						}else{ // GameMaster
							hplose -= 20;
						}
						player.setHp(player.getHp() + hplose);
						player.setMaxHp(player.getMaxHp() + hplose);
						player.setHpMpApUsed(player.getHpMpApUsed() - 1);
						statupdate.add(new Pair<>(MapleStat.HP, player.getHp()));
						statupdate.add(new Pair<>(MapleStat.MAXHP, player.getMaxHp()));
						break;
					case 8192: // MP
						if(player.getHpMpApUsed() == 0){
							player.dropMessage(5, "You must first put a point into HP or MP to take one out.");
							player.announce(CWvsContext.enableActions());
							return;
						}
						int mp = player.getMp();
						int level = player.getLevel();
						MapleJob job = player.getJob();
						boolean canWash = true;
						if(job.isA(MapleJob.SPEARMAN) || job.isA(MapleJob.ARAN1)){
							if(mp < 4 * level + 156) canWash = false;
						}else if(job.isA(MapleJob.FIGHTER)){
							if(mp < 4 * level + 56) canWash = false;
						}else if(job.isA(MapleJob.THIEF) && job.getId() % 100 > 0){
							if(mp < level * 14 - 4) canWash = false;
						}else if(mp < level * 14 + 148){
							canWash = false;
						}
						if(canWash){
							int minmp = 0;
							if(job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)){
								minmp += 4;
							}else if(job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)){
								minmp += 36;
							}else if(job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)){
								minmp += 12;
							}else if(job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)){
								minmp += 16;
							}else{
								minmp += 8;
							}
							player.setMp(player.getMp() - minmp);
							player.setMaxMp(player.getMaxMp() - minmp);
							player.setHpMpApUsed(player.getHpMpApUsed() - 1);
							statupdate.add(new Pair<>(MapleStat.MP, player.getMp()));
							statupdate.add(new Pair<>(MapleStat.MAXMP, player.getMaxMp()));
							break;
						}else{
							player.dropMessage(5, "You do not have the minimum MP required to remove it at the current level.");
						}
					default:
						c.announce(CWvsContext.enableActions());
						return;
				}
				DistributeAPHandler.addStat(c, APTo);
				c.announce(CWvsContext.updatePlayerStats(statupdate, true, c.getPlayer()));
			}
			remove(c, pos);
		}else if(itemType == 506){
			Item eq = null;
			if(itemId == 5060000){ // Item tag.
				int equipSlot = slea.readShort();
				if(equipSlot == 0) return;
				eq = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) equipSlot);
				eq.setOwner(player.getName());
			}else if(itemId == 5060001 || itemId == 5061000 || itemId == 5061001 || itemId == 5061002 || itemId == 5061003){ // Sealing lock
				MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
				eq = c.getPlayer().getInventory(type).getItem((short) slea.readInt());
				if(eq == null){ // Check if the type is EQUIPMENT?
					return;
				}
				byte flag = eq.getFlag();
				flag |= ItemConstants.LOCK;
				eq.setFlag(flag);
				long period = ItemInformationProvider.getInstance().getItemData(itemId).protectTime;
				if(period > 0){
					eq.setLockExpiration(System.currentTimeMillis() + (period * 60 * 60 * 24 * 1000));
				}
				remove(c, pos);
			}else if(itemId == 5060002){ // Incubator
				byte inventory2 = (byte) slea.readInt();
				short slot2 = (short) slea.readInt();
				Item item2 = c.getPlayer().getInventory(MapleInventoryType.getByType(inventory2)).getItem(slot2);
				if(item2 == null){ // hacking
					AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried using an incubator with null item");
					return;
				}
				if(getIncubatedItem(c, itemId)){
					MapleInventoryManipulator.removeItem(c, MapleInventoryType.getByType(inventory2), slot2, (short) 1, true, false);
					remove(c, pos);
				}
				return;
			}else if(itemId == 5062000){
				if(!FeatureSettings.CUBING){
					c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.CUBING_DISABLED);
					c.announce(CWvsContext.enableActions());
					return;
				}
				short eqpPos = slea.readShort();
				slea.readShort();// int?
				Item eqp = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(eqpPos);
				if(eqp == null){
					AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried using a cube on a null eqp");
					c.getPlayer().getMap().broadcastMessage(UserCommon.showItemUnreleaseEffect(c.getPlayer().getId(), false));
					return;
				}
				Equip e = (Equip) eqp;
				if(e.getGrade() < 5 || e.getGrade() > 8){
					AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried using a cube on equip with a grade of " + e.getGrade());
					c.getPlayer().getMap().broadcastMessage(UserCommon.showItemUnreleaseEffect(c.getPlayer().getId(), false));
					return;
				}
				if(c.getPlayer().canHoldItem(new Item(2430112, (short) 1))){
					e.setGrade((byte) (e.getGrade() - 4));
					c.getPlayer().forceUpdateItem(e);
					remove(c, pos);
					MapleInventoryManipulator.addFromDrop(c, new Item(2430112, (short) 1), false);
					c.getPlayer().getMap().broadcastMessage(UserCommon.showItemUnreleaseEffect(c.getPlayer().getId(), true));
					return;
				}else{
					c.getPlayer().getMap().broadcastMessage(UserCommon.showItemUnreleaseEffect(c.getPlayer().getId(), false));
					return;
				}
			}
			slea.readInt(); // time stamp
			if(eq != null){
				player.forceUpdateItem(eq);
				remove(c, pos);
			}
		}else if(itemType == 507){
			boolean whisper;
			if(player.isChatBanned()){
				player.dropMessage(5, "You are curently banned from using megaphones!");
				return;
			}
			switch (itemId / 1000 % 10){
				case 1: // Megaphone
					if(player.getLevel() > 9){
						player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(MessageType.MEGAPHONE.getValue(), medal + player.getName() + " : " + slea.readMapleAsciiString()));
					}else{
						player.dropMessage(1, "You may not use this until you're level 10.");
					}
					break;
				case 2: // Super megaphone
					try{
						ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(MessageType.SUPER_MEGAPHONE.getValue(), c.getChannel(), medal + player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)));
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
					break;
				case 4:// Skull
					try{
						ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(MessageType.SkullSpeaker.getValue(), c.getChannel(), medal + player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)));
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
					break;
				case 5: // Maple TV
					int tvType = itemId % 10;
					boolean megassenger = false;
					boolean ear = false;
					MapleCharacter victim = null;
					if(tvType != 1){
						if(tvType >= 3){
							megassenger = true;
							if(tvType == 3){
								slea.readByte();
							}
							ear = 1 == slea.readByte();
						}else if(tvType != 2){
							slea.readByte();
						}
						if(tvType != 4){
							victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
						}
					}
					List<String> messages = new LinkedList<>();
					StringBuilder builder = new StringBuilder();
					for(int i = 0; i < 5; i++){
						String message = slea.readMapleAsciiString();
						if(megassenger){
							builder.append(" ").append(message);
						}
						messages.add(message);
					}
					slea.readInt();
					if(megassenger){
						try{
							ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + builder.toString(), ear));
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						}
					}
					if(!MapleTVEffect.isActive()){
						new MapleTVEffect(player, victim, messages, tvType);
						remove(c, pos);
					}else{
						player.dropMessage(1, "MapleTV is already in use.");
						return;
					}
					break;
				case 6: // item megaphone
					String msg = medal + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
					whisper = slea.readByte() == 1;
					Item item = null;
					if(slea.readByte() == 1){ // item
						item = c.getPlayer().getInventory(MapleInventoryType.getByType((byte) slea.readInt())).getItem((short) slea.readInt());
						if(item == null){ // hack
							AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried using Item Megaphone on a null item");
							return;
						}
					}
					try{
						ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.itemMegaphone(msg, whisper, c.getChannel(), item));
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
					break;
				case 7: // triple megaphone
					int lines = slea.readByte();
					if(lines < 1 || lines > 3){ // hack
						AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried sending an invalid amount of lines for triple megaphone");
						return;
					}
					String[] msg2 = new String[lines];
					for(int i = 0; i < lines; i++){
						msg2[i] = medal + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
					}
					whisper = slea.readByte() == 1;
					try{
						ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.getMultiMegaphone(msg2, c.getChannel(), whisper));
					}catch(RemoteException | NullPointerException ex){
						Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
					}
					break;
				default:
					Logger.log(LogType.INFO, LogFile.GENERAL_INFO, "Unhandled megaphone " + itemId);
					break;
			}
			remove(c, pos);
		}else if(itemType == 508){ // graduation banner
			String message = slea.readMapleAsciiString(); // message, sepearated by 0A for lines can be split by ' '
			final Point position = player.getMap().calcPointBelow(player.getPosition());
			if(position == null || !player.getMap().getMapObjectsInRange(player.getPosition(), 5000, Arrays.asList(MapleMapObjectType.KITE)).isEmpty() || message.length() > 200){
				c.announce(MessageBoxPool.spawnKiteError());
				c.announce(CWvsContext.enableActions());
				return;
			}
			Kite kite = new Kite(player, itemId, message);
			player.getMap().addMapObject(kite);
			player.getMap().broadcastMessage(MessageBoxPool.spawnKite(kite));
			remove(c, pos);
		}else if(itemType == 509){
			String sendTo = slea.readMapleAsciiString();
			String msg = slea.readMapleAsciiString();
			try{
				player.sendNote(sendTo, msg, (byte) 0);
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
			remove(c, pos);
		}else if(itemType == 510){
			player.getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
			remove(c, pos);
		}else if(itemType == 512){
			/*if(player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId)){
				if(ii.getStateChangeItem(itemId) != 0){
					for(MapleCharacter mChar : c.getPlayer().getMap().getCharacters()){
						ii.getItemEffect(ii.getStateChangeItem(itemId)).applyTo(mChar);
					}
				}
				remove(c, pos);
			}else{
				c.getPlayer().dropMessage(MessageType.ERROR, "A MapEffect is currently already running. Please try again later.");
			}*/
			c.getPlayer().dropMessage(MessageType.ERROR, "Map Effects are currently disabled.");
		}else if(itemType == 517){
			MaplePet pet = player.getPet(0);
			if(pet == null){
				c.announce(CWvsContext.enableActions());
				return;
			}
			Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
			String newName = slea.readMapleAsciiString();
			pet.setName(newName);
			pet.saveToDb();
			player.forceUpdateItem(item);
			player.getMap().broadcastMessage(player, MaplePacketCreator.changePetName(player, newName, 1), true);
			c.announce(CWvsContext.enableActions());
			remove(c, pos);
		}else if(itemType == 504){ // teleport rock
			String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
			boolean vip = slea.readByte() == 1;
			if(FieldLimit.CANNOTVIPROCK.check(player.getMap().getMapData().getFieldLimit())){
				System.out.println("Fail0");
				c.getPlayer().dropMessage(1, error1);
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(!vip){
				int mapId = slea.readInt();
				boolean mapFail = false;
				if(itemId == 5041000){
					if(!player.isVipTrockMap(mapId)){
						mapFail = true;
						System.out.println("Fail1");
					}
				}else{
					if(!player.isTrockMap(mapId)){
						mapFail = true;
						System.out.println("Fail2");
					}
				}
				if(!mapFail && c.getChannelServer().getMap(mapId).getMapData().getForcedReturnMap() == 999999999 && !GameConstants.isBadMap(mapId)){
					MapleMap map = c.getChannelServer().getMap(mapId);
					if(!FieldLimit.CANNOTVIPROCK.check(map.getMapData().getFieldLimit())){
						remove(c, pos);
						player.changeMap(c.getChannelServer().getMap(mapId));
					}else{
						c.getPlayer().dropMessage(1, error1);
						c.announce(CWvsContext.enableActions());
					}
				}else{
					c.getPlayer().dropMessage(1, error1);
					c.announce(CWvsContext.enableActions());
				}
			}else{
				String name = slea.readMapleAsciiString();
				MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
				if(victim != null){
					MapleMap target = victim.getMap();
					if(target.getInstanceID() == null || (victim.getPartyId() != -1 && victim.getPartyId() == c.getPlayer().getPartyId())){
						if((c.getChannelServer().getMap(victim.getMapId()).getMapData().getForcedReturnMap() == 999999999 || victim.getMapId() < 100000000) && !GameConstants.isBadMap(victim.getMapId())){
							if(!victim.isGM()){
								if(itemId == 5041000 || victim.getMapId() / player.getMapId() == 1){ // viprock & same continent
									if(!FieldLimit.CANNOTVIPROCK.check(target.getMapData().getFieldLimit())){
										remove(c, pos);
										player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
									}else{
										player.dropMessage(1, "You cannot teleport to this map.");
									}
								}else{
									player.dropMessage(1, "You cannot teleport between continents with this teleport rock.");
								}
							}else{
								player.dropMessage(1, error1);
							}
						}else{
							player.dropMessage(1, "You cannot teleport to this map.");
						}
					}else{
						player.dropMessage(1, "You cannot teleport to this map.");
					}
				}else{
					player.dropMessage(1, "Player could not be found in this channel.");
				}
			}
		}else if(itemType == 520){
			player.gainMeso(ii.getItemData(itemId).meso, true, false, true);
			c.announce(CWvsContext.enableActions());
			remove(c, pos);
		}else if(itemType == 524){
			for(byte i = 0; i < 3; i++){
				MaplePet pet = player.getPet(i);
				if(pet != null){
					if(pet.canConsume(itemId)){
						pet.setFullness(100);
						pet.gainCloseness(player, 100);
						// Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
						// player.forceUpdateItem(item);
						player.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(player.getId(), i, 1, true), true);
						remove(c, pos);
						c.announce(CWvsContext.enableActions());
						break;
					}
				}
			}
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 530){
			ii.getItemData(itemId).itemEffect.applyTo(player);
			remove(c, pos);
		}else if(itemType == 533){
			NPCScriptManager.getInstance().start(c, 9010009, null);
		}else if(itemType == 537){
			player.setChalkboard(slea.readMapleAsciiString());
			player.getMap().broadcastMessage(CUserPool.CommonPacket.useChalkboard(player, false));
			player.getClient().announce(CWvsContext.enableActions());
		}else if(itemType == 539){
			List<String> lines = new LinkedList<>();
			for(int i = 0; i < 4; i++){
				lines.add(slea.readMapleAsciiString());
			}
			try{
				ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.getAvatarMega(c.getPlayer(), medal, c.getChannel(), itemId, lines, (slea.readByte() != 0)));
				TimerManager.getInstance().schedule("mega", new Runnable(){

					@Override
					public void run(){
						try{
							ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.byeAvatarMega());
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						}
					}
				}, 1000 * 10);
				remove(c, pos);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}else if(itemType == 545){ // MiuMiu's travel store
			if(player.getShop() == null){
				MapleShop shop = MapleShopFactory.getInstance().getShop(1338);
				if(shop != null){
					shop.sendShop(c);
					remove(c, pos);
				}
			}else{
				c.announce(CWvsContext.enableActions());
			}
		}else if(itemType == 550){ // Extend item expiration
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 552){
			MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
			short slot = (short) slea.readInt();
			Item item = c.getPlayer().getInventory(type).getItem(slot);
			if(item == null || item.getQuantity() <= 0 || (item.getFlag() & ItemConstants.KARMA) > 0 && ii.getItemData(item.getItemId()).tradeAvailable){
				c.announce(CWvsContext.enableActions());
				return;
			}
			if(type.equals(MapleInventoryType.USE)){
				item.setFlag((byte) ItemConstants.SPIKES);
			}else{
				item.setFlag((byte) ItemConstants.KARMA);
			}
			c.getPlayer().forceUpdateItem(item);
			remove(c, pos);
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 552){ // DS EGG THING
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 553){// If updated, ItemRewardHandler
			Pair<Integer, List<RewardItem>> rewards = ii.getItemData(itemId).rewardItems;
			for(RewardItem reward : rewards.getRight()){
				if(!c.getPlayer().canHoldItem(new Item(reward.itemid, reward.quantity))){
					c.announce(CWvsContext.OnMessage.getShowInventoryFull());
					break;
				}
				if(Randomizer.nextInt(rewards.getLeft()) < reward.prob){// Is it even possible to get an item with prob 1?
					if(ItemConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP){
						final Item item = ii.getEquipById(reward.itemid);
						if(reward.period != -1){
							item.setExpiration(System.currentTimeMillis() + (reward.period * 60 * 60 * 10));
						}
						MapleInventoryManipulator.addFromDrop(c, item, false);
					}else{
						MapleInventoryManipulator.addFromDrop(c, new Item(reward.itemid, reward.quantity), false);
					}
					remove(c, pos);
					if(reward.worldmsg != null){
						String msg = reward.worldmsg;
						msg.replaceAll("/name", c.getPlayer().getName());
						msg.replaceAll("/item", ii.getItemData(reward.itemid).name);
						try{
							ChannelServer.getInstance().getWorldInterface().broadcastPacket(MaplePacketCreator.serverNotice(6, msg));
						}catch(RemoteException | NullPointerException ex){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
						}
					}
					if(reward.effect != null){
						c.announce(UserLocal.UserEffect.showInfo(reward.effect));
						c.getPlayer().getMap().broadcastMessage(UserRemote.UserEffect.showInfo(c.getPlayer().getId(), reward.effect));
					}
					break;
				}
			}
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 557){
			slea.readInt();
			int itemSlot = slea.readInt();
			slea.readInt();
			final Equip equip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) itemSlot);
			if(equip.getVicious() == 2 || c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5570000) == null) return;
			equip.setVicious(equip.getVicious() + 1);
			equip.setUpgradeSlots(equip.getUpgradeSlots() + 1);
			remove(c, pos);
			c.announce(CWvsContext.enableActions());
			c.announce(MaplePacketCreator.sendHammerData(equip.getVicious()));
			player.forceUpdateItem(equip);
		}else if(itemType == 561){ // VEGA'S SPELL
			// 56 00
			// 0D CA 3A 01 - timestamp
			// 3B 00 - vega slot
			// 11 9A 55 00 - vega scroll
			// 01 00 00 00 - ?
			// 02 00 00 00 - equip slot
			// 02 00 00 00 - ?
			// 05 00 00 00 - scroll slot
			// 01 00 00 00 3F E3 3A 01
			slea.readInt();
			int equipSlot = slea.readInt();
			slea.readInt();
			int scrollSlot = slea.readInt();
			Equip equip = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) equipSlot);
			Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) scrollSlot);
			if(equip == null || item == null){
				AutobanFactory.PACKET_EDIT.autoban(c.getPlayer(), "Invalid item for vega scroll");
				return;
			}
			c.announce(Field.vegaFail());
			c.announce(CWvsContext.enableActions());
		}else if(itemType == 523){
			int searchedItem = slea.readInt();
			byte sortByPrice = slea.readByte();
			slea.readInt();// time?
			List<PlayerShop> shops = new ArrayList<>();
			List<Integer> shopOIDS = new ArrayList<>();
			List<MaplePlayerShopItem> items = new ArrayList<>();
			/*for(Channel ch : Server.getInstance().getChannelsFromWorld(c.getPlayer().getWorld())){
				for(int idOffset = 1; idOffset <= 22; idOffset++){
					int mapid = 910000000 + idOffset;
					MapleMap map = ch.getMap(mapid);
					for(PlayerShop shop : map.getPlayerShops()){*/
			for(HiredMerchant merch : c.getPlayer().getClient().getChannelServer().getMerchants().values()){
				for(MaplePlayerShopItem item : merch.getItems()){
					if(item.getItem().getItemId() == searchedItem && item.isExist()){
						if(!shopOIDS.contains(merch.getObjectId())){
							shops.add(merch);
							shopOIDS.add(merch.getObjectId());
						}
						items.add(item);
					}
				}
			}
			// }
			// }
			// }
			c.announce(CWvsContext.owlOfMinerva(c, sortByPrice, searchedItem, shops, items));
		}else if(itemType == 562){// Cash mastery book
			boolean canuse;
			boolean success = false;
			int skill = 0;
			int maxlevel = 0;
			ItemData skilldata = ItemInformationProvider.getInstance().getItemData(toUse.getItemId());
			if(skilldata.skills.isEmpty()){
				player.getClient().announce(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, false, success));
				return;
			}
			Skill skill2 = null;
			if(c.getPlayer().getJob() != null && c.getPlayer().getJob().getJobTree() != null){
				for(MapleJob job : c.getPlayer().getJob().getJobTree()){
					if(skilldata.skills.get(0) / 10000 == job.getId()){
						skill2 = SkillFactory.getSkill(skilldata.skills.get(0));
					}
				}
			}
			if(skill2 == null){
				canuse = false;
			}else if(player.getReincarnations() < skilldata.rcount){
				canuse = false;
			}else if((player.getSkillLevel(skill2) >= skilldata.reqSkillLevel || skilldata.reqSkillLevel == 0) && player.getMasterLevel(skill2) < skilldata.masterLevel){
				canuse = true;
				if(Randomizer.nextInt(101) < skilldata.success && skilldata.success != 0){
					success = true;
					player.changeSkillLevel(skill2, player.getSkillLevel(skill2), Math.max(skilldata.masterLevel, player.getMasterLevel(skill2)), -1);
				}else{
					success = false;
				}
				Logger.log(LogType.INFO, LogFile.CASH_ITEM, c.getAccountName(), c.getPlayer().getName() + " Used skillbook: " + toUse.getItemId());
				remove(c, pos);
			}else{
				canuse = false;
			}
			player.getClient().announce(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, canuse, success));
		}else{
			System.out.println("NEW CASH ITEM: " + itemType + "\n" + slea.toString());
			c.announce(CWvsContext.enableActions());
		}
		c.announce(CWvsContext.enableActions());
	}

	private static void remove(MapleClient c, short pos){
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.CASH, pos, 1, true, true);
		// MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
	}

	private static boolean getIncubatedItem(MapleClient c, int id){
		final int[] ids = {1012070, 1302049, 1302063, 1322027, 2000004, 2000005, 2020013, 2020015, 2040307, 2040509, 2040519, 2040521, 2040533, 2040715, 2040717, 2040810, 2040811, 2070005, 2070006, 4020009,};
		final int[] quantitys = {1, 1, 1, 1, 240, 200, 200, 200, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3};
		int amount = 0;
		for(int i = 0; i < ids.length; i++){
			if(i == id){
				amount = quantitys[i];
			}
		}
		if(c.getPlayer().getInventory(MapleInventoryType.getByType((byte) (id / 1000000))).isFull()) return false;
		MapleInventoryManipulator.addFromDrop(c, new Item(id, (short) amount), false);
		return true;
	}
}
