package tools.packets.field;

import net.SendOpcode;
import server.maps.objects.Kite;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class MessageBoxPool{

	public static byte[] spawnKiteError(){// CMessageBoxPool::OnCreateFailed
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_KITE_MESSAGE.getValue());
		return mplew.getPacket();
	}

	public static byte[] spawnKite(Kite kite){// CMessageBoxPool::OnMessageBoxEnterField
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_KITE.getValue());
		mplew.writeInt(kite.getObjectId());
		mplew.writeInt(kite.getItemID());
		mplew.writeMapleAsciiString(kite.getMessage());
		mplew.writeMapleAsciiString(kite.getPlayerName());
		mplew.writePos(kite.getPosition());
		return mplew.getPacket();
	}

	/**
	 * AnimationType 0 is 10/10
	 * AnimationType 1 just vanishes
	 */
	public static byte[] destroyKite(int objectid, byte animationType){// CMessageBoxPool::OnMessageBoxLeaveField
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DESTROY_KITE.getValue());
		mplew.write(animationType);
		mplew.writeInt(objectid);
		return mplew.getPacket();
	}
}
