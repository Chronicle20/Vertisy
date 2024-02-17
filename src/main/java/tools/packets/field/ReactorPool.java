package tools.packets.field;

import net.SendOpcode;
import server.reactors.MapleReactor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 1, 2017
 */
public class ReactorPool{

	public static byte[] spawnReactor(MapleReactor reactor){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writeInt(reactor.getId());
		mplew.write(reactor.getCurrStateAsByte());
		mplew.writePos(reactor.getPosition());
		mplew.write(0);
		mplew.writeMapleAsciiString("");// ?
		return mplew.getPacket();
	}

	public static byte[] triggerReactor(MapleReactor reactor, int stance){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REACTOR_HIT.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getCurrStateAsByte());
		mplew.writePos(reactor.getPosition());
		mplew.writeShort(stance);
		mplew.write(0);
		mplew.write(3); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
		return mplew.getPacket();
	}

	public static byte[] destroyReactor(MapleReactor reactor){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.write(reactor.getCurrStateAsByte());
		mplew.writePos(reactor.getPosition());
		return mplew.getPacket();
	}

	public static byte[] moveReactor(MapleReactor reactor){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REACTOR_MOVE.getValue());
		mplew.writeInt(reactor.getObjectId());
		mplew.writePos(reactor.getPosition());
		return mplew.getPacket();
	}
}
