package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 28, 2017
 */
public class Field{

	public static byte[] vegaResult(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VEGA_RESULT.getValue());
		return mplew.getPacket();
	}

	public static byte[] vegaFail(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VEGA_FAIL.getValue());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] getWhisper(String sender, int channel, String text){
		return getWhisper(sender, channel, false, text);
	}

	public static byte[] getWhisper(String sender, int channel, boolean bFromAdmin, String text){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(0x12);
		mplew.writeMapleAsciiString(sender);
		mplew.write(channel);
		mplew.writeBoolean(bFromAdmin);// bFromAdmin. ignores blacklist
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	/**
	 * @param target name of the target character
	 * @param reply error code: 0x0 = cannot find char, 0x1 = success
	 * @return the MaplePacket
	 */
	public static byte[] getWhisperReply(String target, byte reply){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(0x0A); // whisper?
		mplew.writeMapleAsciiString(target);
		mplew.write(reply);
		return mplew.getPacket();
	}
}
