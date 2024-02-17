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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.PlayerBuffValueHolder;
import net.server.channel.Channel;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.world.family.Family;
import net.world.family.FamilyCharacter;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CStage;
import tools.packets.CWvsContext;
import tools.packets.FamilyPackets;
import tools.packets.FuncKeyMappedMan;

public final class PlayerLoggedinHandler extends AbstractMaplePacketHandler{

	@Override
	public final boolean validateState(MapleClient c){
		return !c.isLoggedIn();
	}

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, final MapleClient c){
		long start = System.currentTimeMillis();
		final int cid = slea.readInt();
		MapleCharacter player = ChannelServer.getInstance().getMTSCharacterById(cid);
		if(player != null) ChannelServer.getInstance().removeMTSPlayer(cid);
		if(player == null){// check for same channel server channel changing.
			player = ChannelServer.getInstance().getPlayerFromTempStorage(cid);
			if(player != null) ChannelServer.getInstance().removePlayerFromTempStorage(cid);
		}
		boolean newcomer = false;
		if(player == null){
			try{
				player = MapleCharacter.loadCharFromDB(cid, c, true);
				newcomer = true;
			}catch(SQLException e){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
			}
		}else{
			player.newClient(c);
		}
		if(player == null){ // If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
			c.disconnect(true, true, false);
			return;
		}
		c.setPlayer(player);
		int state = c.getLoginState();
		c.setAccID(player.getAccountID());
		boolean allowLogin = true;
		Channel cserv = c.getChannelServer();
		if(state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin){
			// System.out.println((state != MapleClient.LOGIN_SERVER_TRANSITION) + "," + !allowLogin);
			Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, null, "Setting character %s to null. State: %d, Expected: %d", player.getName(), state, MapleClient.LOGIN_SERVER_TRANSITION);
			c.setPlayer(null);
			c.announce(MaplePacketCreator.getAfterLoginError(7));
			return;
		}
		c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
		if(player.getCashShop().isOpened()){
			player.getCashShop().open(0);
		}
		cserv.addPlayer(player);
		c.announce(CStage.getCharInfo(player));
		if(!player.isHidden()){
			player.toggleHide(true);
		}
		player.sendKeymap();
		player.sendMacros();
		if(player.getKeymap().get(93) != null) player.announce(FuncKeyMappedMan.getQuickSlots(player.getKeymap(), false));
		else player.announce(FuncKeyMappedMan.getQuickSlots(player.getKeymap(), true));
		if(player.getKeymap().get(91) != null){
			player.announce(FuncKeyMappedMan.sendAutoHpPot(player.getKeymap().get(91).getAction()));
		}
		if(player.getKeymap().get(92) != null){
			player.announce(FuncKeyMappedMan.sendAutoMpPot(player.getKeymap().get(92).getAction()));
		}
		if(player.getJob().isA(MapleJob.EVAN1)){
			player.createDragon();
		}
		player.getMap().addPlayer(player);
		try{
			List<PlayerBuffValueHolder> buffs = ChannelServer.getInstance().getWorldInterface().getBuffsFromStorage(cid);
			if(buffs != null){
				c.getPlayer().silentGiveBuffs(buffs);
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		int buddyIds[] = c.getPlayer().getBuddylist().getBuddyIds();
		if(!c.isFakeLogin){
			try{
				ChannelServer.getInstance().getWorldInterface().loggedOn(c.getPlayer().getName(), c.getPlayer().getId(), c.getChannel(), buddyIds);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		try{
			for(CharacterIdChannelPair onlineBuddy : ChannelServer.getInstance().getWorldInterface().multiBuddyFind(c.getPlayer().getId(), buddyIds)){
				BuddylistEntry ble = c.getPlayer().getBuddylist().get(onlineBuddy.getCharacterId());
				ble.setChannel(onlineBuddy.getChannel());
				c.getPlayer().getBuddylist().put(ble);
			}
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		c.announce(CWvsContext.Friend.updateBuddylist(c.getPlayer().getBuddylist().getBuddies()));
		if(c.getPlayer().getGuildId() > 0){
			try{
				MapleGuild playerGuild = ChannelServer.getInstance().getWorldInterface().getGuild(c.getPlayer().getGuildId(), c.getPlayer().getMGC());
				if(playerGuild == null){
					c.getPlayer().deleteGuild(c.getPlayer().getGuildId());
					c.getPlayer().resetMGC();
					c.getPlayer().setGuildId(0);
				}else{
					if(!c.isFakeLogin && newcomer) ChannelServer.getInstance().getWorldInterface().setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
					c.announce(MaplePacketCreator.showGuildInfo(c.getPlayer()));
					int allianceId = c.getPlayer().getGuild().getAllianceId();
					if(allianceId > 0){
						MapleAlliance newAlliance = ChannelServer.getInstance().getWorldInterface().getAlliance(allianceId);
						if(newAlliance == null){
							newAlliance = MapleAlliance.loadAlliance(allianceId);
							if(newAlliance != null){
								ChannelServer.getInstance().getWorldInterface().addAlliance(allianceId, newAlliance);
							}else{
								c.getPlayer().getGuild().setAllianceId(0);
							}
						}
						if(newAlliance != null){
							c.announce(MaplePacketCreator.getAllianceInfo(newAlliance));
							c.announce(MaplePacketCreator.getGuildAlliances(newAlliance));
							if(!c.isFakeLogin && newcomer) ChannelServer.getInstance().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(c.getPlayer(), true), c.getPlayer().getId(), -1);
						}
					}
				}
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}else c.announce(MaplePacketCreator.showGuildInfo(c.getPlayer()));
		if(c.getPlayer().isInParty()){
			MaplePartyCharacter pchar = c.getPlayer().getMPC();
			pchar.setChannel(c.getChannel());
			pchar.setMapId(c.getPlayer().getMapId());
			pchar.setOnline(true);
			try{
				ChannelServer.getInstance().getWorldInterface().updateParty(c.getPlayer().getPartyId(), PartyOperation.LOG_ONOFF, pchar);
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
		}
		c.getPlayer().updatePartyCharacter();
		c.getPlayer().checkMessenger();
		if(FeatureSettings.FAMILY){
			if(c.getPlayer().getFamilyId() >= 0){
				try{
					Family family = ChannelServer.getInstance().getWorldInterface().getFamily(c.getPlayer().getFamilyId());
					c.announce(FamilyPackets.priviliegeList(c.getPlayer()));// should be grabbing shit from ^
					c.announce(FamilyPackets.getFamilyInfo(family, family.members.get(c.getPlayer().getId())));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}else{
				Family fake = new Family();
				fake.bossID = c.getPlayer().getId();
				fake.familyName = "";
				FamilyCharacter fc = new FamilyCharacter();
				fc.characterID = fake.bossID;
				c.announce(FamilyPackets.priviliegeList(c.getPlayer()));
				c.announce(FamilyPackets.getFamilyInfo(fake, fc));
			}
		}
		player.showNote();
		if(player.getInventory(MapleInventoryType.EQUIPPED).findById(1122017) != null){
			player.equipPendantOfSpirit();
		}
		CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
		if(pendingBuddyRequest != null){
			c.announce(CWvsContext.Friend.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
		}
		if(newcomer){
			for(MaplePet pet : player.getPets()){
				if(pet != null){
					player.startFullnessSchedule(ItemInformationProvider.getInstance().getItemData(pet.getItemId()).hungry, pet, player.getPetIndex(pet));
				}
			}
		}
		c.announce(MaplePacketCreator.updateGender(player));
		c.announce(MaplePacketCreator.enableReport());
		int linkedLevel = (player.getExplorerLinkedLevel() / 10);
		player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (linkedLevel > 20 ? 20 : linkedLevel), 20, -1);
		player.expirationTask();
		player.getStats().recalcLocalStats(player);
		/*if(c.getPlayer().getIronMan() == 0){
			c.getPlayer().addTimer("ironman", TimerManager.getInstance().schedule("ironman", ()-> {
				NPCScriptManager.getInstance().start(c, 2007, "ironman", c.getPlayer());
			}, 5 * 1000));
		}*/
		c.announce(MaplePacketCreator.setNPCScriptable(9201074, ""));
		c.announce(MaplePacketCreator.setNPCScriptable(9000037, ""));
		if(c.getPlayer().isIntern()){
			if(c.getPlayer().getItemQuantity(1002959, true) == 0){
				Item item = ItemInformationProvider.getInstance().getEquipById(1002959);
				item.setOwner(c.getPlayer().getName());
				if(!c.getPlayer().isGM()) MapleInventoryManipulator.addFromDrop(c, item, false);
			}
		}else if(!c.getPlayer().isIntern()){
			if(c.getPlayer().getItemQuantity(1002959, false) > 0) MapleInventoryManipulator.removeById(c, MapleInventoryType.EQUIP, 1002959, (short) 1, true, false);
			else if(c.getPlayer().getItemQuantity(1002959, true) > 0) MapleInventoryManipulator.removeById(c, MapleInventoryType.EQUIPPED, 1002959, (short) 1, true, false);
		}
		if(c.getPlayer().getMarriedTo() > 0){
			try{// move this up to timer?
				if(!c.isFakeLogin) ChannelServer.getInstance().getWorldInterface().broadcastPacket(Arrays.asList(c.getPlayer().getMarriedTo()), MaplePacketCreator.onNotifyWeddingPartnerTransfer(c.getPlayer().getId(), c.getPlayer().getMapId()));
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			}
		}
		if(c.isAlphaUser()){
			if(c.getPlayer().getItemQuantity(1142099, true) == 0){
				Item item = ItemInformationProvider.getInstance().getEquipById(1142099);
				item.setOwner(c.getPlayer().getName());
				// MapleInventoryManipulator.addFromDrop(c, item, false);
			}
		}
		if(Calendar.getInstance().getTimeInMillis() < ServerConstants.expEventEnd){
			c.getPlayer().dropMessage(MessageType.NOTICE, "You have been blessed with the Maple Tree and have gained a 50% bonus exp boost.");
		}
		if(GameConstants.isWeekend()){
			c.getPlayer().dropMessage(MessageType.NOTICE, "The Strength of the weekend has given you a 20% bonus exp boost.");
		}
		c.updateEliteStatus(true, false);
		MapleQuest.getInstance(8248).forceComplete(c.getPlayer(), 9209001, false);
		MapleQuest.getInstance(8249).forceComplete(c.getPlayer(), 9209001, false);
		MapleQuest.getInstance(28433).forceComplete(c.getPlayer(), 9010000, false);
		MapleQuest.getInstance(28433).forceComplete(c.getPlayer(), 9010000, false);
		MapleQuest.getInstance(28436).forceComplete(c.getPlayer(), 9010000, false);
		c.isFakeLogin = false;
		Logger.log(LogType.INFO, LogFile.LOGIN, "Account: " + c.getAccountName() + " Player: " + c.getPlayer().getName() + " logged in at " + Calendar.getInstance().getTime().toString() + " Took: " + ((System.currentTimeMillis() - start) / 1000.0) + " seconds.");
	}
}
