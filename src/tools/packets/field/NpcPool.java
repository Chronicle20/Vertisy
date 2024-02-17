package tools.packets.field;

import net.SendOpcode;
import server.life.MapleNPC;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class NpcPool{

	public static byte[] spawnNPC(MapleNPC life){
		return spawnNPC(life, true);
	}

	public static byte[] spawnNPC(MapleNPC life, boolean minimap){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(22);
		mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
		mplew.writeInt(life.getObjectId());
		mplew.writeInt(life.getId());
		// CNpc::Init
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		if(life.getF() == 1){
			mplew.write(0);
		}else{
			mplew.write(1);
		}
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getRx0());
		mplew.writeShort(life.getRx1());
		mplew.writeBoolean(minimap);
		return mplew.getPacket();
	}
}
