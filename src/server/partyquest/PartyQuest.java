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
package server.partyquest;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import client.MapleCharacter;
import client.MapleCharacter.PQRankRecord;
import client.MapleClient;
import client.MapleQuestStatus;
import client.MapleQuestStatus.Status;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import net.channel.ChannelServer;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.UserLocal;

/**
 * @author kevintjuh93
 */
public abstract class PartyQuest{

	protected static final short UNDEFINED = -404; // default
	protected String pq;
	// npc that starts the pq, or more accurately, starts the quest (the one that show ranks) of the PQ
	protected int npcId = UNDEFINED; // default
	protected short questId = UNDEFINED; // default
	// the item that is required for pq ranks
	protected int itemId = UNDEFINED; // default
	protected int channel, world;
	protected MapleParty party;
	protected List<MapleCharacter> participants = new ArrayList<>();
	// key = char id, value = pqhistory id
	protected HashMap<Integer, Integer> historyIds = new HashMap<>();
	protected Connection con = DatabaseConnection.getConnection();
	protected long timeStarted = UNDEFINED;

	public PartyQuest(){}

	// creates, initializes and return PartyQuest instance
	public static PartyQuest create(Class<? extends PartyQuest> c, MapleParty party, long timeStarted){
		if(party == null){ return null; }
		try{
			PartyQuest partyQuest = c.newInstance();
			partyQuest.initPQIdentity(c);
			partyQuest.setParty(party);
			partyQuest.setTimeStarted(timeStarted);
			partyQuest.init();
			return partyQuest;
		}catch(InstantiationException | IllegalAccessException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
		return null;
	}

	// init channel, world
	// record pq start for all the pt member, set pq to all pq chars and add them into participants
	// start pq related quest if havent started
	// fill historyIds with participant char id and pqhistory id
	public void init(){
		if(party == null || pq == null){ // not gonna check for empty string.
			return;
		}
		MaplePartyCharacter leader = party.getLeader();
		channel = leader.getChannel();
		world = leader.getWorld();
		int mapid = leader.getMapId();
		Timestamp startAt = new Timestamp(timeStarted);
		// record pq start for all the pt member, set pq to all pq chars and add them into participants
		try(PreparedStatement ps = con.prepareStatement("insert into pqhistory (characterid, pq, startat) values(?,?,?)", Statement.RETURN_GENERATED_KEYS)){
			ps.setString(2, pq); // pq name
			ps.setTimestamp(3, startAt); // start time
			// to indicate whether questid and npcid are initialized so that related quest can start.
			boolean canStartQuest = npcId != UNDEFINED && questId != UNDEFINED;
			for(MaplePartyCharacter pchr : party.getMembers()){
				// not sure if a checking for isOnline is needed here, to be tested.
				if(pchr != null && pchr.getChannel() == channel && pchr.getMapId() == mapid){
					MapleCharacter chr = ChannelServer.getInstance().getChannel(channel).getPlayerStorage().getCharacterById(pchr.getId());
					if(chr == null){
						continue; // just so lesser nested shiets
					}
					ps.setInt(1, chr.getId()); // char id
					ps.addBatch();
					chr.setPartyQuest(this);
					// start the pq related quest if not yet started.
					if(canStartQuest){
						MapleQuest quest = MapleQuest.getInstance(questId);
						MapleQuestStatus questStatus = chr.getQuest(quest);
						// this assumes it's not repeatable or other shiet. add other checking or use quest.start() if you want.
						// questStatus should not be null, but yahh, weird shiets can always happen.
						if(questStatus != null && questStatus.getStatus() == Status.NOT_STARTED){
							quest.forceStart(chr, npcId);
						}
					}
					participants.add(chr);
				}
			}
			ps.executeBatch();
			// fill historyIds with char id and pqhistory id
			try(ResultSet rs = ps.getGeneratedKeys()){
				for(MapleCharacter chr : participants){
					// quit looping when no results found. shouldn't happen tho, putting this checking nonetheless.
					if(!rs.next()){ // might wanna log this cuz it can mean things are fxed up.
						break;
					}
					if(chr != null){
						historyIds.put(chr.getId(), rs.getInt(1));
					}
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public final void setPQIdentity(String pq, int npcId){
		setPQIdentity(pq, npcId, questId);
	}

	public final void setPQIdentity(String pq, int npcId, short questId){
		setPQIdentity(pq, npcId, questId, itemId);
	}

	public final void setPQIdentity(String pq, int npcId, short questId, int itemId){
		this.pq = pq;
		this.npcId = npcId;
		this.questId = questId;
		this.itemId = itemId;
	}

	public final void initPQIdentity(Class<? extends PartyQuest> partyQuest){
		try{
			pq = getOnePQIdentityID(partyQuest, "PQ") + "";
			npcId = (int) getOnePQIdentityID(partyQuest, "NPC");
			questId = (short) getOnePQIdentityID(partyQuest, "QUEST");
			itemId = (int) getOnePQIdentityID(partyQuest, "ITEM");
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
		}
	}

	public static Item gainRandomizedItem(MapleClient c, int id, short quantity, double deviationPercentage){
		return gainRandomizedItem(c, id, quantity, deviationPercentage, false, -1);
	}

	public static Item gainRandomizedItem(MapleClient c, int id, short quantity, double deviationPercentage, boolean includeLowerBound){
		return gainRandomizedItem(c, id, quantity, deviationPercentage, includeLowerBound, -1);
	}

	// it will always check for slots availability
	// it also shows item gain/ loss.
	public static Item gainRandomizedItem(MapleClient c, int id, short quantity, double deviationPercentage, boolean includeLowerBound, long expires){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		if(!ii.isItemValid(id)){ // yahh, lets validate the item id
			return null;
		}
		Item item = null;
		if(quantity >= 0){
			if(id >= 5000000 && id <= 5000100){
				Item petItem = new Item(id, (short) 1);
				petItem.setPetId(MaplePet.createPet(id));
				petItem.setExpiration(expires == -1 ? -1 : System.currentTimeMillis() + expires);
				MapleInventoryManipulator.addFromDrop(c, petItem, false);
			}
			if(ii.getInventoryType(id).equals(MapleInventoryType.EQUIP)){
				item = ii.getEquipById(id);
				if(!ItemConstants.isRechargable(item.getItemId())){
					item = ii.randomizeStats((Equip) item);
				}
			}else{
				quantity = ItemInformationProvider.randomizeItemQty(id, quantity, deviationPercentage, includeLowerBound);
				item = new Item(id, (short) 0, quantity);
			}
			if(!c.getPlayer().beforeGetItem(id, quantity)){ // check for slots availability and prompt msg if n/a
				return null;
			}
			if(expires != -1){
				item.setExpiration(System.currentTimeMillis() + expires);
			}
			MapleInventoryManipulator.addFromDrop(c, item, false);
		}else{
			// might wanna remove this shiet
			// or randomize the qty here too :D
			MapleInventoryManipulator.removeById(c, ItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, true);
		}
		c.announce(UserLocal.UserEffect.getShowItemGain(id, quantity, true));
		return item;
	}

	public static Object getOnePQIdentityID(Class<? extends PartyQuest> partyQuest, String fieldName) throws Exception{
		return partyQuest.getField(fieldName).get(null);
	}

	// quest progress id 1
	public static int getItemQty(MapleCharacter chr, Class<? extends PartyQuest> partyQuest){
		short questId_;
		try{
			questId_ = (short) getOnePQIdentityID(partyQuest, "QUEST");
			return chr.getQuestProgress(questId_, 1); // get progress id 1
		}catch(Exception ex){ // everything will be handled by this shiet, yahh i know, but idc xD
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, chr.getName() + " Method: getItemQty\n" + partyQuest.getClass().getName() + " can't get QUEST static field");
		}
		return 0;
	}

	// quest progress id 1
	public static void updateItemQty(MapleCharacter chr, Class<? extends PartyQuest> partyQuest, int qty){
		short questId_;
		try{
			questId_ = (short) getOnePQIdentityID(partyQuest, "QUEST");
			chr.updateQuestProgress(questId_, 1, qty); // get progress id 1
		}catch(Exception ex){ // everything will be handled by this shiet, yahh i know, but idc xD
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, chr.getName() + " Method: updateItemQty\n" + partyQuest.getClass().getName() + " can't get QUEST static field");
		}
	}

	// quest progress id 69 = completed PQ count.
	// NOTE: this is only used by LPQ as of now.
	public static int getPQCount(MapleCharacter chr, Class<? extends PartyQuest> partyQuest){
		short questId_;
		try{
			questId_ = (short) getOnePQIdentityID(partyQuest, "QUEST");
			return chr.getQuestProgress(questId_, 69);
		}catch(Exception ex){ // everything will be handled by this shiet, yahh i know, but idc xD
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, chr.getName() + " Method: getPQCount\n" + partyQuest.getClass().getName() + " can't get QUEST static field");
		}
		return 0;
	}

	// quest progress id 69 = completed PQ count.
	// NOTE: this is only used by LPQ as of now.
	public static void updatePQCount(MapleCharacter chr, Class<? extends PartyQuest> partyQuest, int qty){
		short questId_;
		try{
			questId_ = (short) getOnePQIdentityID(partyQuest, "QUEST");
			chr.updateQuestProgress(questId_, 69, qty);
		}catch(Exception ex){ // everything will be handled by this shiet, yahh i know, but idc xD
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, chr.getName() + " Method: updatePQCount\n" + partyQuest.getClass().getName() + " can't get QUEST static field");
		}
	}

	public static boolean checkItemAndUpdate(MapleCharacter chr, Class<? extends PartyQuest> partyQuest){
		short questId_;
		int itemId_;
		try{
			questId_ = (short) getOnePQIdentityID(partyQuest, "QUEST");
			itemId_ = (int) getOnePQIdentityID(partyQuest, "ITEM");
		}catch(Exception ex){ // everything will be handled by this shiet, yahh i know, but idc xD
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, chr.getName() + " Method: checkItemAndUpdate\n" + partyQuest.getClass().getName() + " can't get QUEST and/or ITEM static field(s)");
			return false;
		}
		boolean haveItem = hadItem(chr, questId_);
		if(!haveItem){ // if char didnt have it
			haveItem = chr.haveItem(itemId_, true); // see if the char has it now
			if(haveItem){ // update quest progress id 0 to show that the char has the item.
				updateHadItem(chr, questId_);
			}
		}
		return haveItem;
	}

	// quest progress id 0 = hadItem indicator.
	public static boolean hadItem(MapleCharacter chr, short questId){
		return chr.getQuestProgress(questId) == 1; // get progress id 0, only 0 or 1 as value
	}

	// quest progress id 0 = hadItem indicator.
	public static void updateHadItem(MapleCharacter chr, short questId){
		chr.updateQuestProgress(questId, 1); // updates progress id 0, make it 1
	}

	// you may refer to HPQ for this.
	// rank can only be 3 chars max (restricted in the db), but we only use 1 char.
	// NOTE: null checking must be done for pqRankRecord before using this method.
	public abstract boolean calcAndUpdateRank(MapleCharacter chr, final PQRankRecord pqRankRecord);

	// gets the PQRank db record (most columns) of the specified player and PQ.
	public PQRankRecord getPQRankRecord(MapleCharacter chr){
		// get existing pq rank record from db (everything is auto-calculated by triggers except for rank)
		try(PreparedStatement ps = con.prepareStatement("select * from pqranks where characterid=? and pq=?")){
			ps.setInt(1, chr.getId()); // char id
			ps.setString(2, pq); // pq name
			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					PQRankRecord pqRankRecord = new PQRankRecord();
					pqRankRecord.pq = pq; // just put for fun :p well, you can also always know what pq it is
					pqRankRecord.tries = rs.getInt("tries");
					pqRankRecord.completed = rs.getInt("completed");
					pqRankRecord.completeRate = (byte) rs.getInt("completerate");
					pqRankRecord.fastestTime = rs.getInt("fastesttime");
					pqRankRecord.rank = rs.getString("rank");
					pqRankRecord.fastestDate = rs.getDate("fastestdate");
					updatePQCount(chr);
					return pqRankRecord;
				}
			}
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "get pqranks record \nPQ: " + pq + " character id: " + chr.getId());
		}
		return null;
	}

	// increment the number of successful PQs the player has.
	// quest progress id 69 = completed PQ count. and yes, it's 69.
	// NOTE: LPQ count will be deducted when it's used to claim broken glasses.
	public void updatePQCount(MapleCharacter chr){
		if(questId != UNDEFINED){
			chr.updateQuestProgress(questId, 69, chr.getQuestProgress(questId, 69) + 1);
		}else{
			Logger.log(LogType.WARNING, LogFile.GENERAL_ERROR, "Player: " + chr.getName() + " PQ count cannot be updated.\nQuest ID not found in " + this.getClass().getName());
		}
	}

	// check if the char had the item before
	// if the char didnt, check if he/she has the item now and
	// update hadItem (quest progress) if he/she has the item now.
	public boolean checkItemAndUpdate(MapleCharacter chr){
		return checkItemAndUpdate(chr, itemId, 1);
	}

	public boolean checkItemAndUpdate(MapleCharacter chr, int itemId, int progressId){
		boolean haveItem = hadItem(chr);
		if(!haveItem){ // if char didnt have it
			if(itemId != UNDEFINED){
				haveItem = chr.haveItem(itemId, true); // see if the char has it now
				if(haveItem){ // update quest progress id 0 to show that the char has the item.
					updateHadItem(chr, progressId);
				}
			}else{
				Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "Item cannot be checked for existence.\nItem ID not found in " + this.getClass().getName());
			}
		}
		return haveItem;
	}

	// quest progress id 0 = hadItem indicator.
	public boolean hadItem(MapleCharacter chr){
		return questId == UNDEFINED ? false : chr.getQuestProgress(questId) > 0; // get progress id 0
	}

	// quest progress id 0 = hadItem indicator.
	public void updateHadItem(MapleCharacter chr, int progressId){
		if(questId != UNDEFINED){
			chr.updateQuestProgress(questId, progressId); // updates progress id 0, make it 1
		}else{
			Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "HadItem cannot be updated.\nQuest ID not found in " + this.getClass().getName());
		}
	}

	// update rank and/or haveitem in db
	// update area info (quest info UI)
	// process fastestTime and date to be shown in area info (quest info UI)
	public boolean updateRank(MapleCharacter chr, String rank_, String areaInfo, int fastestTime, Date fastestDate){
		// update rank in db only if the rank_ is diff from db (is not null)
		if(rank_ != null && !chr.updatePQRank(pq, rank_)){ return false; }
		if(questId == UNDEFINED){
			Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "Quest ID not found in " + this.getClass().getName());
			return false;
		}
		short sec;
		short min;
		if(fastestTime == 65535){ // default in db
			sec = 0;
			min = 0;
		}else{
			sec = (short) (fastestTime % 60);
			min = (short) (fastestTime / 60);
		}
		String minSecDate = ";min=" + min + ";sec=" + sec + ";date=" + (fastestDate == null ? "N/A" : fastestDate);
		// display the updated quest (rather, area) info (quest UI)
		chr.updateAreaInfo(questId, areaInfo + minSecDate);
		return true;
	}

	// put openPQ in here!
	public abstract void invokeOpenPQ(MapleCharacter chr);

	public void openPQ(MapleCharacter chr, String emName){
		openPQ(chr, emName, emName); // :p
	}

	// allow entrance to PQ in the chr channel, unregister player and dispose eim.
	public void openPQ(MapleCharacter chr, String emName, String eimName){
		if(pq == null) return;
		EventManager em = ChannelServer.getInstance().getChannel(channel).getEventSM().getEventManager(emName);
		if(em == null) return;
		EventInstanceManager eim = em.getInstance(eimName);
		if(eim != null){
			// so scripts dont need to have repeated unregisterPlayer, yay for reusability and combo set :D
			eim.unregisterPlayer(chr); // dont think we will need null checking here.
		}
	}

	public boolean end(MapleCharacter chr, boolean success, long duration){
		return end(chr, success, duration, true);
	}

	public boolean end(MapleCharacter chr, boolean success, long duration, boolean disposeEim){
		if(chr == null){ // not gonna do historyIds and participants null checking
			return false;
		}
		Integer id_ = null;
		// if the char pq history and rank havent been recorded and updated
		if(!participants.isEmpty() && !historyIds.isEmpty() && participants.contains(chr) && (id_ = historyIds.get(chr.getId())) != null){
			// NOTE: refer to duration as it is more accurate (closer to the timer), endat might be slightly off.
			try(PreparedStatement ps = con.prepareStatement("update pqhistory set success=?, endat=current_timestamp, duration=? where id=?")){
				ps.setInt(1, success ? 1 : 0); // success
				ps.setInt(2, (int) (duration / 1000)); // duration: milliseconds converts to seconds.
				ps.setInt(3, id_); // id
				ps.executeUpdate();
			}catch(SQLException ex){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex, "Player: " + chr.getName() + " Method: end \n" + "PQ: " + pq + " character id: " + chr.getId());
				return false;
			}
			// update pq rank in the db and area info.
			if(pq != null){
				// gets pq rank record and update pq count to quest progress id 69.
				final PQRankRecord pqRankRecord = getPQRankRecord(chr);
				if(pqRankRecord == null){
					try(PreparedStatement ps = con.prepareStatement("INSERT INTO pqranks VALUES(?, ?, ?, ?, ?, ?, ?)")){
						ps.setString(1, pq);
						ps.setInt(2, chr.getId());
						ps.setString(3, "F");
						ps.setInt(4, 1);
						ps.setInt(5, 1);
						ps.setLong(6, (int) (duration / 1000));
						ps.setDate(7, new java.sql.Date(System.currentTimeMillis()));
						ps.executeUpdate();
					}catch(Exception ex){
						Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "Failed to update pq record for: " + this.getClass().getName());
					}
				}else{
					calcAndUpdateRank(chr, pqRankRecord);
				}
			}else{
				Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, "PQ name not found in " + this.getClass().getName());
				return false;
			}
			// remove chr pq history id when it's done
			historyIds.remove(chr.getId());
		}
		if(disposeEim){ // handles all the finishing up jobs (unregister player, dispose eim, open pq)
			invokeOpenPQ(chr);
		}
		return true;
	}

	public void setParty(MapleParty party){
		this.party = party;
	}

	public MapleParty getParty(){
		return party;
	}

	public List<MapleCharacter> getParticipants(){
		return participants;
	}

	public void removeParticipant(MapleCharacter chr){
		removeParticipant(chr, null, null);
	}

	public void removeParticipant(MapleCharacter chr, EventManager em, EventInstanceManager eim){
		synchronized(participants){
			participants.remove(chr);
			chr.setPartyQuest(null);
			if(participants.isEmpty()){
				if(eim != null && em != null){
					em.setProperty(pq + "Open", "true");
				}
				if(eim != null){
					eim.dispose();
				}
				clearAll(); // dereference this object.
				try{
					super.finalize();
				}catch(Throwable ex){
					Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
				}
			}
			// System.gc();
		}
	}

	public void clearAll(){
		participants = null;
		con = null;
		pq = null;
		historyIds = null;
		party = null;
	}

	public static int getExp(String PQ, int level){
		switch (PQ){
			case "HenesysPQ":
				return 1250 * level / 5;
			case "KerningPQFinal":
				return 500 * level / 5;
			case "KerningPQ4th":
				return 400 * level / 5;
			case "KerningPQ3rd":
				return 300 * level / 5;
			case "KerningPQ2nd":
				return 200 * level / 5;
			case "KerningPQ1st":
				return 100 * level / 5;
			case "LudiMazePQ":
				return 1500 * level / 5;
			case "LudiPQ1st":
				return 100 * level / 5;
			case "LudiPQ2nd":
				return 250 * level / 5;
			case "LudiPQ3rd":
				return 350 * level / 5;
			case "LudiPQ4th":
				return 350 * level / 5;
			case "LudiPQ5th":
				return 400 * level / 5;
			case "LudiPQ6th":
				return 450 * level / 5;
			case "LudiPQ7th":
				return 500 * level / 5;
			case "LudiPQ8th":
				return 650 * level / 5;
			case "LudiPQLast":
				return 1000 * level / 5;
			case "OrbisPQPre":
				return 1000 * level / 5;
			case "OrbisPQWalk":
				return 1000 * level / 5;
			case "OrbisPQStore":
				return 1000 * level / 5;
			case "OrbisPQLobby":
				return 1000 * level / 5;
			case "OrbisPQSealed":
				return 1000 * level / 5;
			case "OrbisPQLounge":
				return 1000 * level / 5;
			case "OrbisPQUp":
				return 1000 * level / 5;
			case "OrbisPQ":
				return 2000 * level / 5;
			case "PiratePQ1":
				return 300 * level / 5;
			case "PiratePQ2":
				return 750 * level / 5;
			case "PiratePQ3":
				return 300 * level / 5;
			case "PiratePQ4":
				return 500 * level / 5;
			case "PiratePQ":
				if(level < 71){ return 1500 * level / 5; }
				return 2000 * level / 5;
			case "MagatiaPQ0":
			case "MagatiaPQ01":
			case "MagatiaPQ1":
			case "MagatiaPQ2":
			case "MagatiaPQ23":
			case "MagatiaPQ4":
				return 100 * level / 5;
			case "Romeo&JulietBoss":
				return 200 * level / 5;
			case "MagatiaPQComplete":
				return 140000 * level / 125;
			case "MagatiaPQFail":
				return 105000 * level / 125;
		}
		Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Unhandled PartyQuest: " + PQ);
		return 0;
	}

	public long getTimeStarted(){
		return timeStarted;
	}

	public void setTimeStarted(long timeStarted){
		this.timeStarted = timeStarted;
	}

	public int getNpcId(){
		return npcId;
	}

	public void setNpcId(int npcId){
		this.npcId = npcId;
	}

	public String getPq(){
		return pq;
	}

	public void setPq(String pq){
		this.pq = pq;
	}

	public short getQuestId(){
		return questId;
	}

	public void setQuestId(short questId){
		this.questId = questId;
	}
}