package tools.packets;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.field.userpool.UserEffectType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Sep 16, 2017
 */
public class UserLocal{

	public static byte[] lockUI(boolean enable){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.LOCK_UI.getValue());
		mplew.write(enable ? 1 : 0);
		mplew.writeInt(0);// m_tAfterLeaveDirectionMode, adds to get_update_time, only for disable pretty sure
		return mplew.getPacket();
	}

	public static byte[] disableUI(boolean enable){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DISABLE_UI.getValue());
		mplew.write(enable ? 1 : 0);
		return mplew.getPacket();
	}

	public static byte[] showCombo(int count){// OnIncComboResponse
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.SHOW_COMBO.getValue());
		mplew.writeInt(count);
		return mplew.getPacket();
	}

	public static byte[] followCharacterFailed(int nError, int dwDriverID){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.FOLLOW_CHARACTER_FAILED.getValue());
		mplew.writeInt(nError);
		mplew.writeInt(dwDriverID);// needed for already following
		return mplew.getPacket();
	}

	public static class FollowCharacterFailType{

		public static final int INVALID_PLACE = 1, IS_FOLLOWING = 2/*Needs dwDriverID to be set, otherwise does 1*/, CANT_ACCEPT = 3, ALREADY_FOLLOWING = 4/*Requester is following someone*/, DENIED = 5, TOO_FAR = 6, UNKNOWN = 7;
	}

	public static class UserEffect{

		/**
		 * Gets a packet telling the client to show a item gain.
		 *
		 * @param itemId The ID of the item gained.
		 * @param quantity How many items gained.
		 * @return The item gain packet.
		 */
		public static byte[] getShowItemGain(int itemId, short quantity){
			return getShowItemGain(itemId, quantity, false);
		}

		/**
		 * Gets a packet telling the client to show an item gain.
		 *
		 * @param itemId The ID of the item gained.
		 * @param quantity The number of items gained.
		 * @param inChat Show in the chat window?
		 * @return The item gain packet.
		 */
		public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			if(inChat){
				mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
				mplew.write(UserEffectType.Quest);
				mplew.write(1);
				mplew.writeInt(itemId);
				mplew.writeInt(quantity);
			}else{
				mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
				mplew.writeShort(0);
				mplew.writeInt(itemId);
				mplew.writeInt(quantity);
				mplew.writeInt(0);
				mplew.writeInt(0);
			}
			return mplew.getPacket();
		}

		public static byte[] showOwnBuffEffect(int skillid, int effectid){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(effectid);
			mplew.writeInt(skillid);
			mplew.write(0xA9);
			mplew.write(1);
			return mplew.getPacket();
		}

		public static byte[] showOwnBerserk(int skilllevel, boolean Berserk){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.SkillUse);// bunch of skillid checks for other shit
			mplew.writeInt(1320006);
			mplew.write(0xA9);
			mplew.write(skilllevel);
			mplew.write(Berserk ? 1 : 0);
			return mplew.getPacket();
		}

		public static byte[] showOwnPetLevelUp(byte index){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.Pet);
			mplew.write(0);
			mplew.write(index); // Pet Index
			return mplew.getPacket();
		}

		public static byte[] showGainCard(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.MonsterBookCardGet);
			return mplew.getPacket();
		}

		public static byte[] showIntro(String path){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.ReservedEffect);
			mplew.writeMapleAsciiString(path);
			return mplew.getPacket();
		}

		public static byte[] showEffect(String path){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.ReservedEffect);
			mplew.writeMapleAsciiString(path);
			return mplew.getPacket();
		}

		public static byte[] playPortalSound(){
			return showSpecialEffect(UserEffectType.PlayPortalSE);
		}

		public static byte[] showMonsterBookPickup(){
			return showSpecialEffect(UserEffectType.MonsterBookCardGet);
		}

		public static byte[] showEquipmentLevelUp(){
			return showSpecialEffect(UserEffectType.ItemLevelUp);
		}

		public static byte[] showItemLevelup(){
			return showSpecialEffect(UserEffectType.ItemLevelUp);
		}

		/**
		 * 6 = Exp did not drop (Safety Charms)
		 * 7 = Enter portal sound
		 * 8 = Job change
		 * 9 = Quest complete
		 * 10 = Recovery
		 * 14 = Monster book pickup
		 * 15 = Equipment levelup
		 * 16 = Maker Skill Success
		 * 19 = Exp card [500, 200, 50]
		 *
		 * @param effect
		 * @return
		 */
		public static byte[] showSpecialEffect(int effect){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(effect);
			if(effect == UserEffectType.ItemMaker){ // Maker
				mplew.writeInt(0);
			}
			return mplew.getPacket();
		}

		/**
		 * Used for stuff in Effect.wz
		 */
		public static byte[] showInfo(String path){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.AvatarOriented);
			mplew.writeMapleAsciiString(path);
			mplew.writeInt(1);
			return mplew.getPacket();
		}

		public static byte[] showOwnRecovery(byte heal){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.IncDecHPEffect);
			mplew.write(heal);
			return mplew.getPacket();
		}

		public static byte[] showWheelsLeft(int left){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.ProtectOnDieItemUse);
			mplew.write(left);
			return mplew.getPacket();
		}

		public static byte[] questComplete(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.QuestComplete);
			return mplew.getPacket();
		}

		public static byte[] reservedEffect(String effect){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
			mplew.write(UserEffectType.ReservedEffect);
			mplew.writeMapleAsciiString(effect);
			return mplew.getPacket();
		}
	}
}
