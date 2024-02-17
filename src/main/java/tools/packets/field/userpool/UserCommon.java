package tools.packets.field.userpool;

import java.awt.Point;

import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Oct 20, 2017
 */
public class UserCommon{// All need chr pretty sure

	public static byte[] followCharacter(int cid, int driverID){
		return followCharacter(cid, driverID, null);
	}

	public static byte[] removeFollow(int cid, Point transferFieldPos){
		return followCharacter(cid, 0, transferFieldPos);
	}

	private static byte[] followCharacter(int cid, int driverID, Point transferFieldPos){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FOLLOW_CHARACTER.getValue());
		mplew.writeInt(cid);
		mplew.writeInt(driverID);
		mplew.writeBoolean(transferFieldPos != null);
		if(transferFieldPos != null){
			mplew.writeInt(transferFieldPos.x);
			mplew.writeInt(transferFieldPos.y);
		}
		return mplew.getPacket();
	}

	public static byte[] getScrollEffect(int chr, boolean success, boolean cursed, boolean enchantSkill, int enchantCategory, boolean usedWhiteScroll, boolean recoverable){// CUser::ShowItemUpgradeEffect
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		mplew.writeBoolean(cursed);
		mplew.writeBoolean(enchantSkill);
		mplew.writeInt(enchantCategory);
		mplew.writeBoolean(usedWhiteScroll);
		mplew.writeBoolean(recoverable);
		return mplew.getPacket();
	}

	public static byte[] showItemReleaseEffect(int chr, short slot){// CUser::ShowItemReleaseEffect
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_RELEASE_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeShort(slot);
		return mplew.getPacket();
	}

	public static byte[] showItemOptionUpgradeEffect(int chr, boolean success, boolean cursed, boolean enchantSkill, int enchantCategory){// CUser::ShowItemOptionUpgradeEffect
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_OPTION_UPGRADE_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		mplew.writeBoolean(cursed);
		mplew.writeBoolean(enchantSkill);
		mplew.writeInt(enchantCategory);
		return mplew.getPacket();
	}

	public static byte[] showItemUnreleaseEffect(int chr, boolean success){// CUser::ShowItemUnreleaseEffect
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_UNRELEASE_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		// Potential successfully reset.\r\nMiracle Cube Fragment obtained!
		// Resetting Potential has failed due to insufficient space in the Use item.
		return mplew.getPacket();
	}

	public static byte[] showItemHyperUpgradeEffect(int chr, boolean success, boolean cursed, boolean enchantSkill, int enchantCategory){// CUser::ShowItemHyperUpgradeEffect
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_ITEM_HYPER_UPGRADE_EFFECT.getValue());
		mplew.writeInt(chr);
		mplew.writeBoolean(success);
		mplew.writeBoolean(cursed);
		mplew.writeBoolean(enchantSkill);
		mplew.writeInt(enchantCategory);
		return mplew.getPacket();
	}
}
