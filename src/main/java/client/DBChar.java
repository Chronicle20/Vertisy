package client;

import client.inventory.MapleInventoryType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Mar 2, 2017
 */
public class DBChar{

	public static final int Character = 0x1, Money = 0x2, ItemSlotEquip = 0x4, ItemSlotConsume = 0x8, ItemSlotInstall = 0x10, ItemSlotEtc = 0x20, ItemSlotCash = 0x40, InventorySize = 0x80, SkillRecord = 0x100, QuestRecord = 0x200, MiniGameRecord = 0x400, CoupleRecord = 0x800, MapTransfer = 0x1000, Avatar = 0x2000, // center uses this
	        QuestComplete = 0x4000, SkillCooltime = 0x8000, MonsterBookCard = 0x10000, MonsterBookCover = 0x20000, NewYearCard = 0x40000, QuestRecordEx = 0x80000, AdminShopCount = 0x100000, EquipExt = 0x100000, WildHunterInfo = 0x200000, QuestComplete_Old = 0x400000, VisitorLog = 0x800000;

	public static int getByInventoryType(MapleInventoryType mit){
		switch (mit){
			case CASH:
				return ItemSlotCash;
			case EQUIP:
				return ItemSlotEquip;
			case EQUIPPED:
				return -1;
			case ETC:
				return ItemSlotEtc;
			case SETUP:
				return ItemSlotInstall;
			case UNDEFINED:
				return -1;
			case USE:
				return ItemSlotConsume;
			default:
				return -1;
		}
	}
}