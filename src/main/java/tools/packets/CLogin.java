package tools.packets;

import java.util.List;
import java.util.Map;
import java.util.Random;

import client.MapleClient;
import client.PlayerGMRank;
import constants.ServerConstants;
import net.SendOpcode;
import net.login.LoginCharacter;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 27, 2017
 */
public class CLogin{

	/**
	 * Gets a successful authentication and PIN Request packet.
	 *
	 * @param c
	 * @param account The account name.
	 * @return The PIN request packet.
	 */
	public static byte[] getAuthSuccess(MapleClient c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.write(0);// some error code shit, and nNumOfCharacter
		mplew.write(0);// sMsg + 500, 0 or 1 decodes a bunch of shit
		mplew.writeInt(0);// not read
		mplew.writeInt(c.getAccID()); // user id
		mplew.write(c.getGender());
		//
		PlayerGMRank rank = PlayerGMRank.getByLevel(c.getGMLevel());
		byte nSubGradeCode = 0;
		nSubGradeCode |= rank.getSubGrade();
		mplew.writeBoolean(rank.getLevel() >= PlayerGMRank.GM.getLevel());// nGradeCode
		mplew.write(nSubGradeCode);// a short in v95
		// v90;
		// Value = (unsigned __int8)CInPacket::Decode1(v5);
		// v118 = ((unsigned int)(unsigned __int8)Value >> 8) & 1; this is for tester account.
		// v118 will only be 1 if nSubGradeCode is 0x100
		mplew.writeBoolean(false);// nCountryID, admin accounts?
		//
		mplew.writeMapleAsciiString(c.getAccountName());// sNexonClubID
		mplew.write(0);// nPurchaseExp
		mplew.write(0); // isquietbanned, nChatBlockReason
		mplew.writeLong(0);// isquietban time, dtChatUnblockDate
		mplew.writeLong(0); // creation time, dtRegisterDate
		mplew.writeInt(0);// nNumOfCharacter? or just reusing a variable
		mplew.write(2);// pin
		mplew.write(0);
		mplew.writeLong(0);// LABEL_120
		// Generates a random sessionID and saves it
		Random random = new Random();
		long sessionID = random.nextLong();
		c.setSessionID(sessionID);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a server and its channels.
	 *
	 * @param serverId
	 * @param serverName The name of the server.
	 * @param channelLoad Load of the channel - 1200 seems to be max.
	 * @return The server info packet.
	 */
	public static byte[] getServerList(int serverId, String serverName, int flag, String eventmsg, Map<Integer, Integer> channelLoad){// CLogin::OnWorldInformation
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERLIST.getValue());
		mplew.write(serverId);// nWorldID
		mplew.writeMapleAsciiString(serverName);// sName
		mplew.write(flag);// nWorldState
		mplew.writeMapleAsciiString(eventmsg);// sWorldEventDesc
		mplew.writeShort(100);// nWorldEventEXP_WSE
		mplew.writeShort(100);// nWorldEventDrop_WSE
		mplew.write(0);// nBlockCharCreation
		if(channelLoad == null){
			mplew.write(0);
		}else{
			mplew.write(channelLoad.size());
			for(int ch : channelLoad.keySet()){
				mplew.writeMapleAsciiString(serverName + "-" + (ch + 1));// sName
				mplew.writeInt((channelLoad.get(ch) * 1200) / ServerConstants.CHANNEL_LOAD);// nUserNO
				mplew.write(1);// nWorldID
				mplew.write(ch);// nChannelID
				mplew.writeBoolean(false);// bAdultChannel
			}
		}
		mplew.writeShort(0);// m_nBalloonCount
		// nX short
		// nY short
		// string
		return mplew.getPacket();
	}

	/**
	 * Gets a packet saying that the server list is over.
	 *
	 * @return The end of server list packet.
	 */
	public static byte[] getEndOfServerList(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SERVERLIST.getValue());
		mplew.write(0xFF);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet with a list of characters.
	 *
	 * @param c The MapleClient to load characters of.
	 * @param serverId The ID of the server requested.
	 * @return The character list packet.
	 */
	public static byte[] getCharList(MapleClient c, int serverId){// CLogin::OnSelectWorldResult
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHARLIST.getValue());
		mplew.write(0);
		List<LoginCharacter> chars = c.loadLoginCharacters(serverId);
		mplew.write((byte) chars.size());
		for(LoginCharacter chr : chars){
			MaplePacketCreator.addCharEntry(mplew, chr, false);
		}
		if(ServerConstants.ENABLE_PIC){// m_bLoginOpt
			mplew.write(c.getPic() == null || c.getPic().length() == 0 ? 0 : 1);
		}else{
			mplew.write(2);
		}
		mplew.writeInt(c.getCharacterSlots());// nSlotCount
		mplew.writeInt(c.nBuyCharacterCount);// m_nBuyCharCount
		return mplew.getPacket();
	}

	public static byte[] onSelectCharacterResult(int error, int error2){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVER_IP.getValue());
		/**
		 * 2, 3: Deleted or blocked
		 * 4: Incorrect password
		 * 5: Not a registered id
		 * 6, 8: Trouble logging in?
		 * 7: Id already logged in
		 * 10: Could not process due to too many connections
		 * 11: Only those who are 20 years old or older can use this.
		 * 13: Unable to log-on as a master at IP
		 * 14, 17: You have either selected the wrong gateway, or you have yet to change your personal information
		 * 15: Processing a request, etc, etc
		 * 16: Opens 'http://passport.nexon.net/?PART=/Registration/AgeCheck'
		 * 21: Please verify your account via email in order to play the game.
		 */
		mplew.write(error);
		mplew.write(error2);
		return mplew.getPacket();
	}
}
