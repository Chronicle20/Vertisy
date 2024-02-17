/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package constants;

import java.util.HashSet;
import java.util.Set;

import client.inventory.MapleInventoryType;
import server.item.Potential;

/**
 * @author Jay Estrella
 */
public final class ItemConstants{

	public final static int LOCK = 0x01;
	public final static int SPIKES = 0x02;
	public final static int COLD = 0x04;
	public final static int UNTRADEABLE = 0x08;
	public final static int KARMA = 0x10;
	public final static int PET_COME = 0x80;
	public final static int ACCOUNT_SHARING = 0x100;
	public final static float ITEM_ARMOR_EXP = 1 / 350000;
	public static final float ITEM_WEAPON_EXP = 1 / 700000;
	public final static boolean EXPIRING_ITEMS = true;
	public static int PET_ITEM_VAC = 5220010;
	public static int[] ELITE_ITEMS = {PET_ITEM_VAC};
	public static int MAX_SCROLLS = -1;// MapleItemInformationProvider, if "tuc" is > 0, set max to this. -1 to disable
	public static Set<Integer> AUTO_SELL_BLACKLIST = new HashSet<>();

	public static int getFlagByInt(int type){
		if(type == 128){
			return PET_COME;
		}else if(type == 256){ return ACCOUNT_SHARING; }
		return 0;
	}

	public static boolean isSpecialItem(int itemid){
		switch (itemid){
			case 4001126:// maple leaf
			case 5220000:// gachapon
			case 5451000:// remote gacha
			case 4031530:
			case 4031531:
			case 4000514:// bone
			case 4032473:// bone
			case 4007014:// exp gem
			case 4007015:// exp gem
			case 4007016:// exp gem
			case 4031203:// Halloween Candies
			case 4032056:// Magic Crystal for halloween
				return true;
		}
		return false;
	}

	public static boolean isThrowingStar(int itemId){
		return itemId / 10000 == 207;
	}

	public static boolean isBullet(int itemId){
		return itemId / 10000 == 233;
	}

	public static boolean isRechargable(int itemId){
		return isThrowingStar(itemId) || isBullet(itemId);
	}

	public static boolean isArrowForCrossBow(int itemId){
		return itemId / 1000 == 2061;
	}

	public static boolean isArrowForBow(int itemId){
		return itemId / 1000 == 2060;
	}

	public static boolean isPet(int itemId){
		return itemId / 1000 == 5000;
	}

	public static int get_weapon_type(int nItemID){
		if(nItemID / 1000000 == 1){
			int result = nItemID / 10000 % 100;
			switch (result){
				default:
					return 0;
				case 30:
				case 31:
				case 32:
				case 33:
				case 34:
				case 37:
				case 38:
				case 39:
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
				case 49:
					return result;
			}
		}
		return 0;
	}

	public static MapleInventoryType getInventoryType(final int itemId){
		final byte type = (byte) (itemId / 1000000);
		if(type < 1 || type > 5) return MapleInventoryType.UNDEFINED;
		return MapleInventoryType.getByType(type);
	}

	public static boolean is_chair(int id){
		return id / 10000 == 301;
	}

	public static int get_gender_from_id(int nItemID){
		if(nItemID / 1000000 == 1){
			switch (nItemID / 1000 % 10){
				case 0:
					return 0;
				case 1:
					return 1;
				default:
					return 2;
			}
		}else return 2;
	}


	public static boolean IsLongCoat(int nItemID){
		return nItemID / 10000 == 105;
	}

	public static boolean IsWeapon(int itemid){
		return itemid / 10000 >= 130 && itemid / 10000 <= 170;
	}

	public static boolean IsGlove(int itemid){
		return itemid / 10000 == 108;
	}

	public static boolean isValidOptionType(Potential pOption, int nItemID){
		if(pOption == null) return false;
		int nBodyPart = getBodyPartFromItem(nItemID, 2);
		if(nItemID / 1000000 != 1 || nBodyPart == 0) return false;
		switch (pOption.optionType){
			case 90:
				return false;
			default:
				return true;
		}
	}

	public static int getBodyPartFromItem(int nItemID, int nGender){
		int v4; // eax@1
		int v6; // ecx@5
		int v7; // eax@66
		v4 = get_gender_from_id(nItemID);
		if(nGender == 2 || v4 == 2 || v4 == nGender){
			v6 = nItemID / 10000;
			switch (nItemID / 10000){
				case 100:
					return 1;
				case 101:
					return 2;
				case 102:
					return 3;
				case 103:
					return 4;
				case 104:
				case 105:
					return 5;
				case 106:
					return 6;
				case 107:
					return 7;
				case 108:
					return 8;
				case 109:
				case 119:
				case 134:
					return 10;
				case 110:
					return 9;
				case 111:
					return 12;
				case 112:
					return 17;
				case 113:
					return 50;
				case 114:
					return 49;
				case 115:
					return 51;
				case 165:
					return 1104;
				case 161:
					return 1100;
				case 162:
					return 1101;
				case 163:
					return 1102;
				case 164:
					return 1103;
				case 190:
					return 18;
				case 191:
					return 19;
				case 192:
					return 20;
				case 194:
					return 1000;
				case 195:
					return 1001;
				case 196:
					return 1002;
				case 197:
					return 1003;
				case 180:
					if(nItemID == 1802100) return 21;
					else return 14;
				case 181:
					switch (nItemID){
						case 1812000:
							return 23;
						case 1812001:
							return 22;
						case 1812002:
							return 24;
						case 1812003:
							return 25;
						case 1812004:
							return 26;
						case 1812005:
							return 27;
						case 1812006:
							return 28;
						case 1812007:
							return 46;
						default:
							return 21;
					}
				case 182:
					return 21;
				case 183:
					return 29;
				default:
					v7 = v6 / 10;
					if(v6 / 10 != 13 && v7 != 14 && v7 != 16 && v7 != 17) return 0;
					return 11;
			}
		}else{
			return 0;
		}
	}

	public static boolean dontLogItemUse(int itemid){
		if(itemid / 10000 == 200 || itemid / 10000 == 201) return true;
		return false;
	}
}
