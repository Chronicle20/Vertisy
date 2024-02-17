package tools.packets;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleGuildSummary;
import server.maps.objects.miniroom.MiniGame;
import server.maps.objects.miniroom.MiniRoom;
import tools.MaplePacketCreator;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 14, 2017
 */
public class CUserPool{

	/**
	 * Gets a packet spawning a player as a mapobject to other clients.
	 *
	 * @param chr The character to spawn to other clients.
	 * @return The spawn player packet.
	 */
	public static byte[] spawnPlayerMapobject(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
		mplew.writeInt(chr.getId());
		mplew.writeInt(0); // damage skin
		// CUserRemote::Init
		mplew.write(chr.getLevel()); // v83
		mplew.writeMapleAsciiString(chr.getName());
		if(chr.getGuildId() < 1){
			mplew.writeMapleAsciiString("");
			mplew.write(new byte[6]);
		}else{
			MapleGuildSummary gs = null;
			try{
				gs = ChannelServer.getInstance().getWorldInterface().getGuildSummary(chr.getGuildId());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
			if(gs != null){
				mplew.writeMapleAsciiString(gs.getName());
				mplew.writeShort(gs.getLogoBG());
				mplew.write(gs.getLogoBGColor());
				mplew.writeShort(gs.getLogo());
				mplew.write(gs.getLogoColor());
			}else{
				mplew.writeMapleAsciiString("");
				mplew.write(new byte[6]);
			}
		}
		// chr.secondaryStat.encodeRemote(mplew, chr.getAllStatups());
		MaplePacketCreator.writeForeignBuffs(mplew, chr.getAllStatups());
		mplew.writeShort(chr.getJob().getId());
		MaplePacketCreator.addCharLook(mplew, chr, false);
		mplew.writeInt(0);// m_dwDriverID
		mplew.writeInt(0);// m_dwPassenserID
		mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).countById(5110000));// nChocoCount
		mplew.writeInt(chr.getItemEffect());// nActiveEffectItemID
		mplew.writeInt(0);// m_nCompletedSetItemID
		mplew.writeInt(ItemConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);// m_nPortableChairID
		mplew.writePos(chr.getPosition());
		mplew.write(chr.getStance());
		mplew.writeShort(0);// chr.getFh()
		mplew.write(0);// Admin Byte - Just pulls effect from the WZ file if its there.
		MaplePet[] pet = chr.getPets();
		for(int i = 0; i < 3; i++){
			if(pet[i] != null){
				MaplePacketCreator.addPetInfo(mplew, pet[i], false);
			}
		}
		mplew.write(0); // end of pets
		if(chr.getMount() == null){
			mplew.writeInt(1); // mob level
			mplew.writeLong(0); // mob exp + tiredness
		}else{
			mplew.writeInt(chr.getMount().getLevel());
			mplew.writeInt(chr.getMount().getExp());
			mplew.writeInt(chr.getMount().getTiredness());
		}
		if(chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr)){
			if(chr.getPlayerShop().hasFreeSlot()){
				MaplePacketCreator.addAnnounceBox(mplew, chr.getPlayerShop(), chr.getPlayerShop().getVisitors().length);
			}else{
				MaplePacketCreator.addAnnounceBox(mplew, chr.getPlayerShop(), 1);
			}
		}else if(chr.getMiniGame() != null && chr.getMiniGame().isOwner(chr)){
			if(chr.getMiniGame().hasFreeSlot()){
				MaplePacketCreator.addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 1, 0);
			}else{
				MaplePacketCreator.addAnnounceBox(mplew, chr.getMiniGame(), 1, 0, 2, 1);
			}
		}else{
			mplew.write(0);
		}
		if(chr.getChalkboard() != null){
			mplew.write(1);
			mplew.writeMapleAsciiString(chr.getChalkboard());
		}else{
			mplew.write(0);
		}
		MaplePacketCreator.addRingLook(mplew, chr.getCrushRings());
		MaplePacketCreator.addRingLook(mplew, chr.getFriendshipRings());
		MaplePacketCreator.addMarriageRingLook(mplew, chr);
		mplew.write(0);// some effect shit. CUser::LoadDarkForceEffect, CDragon::CreateEffect, CUser::LoadSwallowingEffect
		mplew.write(0);// (boolean)new year card record add, int size, 1 int in the loop
		mplew.writeInt(0);// m_nPhase
		mplew.write(chr.getTeam());// only needed in specific fields
		return mplew.getPacket();
	}

	public static byte[] removePlayerFromMap(int cid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
		mplew.writeInt(cid);
		return mplew.getPacket();
	}

	public static class CommonPacket{
		// all of these have a chrid for first bytes

		public static byte[] miniRoomBalloon(MapleCharacter chr, MiniGame mg){
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX);// CUser::OnMiniRoomBalloon
			mplew.writeInt(chr.getId());
			addMiniRoomBalloon(mplew, mg);
			return mplew.getPacket();
		}

		public static void addMiniRoomBalloon(LittleEndianWriter lew, MiniRoom mr){
			lew.write(mr.getMiniRoomType());// nMiniRoomType
			lew.writeInt(mr.getObjectId());// m_dwMiniRoomSN
			lew.writeMapleAsciiString(mr.getTitle());// m_sMiniRoomTitle
			lew.writeBoolean(mr.getPassword() != null);// m_bPrivate
			lew.write(mr.getGameType());/// m_nGameKind
			lew.write(mr.getCurrentUsers());// m_nCurUsers
			lew.write(mr.getMaxSlots());// m_nMaxUsers
			lew.writeBoolean((mr instanceof MiniGame ? ((MiniGame) mr).hasStarted() : false));
		}

		public static byte[] useChalkboard(MapleCharacter chr, boolean close){// CUser::OnADBoard
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.CHALKBOARD.getValue());
			mplew.writeInt(chr.getId());
			if(close){
				mplew.write(0);
			}else{
				mplew.write(1);
				mplew.writeMapleAsciiString(chr.getChalkboard());
			}
			return mplew.getPacket();
		}
	}
}
