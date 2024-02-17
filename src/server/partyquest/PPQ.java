package server.partyquest;

import java.util.Date;

import client.MapleCharacter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jan 6, 2017
 */
public class PPQ extends PartyQuest{

	public static final String PQ = "PPQ";
	public static final short QUEST = 1204;
	public static final int ITEM = 1072369;// todo:
	public static final int NPC = 9020000;// todo:

	public PPQ(){
		super();
		setPQIdentity("PPQ", NPC, QUEST, ITEM);
	}

	// go thru rank requirements to determine the correct rank and record/update it in the db
	// insert/update areainfo in the db and also in game.
	@Override
	public boolean calcAndUpdateRank(MapleCharacter chr, final MapleCharacter.PQRankRecord pqRankRecord){
		int tries = pqRankRecord.tries;
		int completed = pqRankRecord.completed;
		byte completeRate = pqRankRecord.completeRate; // completed / tries
		int fastestTime = pqRankRecord.fastestTime; // in seconds
		Date fastestDate = pqRankRecord.fastestDate; // the date in which fastestTime occurs (based on pq end date)
		String rank = pqRankRecord.rank; // rank in the db
		String rank_; // rank now
		// ranking requirements
		int haveItem = checkItemAndUpdate(chr, 1002573, 3) ? 3 : checkItemAndUpdate(chr, 1002572, 2) ? 2 : checkItemAndUpdate(chr, 1002571, 1) ? 1 : 0;
		if(tries >= 100 && completeRate >= 80 && haveItem == 3){ // rank S
			rank_ = PQRank.S.name(); // 10 mins
		}else if(tries >= 50 && completeRate >= 70 && haveItem == 2){ // rank A
			rank_ = PQRank.A.name(); // 15 mins
		}else if(tries >= 30 && completeRate >= 50 && haveItem == 1){ // rank B
			rank_ = PQRank.B.name(); // 20 mins
		}else if(tries >= 10 && completeRate >= 30){ // rank C
			rank_ = PQRank.C.name(); // 25 mins
		}else if(tries >= 1 && completeRate >= 20){ // rank D
			rank_ = PQRank.D.name(); // 30 mins
		}else if(tries >= 1 && completeRate >= 0){ // rank F
			rank_ = PQRank.F.name();
		}else{ // should not reach here, if its here, might be bugs/ exploits.
			rank_ = PQRank.z.name(); // default, should not happen.
		}
		String areaInfo = "cmp=" + completed + ";try=" + tries + ";have=" + haveItem + ";rank=" + rank_;
		// update rank in db and area info (quest info UI)
		updateRank(chr, !rank.equalsIgnoreCase(rank_) ? null : rank_, // update ranks only if it's diff from the one in db
		        areaInfo, fastestTime, fastestDate);
		return true;
	}

	@Override
	public void invokeOpenPQ(MapleCharacter chr){
		openPQ(chr, "PiratePQ");
	}
}