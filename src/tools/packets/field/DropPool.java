package tools.packets.field;

import java.awt.Point;

import net.SendOpcode;
import server.maps.MapleMapItem;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jul 30, 2017
 */
public class DropPool{

	// OwnType
	public static final int UserOwn = 0x0;
	public static final int PartyOwn = 0x1;
	public static final int NoOwn = 0x2;
	public static final int Explosive_NoOwn = 0x3;
	// EnterType
	public static final int JustShowing = 0x0;
	public static final int Create = 0x1;
	public static final int OnTheFoothold = 0x2;
	public static final int FadingOut = 0x3;
	// LeaveType
	public static final int ByTimeOut = 0x0;
	public static final int ByScreenScroll = 0x1;
	public static final int PickedUpByUser = 0x2;
	public static final int PickedUpByMob = 0x3;
	public static final int Explode = 0x4;
	public static final int PickedUpByPet = 0x5;
	public static final int PassConvex = 0x6;
	public static final int SkillPet = 0x7;

	public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		mplew.write(mod);
		mplew.writeInt(drop.getObjectId());
		mplew.writeBoolean(drop.getMeso() > 0); // 1 mesos, 0 item, 2 and above all item meso bag,
		mplew.writeInt(drop.getItemId()); // drop object ID
		mplew.writeInt(drop.getDropType() > 0 ? drop.getOwner() : drop.getOwnerChrId()); // might need to set this to 0 if drop type > 1
		mplew.write(drop.getDropType()); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
		mplew.writePos(dropto);
		mplew.writeInt(drop.getDropType() == 0 ? drop.getOwner() : 0);
		// mplew.writeInt(mod == 2 ? drop.getDropper().getObjectId() : drop.getOwner());
		if(mod == JustShowing || mod == Create || mod == FadingOut || mod == Explode){
			mplew.writePos(dropfrom);
			mplew.writeShort(0);// delay
		}
		if(drop.getMeso() == 0){
			MaplePacketCreator.addExpirationTime(mplew, drop.getItem().getExpiration());
		}
		mplew.write(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, byte enterType, int delay){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
		mplew.write(enterType);
		mplew.writeInt(drop.getObjectId());
		mplew.writeBoolean(drop.getMeso() > 0); // 1 mesos, 0 item, 2 and above all item meso bag,
		mplew.writeInt(drop.getItemId()); // drop object ID
		mplew.writeInt(drop.getDropType() > 0 ? drop.getOwner() : drop.getOwnerChrId()); // might need to set this to 0 if drop type > 1
		mplew.write(drop.getDropType()); // OwnType - 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
		mplew.writePos(drop.getPosition());
		mplew.writeInt(drop.getDropType() == 0 ? drop.getOwner() : 0);
		if(enterType == JustShowing || enterType == Create || enterType == FadingOut || enterType == Explode){
			mplew.writePos(dropfrom);
			mplew.writeShort(delay);
		}
		if(drop.getMeso() == 0){
			MaplePacketCreator.addExpirationTime(mplew, drop.getItem().getExpiration());
		}
		mplew.write(drop.isPlayerDrop() ? 0 : 1); // pet EQP pickup
		mplew.write(0);//
		return mplew.getPacket();
	}

	/**
	 * animation: 0 - expire<br/>
	 * 1 - without animation<br/>
	 * 2 - pickup<br/>
	 * 4 -
	 * explode<br/>
	 * cid is ignored for 0 and 1
	 *
	 * @param oid
	 * @param animation
	 * @param cid
	 * @return
	 */
	public static byte[] removeItemFromMap(int oid, int animation, int cid){
		return removeItemFromMap(oid, animation, cid, false, 0);
	}

	/**
	 * animation: 0 - expire<br/>
	 * 1 - without animation<br/>
	 * 2 - pickup<br/>
	 * 4 -
	 * explode<br/>
	 * cid is ignored for 0 and 1.<br />
	 * <br />
	 * Flagging pet as true
	 * will make a pet pick up the item.
	 *
	 * @param oid
	 * @param animation
	 * @param cid
	 * @param pet
	 * @param slot
	 * @return
	 */
	public static byte[] removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
		mplew.write(animation); // expire
		mplew.writeInt(oid);
		if(animation == 2 || animation == 3 || animation == 5){
			mplew.writeInt(cid);
		}else if(animation == 4){
			mplew.writeShort(0);// prob delay
			return mplew.getPacket();
		}
		if(animation == 5){
			mplew.writeInt(slot);
		}
		return mplew.getPacket();
	}
}
