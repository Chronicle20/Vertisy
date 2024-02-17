package tools.packets.field.userpool;

import client.MapleCharacter;
import net.SendOpcode;
import net.server.guild.MapleGuild;
import server.movement.MovePath;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 6, 2017
 */
public class UserRemote{// All of these start with an int chrid

	public static byte[] OnEmotion(MapleCharacter from, int expression, boolean usedCashItem){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
		mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(expression);
		mplew.writeInt(10000);// tDuration, allows you to control how long you do a facial expression for. Has a max
		mplew.writeBoolean(usedCashItem);// m_bEmotionByItemOption
		return mplew.getPacket();
	}

	public static byte[] itemEffect(int characterid, int itemid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	public static byte[] movePlayer(int cid, MovePath moves){// CUserRemote::OnMove
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
		mplew.writeInt(cid);
		moves.encode(mplew);
		return mplew.getPacket();
	}

	/**
	 * @param guildName The Guild name, blank for nothing.
	 */
	public static byte[] guildNameChanged(int chrid, String guildName){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_NAME_CHANGED);
		mplew.writeInt(chrid);
		mplew.writeMapleAsciiString(guildName);
		return mplew.getPacket();
	}

	public static byte[] guildMarkChanged(int chrid, short logoBG, byte logoBGColor, short logo, byte logoColor){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_MARK_CHANGED);
		mplew.writeInt(chrid);
		mplew.writeShort(logoBG);
		mplew.write(logoBGColor);
		mplew.writeShort(logo);
		mplew.write(logoColor);
		return mplew.getPacket();
	}

	public static byte[] guildMarkChanged(int chrid, MapleGuild guild){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_MARK_CHANGED);
		mplew.writeInt(chrid);
		mplew.writeShort(guild.getLogoBG());
		mplew.write(guild.getLogoBGColor());
		mplew.writeShort(guild.getLogo());
		mplew.write(guild.getLogoColor());
		return mplew.getPacket();
	}

	public static byte[] updateCharLook(MapleCharacter chr){// CUserRemote::OnAvatarModified
		int type = 1;
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(type);
		if((type & AvatarModifiedType.LOOK) > 0){
			MaplePacketCreator.addCharLook(mplew, chr, false);
		}
		if((type & AvatarModifiedType.SPEED) > 0){
			mplew.write(0);// speed
		}
		if((type & AvatarModifiedType.CHOCO) > 0){
			mplew.write(0);// choco?
		}
		MaplePacketCreator.addRingLook(mplew, chr.getCrushRings());
		MaplePacketCreator.addRingLook(mplew, chr.getFriendshipRings());
		MaplePacketCreator.addMarriageRingLook(mplew, chr);
		mplew.writeInt(0);// completedSetItemID
		return mplew.getPacket();
	}

	public static class AvatarModifiedType{

		public static final int LOOK = 0x1, SPEED = 0x2, CHOCO = 0x4;
	}

	public static class UserEffect{

		public static byte[] questComplete(int chrid){
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT);
			mplew.writeInt(chrid);
			mplew.write(UserEffectType.QuestComplete);
			return mplew.getPacket();
		}

		/**
		 * Used for stuff in Effect.wz
		 */
		public static byte[] showInfo(int chrid, String path){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(chrid);
			mplew.write(UserEffectType.AvatarOriented);
			mplew.writeMapleAsciiString(path);
			mplew.writeInt(1);
			return mplew.getPacket();
		}

		public static byte[] showPetLevelUp(MapleCharacter chr, byte index){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(chr.getId());
			mplew.write(UserEffectType.Pet);
			mplew.write(0);
			mplew.write(index);
			return mplew.getPacket();
		}

		public static byte[] showForeginCardEffect(int id){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(id);
			mplew.write(UserEffectType.MonsterBookCardGet);
			return mplew.getPacket();
		}

		public static byte[] showForeignEffect(int cid, int effect){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(cid);
			mplew.write(effect);
			if(effect == UserEffectType.ItemMaker){ // Maker
				mplew.writeInt(0);
			}
			return mplew.getPacket();
		}

		public static byte[] showRecovery(int cid, byte amount){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(cid);
			mplew.write(UserEffectType.IncDecHPEffect);
			mplew.write(amount);
			return mplew.getPacket();
		}

		public static byte[] showBerserk(int cid, int skilllevel, boolean Berserk){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(cid);
			mplew.write(UserEffectType.SkillUse);
			mplew.writeInt(1320006);
			mplew.write(0xA9);
			mplew.write(skilllevel);
			mplew.write(Berserk ? 1 : 0);
			return mplew.getPacket();
		}

		public static byte[] showBuffeffect(int cid, int skillid, int effectid){
			return showBuffeffect(cid, skillid, effectid, (byte) 3);
		}

		public static byte[] showBuffeffect(int cid, int skillid, int effectid, byte direction){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
			mplew.writeInt(cid);
			mplew.write(effectid); // buff level
			mplew.writeInt(skillid);
			mplew.write(direction);
			mplew.write(1);
			mplew.writeLong(0);
			return mplew.getPacket();
		}
	}
}