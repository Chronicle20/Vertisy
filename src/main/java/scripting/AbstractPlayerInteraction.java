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
package scripting;

import java.awt.Point;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import constants.ItemConstants;
import net.channel.ChannelServer;
import net.server.guild.MapleGuild;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventManager;
import scripting.npc.NPCConversationManager;
import scripting.npc.NPCScriptManager;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.expeditions.MapleExpedition;
import server.expeditions.MapleExpeditionType;
import server.life.*;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.SavedLocation;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.partyquest.PartyQuest;
import server.partyquest.Pyramid;
import server.propertybuilder.ExpProperty;
import server.quest.MapleQuest;
import server.reactors.MapleReactor;
import server.reactors.ReactorHitInfo;
import server.shops.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.DropPool;
import tools.packets.field.NpcPool;

public class AbstractPlayerInteraction{

	public MapleClient c;

	public AbstractPlayerInteraction(MapleClient c){
		this.c = c;
	}

	public MapleClient getClient(){
		return c;
	}

	public MapleCharacter getPlayer(){
		return c.getPlayer();
	}

	public void warp(SavedLocation sl){
		if(sl != null) warp(sl.getMapId(), sl.getPortal());
	}

	public void warp(int map){
		getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(0));
	}

	public void warp(int map, int portal){
		getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(portal));
	}

	public void warp(int map, String portal){
		getPlayer().changeMap(getWarpMap(map), getWarpMap(map).getPortal(portal));
	}

	public void warpMap(int map){
		getPlayer().getMap().warpEveryone(map);
	}

	public void warpParty(int id){
		warpParty(id, false, 0);
	}

	public void warpParty(int id, int mapid){
		warpParty(id, true, mapid);
	}

	public void warpParty(int id, boolean inMap, int mapid){
		for(MapleCharacter mc : getPartyMembers()){
			if(id == 925020100){
				mc.setDojoParty(true);
			}
			if(inMap){
				if(mc.getMapId() != mapid) continue;
			}
			mc.changeMap(getWarpMap(id));
		}
		if(id == 925100200){ // 'cuz map scripts suck
			MapleMap thirdMap = getPlayer().getEventInstance().getMapInstance(id);
			for(int i = 0; i < 10; i++){
				MapleMonster mob = MapleLifeFactory.getMonster(9300124);
				MapleMonster mob2 = MapleLifeFactory.getMonster(9300125);
				MapleMonster mob3 = MapleLifeFactory.getMonster(9300124);
				MapleMonster mob4 = MapleLifeFactory.getMonster(9300125);
				thirdMap.spawnMonsterOnGroundBelow(mob, new java.awt.Point(430, 75));
				thirdMap.spawnMonsterOnGroundBelow(mob2, new java.awt.Point(1600, 75));
				thirdMap.spawnMonsterOnGroundBelow(mob3, new java.awt.Point(430, 238));
				thirdMap.spawnMonsterOnGroundBelow(mob4, new java.awt.Point(1600, 238));
			}
		}else if(id == 925100500){
			getPlayer().getEventInstance().registerMonster(MapleLifeFactory.getMonster(9300105));
			getPlayer().getEventInstance().getMapInstance(id).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300105), new Point(750, 238));
		}
	}

	public List<MapleCharacter> getPartyMembers(){
		if(getPlayer().getParty() == null) return null;
		List<MapleCharacter> chars = new LinkedList<>();
		for(MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
			MapleCharacter chr = mpc.getPlayerInChannel();
			if(chr != null) chars.add(chr);
		}
		return chars;
	}

	public List<MapleCharacter> getPartyMembers(boolean inMap){
		if(getPlayer().getParty() == null) return new ArrayList<>();
		List<MapleCharacter> chars = new LinkedList<>();
		for(MaplePartyCharacter mpc : getPlayer().getParty().getMembers()){
			if(mpc.isOnline()){
				MapleCharacter mc = mpc.getPlayerInChannel();
				if(mc != null){// incase somehow.
					if(inMap){
						if(mc.getMapId() != getPlayer().getMapId()) break;
					}
					chars.add(mc);
				}
			}
		}
		return chars;
	}

	protected MapleMap getWarpMap(int map){
		MapleMap target;
		if(getPlayer().getEventInstance() == null){
			target = c.getChannelServer().getMap(map);
		}else{
			target = getPlayer().getEventInstance().getMapInstance(map);
		}
		return target;
	}

	public MapleMap getMap(int map){
		return getWarpMap(map);
	}

	public MapleMapFactory getMapFactory(){
		return c.getChannelServer().getMapFactory();
	}

	public EventManager getEventManager(String event){
		return getClient().getChannelServer().getEventSM().getEventManager(event);
	}

	// this is cuz some places are still using this method
	public boolean hasItem(int itemid){
		return haveItem(itemid); // so no extra loops
	}

	public boolean hasItem(int itemid, int quantity){
		return haveItem(itemid, quantity);
	}

	public boolean haveItem(int itemid){
		return haveItem(itemid, false);
	}

	public boolean haveItem(int itemid, boolean checkEquipped){
		return getPlayer().haveItem(itemid, checkEquipped); // so no extra loops
	}

	public boolean haveItem(int itemid, int quantity){
		return haveItem(itemid, quantity, false);
	}

	public boolean haveItem(int itemid, int quantity, boolean checkEquipped){
		return getPlayer().getItemQuantity(itemid, checkEquipped) >= quantity;
	}

	public boolean canHold(int itemid){
		return canHold(itemid, (short) 1);
	}

	public boolean canHold(int itemid, short amount){
		return getPlayer().canHoldItem(new Item(itemid, amount));
	}

	public int getNumberOfFreeSlots(byte invType){
		MapleInventoryType type = MapleInventoryType.getByType(invType);
		if(type == null) return 0;
		return c.getPlayer().getInventory(type).getNumFreeSlot();
	}

	public final boolean canHoldMerchantItems(List<Pair<Item, MapleInventoryType>> items){
		List<Item> itemList = new ArrayList<>();
		for(Pair<Item, MapleInventoryType> p : items){
			itemList.add(p.getLeft());
		}
		if(itemList.size() > 0) return getPlayer().canHoldItems(itemList);
		else return true;
	}

	public void openNpc(int npcid){
		openNpc(npcid, null);
	}

	public void openNpc(int npcid, String script){
		c.getPlayer().dispose();
		NPCScriptManager.getInstance().start(c, npcid, script, null);
	}

	public final void openNpc(final MapleClient clt, final int id, final MapleCharacter player){
		clt.getPlayer().dispose();
		NPCScriptManager.getInstance().start(clt, id, player);
	}

	public void openShop(int shopId){
		if(c.getPlayer().getShop() != null) return;
		MapleShopFactory.getInstance().getShop(shopId).sendShop(c);
	}

	public void spawnNpc(int npcId, Point pos){
		MapleNPC npc = MapleLifeFactory.getNPC(npcId);
		if(npc != null){
			npc.setPosition(pos);
			npc.setCy(pos.y);
			npc.setRx0(pos.x + 50);
			npc.setRx1(pos.x - 50);
			npc.setFh(c.getPlayer().getMap().getMapData().getFootholds().findBelow(pos).getId());
			c.getPlayer().getMap().addMapObject(npc);
			c.getPlayer().getMap().broadcastMessage(NpcPool.spawnNPC(npc));
		}
	}

	public final void removeNpc(final int mapid, final int npcId){
		c.getChannelServer().getMap(mapid).removeNpc(npcId);
	}

	public final void removeNpc(final int npcId){
		c.getPlayer().getMap().removeNpc(npcId);
	}

	public void updateQuest(int questid, String data){
		updateQuest(questid, data, 0);
	}

	public void updateQuest(int questid, String data, int progressid){
		MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(questid));
		status.setStatus(MapleQuestStatus.Status.STARTED);
		status.setProgress(progressid, data);// override old if exists
		c.getPlayer().updateQuest(status);
	}

	// since the data is int most of the time :D
	public void updateQuest(int questid, int data){
		updateQuest(questid, data, 0);
	}

	public void updateQuest(int questid, int data, int progressid){
		MapleQuestStatus status = c.getPlayer().getQuest(MapleQuest.getInstance(questid));
		status.setStatus(MapleQuestStatus.Status.STARTED);
		status.setProgress(progressid, data + "");// override old if exists
		c.getPlayer().updateQuest(status);
	}

	public MapleQuestStatus.Status getQuestStatus(int id){
		return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus();
	}

	public boolean isQuestCompleted(int quest){
		try{
			return getQuestStatus(quest) == MapleQuestStatus.Status.COMPLETED;
		}catch(NullPointerException e){
			return false;
		}
	}

	public boolean isQuestStarted(int quest){
		try{
			return getQuestStatus(quest) == MapleQuestStatus.Status.STARTED;
		}catch(NullPointerException e){
			return false;
		}
	}

	public int getQuestProgress(int qid){
		return getQuestProgress(qid, 0);
	}

	public int getQuestProgress(int qid, int progressid){
		try{
			return Integer.parseInt(getPlayer().getQuest(MapleQuest.getInstance(qid)).getProgress(progressid));
		}catch(NumberFormatException ex){
			// Just return a 0 since it isn't there.
			return 0;
		}
	}

	/*
	 *  Horntail: 30000, 30001
	 *  Captain Latanica: 30005, 30006
	 *  Pink Bean: 30010, 30011
	 *  Zakum: 30020, 30021
	 *  Papulatus Clock: 30030, 30031
	 */
	public final MapleQuestStatus getQuestRecord(final short id){
		return c.getPlayer().getQuestNAdd(id);
	}

	public void partyGainItem(int id, short quantity){
		if(getParty() == null){
			gainItem(id, quantity);
		}else{
			for(MapleCharacter mc : getPartyMembers()){
				gainItem(mc, id, quantity, false, false, -1);
			}
		}
	}

	public void gainItemMap(int id, short quantity){
		for(MapleCharacter mc : getPlayer().getMap().getCharacters()){
			gainItem(mc, id, quantity, false, false, -1);
		}
	}

	/*public Item gainItem(boolean checkSpace, int id, short quantity){
		return gainItem(checkSpace, id, quantity, false, true);
	}
	
	public Item gainItem(boolean checkSpace, int id, short quantity, boolean randomStats, boolean showMessage){
		return gainItem(getPlayer(), id, quantity, randomStats, showMessage, -1, checkSpace);
	}*/
	public Item gainItem(MapleCharacter mc, boolean checkSpace, int id, short quantity, boolean randomStats, boolean showMessage){
		return gainItem(mc, id, quantity, randomStats, showMessage, -1, checkSpace);
	}

	public Item gainItem(int id, short quantity){
		return gainItem(id, quantity, false, false);
	}

	public Item gainItem(int id, short quantity, boolean show){// this will fk randomStats equip :P
		return gainItem(id, quantity, false, show);
	}

	public Item gainItem(int id, boolean show){
		return gainItem(id, (short) 1, false, show);
	}

	public Item gainItem(int id){
		return gainItem(id, (short) 1, false, false);
	}

	public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage){
		return gainItem(id, quantity, randomStats, showMessage, -1);
	}

	public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage, long expires){
		return gainItem(c.getPlayer(), id, quantity, randomStats, showMessage, expires);
	}

	public Item gainItem(int id, short quantity, boolean randomStats, boolean showMessage, long expires, boolean checkSpace){
		return gainItem(c.getPlayer(), id, quantity, randomStats, showMessage, expires, checkSpace);
	}

	public Item gainItem(MapleCharacter mc, int id, short quantity, boolean randomStats, boolean showMessage, long expires){
		return gainItem(mc, id, quantity, randomStats, showMessage, expires, true);
	}

	public Item gainItem(MapleCharacter mc, int id, short quantity, boolean randomStats, boolean showMessage, long expires, boolean checkSpace){
		return gainItem(mc, id, quantity, randomStats, showMessage, expires, checkSpace, "Gained item: " + id + " quantity: " + quantity + " randomStats: " + randomStats + " showMessage: " + showMessage + " expires: " + expires);
	}

	public Item gainItem(MapleCharacter mc, int id, short quantity, boolean randomStats, boolean showMessage, long expires, boolean checkSpace, String gainLog){
		Item item = null;
		if(id >= 5000000 && id <= 5000100){
			Item petItem = new Item(id, (short) 1);
			petItem.setPetId(MaplePet.createPet(id));
			petItem.setExpiration(expires == -1 ? -1 : System.currentTimeMillis() + expires);
			MapleInventoryManipulator.addFromDrop(mc.getClient(), petItem, false);
		}
		if(quantity >= 0){
			ItemInformationProvider ii = ItemInformationProvider.getInstance();
			if(ii.getInventoryType(id).equals(MapleInventoryType.EQUIP)){
				item = ii.getEquipById(id);
			}else{
				item = new Item(id, (short) 0, quantity);
			}
			if(expires != -1){
				item.setExpiration(System.currentTimeMillis() + expires);
			}
			if(!c.getPlayer().canHoldItem(item)){
				mc.dropMessage(1, "Your inventory is full. Please remove an item from your " + ii.getInventoryType(id).name() + " inventory.");
				return null;
			}
			if(ii.getInventoryType(id).equals(MapleInventoryType.EQUIP) && !ItemConstants.isRechargable(item.getItemId())){
				if(randomStats){
					item = ii.randomizeStats((Equip) item);
					MapleInventoryManipulator.addFromDrop(mc.getClient(), (Equip) item, false, false);
				}else{
					MapleInventoryManipulator.addFromDrop(mc.getClient(), (Equip) item, false, false);
				}
			}else{
				MapleInventoryManipulator.addFromDrop(mc.getClient(), item, false, false);
			}
		}else{
			MapleInventoryManipulator.removeById(mc.getClient(), ItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
		}
		if(showMessage){
			mc.getClient().announce(UserLocal.UserEffect.getShowItemGain(id, quantity, true));
		}
		mc.getStats().recalcLocalStats(mc);
		return item;
	}

	public void changeMusic(String songName){
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
	}

	public void playerMessage(int type, String message){
		c.announce(MaplePacketCreator.serverNotice(type, message));
	}

	public void message(String message){
		getPlayer().message(message);
	}

	public void mapMessage(int type, String message){
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
	}

	public void mapEffect(String path){
		c.announce(MaplePacketCreator.mapEffect(path));
	}

	public void mapSound(String path){
		c.announce(MaplePacketCreator.mapSound(path));
	}

	public void displayAranIntro(){
		String intro = "";
		switch (c.getPlayer().getMapId()){
			case 914090010:
				intro = "Effect/Direction1.img/aranTutorial/Scene0";
				break;
			case 914090011:
				intro = "Effect/Direction1.img/aranTutorial/Scene1" + (c.getPlayer().getGender() == 0 ? "0" : "1");
				break;
			case 914090012:
				intro = "Effect/Direction1.img/aranTutorial/Scene2" + (c.getPlayer().getGender() == 0 ? "0" : "1");
				break;
			case 914090013:
				intro = "Effect/Direction1.img/aranTutorial/Scene3";
				break;
			case 914090100:
				intro = "Effect/Direction1.img/aranTutorial/HandedPoleArm" + (c.getPlayer().getGender() == 0 ? "0" : "1");
				break;
			case 914090200:
				intro = "Effect/Direction1.img/aranTutorial/Maha";
				break;
		}
		showIntro(intro);
	}

	public void showIntro(String path){
		c.announce(UserLocal.UserEffect.showIntro(path));
	}

	public void showInfo(String path){
		c.announce(UserLocal.UserEffect.showInfo(path));
		c.announce(CWvsContext.enableActions());
	}

	public void guildMessage(int type, String message){
		try{
			ChannelServer.getInstance().getWorldInterface().guildMessage(getPlayer().getGuildId(), MaplePacketCreator.serverNotice(type, message));
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public MapleGuild getGuild(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getGuild(getPlayer().getGuildId(), null);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			return null;
		}
	}

	public MapleGuild getGuildIfExists(){
		try{
			return ChannelServer.getInstance().getWorldInterface().getGuildIfExists(getPlayer().getGuildId());
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			return null;
		}
	}

	public boolean hasGuild(){
		return getPlayer().getGuildId() > 0;
	}

	public boolean isInParty(){
		return getPlayer().isInParty();
	}

	public MapleParty getParty(){
		return getPlayer().getParty();
	}

	public MapleCharacter getPartyLeaderChar(){
		if(getPlayer().isInParty()/* && getParty().getLeader() != null*/){ // the leader null checking is commented until needed.
			return getParty().getLeader().getPlayerInChannel();
		}
		return null;
	}

	public boolean isGM(){
		return getPlayer().isGM();
	}

	public boolean isLeader(){
		if(getParty() == null) return false;
		return getParty().getLeader().equals(getPlayer().getMPC());
	}

	public void givePartyItems(int id, short quantity, List<MapleCharacter> party){
		for(MapleCharacter chr : party){
			MapleClient cl = chr.getClient();
			if(quantity >= 0){
				gainItem(chr, id, quantity, false, true, -1);
			}else{
				MapleInventoryManipulator.removeById(cl, ItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, true);
			}
			cl.announce(UserLocal.UserEffect.getShowItemGain(id, quantity, true));
		}
	}

	public void removeHPQItems(){
		int[] items = {4001095, 4001096, 4001097, 4001098, 4001099, 4001100, 4001101};
		for(int i = 0; i < items.length; i++){
			removePartyItems(items[i]);
		}
	}

	public void removePartyItems(int id){
		if(getParty() == null){
			removeAll(id);
			return;
		}
		for(MaplePartyCharacter mpc : getParty().getMembers()){
			if(mpc != null && mpc.isOnline()){
				MapleCharacter chr = mpc.getPlayerInChannel();
				if(chr != null) removeAll(id, chr.getClient());
			}
		}
	}

	public void givePartyExp(int amount, List<MapleCharacter> party){
		for(MapleCharacter chr : party){
			gainExp(chr, amount, chr.getStats().getExpRate(), true, "givePartyExp - Base: " + amount);
		}
	}

	public void givePartyQuestExp(String PQ){
		givePartyQuestExp(PQ, true);
	}

	public void givePartyQuestExp(String PQ, boolean instance){
		givePartyQuestExp(0, PQ, instance);
	}

	public void givePartyQuestExp(int base, boolean instance){
		givePartyQuestExp(base, null, instance);
	}

	public void givePartyQuestExp(int base, String PQ, boolean instance){
		// 1 player = 0% bonus (100)
		// 2 players = 0% bonus (100)
		// 3 players = +0% bonus (100)
		// 4 players = +10% bonus (110)
		// 5 players = +20% bonus (120)
		// 6 players = +30% bonus (130)
		MapleParty party = getPlayer().getParty();
		/*int size = party.getMembers().size();
		if(instance){
			for(MaplePartyCharacter member : party.getMembers()){
				if(member == null || !member.isOnline() || member.getPlayer().getEventInstance() == null){
					size--;
				}
			}
		}*/
		// int bonus = size < 4 ? 100 : 70 + (size * 10);
		// int bonus = size < 4 ? 0 : (size * 10);
		for(MaplePartyCharacter member : party.getMembers()){
			if(member == null || !member.isOnline()){
				continue;
			}
			MapleCharacter player = member.getPlayerInChannel();
			if(player != null){
				if(instance && player.getEventInstance() == null){
					continue; // They aren't in the instance, don't give EXP.
				}
				int expBase = base;
				if(PQ != null && PQ.length() > 0) expBase = PartyQuest.getExp(PQ, player.getLevel());
				// int exp = (int) (expBase * player.getStats().getExpRate());
				// exp = exp * bonus / 100;
				ExpProperty property = new ExpProperty(ExpGainType.PARTYQUEST).gain(expBase).inChat().show(true);
				// property.party(exp * bonus / 100);
				gainExp(player, property, "givePartyQuestExp - PQ: " + PQ + " Exp: " + expBase);
				// gainExp(player, exp, ServerConstants.PQ_BONUS_EXP_MOD, true, "givePartyQuestExp - PQ: " + PQ + " Base: " + base);
			}
		}
	}

	public void gainExp(int exp){
		gainExp(exp);
	}

	public void gainExp(int exp, boolean show){
		gainExp(exp, show, "");
	}

	public void gainExp(int exp, boolean show, String logData){
		gainExp(exp, 1D, show, logData);
	}

	public void gainExp(int exp, double multiplier, boolean show, String logData){
		gainExp(getPlayer(), exp, multiplier, show, logData);
	}

	public void gainExp(MapleCharacter player, int exp, double multiplier, boolean show, String logData){
		if(player.getEventInstance() != null){
			logData += " Event Instance: " + player.getEventInstance().getName();
		}
		ExpProperty property = new ExpProperty(ExpGainType.SCRIPT).gain((int) (exp * multiplier)).inChat().show(show);
		gainExp(player, property, logData);
	}

	public void gainExp(MapleCharacter player, ExpProperty expProperty, String logData){
		player.gainExp(expProperty.inChat().logData(logData));
	}

	public void removeFromParty(int id, List<MapleCharacter> party){
		for(MapleCharacter chr : party){
			MapleInventoryType type = ItemInformationProvider.getInstance().getInventoryType(id);
			MapleInventory iv = chr.getInventory(type);
			int possesed = iv.countById(id);
			if(possesed > 0){
				MapleInventoryManipulator.removeById(c, ItemInformationProvider.getInstance().getInventoryType(id), id, possesed, true, true);
				chr.announce(UserLocal.UserEffect.getShowItemGain(id, (short) -possesed, true));
			}
		}
	}

	public int removeAll(int id){
		return removeAll(id, c);
	}

	public int removeAll(int id, MapleClient cl){
		MapleInventoryType invType = ItemInformationProvider.getInstance().getInventoryType(id);
		int possessed = cl.getPlayer().getInventory(invType).countById(id);
		if(possessed > 0){
			MapleInventoryManipulator.removeById(cl, ItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, true);
			cl.announce(UserLocal.UserEffect.getShowItemGain(id, (short) -possessed, true));
		}
		if(invType == MapleInventoryType.EQUIP){
			if(cl.getPlayer().getInventory(MapleInventoryType.EQUIPPED).countById(id) > 0){
				MapleInventoryManipulator.removeById(cl, MapleInventoryType.EQUIPPED, id, 1, true, true);
				cl.announce(UserLocal.UserEffect.getShowItemGain(id, (short) -1, true));
				possessed++;
			}
		}
		return possessed;
	}

	public int getMapId(){
		return c.getPlayer().getMap().getId();
	}

	public int getPlayerCount(int mapid){
		return c.getChannelServer().getMap(mapid).getCharacters().size();
	}

	public void showInstruction(String msg, int width, int height){
		c.announce(MaplePacketCreator.sendHint(msg, width, height));
		c.announce(CWvsContext.enableActions());
	}

	public void disableMinimap(){
		c.announce(MaplePacketCreator.disableMinimap());
	}

	public final void forceStartReactor(final int mapid, final int id){
		MapleMap map = c.getChannelServer().getMap(mapid);
		MapleReactor react;
		for(final MapleMapObject remo : map.getAllReactor()){
			react = (MapleReactor) remo;
			if(react.getId() == id){
				react.runEvents(c, new ReactorHitInfo());
				break;
			}
		}
	}

	public boolean isAllReactorState(final int reactorId, final int state){ // Use for Thief Room CWKPQ
		boolean ret = true;
		for(MapleReactor reactor : getPlayer().getEventInstance().getMapInstance(getPlayer().getMapId()).getAllReactor()){
			if(reactor.getId() == reactorId){
				if(reactor.getCurrStateAsByte() != state) ret = false;
			}
		}
		return ret;
	}

	public void resetMap(int mapid){
		getMap(mapid).resetReactors();
		getMap(mapid).killAllMonsters();
		for(MapleMapObject i : getMap(mapid).getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))){
			getMap(mapid).removeMapObject(i);
			getMap(mapid).broadcastMessage(DropPool.removeItemFromMap(i.getObjectId(), 0, c.getPlayer().getId()));
		}
	}

	public void sendClock(MapleClient d, int time){
		d.announce(MaplePacketCreator.getClock((int) (time - System.currentTimeMillis()) / 1000));
	}

	public void useItem(int id){
		ItemInformationProvider.getInstance().getItemData(id).itemEffect.applyTo(c.getPlayer());
		c.announce(CWvsContext.OnMessage.getItemMessage(id));// Useful shet :3
	}

	public void useItem(int id, MapleCharacter chr){
		ItemInformationProvider.getInstance().getItemData(id).itemEffect.applyTo(chr);
		c.announce(CWvsContext.OnMessage.getItemMessage(id));// Useful shet :3
	}

	public void cancelItem(final int id){
		getPlayer().cancelEffect(ItemInformationProvider.getInstance().getItemData(id).itemEffect, false, -1);
	}

	public void teachSkill(int skillid){
		teachSkill(skillid, (byte) 1, (byte) SkillFactory.getSkill(skillid).getMaxLevel());
	}

	public void teachSkill(int skillid, byte level, byte masterLevel){
		teachSkill(skillid, level, masterLevel, -1);
	}

	public void teachSkill(int skillid, byte level, byte masterLevel, long expiration){
		getPlayer().changeSkillLevel(SkillFactory.getSkill(skillid), level, masterLevel, expiration);
	}

	public void removeEquipFromSlot(short slot){
		Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		MapleInventoryManipulator.removeItem(c, MapleInventoryType.EQUIPPED, slot, tempItem.getQuantity(), true, false);
	}

	public void gainAndEquip(int itemid, short slot){
		final Item old = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
		if(old != null){
			MapleInventoryManipulator.removeItem(c, MapleInventoryType.EQUIPPED, slot, old.getQuantity(), true, false);
		}
		final Item newItem = ItemInformationProvider.getInstance().getEquipById(itemid);
		newItem.setPosition(slot);
		c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(newItem);
		c.announce(MaplePacketCreator.modifyInventory(false, Collections.singletonList(new ModifyInventory(0, newItem))));
	}

	public void spawnMonster(int id, int x, int y){
		MapleMonster monster = MapleLifeFactory.getMonster(id);
		monster.setPosition(new Point(x, y));
		getPlayer().getMap().spawnMonster(monster);
	}

	public MapleMonster getMonsterLifeFactory(int mid){
		return MapleLifeFactory.getMonster(mid);
	}

	public void spawnGuide(){
		c.announce(MaplePacketCreator.spawnGuide(true));
	}

	public void removeGuide(){
		c.announce(MaplePacketCreator.spawnGuide(false));
	}

	public void displayGuide(int num){
		c.announce(UserLocal.UserEffect.showInfo("UI/tutorial.img/" + num));
	}

	public void goDojoUp(){
		c.announce(MaplePacketCreator.dojoWarpUp());
	}

	public void enableActions(){
		c.announce(CWvsContext.enableActions());
	}

	public void showEffect(String effect){
		c.announce(MaplePacketCreator.showEffect(effect));
	}

	public void dojoEnergy(){
		c.announce(MaplePacketCreator.getEnergy("energy", getPlayer().getDojoEnergy()));
	}

	public void talkGuide(String message){
		c.announce(MaplePacketCreator.talkGuide(message));
	}

	public void guideHint(int hint){
		c.announce(MaplePacketCreator.guideHint(hint));
	}

	public void updateAreaInfo(Short area, String info){
		c.getPlayer().updateAreaInfo(area, info);
		c.announce(CWvsContext.enableActions());// idk, nexon does the same :P
	}

	public boolean containsAreaInfo(short area, String info){
		return c.getPlayer().containsAreaInfo(area, info);
	}

	public MobSkill getMobSkill(int skill, int level){
		return MobSkillFactory.getMobSkill(skill, level);
	}

	public void earnTitle(String msg){
		c.announce(MaplePacketCreator.earnTitleMessage(msg));
	}

	public void showInfoText(String msg){
		c.announce(CWvsContext.OnMessage.showInfoText(msg));
	}

	public void openUI(byte ui){
		c.announce(MaplePacketCreator.openUI(ui));
	}

	public void lockUI(){
		c.announce(UserLocal.disableUI(true));
		c.announce(UserLocal.lockUI(true));
	}

	public void unlockUI(){
		c.announce(UserLocal.disableUI(false));
		c.announce(UserLocal.lockUI(false));
	}

	public void playSound(String sound){
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(sound, 4));
	}

	public void environmentChange(String env, int mode){
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.environmentChange(env, mode));
	}

	public Pyramid getPyramid(){
		return (Pyramid) getPlayer().getPartyQuest();
	}

	public void createExpedition(MapleExpeditionType type){
		MapleExpedition exped = new MapleExpedition(getPlayer(), type);
		getPlayer().getClient().getChannelServer().getExpeditions().add(exped);
	}

	public void endExpedition(MapleExpedition exped){
		exped.dispose(true);
		getPlayer().getClient().getChannelServer().getExpeditions().remove(exped);
	}

	public MapleExpedition getExpedition(MapleExpeditionType type){
		for(MapleExpedition exped : getPlayer().getClient().getChannelServer().getExpeditions()){
			if(exped.getType().equals(type)) return exped;
		}
		return null;
	}

	public void summonPepeKing(){
		getPlayer().getClient().getChannelServer().getMap(getPlayer().getMapId()).clearAndReset(true);
		int rand = Randomizer.nextInt(10);
		int mob_ToSpawn = 100100;
		if(rand >= 6){ // 60%
			mob_ToSpawn = 3300007;
		}else if(rand >= 3){
			mob_ToSpawn = 3300006;
		}else{
			mob_ToSpawn = 3300005;
		}
		getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mob_ToSpawn), getPlayer().getPosition());
	}

	public long getCurrentTime(){
		return System.currentTimeMillis();
	}

	public String getReadableMillis(long startMillis, long endMillis){
		return StringUtil.getReadableMillis(startMillis, endMillis);
	}

	public String convertToString(int data){
		return String.valueOf(data);
	}

	public String convertToString(long data){
		return String.valueOf(data);
	}

	public boolean checkForAllJobs(MapleCharacter c){
		boolean warrior = false;
		boolean mage = false;
		boolean archer = false;
		boolean thief = false;
		boolean pirate = false;
		for(MaplePartyCharacter player : c.getParty().getMembers()){
			MapleCharacter chr = player.getPlayerInChannel();
			if(chr != null){
				if(chr.getJob().getId() / 100 == 1){
					warrior = true;
				}
				if(chr.getJob().getId() / 100 == 2){
					mage = true;
				}
				if(chr.getJob().getId() / 100 == 3){
					archer = true;
				}
				if(chr.getJob().getId() / 100 == 4){
					thief = true;
				}
				if(chr.getJob().getId() / 100 == 5){
					pirate = true;
				}
			}
		}
		return(warrior && mage && archer && thief && pirate);
	}

	public Pair<Integer, Integer> makePair(int x, int y){
		return new Pair<>(x, y);
	}

	public int getCash(){
		return getCash(GameConstants.MAIN_NX_TYPE);
	}

	public int getCash(int type){
		return getPlayer().getCashShop().getCash(type);
	}

	public void gainCash(int amount){
		gainCash(GameConstants.MAIN_NX_TYPE, amount);
	}

	public void gainCashParty(int amount){
		if(getPlayer().isInParty()){
			for(MaplePartyCharacter mpc : getParty().getMembers()){
				if(mpc.isOnline()){
					MapleCharacter chr = mpc.getPlayerInChannel();
					if(chr != null) chr.getCashShop().gainCash(4, amount);
				}
			}
		}
	}

	public void gainCashMap(int amount){
		for(MapleCharacter mc : getPlayer().getMap().getCharacters()){
			mc.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, amount);
		}
	}

	public void gainCash(MapleCharacter mc, int amount){
		mc.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, amount);
	}

	public void gainCash(int type, int amount){
		getPlayer().getCashShop().gainCash(type, amount);
	}

	public void gainRSSkillExp(String skillName, long amount){
		getPlayer().gainRSSkillExp(RSSkill.valueOf(skillName), amount);
	}

	public void gainRSSkillLevel(String skillName, byte level){
		getPlayer().gainRSSkillLevel(RSSkill.valueOf(skillName), level);
	}

	public long getRSSkillExp(String skillName){
		return getPlayer().getRSSkillExp(RSSkill.valueOf(skillName));
	}

	public byte gainRSSkillLevel(String skillName){
		return getPlayer().getRSSkillLevel(RSSkill.valueOf(skillName));
	}

	public byte getRSSkillLevel(String skillName){
		return getPlayer().getRSSkillLevel(RSSkill.valueOf(skillName));
	}

	public String getFormattedInt(long value){
		return NumberFormat.getInstance().format(value);
	}

	public void println(String text){
		System.out.println(text);
	}

	public ThreadLocalRandom getRandom(){
		return ThreadLocalRandom.current();
	}

	public void gainGP(int amount){
		try{
			ChannelServer.getInstance().getWorldInterface().gainGP(getPlayer().getGuildId(), getPlayer().getId(), amount);
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	public int getSkillLevel(int id){
		return getPlayer().getSkillLevel(SkillFactory.getSkill(id));
	}

	public boolean forceCompleteQuest(int id){
		return forceCompleteQuest(id, 0);
	}

	public boolean forceCompleteQuest(int id, int npc){
		return MapleQuest.getInstance(id).forceComplete(getPlayer(), npc);
	}

	public boolean forceStartQuest(int quest){
		return forceStartQuest(quest, 0);
	}

	public boolean forceStartQuest(int quest, int npc){
		return MapleQuest.getInstance(quest).forceStart(getPlayer(), npc);
	}

	public void sendImage(String sPath){
		getClient().announce(MaplePacketCreator.onSayImage(NPCConversationManager.NpcReplacedByUser, 0, (byte) 0, sPath));
	}

	public void useSummoningBag(int itemid){
		useSummoningBag(itemid, c.getPlayer().getPosition());
	}

	public void useSummoningBag(int itemid, Point pos){
		List<Pair<Integer, Integer>> pList = ItemInformationProvider.getInstance().getItemData(itemid).mobs;
		for(Pair<Integer, Integer> p : pList){
			if(Randomizer.nextInt(101) <= p.right){
				c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(p.left), pos);
			}
		}
	}

	public void gainSP(int sp){
		getPlayer().gainSp(sp);
	}

	public Equip itemToEquip(Item item){
		return (Equip) item;
	}
}