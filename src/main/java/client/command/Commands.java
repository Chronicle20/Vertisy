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
package client.command;

import java.awt.Point;
import java.io.File;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import client.*;
import client.inventory.Item;
import constants.GameConstants;
import constants.ServerConstants;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.MaplePortal;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.propertybuilder.ExpProperty;
import tools.*;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

public class Commands{

	private static HashMap<String, Integer> gotomaps = new HashMap<String, Integer>();
	// private static String[] tips = {"Use @gm for help, not to report bugs. If you wish to report bugs, please head to the forums and report them there or use the @bug command.", "Use @help to find out the commands that are available.", "Use @bug to report bugs."};
	static{
		gotomaps.put("gmmap", 180000000);
		gotomaps.put("southperry", 60000);
		gotomaps.put("amherst", 1010000);
		gotomaps.put("henesys", 100000000);
		gotomaps.put("ellinia", 101000000);
		gotomaps.put("perion", 102000000);
		gotomaps.put("kerning", 103000000);
		gotomaps.put("lith", 104000000);
		gotomaps.put("sleepywood", 105040300);
		gotomaps.put("florina", 110000000);
		gotomaps.put("orbis", 200000000);
		gotomaps.put("happy", 209000000);
		gotomaps.put("elnath", 211000000);
		gotomaps.put("ludi", 220000000);
		gotomaps.put("aqua", 230000000);
		gotomaps.put("leafre", 240000000);
		gotomaps.put("mulung", 250000000);
		gotomaps.put("herb", 251000000);
		gotomaps.put("omega", 221000000);
		gotomaps.put("korean", 222000000);
		gotomaps.put("nlc", 600000000);
		gotomaps.put("excavation", 990000000);
		gotomaps.put("pianus", 230040420);
		gotomaps.put("horntail", 240060200);
		gotomaps.put("mushmom", 100000005);
		gotomaps.put("griffey", 240020101);
		gotomaps.put("manon", 240020401);
		gotomaps.put("horseman", 682000001);
		gotomaps.put("balrog", 105090900);
		gotomaps.put("zakum", 211042300);
		gotomaps.put("papu", 220080001);
		gotomaps.put("showa", 801000000);
		gotomaps.put("guild", 200000301);
		gotomaps.put("shrine", 800000000);
		gotomaps.put("skelegon", 240040511);
		gotomaps.put("hpq", 100000200);
		gotomaps.put("ht", 240050400);
		gotomaps.put("fm", 910000000);
	}

	public static boolean executePlayerCommand(MapleClient c, String[] sub, char heading){
		MapleCharacter player = c.getPlayer();
		switch (sub[0]){
			case "help":
			case "commands":
				player.yellowMessage("Vertisy Commands:");
				player.message("@dispose: Fixes your character if it is stuck.");
				player.message("@GM <message>: Sends a message to all online GMs in the case of an emergency.");
				player.message("@Bug <bug>: Sends a bug report to all developers.");
				player.message("@Skills: See your current skill level & exp");
				player.message("@Time: See the time till day/night");
				player.message("@Track <skill>: Get updates when you get exp for a specific skill.");
				// player.message("@joinevent: If an event is in progress, use this to warp to the event map.");
				// player.message("@leaveevent: If an event has ended, use this to warp to your original map.");
				player.message("@whatdropsfrom <monster name>: Displays a list of drops for a specific monster for a price of 50k.");// Displays a list of drops and chances for a specified monster.");
				// player.message("@whodrops <item name>: Displays monsters that drop an item given an item name.");
				// player.message("@bosshp: Displays the remaining HP of the bosses on your map.");
				// player.message("@save: Saves your character.");
				player.message("@Rates: Your current rates.");
				player.message("@Night: Completely disables backgrounds. Overrides day/night.");
				player.message("@Playtime: See how long you have played your character.");
				player.message("@NX: See how much NX Credit you have.");
				if(player.getClient().checkEliteStatus()){
					player.message("@AutoSell: Automatically sell items looted.");
					player.message("@Elite: Open Elite only NPC");
					player.message("@EliteLeft: Get how long you have elite for.");
					player.message("@PetVac: Toggle pet vac.");
				}
				// player.message("@donorhelp: Shows donor commands");
				// player.message("@ranks: Shows ranking of players' levels");
				return true;
			/*case "joinevent":
			case "event":
			case "join":
				if(!FieldLimit.CANNOTVIPROCK.check(player.getMap().getMapData().getFieldLimit())){
					MapleEvent event = c.getChannelServer().getEvent();
					if(event != null){
						if(event.getMapId() != player.getMapId()){
							if(event.getLimit() > 0){
								player.saveLocation("EVENT");
								if(event.getMapId() == 109080000 || event.getMapId() == 109060001){
									player.setTeam(event.getLimit() % 2);
									player.dropMessage("Team: " + player.getTeam());
								}
								event.minusLimit();
								player.changeMap(event.getMapId());
							}else{
								player.dropMessage("The limit of players for the event has already been reached.");
							}
						}else{
							player.dropMessage(5, "You are already in the event.");
						}
					}else{
						player.dropMessage(5, "There is currently no event in progress.");
					}
				}else{
					player.dropMessage(5, "You are currently in a map where you can't join an event.");
				}
				return true;
			case "leaveevent":
			case "leave":
				int returnMap = player.getSavedLocation("EVENT");
				if(returnMap != -1){
					if(player.getOla() != null){
						player.getOla().resetTimes();
						player.setOla(null);
					}
					if(player.getFitness() != null){
						player.getFitness().resetTimes();
						player.setFitness(null);
					}
					player.changeMap(returnMap);
					MapleClient cl = player.getClient();
					int id = 4031574; // Jewel Event
					MapleInventoryType invType = MapleItemInformationProvider.getInstance().getInventoryType(id);
					int possessed = cl.getPlayer().getInventory(invType).countById(id);
					if(possessed > 0){
						MapleInventoryManipulator.removeItem(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true);
						cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
					}
					if(invType == MapleInventoryType.EQUIP){
						if(cl.getPlayer().getInventory(MapleInventoryType.EQUIPPED).countById(id) > 0){
							MapleInventoryManipulator.removeItem(cl, MapleInventoryType.EQUIPPED, id, 1, true);
							cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -1, true));
						}
					}
					if(c.getChannelServer().getEvent() != null){
						c.getChannelServer().getEvent().addLimit();
					}
				}else{
					player.dropMessage(5, "You are not currently in an event.");
				}
				return true;*/
		}
		return false;
	}

	public static boolean executeGMCommand(MapleClient c, String[] sub){
		MapleCharacter player = c.getPlayer();
		Channel cserv = c.getChannelServer();
		// Server srv = Server.getInstance();
		/*if(sub[0].equals("stun")){
			MapleCharacter victim = c.getWorldServer().getCharacterByName(sub[1]);
			victim.setStunned(true);
			MobSkillFactory.getMobSkill(123, 3).getEffect().applyTo(victim);
		}else if(sub[0].equals("unstun")){
			MapleCharacter victim = c.getWorldServer().getCharacterByName(sub[1]);
			victim.setStunned(false);
			victim.cancelBuffStats(MapleBuffStat.STUN);
		}else */if(sub[0].equals("stunmap")){
			player.getMap().setStunned(true);
			for(MapleCharacter chrs : player.getMap().getCharacters()){
				if(!chrs.isGM()){
					MobSkillFactory.getMobSkill(123, 3).getEffect().applyTo(chrs);
					chrs.setStunned(true);
				}
			}
		}else if(sub[0].equals("unstunmap")){
			player.getMap().setStunned(false);
			for(MapleCharacter players : player.getMap().getCharacters()){
				players.setStunned(false);
				players.cancelBuffStats(MapleBuffStat.STUN);
			}
		}else if(sub[0].equals("seducemap")){
			player.getMap().setSeduced(true);
			int way = Integer.parseInt(sub[1]);
			for(MapleCharacter chrs : player.getMap().getCharacters()){
				if(!chrs.isGM()){
					chrs.setSeduced(true);
					chrs.getClient().announce(MaplePacketCreator.cancelChair(-1));
					chrs.getMap().broadcastMessage(chrs, MaplePacketCreator.showChair(chrs.getId(), 0), false);
					MobSkillFactory.getMobSkill(128, way).getEffect().applyTo(chrs);
				}
			}
		}else if(sub[0].equals("unseducemap")){
			player.getMap().setSeduced(false);
			for(MapleCharacter players : player.getMap().getCharacters()){
				players.setSeduced(false);
				players.cancelBuffStats(MapleBuffStat.SEDUCE);
			}
		}else if(sub[0].equals("checkitems")){
			for(MapleCharacter players : player.getMap().getCharacters()){
				int quantity = players.getItemQuantity(Integer.parseInt(sub[1]), false);
				player.dropMessage(5, players.getName() + ": " + quantity);
			}
		}else if(sub[0].equals("inmap")){
			String s = "";
			for(MapleCharacter chr : player.getMap().getCharacters()){
				s += chr.getName() + " ";
			}
			player.message(s);
		}else if(sub[0].equals("go")){
			if(player.getEventInstance() == null){
				if(gotomaps.containsKey(sub[1])){
					MapleMap target = c.getChannelServer().getMap(gotomaps.get(sub[1]));
					MaplePortal targetPortal = target.getPortal(0);
					if(player.getEventInstance() != null){
						player.getEventInstance().removePlayer(player);
					}
					player.changeMap(target, targetPortal);
				}else{
					player.dropMessage(5, "That map does not exist.");
				}
			}else{
				player.dropMessage(5, "You may not use this command while in an event.");
			}
		}else if(sub[0].equals("giftnx")){
			MapleCharacter victim = cserv.getChannelServer().getCharacterByName(sub[1]);
			if(victim != null){
				int amt = Integer.parseInt(sub[2]);
				victim.getCashShop().gainCash(4, amt);
				victim.dropMessage(5, "You have gained " + amt + " NX Cash!");
				player.message("Gave " + amt + " NX Cash to " + victim.getName());
			}else{
				player.dropMessage("The player could not be found.");
			}
		}else if(sub[0].equals("heal")){
			player.setHpMp(30000);
		}else if(sub[0].equals("kill")){
			if(sub.length >= 2){
				MapleCharacter victim = cserv.getChannelServer().getCharacterByName(sub[1]);
				victim.setHpMp(0);
				// Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, player.getName() + " used !kill on " + victim.getName()));
			}
		}else if(sub[0].equals("seed")){
			if(player.getMapId() != 910010000){
				player.yellowMessage("This command can only be used in HPQ.");
				return false;
			}
			Point pos[] = {new Point(7, -207), new Point(179, -447), new Point(-3, -687), new Point(-357, -687), new Point(-538, -447), new Point(-359, -207)};
			int seed[] = {4001097, 4001096, 4001095, 4001100, 4001099, 4001098};
			for(int i = 0; i < pos.length; i++){
				Item item = new Item(seed[i], (byte) 0, (short) 1);
				player.getMap().spawnItemDrop(player, player, item, pos[i], false, true);
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				}
			}
		}else if(sub[0].equals("killall")){
			List<MapleMonster> monsters = player.getMap().getMonsters();
			MapleMap map = player.getMap();
			for(MapleMonster monster : monsters){
				if(!monster.getStats().isFriendly()){
					map.killMonster(monster, player, true);
					monster.giveExpToCharacter(player, (int) (monster.getExp() * c.getPlayer().getStats().getExpRate()), true, 1);
				}
				if(sub.length > 1){
					c.getPlayer().getMap().killFriendlies(monster);
				}
			}
			player.dropMessage("Killed " + monsters.size() + " monsters.");
		}else if(sub[0].equals("monsterdebug")){
			List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
			for(MapleMapObject monstermo : monsters){
				MapleMonster monster = (MapleMonster) monstermo;
				player.message("Monster ID: " + monster.getId());
			}
		}else if(sub[0].equals("unbug")){
			c.getPlayer().getMap().broadcastMessage(CWvsContext.enableActions());
		}else if(sub[0].equals("level")){
			player.setLevel(Integer.parseInt(sub[1]) - 1);
			player.gainExp(new ExpProperty(ExpGainType.COMMAND).gain(-player.getExp()));
			player.levelUp(false);
		}else if(sub[0].equals("maxstat")){
			final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
			executeGMCommand(c, s);
			player.setLevel(255);
			player.setMaxHp(30000);
			player.setMaxMp(30000);
			player.updateSingleStat(MapleStat.LEVEL, 255);
			player.updateSingleStat(MapleStat.MAXHP, 30000);
			player.updateSingleStat(MapleStat.MAXMP, 30000);
		}else if(sub[0].equals("maxskills")){
			for(MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()){
				try{
					Skill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
					if(GameConstants.is_correct_job_for_skill_root(player.getJob().getId(), skill.getId() / 10000)){
						player.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
					}
				}catch(NumberFormatException nfe){
					break;
				}catch(NullPointerException npe){
					continue;
				}
			}
		}else if(sub[0].equals("mesos")){
			player.gainMeso(Integer.parseInt(sub[1]), true);
		}else if(sub[0].equals("adminnote")){
			if(sub[1].equals("map")){
				player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(7, joinStringFrom(sub, 2), 9010000));
			}else{
				// Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(7, joinStringFrom(sub, 1), 9010000));
			}
		}else if(sub[0].equals("openportal")){
			player.getMap().getPortal(sub[1]).setPortalState(true);
		}else if(sub[0].equals("closeportal")){
			player.getMap().getPortal(sub[1]).setPortalState(false);
		}else if(sub[0].equals("meffect")){
			player.getMap().startMapEffect(joinStringFrom(sub, 3), Integer.parseInt(sub[1]), Integer.parseInt(sub[2]));
		}else if(sub[0].equals("starttag")){
			if(player.getMap().getMapTag()){
				player.setTag(false);
				player.getMap().setMapTag(false);
				player.dropMessage(6, "You have ended MapleTag.");
			}else{
				player.setTag(true);
				player.getMap().setMapTag(true);
				player.dropMessage(6, "You have started MapleTag.");
			}
		}else if(sub[0].equals("startevent")){
			for(MapleCharacter chr : player.getMap().getCharacters()){
				player.getMap().startEvent(chr);
			}
			c.getChannelServer().setEvent(null);
		}else if(sub[0].equals("scheduleevent")){
			int players = 50;
			if(sub.length > 1){
				players = Integer.parseInt(sub[1]);
			}
			// c.getChannelServer().setEvent(new MapleEvent(player.getMapId(), players));
			player.dropMessage(5, "The event has been set on " + player.getMap().getMapData().getMapName() + " and will allow " + players + " players to join.");
			if(sub.length > 2){
				// Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[Event] An event has started in Channel " + player.getClient().getChannel() + "! Use @join to participate!"));
			}
		}else if(sub[0].equals("endevent")){
			c.getChannelServer().setEvent(null);
			if(sub.length > 1){
				if(Integer.parseInt(sub[1]) > 0){
					if(player.getMapId() == 109030401){ // Ola Ola
						player.dropMessage("Portal turned off.");
						player.getMap().getPortal("join00").setPortalStatus(false);
					}
					if(player.getMapId() == 109010000){
						player.dropMessage("Portal turned off.");
						player.getMap().getPortal("join00").setPortalStatus(false);
					}
				}
			}
			if(sub.length > 2){
				// Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[Event] Event entry has closed. Good luck to all who are participating!"));
			}
			player.dropMessage(5, "You have ended the event. No more players may join.");
		}else if(sub[0].equals("box")){
			MapleMonster monster = MapleLifeFactory.getMonster(9500365);
			if(monster == null) return true;
			if(sub.length > 1){
				monster.setHp(Integer.parseInt(sub[1]));
			}
			player.getMap().spawnMonsterOnGroundBelow(monster, player.getPosition());
		}else if(sub[0].equals("boxevent")){ // STILL TESTING
			int[] townMaps = {100000000, 101000000, 102000000, 103000000, 104000000, 105040300, 110000000, 120000000, // Victoria Island
			        130000000, 140000000, // Ereve, Rien
			        200000000, 211000000, 220000000, 221000000, 222000000, 230000000, 240000000, 250000000, 251000000, 260000000, 261000000, 270000000};
			MapleMonster monster = MapleLifeFactory.getMonster(9500365);
			if(monster == null) return true;
			if(sub.length > 1){
				monster.setHp(Integer.parseInt(sub[1]));
			}
			String s = "";
			for(int i = 0; i < 2; i++){
				int chosen = (int) Math.floor(Math.random() * townMaps.length);
				if(chosen == 1){
					continue;
				}
				MapleMap townMap = player.getClient().getChannelServer().getMap(townMaps[chosen]);
				int bleft = townMap.getMapData().getFootholds().getMinDropX();
				int bright = townMap.getMapData().getFootholds().getMaxDropX();
				int btop = townMap.getMapData().getVRTop();
				int bbottom = townMap.getMapData().getVRBottom();
				int x = Randomizer.rand(0, 2000);
				int y = Randomizer.rand(-50, 50);
				if(bleft == bright){
					x = Randomizer.rand(bleft + 100, bright - 100);
					y = Randomizer.rand(btop + 200, bbottom - 300);
				}
				s += townMaps[chosen] + ": " + monster.getPosition() + ", ";
				player.dropMessage(5, "Chosen town maps: " + s);
				townMap.spawnMonsterOnGroundBelow(monster, new Point(x, y));
				townMaps[chosen] = 1;
			}
		}/*else if(sub[0].equals("online")){
		 int total = 0;
		 for(Channel ch : srv.getChannelsFromWorld(player.getWorld())){
		 int size = ch.getPlayerStorage().getAllCharacters().size();
		 total += size;
		 String s = "(Channel " + ch.getId() + " Online: " + size + ") : ";
		 if(ch.getPlayerStorage().getAllCharacters().size() < 50){
		 	for(MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()){
		 		s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
		 	}
		 	player.dropMessage(s.substring(0, s.length() - 2));
		 }
		 }
		 player.dropMessage("There are a total of " + total + " players online.");
		 }*/else if(sub[0].equalsIgnoreCase("search") || sub[0].equalsIgnoreCase("find")){
			boolean find = sub[0].equalsIgnoreCase("find");
			StringBuilder sb = new StringBuilder();
			if(sub.length > 2){
				String search = joinStringFrom(sub, 2);
				long start = System.currentTimeMillis();// for the lulz
				if(!sub[1].equalsIgnoreCase("ITEM")){
					MapleData data = null;
					MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
					if(sub[1].equalsIgnoreCase("NPC")){
						data = dataProvider.getData("Npc.img");
					}else if(sub[1].equalsIgnoreCase("MOB") || sub[1].equalsIgnoreCase("MONSTER")){
						data = dataProvider.getData("Mob.img");
					}else if(sub[1].equalsIgnoreCase("SKILL")){
						data = dataProvider.getData("Skill.img");
					}else if(sub[1].equalsIgnoreCase("MAP")){
						sb.append("#bUse the '/m' command to find a map. If it finds a map with the same name, it will warp you to it.");
					}else{
						sb.append("#bInvalid search.\r\nSyntax: '/search [type] [name]', where [type] is NPC, ITEM, MOB, or SKILL.");
					}
					if(data != null){
						String name;
						for(MapleData searchData : data.getChildren()){
							name = MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
							if(name.toLowerCase().contains(search.toLowerCase())){
								sb.append("#b").append(Integer.parseInt(searchData.getName())).append("#k - #r").append(name).append("\r\n");
							}
						}
					}
					dataProvider = null;
					data = null;
				}else{
					for(Pair<Integer, String> itemPair : ItemInformationProvider.getInstance().getItemDataByName(search)){
						if(sb.length() < 32654){// ohlol
							if(itemPair.getLeft() >= 1000000 && ItemInformationProvider.getInstance().isItemValid(itemPair.getLeft())){
								sb.append("#b");
								if(!find){
									sb.append("#i");
									sb.append(StringUtil.getLeftPaddedStr(String.valueOf(itemPair.getLeft()), Character.forDigit(0, 10), 7));
									sb.append(":#");
									sb.append("#L");
									sb.append(itemPair.getLeft());
									sb.append("#");
								}
								sb.append("#b" + itemPair.getLeft());
								sb.append("#k - #r").append(itemPair.getRight());
								if(!find){
									sb.append("#l");
								}
								sb.append("\r\n");
							}
						}else{
							sb.setLength(Short.MAX_VALUE - 200);
							sb.append("#bCouldn't load all items, there are too many results.\r\n");
							break;
						}
					}
				}
				if(sb.length() == 0){
					sb.append("#bNo ").append(sub[1].toLowerCase()).append("s found.\r\n");
				}
				sb.append("\r\n#kLoaded within ").append((double) (System.currentTimeMillis() - start) / 1000).append(" seconds.");// because I can, and it's free
			}else{
				sb.append("#bInvalid search.\r\nSyntax: '/search [type] [name]', where [type] is NPC, ITEM, MOB, or SKILL.");
			}
			if(!find){
				NPCScriptManager.getInstance().start(c, 9010000, "searchitem", "start", sb.toString());
			}else c.announce(MaplePacketCreator.onAskMenu(NPCConversationManager.NpcReplayedByNpc, 9010000, (byte) 0, sb.toString()));
		}else if(sub[0].equals("give")){
			MapleCharacter victim = cserv.getChannelServer().getCharacterByName(sub[1]);
			int itemId = Integer.valueOf(sub[2]);
			short quantity = (short) getOptionalIntArg(sub, 3, 1);
			Item item = new Item(itemId, quantity);
			item.setOwner(player.getName());
			item.setExpiration(-1);
			if(victim.canHoldItem(item)){
				MapleInventoryManipulator.addFromDrop(c, item, true);
				player.dropMessage("Item given.");
				victim.dropMessage(player.getName() + " gave you an item. Please check your inventory for it.");
			}else{
				player.dropMessage("The item was not given. Make sure they have enough room.");
			}
		}else if(sub[0].equals("giveallonline")){
			int itemId = Integer.valueOf(sub[1]);
			short quantity = (short) getOptionalIntArg(sub, 2, 1);
			final Item item = new Item(itemId, (byte) 0, quantity);
			item.setOwner(player.getName());
			for(MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()){
				MapleInventoryManipulator.addFromDrop(chr.getClient(), item, true);
			}
			player.dropMessage("Done.");
		}else if(sub[0].equals("giveallmap")){
			int itemId = Integer.valueOf(sub[1]);
			short quantity = (short) getOptionalIntArg(sub, 2, 1);
			final Item item = new Item(itemId, (byte) 0, quantity);
			item.setOwner(player.getName());
			for(MapleCharacter chr : player.getMap().getCharacters()){
				MapleInventoryManipulator.addFromDrop(chr.getClient(), item, true);
			}
			player.dropMessage("Done.");
		}else if(sub[0].equals("healmap")){
			for(MapleCharacter mch : player.getMap().getCharacters()){
				if(mch != null){
					mch.setHp(mch.getMaxHp());
					mch.updateSingleStat(MapleStat.HP, mch.getMaxHp());
					mch.setMp(mch.getMaxMp());
					mch.updateSingleStat(MapleStat.MP, mch.getMaxMp());
				}
			}
		}else if(sub[0].equals("clock")){
			int time = Integer.parseInt(sub[1]); // seconds
			player.getMap().broadcastMessage(MaplePacketCreator.getClock(time));
			if(sub.length > 2){
				TimerManager.getInstance().schedule("clock", new Runnable(){

					@Override
					public void run(){
						player.getMap().setStunned(true);
						for(MapleCharacter chrs : player.getMap().getCharacters()){
							if(!chrs.isGM()){
								MobSkillFactory.getMobSkill(123, 3).getEffect().applyTo(chrs);
							}
						}
						player.getMap().broadcastMessage(MaplePacketCreator.removeClock());
					}
				}, time * 1000);
			}
		}else if(sub[0].equals("killtoleft") || sub[0].equalsIgnoreCase("killtoright")){
			boolean left = sub[0].equalsIgnoreCase("killtoleft");
			boolean kill = false;
			for(MapleCharacter mch : c.getPlayer().getMap().getCharacters()){
				if(mch.getPosition().getX() == c.getPlayer().getPosition().getX()){
					kill = false;
				}else if((mch.getPosition().getX() < c.getPlayer().getPosition().getX()) && left){
					kill = true;
				}else if((mch.getPosition().getX() > c.getPlayer().getPosition().getX()) && !left){
					kill = true;
				}
				if(kill){
					mch.setHp(0);
					mch.setMp(0);
					mch.updateSingleStat(MapleStat.HP, 0);
					mch.updateSingleStat(MapleStat.MP, 0);
				}
			}
		}else if(sub[0].equals("warpsnowball")){
			List<MapleCharacter> chars = new ArrayList<>(player.getMap().getCharacters());
			for(MapleCharacter chr : chars){
				chr.changeMap(109060000, 3 - chr.getTeam());
			}
		}else if(sub[0].equals("setall")){
			final int x = Short.parseShort(sub[1]);
			player.setStr(x);
			player.setDex(x);
			player.setInt(x);
			player.setLuk(x);
			player.updateSingleStat(MapleStat.STR, x);
			player.updateSingleStat(MapleStat.DEX, x);
			player.updateSingleStat(MapleStat.INT, x);
			player.updateSingleStat(MapleStat.LUK, x);
		}else if(sub[0].equals("unban")){
			try{
				try(PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getAccIdByName(sub[1]))){
					p.executeUpdate();
				}
			}catch(Exception e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
				player.message("Failed to unban " + sub[1]);
				return true;
			}
			player.message("Unbanned " + sub[1]);
		}else{
			return false;
		}
		return true;
	}

	private static String joinStringFrom(String arr[], int start){
		StringBuilder builder = new StringBuilder();
		for(int i = start; i < arr.length; i++){
			builder.append(arr[i]);
			if(i != arr.length - 1){
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	public static int getOptionalIntArg(String splitted[], int position, int def){
		if(splitted.length > position){
			try{
				return Integer.parseInt(splitted[position]);
			}catch(NumberFormatException nfe){
				return def;
			}
		}
		return def;
	}

	public static class ShutDownTask extends Thread{

		@Override
		public void run(){
			Commands.shutdown().run();
		}

		public void startShutDown(int timeNeeded){
			ServerConstants.secondsLeft = timeNeeded * 60;
			if(timeNeeded >= 0) ChannelServer.getInstance().broadcastPacket(MaplePacketCreator.serverNotice(0, "Shutting down... in " + timeNeeded + " minute" + (timeNeeded > 1 ? "s" : "") + "."));
			if(timeNeeded > 0){
				for(Channel channel : ChannelServer.getInstance().getChannels()){
					for(MapleCharacter chr : channel.getPlayerStorage().getAllCharacters()){
						chr.getMap().broadcastMessage(MaplePacketCreator.getClock(ServerConstants.secondsLeft));
					}
				}
			}
			ServerConstants.ts = TimerManager.getInstance().register("shutdown", new Runnable(){

				private boolean dced = false;

				@Override
				public void run(){
					if(ServerConstants.secondsLeft == 60 || (ServerConstants.secondsLeft <= 0 && !dced)){
						for(Channel channel : ChannelServer.getInstance().getChannels()){
							for(MapleCharacter chr : channel.getPlayerStorage().getAllCharacters()){
								// chr.getClient().disconnect(false, false);
								// chr.getClient().updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN);
								// chr.getClient().getSession().removeAttribute(MapleClient.CLIENT_KEY); // prevents double dcing during login
								// chr.getClient().getSession().close(true);
								chr.saveToDB();
							}
						}
						dced = true;
					}
					if(ServerConstants.secondsLeft <= 0 && dced){
						start();
						ServerConstants.ts.cancel(false);
						return;
					}
					ServerConstants.secondsLeft--;
				}
			}, 1000);
		}
	}

	public static final Runnable shutdown(){// only once :D
		return new Runnable(){

			@Override
			public void run(){
				System.out.println("Shutting down the server!\r\n");
				try{
					ChannelServer.getInstance().getWorldInterface().saveWorldData();
				}catch(RemoteException | NullPointerException e){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
				}
				try{
					ChannelServer.getInstance().getWorldRegistry().removeChannelServer(ChannelServer.getInstance().getChannelServerID(), ChannelServer.getInstance().getChannelIDs());
				}catch(RemoteException | NullPointerException e){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
				}
				ChannelServer.getInstance().shutdown();
				for(Channel ch : ChannelServer.getInstance().getChannels()){
					while(!ch.finishedShutdown()){
						try{
							System.out.println("Channel: " + ch.getId() + " has " + ch.getConnectedClients() + " clients connected still.");
							Thread.sleep(1000);
						}catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
				/*for(MapleWedding wedding : weddings){
					wedding.saveToDB();
				}*/
				/*queue.interrupt();
				worlds.clear();
				worlds = null;
				channels.clear();
				channels = null;
				worldRecommendedList.clear();
				worldRecommendedList = null;*/
				System.out.println("Channels are offline.");
				System.exit(0);
			}
		};
	}
}
