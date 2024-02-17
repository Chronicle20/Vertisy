package tools.packets.field;

import java.util.List;

import constants.skills.BladeMaster;
import net.SendOpcode;
import net.server.channel.handlers.SummonDamageHandler.SummonAttackEntry;
import server.maps.objects.MapleSummon;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 8, 2017
 */
public class SummonedPool{// This use to be in userpool, but it got moved out inbetween v90 and 95

	/**
	 * Gets a packet to spawn a special map object.
	 *
	 * @param summon
	 * @param skillLevel The level of the skill used.
	 * @param animated Animated spawn?
	 * @return The spawn packet for the map object.
	 */
	public static byte[] spawnSummon(MapleSummon summon, boolean animated){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
		mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.writeInt(summon.getSkill());
		mplew.write(summon.getOwner().getLevel()); // nCharLevel
		mplew.write(summon.getSkillLevel());
		mplew.writePos(summon.getPosition());
		mplew.write(0);// nMoveAction
		mplew.writeShort(0);// nCurFoothold
		mplew.write(summon.getMovementType().getValue()); // nMoveAbility
		mplew.write(summon.getAssistType());// nAssistType
		mplew.write(animated ? 0 : 1);// nEnterType
		boolean bShowAvatar = summon.getSkill() == BladeMaster.MIRRORED_TARGET;
		mplew.writeBoolean(bShowAvatar);// bShowAvatar
		if(bShowAvatar) MaplePacketCreator.addCharLook(mplew, summon.getOwner(), false);
		// tesla coil
		// if ( v2->m_nSkillID == 35111002 )
		// v6 = CInPacket::Decode1(iPacket);
		// if ( v6 == 1 )
		/*
		 *         do
		{
		  *(v7 + 8 * v3) = CInPacket::Decode2(iPacket);
		  *(v7 + 8 * v3++ + 4) = CInPacket::Decode2(iPacket);
		}
		while ( v3 < 3 );
		
		 */
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a special map object.
	 *
	 * @param summon
	 * @param animated Animated removal?
	 * @return The packet removing the object.
	 */
	public static byte[] removeSummon(MapleSummon summon, boolean animated){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
		mplew.writeInt(summon.getOwner().getId());
		mplew.writeInt(summon.getObjectId());
		mplew.write(animated ? 4 : 1); // ?
		return mplew.getPacket();
	}

	public static byte[] moveSummon(int cid, int oid, MovePath moves){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(oid);
		moves.encode(mplew);
		return mplew.getPacket();
	}

	public static byte[] summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		// b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
		mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(direction);
		mplew.write(4);
		mplew.write(allDamage.size());
		for(SummonAttackEntry attackEntry : allDamage){
			mplew.writeInt(attackEntry.getMonsterOid()); // oid
			mplew.write(6); // who knows
			mplew.writeInt(attackEntry.getDamage()); // damage
		}
		return mplew.getPacket();
	}

	public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(unkByte);
		mplew.writeInt(damage);
		mplew.writeInt(monsterIdFrom);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] summonSkill(int cid, int summonSkillId, int nAttackAction){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SUMMON_SKILL.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(summonSkillId);
		mplew.write(nAttackAction);
		return mplew.getPacket();
	}
}
