package tools.packets;

import java.awt.Point;

import client.MapleCharacter;
import net.SendOpcode;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 27, 2017
 */
public class CStage{

	/**
	 * Gets a packet telling the client to change maps.
	 *
	 * @param to The <code>MapleMap</code> to warp to.
	 * @param spawnPoint The spawn portal number to spawn at.
	 * @param chr The character warping to <code>to</code>
	 * @return The map change packet.
	 */
	public static byte[] getWarpToMap(MapleMap to, byte spawnPoint, MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_FIELD.getValue());
		mplew.writeShort(0);// decode opt, loop with 2 decode 4s
		mplew.writeInt(chr.getClient().getChannel());
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeBoolean(false);// bCharacterData
		mplew.writeShort(0);// nNotifierCheck
		mplew.write(0);// revive
		mplew.writeInt(to.getId());
		mplew.write(spawnPoint);
		mplew.writeShort(chr.getHp());
		mplew.write(0);// spawn pos
		mplew.writeLong(MaplePacketCreator.getTime(System.currentTimeMillis()));
		return mplew.getPacket();
	}

	public static byte[] getWarpToMap(MapleMap to, byte spawnPoint, Point spawnPosition, MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_FIELD.getValue());
		mplew.writeShort(0);// decode opt, loop with 2 decode 4s
		mplew.writeInt(chr.getClient().getChannel());
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeBoolean(false);// bCharacterData
		mplew.writeShort(0);// nNotifierCheck
		mplew.write(0);// revive
		mplew.writeInt(to.getId());
		mplew.write(spawnPoint);
		mplew.writeShort(chr.getHp());
		mplew.writeBoolean(spawnPosition != null);
		if(spawnPosition != null){
			mplew.writeInt(spawnPosition.x);
			mplew.writeInt(spawnPosition.y);
		}
		mplew.writeLong(MaplePacketCreator.getTime(System.currentTimeMillis()));
		return mplew.getPacket();
	}

	/**
	 * Gets character info for a character.
	 *
	 * @param chr The character to get info about.
	 * @return The character info packet.
	 */
	public static byte[] getCharInfo(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_FIELD.getValue());
		mplew.writeShort(0);// decode opt, loop with 2 decode 4s
		mplew.writeInt(chr.getClient().getChannel());
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeBoolean(true);// bCharacterData
		mplew.writeShort(0);// nNotifierCheck
		chr.getCRand().randomize();
		mplew.writeInt((int) chr.getCRand().seed1);
		mplew.writeInt((int) chr.getCRand().seed2);
		mplew.writeInt((int) chr.getCRand().seed3);
		MaplePacketCreator.addCharacterInfo(mplew, chr);
		setLogutGiftConfig(chr, mplew);
		mplew.writeLong(MaplePacketCreator.getTime(System.currentTimeMillis()));
		return mplew.getPacket();
	}

	private static void setLogutGiftConfig(MapleCharacter chr, LittleEndianWriter lew){
		lew.writeInt(0);// bPredictQuit
		for(int i = 0; i < 3; i++){
			lew.writeInt(0);//
		}
	}
}
