package tools.packets.field;

import net.SendOpcode;
import server.maps.objects.MapleMist;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class AffectedAreaPool{

	public static byte[] spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
		mplew.writeInt(oid);
		mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : mist.isRecoveryMist() ? 4 : 2); // mob mist = 0, player poison = 1, smokescreen = 2, area buff item = 3, recovery = 4
		mplew.writeInt(ownerCid);
		mplew.writeInt(skill);// if type == 3, CItemInfo::GetAreaBuffItem on this int. Item/Cash/0528.img/%08d/tile
		mplew.write(level);
		mplew.writeShort(mist.getSkillDelay()); // Skill delay, effectDelay / 100 (e.g. 3240ms -> 32)
		mplew.writeInt(mist.getBox().x);
		mplew.writeInt(mist.getBox().y);
		mplew.writeInt(mist.getBox().x + mist.getBox().width);
		mplew.writeInt(mist.getBox().y + mist.getBox().height);
		mplew.writeInt(0);// pInfo
		mplew.writeInt(0);// nPhase
		return mplew.getPacket();
	}

	public static byte[] removeMist(int oid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
		mplew.writeInt(oid);
		return mplew.getPacket();
	}
}
