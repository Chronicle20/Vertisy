/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.partyquest;

import java.util.Date;
import java.util.Random;

import client.MapleCharacter;
import client.MapleCharacter.PQRankRecord;
import client.MapleStat;
import client.inventory.Equip;
import scripting.event.EventInstanceManager;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import server.reactors.MapleReactor;
import server.reactors.ReactorHitInfo;
import tools.packets.UserLocal;

/**
 * @author peter
 */
public class HPQ extends PartyQuest{

	public static final String PQ = "HPQ";
	public static final short QUEST = 1200;
	public static final int ITEM = 1002798;
	public static final int NPC = 1012112;

	public HPQ(){
		super();
		setPQIdentity(PQ, NPC, QUEST, ITEM);
	}

	// yahh i know, might not be the best place to put this method, but it's a better place.
	// increment by 1 (getting closer to full moon) from 0 to 6.
	public static void incrementMoonState(EventInstanceManager eim){
		if(eim != null){
			MapleReactor fullMoon = eim.getPartyLeaderChar().getMap().getReactorById(9101000);
			int stage = fullMoon.getCurrState() + 1;
			// NOTE: stage wont be > 6 unless via codes/ w/e.
			if(stage <= 6){ // if it exceeds 6, map crash i.e. your client gets a INT min value (-2147blabla) error.
				fullMoon.setState(stage);
				if(stage == 6){ // trigger fullmoon only when stage is 6, no need to do it otherwise.
					fullMoon.runEvents(eim.getPartyLeaderChar().getClient(), new ReactorHitInfo());
				}
			}
		}
	}

	// go thru rank requirements to determine the correct rank and record/update it in the db
	// insert/update areainfo in the db and also in game.
	@Override
	public boolean calcAndUpdateRank(MapleCharacter chr, final PQRankRecord pqRankRecord){
		int tries = pqRankRecord.tries;
		int completed = pqRankRecord.completed;
		byte completeRate = pqRankRecord.completeRate; // completed / tries
		int fastestTime = pqRankRecord.fastestTime; // in seconds
		Date fastestDate = pqRankRecord.fastestDate; // the date in which fastestTime occurs (based on pq end date)
		String rank = pqRankRecord.rank; // rank in the db
		String rank_; // rank now
		// ranking requirements
		// 1002798 - A Rice Cake on Top of My Head
		boolean haveItem = checkItemAndUpdate(chr);
		if(tries >= 100 && completeRate >= 90 && fastestTime <= 360 && haveItem){ // rank S
			rank_ = PQRank.S.name(); // 6 mins
		}else if(tries >= 50 && completeRate >= 70 && fastestTime <= 420 && haveItem){ // rank A
			rank_ = PQRank.A.name(); // 7 mins
		}else if(tries >= 30 && completeRate >= 50 && fastestTime <= 480){ // rank B
			rank_ = PQRank.B.name(); // 8 mins
		}else if(tries >= 10 && completeRate >= 30 && fastestTime <= 540){ // rank C
			rank_ = PQRank.C.name(); // 9 mins
		}else if(tries >= 1 && completeRate >= 40 && fastestTime <= 600){ // rank D
			rank_ = PQRank.D.name(); // 10 mins
		}else if(tries >= 1 && completeRate >= 0/* && fastestTime <= 600*/){ // rank F
			// NOTE: i removed the fastestTime checking here since it's not needed
			// plus cuz of the default fastesttime value in the db.
			rank_ = PQRank.F.name();
		}else{ // should not reach here, if its here, might be bugs/ exploits.
			rank_ = PQRank.z.name(); // default, should not happen.
		}
		String areaInfo = "cmp=" + completed + ";try=" + tries + ";have=" + (haveItem ? 1 : 0) + ";rank=" + rank_;
		// update rank in db and area info (quest info UI)
		updateRank(chr, !rank.equalsIgnoreCase(rank_) ? null : rank_, // update ranks only if it's diff from the one in db
		        areaInfo, fastestTime, fastestDate);
		return true;
	}

	// not gonna do checkings here cuz they are expected to be done and use this method properly.
	public static void giveRandomizedRiceCakeHat(MapleCharacter chr){
		MapleStat[] stats = {MapleStat.INT, MapleStat.STR, MapleStat.LUK, MapleStat.DEX};
		byte amount = 0;
		byte statIndex = -1; // wont be -1 after this line, if it were, something went wrong.
		while(amount == 0){ // cuz i want the player to have ONE stat on the hat :D
			statIndex = (byte) (new Random().nextInt(stats.length));
			// 0 - 2
			amount = (byte) ItemInformationProvider.getRandStat((short) 1, 2);
		}
		int hatId = 1002798;
		Equip hat = (Equip) ItemInformationProvider.getInstance().getEquipById(hatId);
		hat.setStat(stats[statIndex], amount);
		MapleInventoryManipulator.addFromDrop(chr.getClient(), hat, false);
		chr.getClient().announce(UserLocal.UserEffect.getShowItemGain(hatId, (short) 1, true));
	}

	@Override
	public void invokeOpenPQ(MapleCharacter chr){
		openPQ(chr, "HenesysPQ");
	}
	// no longer used
	// public HPQ(MapleParty party) {
	// //2nd parameter = pq = pq name, 3rd parameter = questId = quest id for the pq (display rank)
	// super(party, "HPQ", (short) 1200);
	// }
}