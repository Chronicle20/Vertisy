package tools.packets.field;

import java.util.Map;
import java.util.Map.Entry;

import net.SendOpcode;
import server.life.MapleMonster;
import server.life.MobStat;
import server.life.MobStatData;
import server.movement.MovePath;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 28, 2017
 */
public class MobPool{

	/**
	 * Gets a spawn monster packet.
	 *
	 * @param life The monster to spawn.
	 * @param newSpawn Is it a new spawn?
	 * @return The spawn monster packet.
	 */
	public static byte[] spawnMonster(MapleMonster life, boolean newSpawn){
		return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
	}

	/**
	 * Gets a spawn monster packet.
	 *
	 * @param life The monster to spawn.
	 * @param newSpawn Is it a new spawn?
	 * @return The spawn monster packet.
	 */
	public static byte[] spawnHPQMonster(MapleMonster life, boolean newSpawn){
		return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
	}

	/**
	 * Gets a spawn monster packet.
	 *
	 * @param life The monster to spawn.
	 * @param newSpawn Is it a new spawn?
	 * @param effect The spawn effect.
	 * @return The spawn monster packet.
	 */
	public static byte[] spawnMonster(MapleMonster life, boolean newSpawn, int effect){
		return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
	}

	/**
	 * Gets a control monster packet.
	 *
	 * @param life The monster to give control to.
	 * @param newSpawn Is it a new spawn?
	 * @param aggro Aggressive monster?
	 * @return The monster control packet.
	 */
	public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro){
		return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
	}

	/**
	 * Removes a monster invisibility.
	 *
	 * @param life
	 * @return
	 */
	public static byte[] removeMonsterInvisibility(MapleMonster life){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		return mplew.getPacket();
		// return spawnMonsterInternal(life, true, false, false, 0, false);
	}

	/**
	 * Makes a monster invisible for Ariant PQ.
	 *
	 * @param life
	 * @return
	 */
	public static byte[] makeMonsterInvisible(MapleMonster life){
		return spawnMonsterInternal(life, true, false, false, 0, true);
	}

	/**
	 * Internal function to handler monster spawning and controlling.
	 *
	 * @param life The mob to perform operations with.
	 * @param requestController Requesting control of mob?
	 * @param newSpawn New spawn (fade in?)
	 * @param aggro Aggressive mob?
	 * @param effect The spawn effect to use.
	 * @return The spawn/control packet.
	 */
	private static byte[] spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		if(makeInvis){
			mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
			mplew.write(0);
			mplew.writeInt(life.getObjectId());
			return mplew.getPacket();
		}
		if(requestController){
			mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
			mplew.write(aggro ? 2 : 1);
		}else{
			mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
		}
		mplew.writeInt(life.getObjectId());
		mplew.write(life.getController() == null ? 5 : 1);
		mplew.writeInt(life.getId());
		setTemporaryStat(mplew, life);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0); // Origin FH //life.getStartFh()
		mplew.writeShort(life.getFh());
		/**
		 * -4: Fake
		 * -3: Appear after linked mob is dead
		 * -2: Fade in
		 * 1: Smoke
		 * 3: King Slime spawn
		 * 4: Summoning rock thing, used for 3rd job?
		 * 6: Magical shit
		 * 7: Smoke shit
		 * 8: 'The Boss'
		 * 9/10: Grim phantom shit?
		 * 11/12: Nothing?
		 * 13: Frankenstein
		 * 14: Angry ^
		 * 15: Orb animation thing, ??
		 * 16: ??
		 * 19: Mushroom kingdom boss thing
		 */
		if(effect != 0){
			mplew.write(effect);// nAppearType
			if(effect == -3 || effect >= 0) mplew.writeInt(effect == -3 ? life.getParentMob() : 0);
		}else{
			mplew.write(newSpawn ? -2 : -1);
		}
		mplew.write(life.getTeam());// m_nTeamForMCarnival
		mplew.writeInt(life.getItemEffect());// nEffectItemID
		mplew.writeInt(0);// m_nPhase
		return mplew.getPacket();
	}

	private static void setTemporaryStat(LittleEndianWriter lew, MapleMonster monster){// CMob::SetTemporaryStat
		setTemporaryStat(lew, monster.getMobStats());
	}

	private static void setTemporaryStat(LittleEndianWriter lew, Map<MobStat, MobStatData> stats){// CMob::SetTemporaryStat
		int[] mask = new int[4];
		for(Entry<MobStat, MobStatData> entry : stats.entrySet()){
			mask[entry.getKey().getSet()] |= entry.getKey().getMask();
		}
		for(int i = 3; i >= 0; i--){
			lew.writeInt(mask[i]);
		}
		int pCounter = -1, mCounter = -1;
		// MobStat::DecodeTemporary
		for(Entry<MobStat, MobStatData> entry : stats.entrySet()){
			MobStat stat = entry.getKey();
			MobStatData data = entry.getValue();
			if(stat.equals(MobStat.Burned)){
				lew.writeInt(0);// size
				// v90: 4, 4, 4
				// v95:
				/**
				 * v47->dwCharacterID = CInPacket::Decode4(v8);
				 * v47->nSkillID = CInPacket::Decode4(v8);
				 * v47->nDamage = CInPacket::Decode4(v8);
				 * v47->tInterval = CInPacket::Decode4(v8);
				 * v47->tEnd = CInPacket::Decode4(v8);
				 * v48 = CInPacket::Decode4(v8);
				 * v49 = tCur-- == 1;
				 * v47->nDotCount = v48;
				 */
			}else if(stat.equals(MobStat.Disable)){
				lew.writeBoolean(false);// Invincible
				lew.writeBoolean(false);// disable
			}else{// need couple more checks
				lew.writeShort(data.nOption);
				if(data.mobSkill){
					lew.writeShort(data.rOption);
					lew.writeShort(data.skillLevel);
				}else lew.writeInt(data.rOption);
				// lew.writeInt(data.rOption);
				lew.writeShort((int) ((data.endTime - System.currentTimeMillis()) / 500));
				if(stat.equals(MobStat.PCounter)) pCounter = entry.getValue().pCounter;
				else if(stat.equals(MobStat.MCounter)) mCounter = entry.getValue().pCounter;
			}
		}
		if(pCounter != -1) lew.writeInt(pCounter);// wPCounter_
		if(mCounter != -1) lew.writeInt(mCounter);// wMCounter_
		if(pCounter != -1 || mCounter != -1) lew.writeInt(100);// nCounterProb_
	}

	/**
	 * Handles monsters not being targettable, such as Zakum's first body.
	 *
	 * @param life The mob to spawn as non-targettable.
	 * @param effect The effect to show when spawning.
	 * @return The packet to spawn the mob as non-targettable.
	 */
	public static byte[] spawnFakeMonster(MapleMonster life, int effect){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.write(5);// nCalcDamageIndex
		mplew.writeInt(life.getId());
		addTemporaryStat(mplew);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0);// life.getStartFh()
		mplew.writeShort(life.getFh());
		if(effect > 0){
			mplew.write(effect);
			mplew.write(0);
			mplew.writeShort(0);
		}
		mplew.writeShort(-2);
		mplew.write(life.getTeam());
		mplew.writeInt(life.getItemEffect());
		mplew.writeInt(0);// m_nPhase
		return mplew.getPacket();
	}

	/**
	 * Makes a monster previously spawned as non-targettable, targettable.
	 *
	 * @param life The mob to make targettable.
	 * @return The packet to make the mob targettable.
	 */
	public static byte[] makeMonsterReal(MapleMonster life){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
		mplew.writeInt(life.getObjectId());// dwMobId
		mplew.write(5);// nCalcDamageIndex
		mplew.writeInt(life.getId());// mob template
		addTemporaryStat(mplew);
		mplew.writePos(life.getPosition());
		mplew.write(life.getStance());
		mplew.writeShort(0);// life.getStartFh()
		mplew.writeShort(life.getFh());
		mplew.writeShort(-1);
		mplew.writeInt(life.getItemEffect());
		mplew.writeInt(0);// m_nPhase
		return mplew.getPacket();
	}

	/**
	 * Gets a stop control monster packet.
	 *
	 * @param oid The ObjectID of the monster to stop controlling.
	 * @return The stop control monster packet.
	 */
	public static byte[] stopControllingMonster(int oid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
		mplew.write(0);
		mplew.writeInt(oid);
		return mplew.getPacket();
	}

	public static byte[] killMonster(int oid, boolean animation){
		return killMonster(oid, animation ? 1 : 0);
	}

	/**
	 * Gets a packet telling the client that a monster was killed.
	 *
	 * @param oid The objectID of the killed monster.
	 * @param animation 0 = dissapear, 1 = fade out, 2+ = special
	 * @return The kill monster packet.
	 */
	public static byte[] killMonster(int oid, int animation){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(animation);
		if(animation == 4) mplew.writeInt(0);
		return mplew.getPacket();
	}

	private static void addTemporaryStat(LittleEndianWriter lew){
		int[] mask = new int[4];
		// for(MapleBuffStat statup : statups){
		// mask[statup.getSet()] |= statup.getMask();
		// }
		for(int i = 3; i >= 0; i--){
			lew.writeInt(mask[i]);
		}
	}

	public static byte[] moveMonster(int useskill, int skill, int skill_1, int skill_2, int skill_3, int skill_4, int oid, MovePath moves){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.writeShort(0);// bNotForceLandingWhenDiscard
		mplew.write(useskill);// bNotChangeAction
		mplew.write(skill);// bNextAttackPossible
		mplew.write(skill_1);// bLeft
		mplew.write(skill_2);
		mplew.write(skill_3);
		mplew.write(skill_4);
		mplew.skip(8);
		// mplew.write(notForceLandingWhenDiscard);// bNotForceLandingWhenDiscard
		// mplew.write(notChangeAction);// bNotChangeAction
		// mplew.writeBoolean(nextAttackPossible);// bNextAttackPossible
		// mplew.writeBoolean(left);// bLeft
		// mplew.writeInt(data);// data?
		// mplew.writeInt(0);// m_aMultiTargetForBall
		// mplew.writeInt(0);// m_aRandTimeforAreaAttack
		moves.encode(mplew);
		return mplew.getPacket();
	}

	/**
	 * Gets a response to a move monster packet.
	 *
	 * @param objectid The ObjectID of the monster being moved.
	 * @param moveid The movement ID.
	 * @param currentMp The current MP of the monster.
	 * @param useSkills Can the monster use skills?
	 * @return The move response packet.
	 */
	public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills){
		return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
	}

	/**
	 * Gets a response to a move monster packet.
	 *
	 * @param objectid The ObjectID of the monster being moved.
	 * @param moveid The movement ID.
	 * @param currentMp The current MP of the monster.
	 * @param useSkills Can the monster use skills?
	 * @param skillId The skill ID for the monster to use.
	 * @param skillLevel The level of the skill to use.
	 * @return The move response packet.
	 */
	public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);
		mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
		mplew.writeInt(objectid);
		mplew.writeShort(moveid);//
		mplew.writeBoolean(useSkills);// bNextAttackPossible
		mplew.writeShort(currentMp);// mp
		mplew.write(skillId);// m_nSkillCommand
		mplew.write(skillLevel);// m_nSLV
		return mplew.getPacket();
	}

	public static byte[] onStatSet(MapleMonster monster, Map<MobStat, MobStatData> stats){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MobStatSet.getValue());
		mplew.writeInt(monster.getObjectId());
		setTemporaryStat(mplew, stats);
		mplew.writeShort(100);// delay
		mplew.write(0);// m_nCalcDamageStatIndex
		boolean movementAffectingStat = false;
		for(MobStat stat : stats.keySet()){
			if(stat.isMovementAffectingStat()) movementAffectingStat = true;
		}
		if(movementAffectingStat){
			mplew.writeBoolean(false);// bStat or m_bDoomReservedSN
		}
		return mplew.getPacket();
	}

	public static byte[] onStatReset(MapleMonster monster, Map<MobStat, MobStatData> stats){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MobStatReset.getValue());
		mplew.writeInt(monster.getObjectId());
		processStatReset(mplew, stats);
		return mplew.getPacket();
	}

	private static void processStatReset(LittleEndianWriter lew, Map<MobStat, MobStatData> stats){// CMob::OnStatReset
		int[] mask = new int[4];
		boolean movementAffectingStat = false;
		for(Entry<MobStat, MobStatData> entry : stats.entrySet()){
			mask[entry.getKey().getSet()] |= entry.getKey().getMask();
			if(entry.getKey().isMovementAffectingStat()) movementAffectingStat = true;
		}
		for(int i = 3; i >= 0; i--){
			lew.writeInt(mask[i]);
		}
		// CMob::ProcessStatReset
		// MobStat::Reset
		for(Entry<MobStat, MobStatData> entry : stats.entrySet()){
			if(entry.getKey().equals(MobStat.Burned)){
				lew.writeInt(0);// size
				// for each size
				// character id
				// skill id
			}
		}
		// end of MobStat::Reset
		lew.write(0);// m_nCalcDamageStatIndex
		// if mob is alive
		if(movementAffectingStat){
			lew.writeBoolean(false);// bStat
		}
	}
}
