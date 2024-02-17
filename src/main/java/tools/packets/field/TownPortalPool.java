package tools.packets.field;

import java.awt.Point;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class TownPortalPool{

	/**
	 * Gets a packet to spawn a door.
	 *
	 * @param oid The door's object ID.
	 * @param pos The position of the door.
	 * @param town
	 * @return The remove door packet.
	 */
	public static byte[] spawnDoor(int oid, Point pos, boolean town){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
		mplew.writeBoolean(town);
		mplew.writeInt(oid);
		mplew.writePos(pos);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a door.
	 *
	 * @param oid The door's ID.
	 * @param town
	 * @return The remove door packet.
	 */
	public static byte[] removeDoor(int oid, boolean town){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
		if(town){
			mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
			mplew.writeInt(999999999);
			mplew.writeInt(999999999);
		}else{
			mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
			mplew.write(0);// face
			mplew.writeInt(oid);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to remove a door.
	 *
	 * @param oid The door's ID.
	 * @param town
	 * @return The remove door packet.
	 */
	public static byte[] removeDoor(int oid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
		mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
		mplew.write(0);
		mplew.writeInt(oid);
		return mplew.getPacket();
	}
}
