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
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import client.*;
import client.MapleCharacter.SkillEntry;
import client.inventory.*;
import constants.EquipSlot;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.ThunderBreaker;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.PlayerCoolDownValueHolder;
import net.server.channel.handlers.AbstractDealDamageHandler.AttackInfo;
import net.server.channel.handlers.PlayerInteractionHandler;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.world.WorldServer;
import scripting.npc.NPCConversationManager;
import scripting.npc.ScriptMessageType;
import server.*;
import server.events.gm.MapleSnowball;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.objects.MapleDragon;
import server.maps.objects.PlayerNPC;
import server.movement.MovePath;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Frz
 */
public class MaplePacketCreator{

	private final static long FT_UT_OFFSET = 116444592000000000L; // EDT
	private final static long DEFAULT_TIME = 150842304000000000L;// 00 80 05 BB 46 E6 17 02
	public final static long ZERO_TIME = 94354848000000000L;// 00 40 E0 FD 3B 37 4F 01
	private final static long PERMANENT = 150841440000000000L; // 00 C0 9B 90 7D E5 17 02

	public static long getTime(long realTimestamp){
		if(realTimestamp == -1){
			return DEFAULT_TIME;// high number ll
		}else if(realTimestamp == -2){
			return ZERO_TIME;
		}else if(realTimestamp == -3){ return PERMANENT; }
		return realTimestamp * 10000 + FT_UT_OFFSET;
	}

	private static void addCharStats(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){// GW_CharacterStat::Decode
//		mplew.writeInt(0); // damage skin

		mplew.writeInt(chr.getId()); // character id
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));// sCharacterName
		mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
		mplew.write(chr.getSkinColor().getId()); // skin color
		mplew.writeInt(chr.getFace()); // face
		mplew.writeInt(chr.getHair()); // hair
		for(int i = 0; i < 3; i++){// pet locker sn, 24 bytes
			if(chr.getPet(i) != null){ // Checked GMS.. and your pets stay when going into the cash shop.
				mplew.writeLong(chr.getPet(i).getUniqueId());
			}else{
				mplew.writeLong(0);
			}
		}
		mplew.write(chr.getLevel()); // level
		mplew.writeShort(chr.getJob().getId()); // job
		mplew.writeShort(chr.getStr()); // str
		mplew.writeShort(chr.getDex()); // dex
		mplew.writeShort(chr.getInt()); // int
		mplew.writeShort(chr.getLuk()); // luk
		// mplew.writeInt(40000); // hp (?)
		// mplew.writeInt(40000); // maxhp
		mplew.writeShort(chr.getHp());
		mplew.writeShort(chr.getMaxHp());
		mplew.writeShort(chr.getMp()); // mp (?)
		mplew.writeShort(chr.getMaxMp()); // maxmp
		mplew.writeShort(chr.getRemainingAp()); // remaining ap
		if(GameConstants.hasExtendedSPTable(chr.getJob())){
			mplew.write(chr.getRemainingSpSize());
			for(int i = 0; i < chr.getRemainingSps().length; i++){
				if(chr.getRemainingSpBySkill(i) > 0){
					mplew.write(i);
					mplew.write(chr.getRemainingSpBySkill(i));
				}
			}
		}else{
			mplew.writeShort(chr.getRemainingSp()); // remaining sp
		}
		mplew.writeInt(chr.getExp()); // current exp
		mplew.writeShort(chr.getFame()); // fame
		mplew.writeInt(chr.getGachaExp()); // Gacha Exp
		mplew.writeInt(chr.getMapId()); // current map id
		mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
		mplew.writeInt(0);// nPlayTime
		mplew.writeShort(chr.getSubJob());
	}

	public static void addCharLook(final LittleEndianWriter mplew, MapleCharacter chr, boolean mega){
		mplew.write(chr.getGender());
		mplew.write(chr.getSkinColor().getId()); // skin color
		mplew.writeInt(chr.getFace()); // face
		mplew.write(mega ? 0 : 1);
		mplew.writeInt(chr.getHair()); // hair
		addCharEquips(mplew, chr);
	}

	private static void addCharLook(final MaplePacketLittleEndianWriter mplew, MapleCharacterLook mcl, boolean mega){
		mplew.write(mcl.getGender());
		mplew.write(mcl.getSkinColor().getId()); // skin color
		mplew.writeInt(mcl.getFace()); // face
		mplew.write(mega ? 0 : 1);
		mplew.writeInt(mcl.getHair()); // hair
		addCharEquips(mplew, mcl);
	}

	public static void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){// CharacterData::Decode
		long mask = -1
		/*DBChar.Character | DBChar.Money | DBChar.InventorySize | 
		DBChar.ItemSlotCash | DBChar.ItemSlotConsume | DBChar.ItemSlotEquip | 
		DBChar.ItemSlotEtc | DBChar.ItemSlotInstall | DBChar.SkillRecord | DBChar.SkillCooltime |
		DBChar.QuestRecord | DBChar.QuestComplete | DBChar.MiniGameRecord | DBChar.CoupleRecord |
		DBChar.MapTransfer | DBChar.MonsterBookCard | DBChar.MonsterBookCover | DBChar.NewYearCard |
		        DBChar.QuestRecordEx | DBChar.WildHunterInfo | DBChar.EquipExt*/;
		mplew.writeLong(mask);
		mplew.write(0);
		if((mask & DBChar.Character) > 0){
			addCharStats(mplew, chr);// GW_CharacterStat::Decode
			mplew.write(chr.getBuddylist().getCapacity());// friendMax
			if(chr.getExplorerLinkedName() == null){
				mplew.write(0);
			}else{
				mplew.write(1);
				mplew.writeMapleAsciiString(chr.getExplorerLinkedName());
			}
		}
		if((mask & DBChar.Money) > 0) mplew.writeInt(chr.getMeso());
		addInventoryInfo(mask, mplew, chr);
		if((mask & DBChar.SkillRecord) > 0) addSkillInfo(mplew, chr);
		if((mask & DBChar.SkillCooltime) > 0) addSkillCooldownInfo(mplew, chr);
		if((mask & DBChar.QuestRecord) > 0) addQuestInfo(mplew, chr);
		if((mask & DBChar.QuestComplete) > 0) addCompletedQuestInfo(mplew, chr);
		if((mask & DBChar.MiniGameRecord) > 0) addMiniGameInfo(mplew, chr);
		if((mask & DBChar.CoupleRecord) > 0) addRingInfo(mplew, chr);
		if((mask & DBChar.MapTransfer) > 0) addTeleportInfo(mplew, chr);
		if((mask & DBChar.MonsterBookCover) > 0) mplew.writeInt(chr.getMonsterBookCover());
		if((mask & DBChar.MonsterBookCard) > 0) addMonsterBookInfo(mplew, chr);
		if((mask & DBChar.NewYearCard) > 0) addNewYearInfo(mplew, chr);
		if((mask & DBChar.QuestRecordEx) > 0) addAreaInfo(mplew, chr);
		if((mask & DBChar.AdminShopCount) > 0) mplew.writeShort(0);// in v90, but not v95 I think
		if((mask & DBChar.WildHunterInfo) > 0){
			mplew.writeShort(0);
			/*different in v95
			v140 = CInPacket::Decode2(v131);
			if ( v140 > 0 )
			{
			v141 = v140;
			do
			{
			a3 = CInPacket::Decode2(v131);
			a2 = CInPacket::Decode2(v131);
			sub_525514(&a3, &a2);
			--v141;
			}
			while ( v141 );
			}
			
			 */
		}
		// these are in v95, but not v90.
		//
		//
		// quest complete old
		// if((mask & DBChar.QuestComplete_Old) > 0){
		// mplew.writeShort(0);
		/*
		 *     v186 = CInPacket::Decode2(v172);
		ZMap<unsigned_short__FILETIME_unsigned_short>::RemoveAll(&v174->mQuestCompleteOld);
		for ( ; v186 > 0; --v186 )
		{
		iPacket = CInPacket::Decode2(v172);
		CInPacket::DecodeBuffer(v172, &ftEnd, 8u);
		ZMap<unsigned_short__FILETIME_unsigned_short>::Insert(&v174->mQuestCompleteOld, &iPacket, &ftEnd);
		}
		
		 */
		// }
		// if((mask & DBChar.VisitorLog) > 0){
		// mplew.writeShort(0);
		/*
		 *     v188 = CInPacket::Decode2(v172);
		if ( v188 > 0 )
		{
		v189 = v188;
		do
		{
		bBackwardUpdate = CInPacket::Decode2(v172);
		iPacket = CInPacket::Decode2(v172);
		ZMap<long_long_long>::Insert(&v174->m_mVisitorQuestLog, &bBackwardUpdate, &iPacket);
		--v189;
		}
		while ( v189 );
		}
		
		 */
		// }
	}

	private static void addNewYearInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.writeShort(0);
		/*  
		 *(_DWORD *)this = CInPacket::Decode4(a2);
		 *((_DWORD *)v2 + 1) = CInPacket::Decode4(a2);
		 CInPacket::DecodeStr(&v7);
		 v9 = 0;
		 (*(void (__stdcall **)(char *, int))((char *)&loc_B1410B + 1))((char *)v2 + 8, v7);
		 *(_DWORD *)((char *)v2 + 21) = (unsigned __int8)CInPacket::Decode1(a2);
		 CInPacket::DecodeBuffer((char *)v2 + 25, 8);
		 *(_DWORD *)((char *)v2 + 33) = CInPacket::Decode4(a2);
		 CInPacket::DecodeStr(&v6);
		 LOBYTE(v8) = 1;
		 (*(void (__stdcall **)(char *, int))((char *)&loc_B1410B + 1))((char *)v2 + 37, v6);
		 *(_DWORD *)((char *)v2 + 50) = (unsigned __int8)CInPacket::Decode1(a2);
		 *(_DWORD *)((char *)v2 + 54) = (unsigned __int8)CInPacket::Decode1(a2);
		 CInPacket::DecodeBuffer((char *)v2 + 58, 8);
		 CInPacket::DecodeStr(&v9);
		 */
	}

	private static void addTeleportInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		final List<Integer> tele = chr.getTrockMaps();
		final List<Integer> viptele = chr.getVipTrockMaps();
		for(int i = 0; i < 5; i++){
			mplew.writeInt(tele.get(i));
		}
		for(int i = 0; i < 10; i++){
			mplew.writeInt(viptele.get(i));
		}
	}

	private static void addMiniGameInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.writeShort(0);
		/*for (int m = size; m > 0; m--) {//nexon does this :P
		 mplew.writeInt(0);
		 mplew.writeInt(0);
		 mplew.writeInt(0);
		 mplew.writeInt(0);
		 mplew.writeInt(0);
		 }*/
	}

	private static void addAreaInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		Map<Short, String> areaInfos = chr.getAreaInfos();
		mplew.writeShort(areaInfos.size());
		for(Short area : areaInfos.keySet()){
			mplew.writeShort(area);
			mplew.writeMapleAsciiString(areaInfos.get(area));
		}
	}

	private static void addCharEquips(final LittleEndianWriter mplew, MapleCharacter chr){
		MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<Item> ii = ItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
		Map<Short, Integer> myEquip = new LinkedHashMap<>();
		Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
		for(Item item : ii){
			short pos = (byte) (item.getPosition() * -1);
			if(pos < 100 && myEquip.get(pos) == null){
				myEquip.put(pos, item.getItemId());
			}else if(pos > 100 && pos != 111){ // don't ask. o.o
				pos -= 100;
				if(myEquip.get(pos) != null){
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, item.getItemId());
			}else if(myEquip.get(pos) != null){
				maskedEquip.put(pos, item.getItemId());
			}
		}
		for(Entry<Short, Integer> entry : myEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		for(Entry<Short, Integer> entry : maskedEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		Item cWeapon = equip.getItem((short) -111);
		mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
		for(int i = 0; i < 3; i++){
			if(chr.getPet(i) != null){
				mplew.writeInt(chr.getPet(i).getItemId());
			}else{
				mplew.writeInt(0);
			}
		}
	}

	private static void addCharEquips(final MaplePacketLittleEndianWriter mplew, MapleCharacterLook mcl){
		Map<Short, Integer> myEquip = new LinkedHashMap<>();
		Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
		for(Short p : mcl.getEquips().keySet()){
			int itemid = mcl.getEquips().get(p);
			short pos = (byte) (p * -1);
			if(pos < 100 && myEquip.get(pos) == null){
				myEquip.put(pos, itemid);
			}else if(pos > 100 && pos != 111){ // don't ask. o.o
				pos -= 100;
				if(myEquip.get(pos) != null){
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, itemid);
			}else if(myEquip.get(pos) != null){
				maskedEquip.put(pos, itemid);
			}
		}
		for(Entry<Short, Integer> entry : myEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		for(Entry<Short, Integer> entry : maskedEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		Integer cWeapon = mcl.getEquips().get((short) -111);
		mplew.writeInt(cWeapon != null ? cWeapon : 0);// nWeaponStickerID
		for(int i = 0; i < 3; i++){
			mplew.writeInt(0);
		}
	}

	public static void addCharEntry(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall){
		addCharStats(mplew, chr);
		addCharLook(mplew, chr, false);
		if(!viewall){
			mplew.write(0);
		}
		if(chr.isGM()){
			mplew.write(0);
			return;
		}
		mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
		mplew.writeInt(chr.getRank()); // world rank
		mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
		mplew.writeInt(chr.getJobRank()); // job rank
		mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
	}

	private static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.writeShort(chr.getStartedQuestsSize());
		for(MapleQuestStatus q : chr.getStartedQuests()){
			mplew.writeShort(q.getQuest().getId());
			mplew.writeMapleAsciiString(q.getQuestData());
			// if(q.getQuest().getInfoNumber() > 0){
			// System.out.println("info number quest");
			// }
			if(q.getQuest().startQuestData.infoNumber > 0){
				mplew.writeShort(q.getQuest().startQuestData.infoNumber);
				mplew.writeMapleAsciiString(q.getQuestData());
			}
		}
	}

	private static void addCompletedQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		List<MapleQuestStatus> completed = chr.getCompletedQuests();
		mplew.writeShort(completed.size());
		for(MapleQuestStatus q : completed){
			mplew.writeShort(q.getQuest().getId());
			mplew.writeLong(getTime(q.getCompletionTime()));
		}
	}

	private static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item){
		addItemInfo(mplew, item, false);
	}

	public static void addExpirationTime(final MaplePacketLittleEndianWriter mplew, long time){
		mplew.writeLong(getTime(time));
	}

	public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item, boolean zeroPosition){
		addItemInfo(mplew, item, zeroPosition, false);
	}

	public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item, boolean zeroPosition, boolean addBundleAmount){
		ItemInformationProvider ii = ItemInformationProvider.getInstance();
		ItemData itemData = ii.getItemData(item.getItemId());
		boolean isPet = item.getPetId() > -1;
		boolean isRing = false;
		Equip equip = null;
		short pos = item.getPosition();
		if(item.getType() == 1){
			equip = (Equip) item;
			isRing = equip.getRingId() > -1;
		}
		if(!zeroPosition){
			if(equip != null){
				if(pos < 0){
					pos *= -1;
				}
				mplew.writeShort(pos > 100 && pos < 999 ? pos - 100 : pos);
			}else{
				mplew.write(pos);
			}
		}
		mplew.write(item.getType());
		mplew.writeInt(item.getItemId());
		mplew.writeBoolean(itemData.isCash);
		if(itemData.isCash){
			mplew.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
		}
		addExpirationTime(mplew, item.getExpiration());
		if(isPet){
			MaplePet pet = item.getPet();
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
			mplew.write(pet.getLevel());
			mplew.writeShort(pet.getCloseness());
			mplew.write(pet.getFullness());
			addExpirationTime(mplew, item.getExpiration());
			mplew.writeShort(0);// nPetAttribute
			mplew.writeShort(0);// usPetSkill
			mplew.writeInt(pet.getRemainLife());// nRemainLife
			mplew.writeShort(0);// nAttribute;
			// oPacket.EncodeByte(nActiveState);
			// oPacket.EncodeInt(nAutoBuffSkill);
			// oPacket.EncodeInt(nPetHue);
			// oPacket.EncodeShort(nGiantRate);
			return;
		}
		if(equip == null){
			mplew.writeShort(item.getQuantity() * (addBundleAmount ? item.getPerBundle() : 1));
			mplew.writeMapleAsciiString(item.getOwner());
			mplew.writeShort(item.getFlag()); // flag
			if(ItemConstants.isRechargable(item.getItemId())){
				mplew.writeLong(0);// liSN
			}
			return;
		}
		mplew.write(equip.getUpgradeSlots()); // upgrade slots
		mplew.write(equip.getLevel()); // level
		mplew.writeShort(equip.getStr()); // str
		mplew.writeShort(equip.getDex()); // dex
		mplew.writeShort(equip.getInt()); // int
		mplew.writeShort(equip.getLuk()); // luk
		mplew.writeShort(equip.getHp()); // hp
		mplew.writeShort(equip.getMp()); // mp
		mplew.writeShort(equip.getWatk()); // watk
		mplew.writeShort(equip.getMatk()); // matk
		mplew.writeShort(equip.getWdef()); // wdef
		mplew.writeShort(equip.getMdef()); // mdef
		mplew.writeShort(equip.getAcc()); // accuracy
		mplew.writeShort(equip.getAvoid()); // avoid
		mplew.writeShort(equip.getHands()); // hands
		mplew.writeShort(equip.getSpeed()); // speed
		mplew.writeShort(equip.getJump()); // jump
		mplew.writeMapleAsciiString(equip.getOwner()); // owner name
		mplew.writeShort(equip.getFlag()); // Item Flags
		mplew.writeBoolean(equip.hasLearnedSkills());// nLevelUpType, says the item can level your skill(For max level timeless/reverse)
		mplew.write(equip.getItemLevel());
		mplew.writeInt((int) equip.getItemExp());
		mplew.writeInt(equip.getDurability());// nDurability
		mplew.writeInt(equip.getVicious());// nIUC
		mplew.write(equip.getGrade());// nGrade
		mplew.write(equip.getChuc());// nCHUC
		mplew.writeShort(equip.getOption1());// nOption1
		mplew.writeShort(equip.getOption2());// nOption2
		mplew.writeShort(equip.getOption3());// nOption3
		mplew.writeShort(0);// nSocket1
		mplew.writeShort(0);// nSocket2
		if(!itemData.isCash) mplew.writeLong(0);// liSN
		mplew.writeLong(getTime(-2));// ftEquipped
		mplew.writeInt(-1);// nPrevBonusExpRate
	}

	/*
	  public void RawEncode(OutPacket oPacket) {
	super.RawEncode(oPacket);
	oPacket.Encode1(nRUC);
	oPacket.Encode1(nCUC);
	oPacket.Encode2(niSTR);
	oPacket.Encode2(niDEX);
	oPacket.Encode2(niINT);
	oPacket.Encode2(niLUK);
	oPacket.Encode2(niMaxHP);
	oPacket.Encode2(niMaxMP);
	oPacket.Encode2(niPAD);
	oPacket.Encode2(niMAD);
	oPacket.Encode2(niPDD);
	oPacket.Encode2(niMDD);
	oPacket.Encode2(niACC);
	oPacket.Encode2(niEVA);
	oPacket.Encode2(niCraft);
	oPacket.Encode2(niSpeed);
	oPacket.Encode2(niJump);
	oPacket.EncodeStr(sTitle);
	oPacket.Encode2(nAttribute);
	oPacket.Encode1(nLevelUpType);
	oPacket.Encode1(nLevel);
	oPacket.Encode4(nEXP);
	if (SystemConstants.Version >= 90) {
	    oPacket.Encode4(nDurability);
	}
	oPacket.Encode4(nIUC);
	if (SystemConstants.Version >= 90) {
	    oPacket.Encode1(option.nGrade);
	    oPacket.Encode1(option.nCHUC);
	    oPacket.Encode2(option.nOption1);
	    oPacket.Encode2(option.nOption2);
	    oPacket.Encode2(option.nOption3);
	    oPacket.Encode2(option.nSocket1);
	    oPacket.Encode2(option.nSocket2);
	}
	if (!ItemInfo.IsCashItem(nItemID)) {
	    oPacket.Encode8(liSN);
	}
	oPacket.Encode8(ftEquipped);
	oPacket.Encode4(nPrevBonusExpRate);
	}*/
	private static void addInventoryInfo(long mask, final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		if((mask & DBChar.InventorySize) > 0){
			for(byte i = 1; i <= 5; i++){
				mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
			}
		}
		if((mask & DBChar.EquipExt) > 0) mplew.writeLong(getTime(-2));
		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<Item> equippedC = iv.list();
		List<Item> equipped = new ArrayList<>(equippedC.size());
		List<Item> equippedCash = new ArrayList<>(equippedC.size());
		for(Item item : equippedC){
			if(item.getPosition() <= -100 && item.getPosition() >= -999){
				equippedCash.add(item);
			}else{
				equipped.add(item);
			}
		}
		Collections.sort(equipped);
		if((mask & DBChar.ItemSlotEquip) > 0){
			for(Item item : equipped){
				if(item.getPosition() > EquipSlot.DP_BEGIN.getSlots()[0] || item.getPosition() < EquipSlot.DP_END.getSlots()[0]) addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotEquip) > 0){
			mplew.writeShort(0); // start of equip cash
			for(Item item : equippedCash){
				addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotEquip) > 0){
			mplew.writeShort(0); // start of equip inventory
			for(Item item : chr.getInventory(MapleInventoryType.EQUIP).list()){
				addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotEquip) > 0){
			mplew.writeShort(0);// evan inv
			for(Item item : equipped){
				if(item.getPosition() <= EquipSlot.DP_BEGIN.getSlots()[0] && item.getPosition() >= EquipSlot.DP_END.getSlots()[0]) addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotConsume) > 0){
			mplew.writeShort(0);
			for(Item item : chr.getInventory(MapleInventoryType.USE).list()){
				addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotInstall) > 0){
			mplew.write(0);
			for(Item item : chr.getInventory(MapleInventoryType.SETUP).list()){
				addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotEtc) > 0){
			mplew.write(0);
			for(Item item : chr.getInventory(MapleInventoryType.ETC).list()){
				addItemInfo(mplew, item);
			}
		}
		if((mask & DBChar.ItemSlotCash) > 0){
			mplew.write(0);
			for(Item item : chr.getInventory(MapleInventoryType.CASH).list()){
				addItemInfo(mplew, item);
			}
		}
		mplew.write(0); // end of inv
	}

	private static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		Map<Skill, MapleCharacter.SkillEntry> skills = chr.getSkills();
		int skillsSize = skills.size();
		// We don't want to include any hidden skill in this, so subtract them from the size list and ignore them.
		for(Iterator<Entry<Skill, SkillEntry>> it = skills.entrySet().iterator(); it.hasNext();){
			Entry<Skill, MapleCharacter.SkillEntry> skill = it.next();
			if(GameConstants.isHiddenSkills(skill.getKey().getId())){
				skillsSize--;
			}
		}
		mplew.writeShort(skillsSize);
		for(Iterator<Entry<Skill, SkillEntry>> it = skills.entrySet().iterator(); it.hasNext();){
			Entry<Skill, MapleCharacter.SkillEntry> skill = it.next();
			if(GameConstants.isHiddenSkills(skill.getKey().getId())){
				continue;
			}
			mplew.writeInt(skill.getKey().getId());
			mplew.writeInt(skill.getValue().skillevel);
			addExpirationTime(mplew, skill.getValue().expiration);
			if(skill.getKey().is_skill_need_master_level()){
				mplew.writeInt(skill.getValue().masterlevel);
			}
		}
	}

	private static void addSkillCooldownInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.writeShort(chr.getAllCooldowns().size());
		for(PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()){
			mplew.writeInt(cooling.skillId);
			int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
			mplew.writeShort(timeLeft / 1000);
		}
	}

	private static void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.write(0);
		Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
		mplew.writeShort(cards.size());
		for(Entry<Integer, Integer> all : cards.entrySet()){
			mplew.writeShort(all.getKey() % 10000); // Id
			mplew.write(all.getValue()); // Level
		}
		// mplew.writeShort(0);
		// mplew.write(0);
		// mplew.write(0);
		// ?
		/*mplew.writeShort(0);
		int buffer = 0;
		mplew.write(buffer);
		mplew.skip(buffer);
		int buffer2 = 0;
		mplew.write(buffer2);
		mplew.skip(buffer2);*/
	}

	public static byte[] sendGuestTOS(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUEST_ID_LOGIN.getValue());
		mplew.writeShort(0x100);
		mplew.writeInt(Randomizer.nextInt(999999));
		mplew.writeLong(0);
		mplew.writeLong(getTime(-2));
		mplew.writeLong(getTime(System.currentTimeMillis()));
		mplew.writeInt(0);
		mplew.writeMapleAsciiString("http://google.com"); // TODO: Change
		return mplew.getPacket();
	}

	/**
	 * Sends a hello packet.
	 *
	 * @param mapleVersion The maple client version.
	 * @param sendIv the IV used by the server for sending
	 * @param recvIv the IV used by the server for receiving
	 * @return
	 */
	public static byte[] getHello(byte[] sendIv, byte[] recvIv){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(0x0E);
		mplew.writeShort(ServerConstants.VERSION);
		mplew.writeMapleAsciiString(ServerConstants.PATCH);
		mplew.write(recvIv);
		mplew.write(sendIv);
		mplew.write(8);// locale
		return mplew.getPacket();
	}

	/**
	 * Sends a ping packet.
	 *
	 * @return The packet.
	 */
	public static byte[] getPing(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.PING.getValue());
		return mplew.getPacket();
	}

	/**
	 * Gets a login failed packet.
	 * Possible values for <code>reason</code>:<br>
	 * 2: ID deleted or blocked<br>
	 * 3: ID deleted or blocked<br>
	 * 4: Incorrect password<br>
	 * 5: Not a registered id<br>
	 * 6: System error<br>
	 * 7: Already logged in<br>
	 * 8: System error<br>
	 * 9: System error<br>
	 * 10: Cannot process so many connections<br>
	 * 11: Only users older than 20 can use this channel<br>
	 * 13: Unable to log on as master at this ip<br>
	 * 14: Wrong gateway or personal info and weird korean button<br>
	 * 15: Processing request with that korean button!<br>
	 * 16: Please verify your account through email...<br>
	 * 17: Wrong gateway or personal info<br>
	 * 21: Please verify your account through email...<br>
	 * 23: License agreement<br>
	 * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
	 * 27: Some weird full client notice, probably for trial versions<br>
	 *
	 * @param reason The reason logging in failed.
	 * @return The login failed packet.
	 */
	public static byte[] getLoginFailed(int reason){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.write(reason);
		mplew.write(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	/**
	 * Possible values for <code>reason</code>:<br>
	 * 2: ID deleted or blocked<br>
	 * 3: ID deleted or blocked<br>
	 * 4: Incorrect password<br>
	 * 5: Not a registered id<br>
	 * 6: Trouble logging into the game?<br>
	 * 7: Already logged in<br>
	 * 8: Trouble logging into the game?<br>
	 * 9: Trouble logging into the game?<br>
	 * 10: Cannot process so many connections<br>
	 * 11: Only users older than 20 can use this channel<br>
	 * 12: Trouble logging into the game?<br>
	 * 13: Unable to log on as master at this ip<br>
	 * 14: Wrong gateway or personal info and weird korean button<br>
	 * 15: Processing request with that korean button!<br>
	 * 16: Please verify your account through email...<br>
	 * 17: Wrong gateway or personal info<br>
	 * 21: Please verify your account through email...<br>
	 * 23: Crashes<br>
	 * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
	 * 27: Some weird full client notice, probably for trial versions<br>
	 *
	 * @param reason The reason logging in failed.
	 * @return The login failed packet.
	 */
	public static byte[] getAfterLoginError(int reason){// same as above o.o
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.SELECT_CHARACTER_BY_VAC.getValue());
		mplew.writeShort(reason);// using other types then stated above = CRASH
		return mplew.getPacket();
	}

	public static byte[] sendPolice(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAKE_GM_NOTICE.getValue());
		mplew.write(0);// doesn't even matter what value
		return mplew.getPacket();
	}

	public static byte[] sendPolice(String text){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DATA_CRC_CHECK_FAILED.getValue());
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	public static byte[] getPermBan(byte reason){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.write(2); // Account is banned
		mplew.write(0);
		mplew.writeInt(0);
		mplew.write(0);
		mplew.writeLong(getTime(-1));
		return mplew.getPacket();
	}

	public static byte[] getTempBan(long timestampTill, byte reason){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
		mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
		mplew.write(2);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.write(reason);
		mplew.writeLong(getTime(timestampTill)); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.
		return mplew.getPacket();
	}
	/*
	 // Let GM/Admins Attack
	// To disable, return EB to 74 (JMP back to JE)
	[enable]
	0095099A:
	db EB
	009509DC:
	db EB
	0095385B:
	db EB
	00955783:
	db EB
	0095F161:
	db EB
	0095F1A3:
	db EB
	009571BB:
	db EB
	009571F6:
	db EB
	 */

	/**
	 * Gets a packet detailing a PIN operation.
	 * Possible values for <code>mode</code>:<br>
	 * 0 - PIN was accepted<br>
	 * 1 -
	 * Register a new PIN<br>
	 * 2 - Invalid pin / Reenter<br>
	 * 3 - Connection
	 * failed due to system error<br>
	 * 4 - Enter the pin
	 *
	 * @param mode The mode.
	 * @return
	 */
	private static byte[] pinOperation(byte mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.CHECK_PINCODE.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static byte[] pinRegistered(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.UPDATE_PINCODE.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] requestPin(){// client needs to enter
		return pinOperation((byte) 4);
	}

	public static byte[] requestPinAfterFailure(){
		return pinOperation((byte) 2);
	}

	public static byte[] registerPin(){
		return pinOperation((byte) 1);
	}

	public static byte[] pinAccepted(){
		return pinOperation((byte) 0);
	}

	public static byte[] wrongPic(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.CHECK_SPW_RESULT.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet detailing a server status message.
	 * Possible values for <code>status</code>:<br>
	 * 0 - Normal<br>
	 * 1 - Highly
	 * populated<br>
	 * 2 - Full
	 *
	 * @param status The server status.
	 * @return The server status packet.
	 */
	public static byte[] getServerStatus(int status){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
		mplew.writeShort(status);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the channel server.
	 *
	 * @param inetAddr The InetAddress of the requested channel server.
	 * @param port The port the channel is on.
	 * @param clientId The ID of the client.
	 * @return The server IP packet.
	 */
	public static byte[] getServerIP(InetAddress inetAddr, int port, int charid){
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
		mplew.write(0);
		mplew.write(0);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		mplew.writeInt(charid);
		mplew.write(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	/**
	 * Gets a packet telling the client the IP of the new channel.
	 *
	 * @param inetAddr The InetAddress of the requested channel server.
	 * @param port The port the channel is on.
	 * @return The server IP packet.
	 */
	public static byte[] getChannelChange(InetAddress inetAddr, int port){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
		mplew.write(1);
		byte[] addr = inetAddr.getAddress();
		mplew.write(addr);
		mplew.writeShort(port);
		return mplew.getPacket();
	}

	public static byte[] enableTV(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.ENABLE_TV.getValue());
		mplew.writeInt(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	/**
	 * Removes TV
	 *
	 * @return The Remove TV Packet
	 */
	public static byte[] removeTV(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.REMOVE_TV.getValue());
		return mplew.getPacket();
	}

	/**
	 * Sends MapleTV
	 *
	 * @param chr The character shown in TV
	 * @param messages The message sent with the TV
	 * @param type The type of TV
	 * @param partner The partner shown with chr
	 * @return the SEND_TV packet
	 */
	public static byte[] sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SEND_TV.getValue());
		mplew.write(partner != null ? 3 : 1);
		mplew.write(type); // Heart = 2 Star = 1 Normal = 0
		addCharLook(mplew, chr, false);
		mplew.writeMapleAsciiString(chr.getName());
		if(partner != null){
			mplew.writeMapleAsciiString(partner.getName());
		}else{
			mplew.writeShort(0);
		}
		for(int i = 0; i < messages.size(); i++){
			if(i == 4 && messages.get(4).length() > 15){
				mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
			}else{
				mplew.writeMapleAsciiString(messages.get(i));
			}
		}
		mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
		if(partner != null){
			addCharLook(mplew, partner, false);
		}
		return mplew.getPacket();
	}

	/**
	 * Gets a packet to spawn a portal.
	 *
	 * @param townId The ID of the town the portal goes to.
	 * @param targetId The ID of the target.
	 * @param pos Where to put the portal.
	 * @return The portal spawn packet.
	 */
	public static byte[] spawnPortal(int townId, int targetId, int skillid, Point pos){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
		mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
		mplew.writeInt(townId);
		mplew.writeInt(targetId);
		mplew.writeInt(skillid);
		if(pos != null){
			mplew.writePos(pos);
		}
		return mplew.getPacket();
	}

	public static byte[] removePortal(Point pos){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
		mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
		mplew.writeInt(999999999);
		mplew.writeInt(999999999);
		mplew.writeInt(0);// skillid
		mplew.writePos(pos);
		return mplew.getPacket();
	}

	/**
	 * Gets the response to a relog request.
	 *
	 * @return The relog response packet.
	 */
	public static byte[] getRelogResponse(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
		mplew.write(1);// 1 O.O Must be more types ):
		return mplew.getPacket();
	}

	/**
	 * Gets a server message packet.
	 *
	 * @param message The message to convey.
	 * @return The server message packet.
	 */
	public static byte[] serverMessage(String message){
		return serverMessage(4, (byte) 0, message, true, false, 0);
	}

	/**
	 * Gets a server notice packet.
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 *
	 * @param type The type of the notice.
	 * @param message The message to convey.
	 * @return The server notice packet.
	 */
	public static byte[] serverNotice(int type, String message){
		return serverMessage(type, (byte) 0, message, false, false, 0);
	}

	/**
	 * Gets a server notice packet.
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text
	 *
	 * @param type The type of the notice.
	 * @param channel The channel this notice was sent on.
	 * @param message The message to convey.
	 * @return The server notice packet.
	 */
	public static byte[] serverNotice(int type, String message, int npc){
		return serverMessage(type, 0, message, false, false, npc);
	}

	public static byte[] serverNotice(int type, int channel, String message){
		return serverMessage(type, channel, message, false, false, 0);
	}

	public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar){
		return serverMessage(type, channel, message, false, smegaEar, 0);
	}

	/**
	 * Gets a server message packet.
	 * Possible values for <code>type</code>:<br>
	 * 0: [Notice]<br>
	 * 1: Popup<br>
	 * 2: Megaphone<br>
	 * 3: Super Megaphone<br>
	 * 4: Scrolling message at top<br>
	 * 5: Pink Text<br>
	 * 6: Lightblue Text<br>
	 * 7: BroadCasting NPC
	 *
	 * @param type The type of the notice.
	 * @param channel The channel this notice was sent on.
	 * @param message The message to convey.
	 * @param servermessage Is this a scrolling ticker?
	 * @return The server notice packet.
	 */
	private static byte[] serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar, int npc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(type);
		if(servermessage || type == 4){// Why do we need a boolean................
			mplew.write(1);
		}
		if(type != 23 && type != 24) mplew.writeMapleAsciiString(message);
		if(type == 3 || type == 22 || type == 25 || type == 26 || type == 9){
			mplew.write(channel); // channel
			if(type != 9) mplew.writeBoolean(megaEar);
		}else if(type == 6 || type == 11 || type == 20){
			mplew.writeInt(channel);// itemid
		}else if(type == 7){
			mplew.writeInt(npc);
		}else if(type == 12){
			mplew.writeInt(channel);// Weather item id
		}else if(type == 24){
			mplew.writeShort(0);
		}
		return mplew.getPacket();
	}

	/**
	 * Sends a Avatar Super Megaphone packet.
	 *
	 * @param chr The character name.
	 * @param medal The medal text.
	 * @param channel Which channel.
	 * @param itemId Which item used.
	 * @param message The message sent.
	 * @param ear Whether or not the ear is shown for whisper.
	 * @return
	 */
	public static byte[] getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
		mplew.writeInt(itemId);
		mplew.writeMapleAsciiString(medal + chr.getName());
		for(String s : message){
			mplew.writeMapleAsciiString(s);
		}
		mplew.writeInt(channel); // channel
		mplew.writeBoolean(ear);
		addCharLook(mplew, chr, true);
		return mplew.getPacket();
	}

	/*
	 * Sends a packet to remove the tiger megaphone
	 * @return
	 */
	public static byte[] byeAvatarMega(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLEAR_AVATAR_MEGAPHONE.getValue());
		mplew.write(1);
		return mplew.getPacket();
	}

	/**
	 * Sends the Gachapon green message when a user uses a gachapon ticket.
	 *
	 * @param item
	 * @param town
	 * @param player
	 * @return
	 */
	public static byte[] gachaponMessage(Item item, String town, MapleCharacter player){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(12);
		mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
		mplew.writeInt(0); // random?
		mplew.writeMapleAsciiString(town);
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
		mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
		mplew.write(1);
		mplew.writeInt(life.getObjectId());
		mplew.writeInt(life.getId());
		mplew.writeShort(life.getPosition().x);
		mplew.writeShort(life.getCy());
		if(life.getF() == 1){
			mplew.write(0);
		}else{
			mplew.write(1);
		}
		mplew.writeShort(life.getFh());
		mplew.writeShort(life.getRx0());
		mplew.writeShort(life.getRx1());
		mplew.writeBoolean(MiniMap);
		return mplew.getPacket();
	}

	/**
	 * Gets a general chat packet.
	 *
	 * @param cidfrom The character ID who sent the chat.
	 * @param text The text of the chat.
	 * @param whiteBG
	 * @param show
	 * @return The general chat packet.
	 */
	public static byte[] getChatText(int cidfrom, String text, boolean gm, int show){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHATTEXT.getValue());
		mplew.writeInt(cidfrom);
		mplew.writeBoolean(gm);
		mplew.writeMapleAsciiString(text);
		mplew.write(show);
		return mplew.getPacket();
	}

	public static void addRingLook(final MaplePacketLittleEndianWriter mplew, List<MapleRing> rings){
		Optional<MapleRing> ring = rings.stream().filter(Objects::nonNull).filter(r-> r.equipped()).findAny();
		if(ring.isPresent()){
			mplew.writeBoolean(true);
			MapleRing r = ring.get();
			mplew.writeLong(r.getRingId());
			mplew.writeLong(r.getPartnerRingId());
			mplew.writeInt(r.getItemId());
		}else mplew.writeBoolean(false);
	}

	public static void addMarriageRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		MapleRing ring = chr.getMarriageRing();
		if(ring != null){
			mplew.writeBoolean(ring.equipped());
			if(ring.equipped()){
				mplew.writeInt(chr.getId());
				mplew.writeInt(chr.getMarriedTo());
				mplew.writeInt(chr.getMarriageRingID());
			}
		}else mplew.writeBoolean(false);
	}

	/**
	 * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
	 *
	 * @param mplew The MaplePacketLittleEndianWriter to add an announcement box to.
	 * @param shop The shop to announce.
	 */
	public static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability){
		mplew.write(4);
		mplew.writeInt(shop.getObjectId());
		mplew.writeMapleAsciiString(shop.getDescription());
		mplew.write(0);
		mplew.write(0);
		mplew.write(1);
		mplew.write(availability);
		mplew.write(0);
	}

	public static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int gametype, int type, int ammount, int joinable){
		mplew.write(gametype);
		mplew.writeInt(game.getObjectId()); // gameid/shopid
		mplew.writeMapleAsciiString(game.getDescription()); // desc
		mplew.writeBoolean(game.getPassword() != null);
		mplew.write(type);
		mplew.write(ammount);
		mplew.write(2);
		mplew.write(joinable);
	}

	public static byte[] closeRangeAttack(MapleCharacter chr, AttackInfo info){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
		addAttackBody(mplew, chr, info);
		return mplew.getPacket();
	}

	public static byte[] rangedAttack(MapleCharacter chr, AttackInfo info){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
		addAttackBody(mplew, chr, info);
		return mplew.getPacket();
	}

	public static byte[] magicAttack(MapleCharacter chr, AttackInfo info){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
		addAttackBody(mplew, chr, info);
		return mplew.getPacket();
	}

	private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, AttackInfo info){
		lew.writeInt(chr.getId());
		lew.write(info.numAttackedAndDamage);
		lew.write(0x5B);// nLevel
		lew.write(info.skilllevel);
		if(info.skilllevel > 0){
			lew.writeInt(info.skill);
		}
		lew.write(info.display);// bSerialAttack = CInPacket::Decode1(v4) & 32;
		lew.write(info.action);// short, has bLeft and nAction
		lew.write(info.stance);
		lew.write(info.speed);// nActionSpeed
		lew.write(0x0A);// nMastery
		lew.writeInt(info.projectile);// nBulletItemID
		for(Integer oned : info.allDamage.keySet()){// mob, lines, crit.
			List<Pair<Integer, Boolean>> onedList = info.allDamage.get(oned);
			if(onedList != null){
				lew.writeInt(oned);
				lew.write(0xFF);// 0x07 in another src
				if(info.skill == 4211006){
					lew.write(onedList.size());
				}
				for(Pair<Integer, Boolean> eachd : onedList){
					lew.writeBoolean(eachd.right);
					lew.writeInt(eachd.left);
				}
			}
		}
		if(info.ranged){
			lew.writeInt(0);// ptBallStart x, y(shorts)
		}
		if(info.skill == 2121001 || info.skill == 2221001 || info.skill == 2321001 || info.skill == 22121000 || info.skill == 22151001){
			lew.writeInt(info.charge);
		}else if(info.skill == 33101007){
			lew.writeInt(0);// dwSwallowMobTemplateID
		}
	}

	public static byte[] updateInventorySlotLimit(int type, int newLimit){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.INVENTORY_GROW.getValue());
		mplew.write(type);
		mplew.write(newLimit);
		return mplew.getPacket();
	}

	public static byte[] modifyInventory(boolean updateTick, final List<ModifyInventory> mods){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
		mplew.writeBoolean(updateTick);
		mplew.write(mods.size());
		// mplew.write(0); v104 :)
		int addMovement = -1;
		for(ModifyInventory mod : mods){
			mplew.write(mod.getMode());
			mplew.write(mod.getInventoryType());
			mplew.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());// nPOS
			switch (mod.getMode()){
				case 0:{// add item
					addItemInfo(mplew, mod.getItem(), true);
					break;
				}
				case 1:{// update quantity
					mplew.writeShort(mod.getQuantity());
					break;
				}
				case 2:{// move
					mplew.writeShort(mod.getPosition());// nPOS2
					if(mod.getPosition() < 0 || mod.getOldPosition() < 0){
						addMovement = mod.getOldPosition() < 0 ? 1 : 2;
					}
					break;
				}
				case 3:{// remove
					if(mod.getPosition() < 0){
						addMovement = 2;
					}
					break;
				}
				// exp, int
			}
			mod.clear();
		}
		if(addMovement > -1){
			mplew.write(addMovement);
		}
		return mplew.getPacket();
	}

	public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
		mplew.writeInt(cid);
		mplew.write(skill);
		mplew.writeInt(damage);
		mplew.writeInt(monsteridfrom);
		mplew.write(direction);
		if(pgmr){
			mplew.write(pgmr_1);
			mplew.write(is_pg ? 1 : 0);
			mplew.writeInt(oid);
			mplew.write(6);
			mplew.writeShort(pos_x);
			mplew.writeShort(pos_y);
			mplew.write(0);
		}else{
			mplew.writeShort(0);
		}
		mplew.writeInt(damage);
		if(fake > 0){
			mplew.writeInt(fake);
		}
		return mplew.getPacket();
	}

	public static byte[] charNameResponse(String charname, boolean nameUsed){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
		mplew.writeMapleAsciiString(charname);
		mplew.write(nameUsed ? 1 : 0);
		return mplew.getPacket();
	}

	public static byte[] addNewCharEntry(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
		mplew.write(0);
		addCharEntry(mplew, chr, false);
		return mplew.getPacket();
	}

	/**
	 * state 0 = del ok state 12 = invalid bday state 14 = incorrect pic
	 *
	 * @param cid
	 * @param state
	 * @return
	 */
	public static byte[] deleteCharResponse(int cid, int state){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
		mplew.writeInt(cid);
		mplew.write(state);
		return mplew.getPacket();
	}

	public static byte[] selectWorld(int world){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LAST_CONNECTED_WORLD.getValue());
		mplew.writeInt(world);// According to GMS, it should be the world that contains the most characters (most active)
		return mplew.getPacket();
	}

	public static byte[] sendRecommended(List<Pair<Integer, String>> worlds){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RECOMMENDED_WORLD_MESSAGE.getValue());
		mplew.write(worlds.size());// size
		for(Iterator<Pair<Integer, String>> it = worlds.iterator(); it.hasNext();){
			Pair<Integer, String> world = it.next();
			mplew.writeInt(world.getLeft());
			mplew.writeMapleAsciiString(world.getRight());
		}
		return mplew.getPacket();
	}

	/**
	 * public void EncodeForLocal(OutPacket oPacket, Flag uFlag) {
	 * oPacket.EncodeBuffer(uFlag.ToByteArray());
	 * for (CharacterTemporaryStat enCTS: this.mStat.keySet()) {
	 * if (!uFlag.OperatorAND(enCTS.GetMask()).IsZero()) {
	 * SecondaryStatOption opt = GetStat(enCTS);
	 * oPacket.Encode2(opt.nOption);
	 * oPacket.Encode4(opt.rOption);
	 * oPacket.Encode4((int)(opt.tDuration - System.currentTimeMillis()));
	 * }
	 * }
	 * oPacket.Encode1(this.nDefenseAtt);
	 * oPacket.Encode1(this.nDefenseState);
	 * for (TSIndex enIndex: TSIndex.values()) {
	 * if (!uFlag.OperatorAND(SkillConstants.get_CTS_from_TSIndex(enIndex.getIndex()).GetMask()).IsZero()) {
	 * this.aTemporaryStat.get(enIndex.getIndex()).EncodeForClient(oPacket);
	 * }
	 * }
	 * }
	 */
	/**
	 * It is important that statups is in the correct order (see decleration
	 * order in MapleBuffStat) since this method doesn't do automagical
	 * reordering.
	 *
	 * @param buffid
	 * @param bufflength
	 * @param statups
	 * @return
	 */
	public static byte[] giveBuff(MapleCharacter chr, int buffid, int bufflength, List<Pair<MapleBuffStat, BuffDataHolder>> statups){// CWvsContext::OnTemporaryStatSet
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		chr.secondaryStat.encodeLocal(mplew, statups, buffid, bufflength);
		return mplew.getPacket();
	}

	/*public static byte[] givePirateBuff(List<Pair<MapleBuffStat, BuffValueHolder>> statups, int buffid, int duration){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for(Pair<MapleBuffStat, BuffValueHolder> stat : statups){
			mplew.writeInt(stat.getRight().getValue());
			mplew.writeInt(buffid);
			mplew.skip(infusion ? 10 : 5);
			mplew.writeShort(duration);
		}
		mplew.skip(3);
		return mplew.getPacket();
	}*/
	public static byte[] giveForgeinPirateBuff(int cid, int buffid, int time, List<Pair<MapleBuffStat, BuffDataHolder>> statups){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMask(mplew, statups);
		mplew.writeShort(0);
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			mplew.writeInt(statup.getRight().getValue());
			mplew.writeInt(buffid);
			mplew.skip(infusion ? 10 : 5);
			mplew.writeShort(time);
		}
		mplew.writeShort(0);
		mplew.write(2);
		return mplew.getPacket();
	}

	/**
	 * @param cid
	 * @param statups
	 * @param mount
	 * @return
	 */
	public static byte[] showMonsterRiding(int cid, MapleMount mount){ // Gtfo with this, this is just giveForeignBuff
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		List<MapleBuffStat> temp = new ArrayList<>();
		temp.add(MapleBuffStat.MONSTER_RIDING);
		writeLongMaskFromList(mplew, temp);
		mplew.writeShort(0);
		mplew.writeInt(mount.getItemId());
		mplew.writeInt(mount.getSkillId());
		mplew.writeInt(0); // Server Tick value.
		mplew.writeShort(0);
		mplew.write(0); // Times you have been buffed
		return mplew.getPacket();
	}

	/**
	 * @param c
	 * @param quest
	 * @param npc
	 * @param progress
	 * @return
	 */
	public static byte[] updateQuestInfo(short quest, int npc, String progress){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(8); // 0x0A in v95
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] addQuestTimeLimit(final short quest, final int time){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(6);
		mplew.writeShort(1);// Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
		mplew.writeShort(quest);
		mplew.writeInt(time);
		return mplew.getPacket();
	}

	public static byte[] removeQuestTimeLimit(final short quest){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(7);
		mplew.writeShort(1);// Position
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static byte[] cancelForeignDebuff(int cid, long mask){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		// this is a decodeBuffer(16)
		mplew.writeLong(0);
		mplew.writeLong(mask);
		return mplew.getPacket();
	}

	public static byte[] giveForeignBuff(MapleCharacter chr, List<Pair<MapleBuffStat, BuffDataHolder>> statups){// CUserRemote::OnSetTemporaryStat
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
		mplew.writeInt(chr.getId());
		chr.secondaryStat.encodeRemote(mplew, statups);
		mplew.writeShort(0);// tDelay
		return mplew.getPacket();
	}

	public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
		mplew.writeInt(cid);
		writeLongMaskFromList(mplew, statups);
		return mplew.getPacket();
	}

	public static byte[] cancelBuff(List<MapleBuffStat> statups){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		writeLongMaskFromList(mplew, statups);
		mplew.write(0);// ?
		return mplew.getPacket();
	}

	public static void writeLongMask(final LittleEndianWriter mplew, List<Pair<MapleBuffStat, BuffDataHolder>> statups){
		int[] mask = new int[4];
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			mask[statup.left.getSet()] |= statup.left.getMask();
		}
		for(int i = 3; i >= 0; i--){
			mplew.writeInt(mask[i]);
		}
	}

	public static void writeLongMask(final LittleEndianWriter mplew, int[] mask, List<Pair<MapleBuffStat, BuffDataHolder>> statups){
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			mask[statup.left.getSet()] |= statup.left.getMask();
		}
		for(int i = 3; i >= 0; i--){
			mplew.writeInt(mask[i]);
		}
	}

	private static void writeLongMaskFromList(final MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups){
		int[] mask = new int[4];
		for(MapleBuffStat statup : statups){
			mask[statup.getSet()] |= statup.getMask();
		}
		for(int i = 3; i >= 0; i--){
			mplew.writeInt(mask[i]);
		}
	}

	public static void writeForeignBuffs(final MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, BuffDataHolder>> statups){
		int[] buffmask = new int[4];
		int monsterRiding = 0;
		List<Pair<Integer, Integer>> buffList = new ArrayList<>();
		buffmask[MapleBuffStat.ENERGY_CHARGE.getSet()] |= MapleBuffStat.ENERGY_CHARGE.getMask();
		buffmask[MapleBuffStat.DASH_SPEED.getSet()] |= MapleBuffStat.DASH_SPEED.getMask();
		buffmask[MapleBuffStat.DASH_JUMP.getSet()] |= MapleBuffStat.DASH_JUMP.getMask();
		buffmask[MapleBuffStat.MONSTER_RIDING.getSet()] |= MapleBuffStat.MONSTER_RIDING.getMask();
		buffmask[MapleBuffStat.SPEED_INFUSION.getSet()] |= MapleBuffStat.SPEED_INFUSION.getMask();
		buffmask[MapleBuffStat.HOMING_BEACON.getSet()] |= MapleBuffStat.HOMING_BEACON.getMask();
		buffmask[MapleBuffStat.UNDEAD.getSet()] |= MapleBuffStat.UNDEAD.getMask();
		for(Pair<MapleBuffStat, BuffDataHolder> statup : statups){
			if(statup.getLeft() == MapleBuffStat.SPEED){
				buffmask[MapleBuffStat.SPEED.getSet()] |= MapleBuffStat.SPEED.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 1));
			}
			if(statup.getLeft() == MapleBuffStat.COMBO){
				buffmask[MapleBuffStat.COMBO.getSet()] |= MapleBuffStat.COMBO.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 1));
			}
			if(statup.getLeft() == MapleBuffStat.WK_CHARGE){
				buffmask[MapleBuffStat.WK_CHARGE.getSet()] |= MapleBuffStat.WK_CHARGE.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.SHADOWPARTNER){
				buffmask[MapleBuffStat.SHADOWPARTNER.getSet()] |= MapleBuffStat.SHADOWPARTNER.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.DARKSIGHT){
				buffmask[MapleBuffStat.DARKSIGHT.getSet()] |= MapleBuffStat.DARKSIGHT.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.SOULARROW){
				buffmask[MapleBuffStat.SOULARROW.getSet()] |= MapleBuffStat.SOULARROW.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.MORPH){
				buffmask[MapleBuffStat.MORPH.getSet()] |= MapleBuffStat.MORPH.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.GHOST_MORPH){
				buffmask[MapleBuffStat.GHOST_MORPH.getSet()] |= MapleBuffStat.GHOST_MORPH.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.SEDUCE){
				buffmask[MapleBuffStat.SEDUCE.getSet()] |= MapleBuffStat.SEDUCE.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.SHADOW_CLAW){
				buffmask[MapleBuffStat.SHADOW_CLAW.getSet()] |= MapleBuffStat.SHADOW_CLAW.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.BAN_MAP){
				buffmask[MapleBuffStat.BAN_MAP.getSet()] |= MapleBuffStat.BAN_MAP.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.BARRIER){
				buffmask[MapleBuffStat.BARRIER.getSet()] |= MapleBuffStat.BARRIER.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.DOJANG_SHIELD){
				buffmask[MapleBuffStat.DOJANG_SHIELD.getSet()] |= MapleBuffStat.DOJANG_SHIELD.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.CONFUSE){
				buffmask[MapleBuffStat.CONFUSE.getSet()] |= MapleBuffStat.CONFUSE.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.RESPECT_PIMMUNE){
				buffmask[MapleBuffStat.RESPECT_PIMMUNE.getSet()] |= MapleBuffStat.RESPECT_PIMMUNE.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.RESPECT_MIMMUNE){
				buffmask[MapleBuffStat.RESPECT_MIMMUNE.getSet()] |= MapleBuffStat.RESPECT_MIMMUNE.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.DEFENSE_ATT){
				buffmask[MapleBuffStat.DEFENSE_ATT.getSet()] |= MapleBuffStat.DEFENSE_ATT.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.DEFENSE_STATE){
				buffmask[MapleBuffStat.DEFENSE_STATE.getSet()] |= MapleBuffStat.DEFENSE_STATE.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.BERSERK_FURY){
				buffmask[MapleBuffStat.BERSERK_FURY.getSet()] |= MapleBuffStat.BERSERK_FURY.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.DIVINE_BODY){
				buffmask[MapleBuffStat.DIVINE_BODY.getSet()] |= MapleBuffStat.DIVINE_BODY.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.WIND_WALK){
				buffmask[MapleBuffStat.WIND_WALK.getSet()] |= MapleBuffStat.WIND_WALK.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.REPEAT_EFFECT){
				buffmask[MapleBuffStat.REPEAT_EFFECT.getSet()] |= MapleBuffStat.REPEAT_EFFECT.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.STOP_PORTION){
				buffmask[MapleBuffStat.STOP_PORTION.getSet()] |= MapleBuffStat.STOP_PORTION.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.STOP_MOTION){
				buffmask[MapleBuffStat.STOP_MOTION.getSet()] |= MapleBuffStat.STOP_MOTION.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.FEAR){
				buffmask[MapleBuffStat.FEAR.getSet()] |= MapleBuffStat.FEAR.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.Flying){
				buffmask[MapleBuffStat.Flying.getSet()] |= MapleBuffStat.Flying.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.Frozen){
				buffmask[MapleBuffStat.Frozen.getSet()] |= MapleBuffStat.Frozen.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.SuddenDeath){
				buffmask[MapleBuffStat.SuddenDeath.getSet()] |= MapleBuffStat.SuddenDeath.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.FinalCut){
				buffmask[MapleBuffStat.FinalCut.getSet()] |= MapleBuffStat.FinalCut.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.Cyclone){
				buffmask[MapleBuffStat.Cyclone.getSet()] |= MapleBuffStat.Cyclone.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.Sneak){
				buffmask[MapleBuffStat.Sneak.getSet()] |= MapleBuffStat.Sneak.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.MorewildDamageUp){
				buffmask[MapleBuffStat.MorewildDamageUp.getSet()] |= MapleBuffStat.MorewildDamageUp.getMask();
			}
			if(statup.getLeft() == MapleBuffStat.Mechanic){
				buffmask[MapleBuffStat.Mechanic.getSet()] |= MapleBuffStat.Mechanic.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.DarkAura){
				buffmask[MapleBuffStat.DarkAura.getSet()] |= MapleBuffStat.DarkAura.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.BlueAura){
				buffmask[MapleBuffStat.BlueAura.getSet()] |= MapleBuffStat.BlueAura.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.Mechanic){
				buffmask[MapleBuffStat.YellowAura.getSet()] |= MapleBuffStat.YellowAura.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 4));
			}
			if(statup.getLeft() == MapleBuffStat.STUN){
				buffmask[MapleBuffStat.STUN.getSet()] |= MapleBuffStat.STUN.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.DARKNESS){
				buffmask[MapleBuffStat.DARKNESS.getSet()] |= MapleBuffStat.DARKNESS.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.SEAL){
				buffmask[MapleBuffStat.SEAL.getSet()] |= MapleBuffStat.SEAL.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.WEAKEN){
				buffmask[MapleBuffStat.WEAKEN.getSet()] |= MapleBuffStat.WEAKEN.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.CURSE){
				buffmask[MapleBuffStat.CURSE.getSet()] |= MapleBuffStat.CURSE.getMask();
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.POISON){
				buffmask[MapleBuffStat.POISON.getSet()] |= MapleBuffStat.POISON.getMask();
				buffList.add(new Pair<>(statup.getRight().getValue(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceLevel(), 2));
				buffList.add(new Pair<>(statup.getRight().getSourceID(), 2));
			}
			if(statup.getLeft() == MapleBuffStat.MONSTER_RIDING){
				monsterRiding = statup.getRight().getValue();
			}
		}
		for(int i = 3; i >= 0; i--){
			mplew.writeInt(buffmask[i]);
		}
		for(Pair<Integer, Integer> buff : buffList){
			if(buff.right == 4){
				mplew.writeInt(buff.left);
			}else if(buff.right == 2){
				mplew.writeShort(buff.left);
			}else if(buff.right == 1){
				mplew.write(buff.left);
			}
		}
		int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
		// Energy Charge
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		// Dash Speed
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		// Dash Jump
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		// Monster Riding
		if(monsterRiding != 0){
			mplew.writeInt(monsterRiding);
			mplew.writeInt(1004);
		}else{
			mplew.writeLong(0);
		}
		mplew.write(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		// Speed Infusion
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.write(0);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		// Homing Beacon
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeInt(0);
		// Zombify
		mplew.skip(9);
		mplew.writeInt(CHAR_MAGIC_SPAWN);
		mplew.writeShort(0);
		mplew.writeShort(0);
	}

	public static byte[] cancelDebuff(long mask){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
		mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
		mplew.writeLong(0);
		mplew.writeLong(mask);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] addCharBox(MapleCharacter c, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getPlayerShop(), type);
		return mplew.getPacket();
	}

	public static byte[] removeCharBox(MapleCharacter c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] onSay(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText, boolean bPrev, boolean bNext){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.Say.getMsgType());
		mplew.write(bParam);
		if((bParam & 0x4) > 0)// idek xd, its a 2nd template for something
		    mplew.writeInt(nSpeakerTemplateID);
		mplew.writeMapleAsciiString(sText);
		mplew.writeBoolean(bPrev);
		mplew.writeBoolean(bNext);
		return mplew.getPacket();
	}

	public static byte[] onSayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, List<String> asPath){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.SayImage.getMsgType());
		mplew.write(bParam);
		mplew.write(asPath.size());
		for(String sPath : asPath){
			mplew.writeMapleAsciiString(sPath);// CUtilDlgEx::AddImageList(v8, sPath);
		}
		return mplew.getPacket();
	}

	public static byte[] onSayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sPath){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.SayImage.getMsgType());
		mplew.write(bParam);
		mplew.write(1);
		mplew.writeMapleAsciiString(sPath);
		return mplew.getPacket();
	}

	public static byte[] onAskYesNo(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskYesNo.getMsgType());
		mplew.write(bParam);// (bParam & 0x6)
		mplew.writeMapleAsciiString(sText);
		return mplew.getPacket();
	}

	public static byte[] onAskAccept(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskAccept.getMsgType());
		mplew.write(bParam);
		mplew.writeMapleAsciiString(sText);
		return mplew.getPacket();
	}

	public static byte[] onAskText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nLenMin, int nLenMax){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskText.getMsgType());
		mplew.write(bParam);// (bParam & 0x6)
		mplew.writeMapleAsciiString(sMsg);
		mplew.writeMapleAsciiString(sMsgDefault);
		mplew.writeShort(nLenMin);
		mplew.writeShort(nLenMax);
		return mplew.getPacket();
	}

	public static byte[] onAskBoxText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nCol, int nLine){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskBoxText.getMsgType());
		mplew.write(bParam);// (bParam & 0x6)
		mplew.writeMapleAsciiString(sMsg);
		mplew.writeMapleAsciiString(sMsgDefault);
		mplew.writeShort(nCol);
		mplew.writeShort(nLine);
		return mplew.getPacket();
	}

	public static byte[] onAskNumber(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, int nDef, int nMin, int nMax){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskNumber.getMsgType());
		mplew.write(bParam);// (bParam & 0x6)
		mplew.writeMapleAsciiString(sMsg);
		mplew.writeInt(nDef);
		mplew.writeInt(nMin);
		mplew.writeInt(nMax);
		return mplew.getPacket();
	}

	public static byte[] onAskMenu(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskMenu.getMsgType());
		mplew.write(bParam);// (bParam & 0x6)
		mplew.writeMapleAsciiString(sMsg);
		return mplew.getPacket();
	}

	public static byte[] onAskAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] anCanadite){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskAvatar.getMsgType());
		mplew.write(0);
		mplew.writeMapleAsciiString(sMsg);
		mplew.write(anCanadite.length);
		for(int nCanadite : anCanadite){
			mplew.writeInt(nCanadite);// hair id's and stuff lol
		}
		return mplew.getPacket();
	}

	public static byte[] onAskMembershopAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] aCanadite){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskMemberShopAvatar.getMsgType());
		mplew.write(0);
		mplew.writeMapleAsciiString(sMsg);
		mplew.write(aCanadite.length);
		for(int nCanadite : aCanadite){
			mplew.writeInt(nCanadite);// hair id's and stuff lol
		}
		return mplew.getPacket();
	}

	public static byte[] onAskPet(byte nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, List<Item> apPet){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskPet.getMsgType());
		mplew.write(0);
		mplew.writeMapleAsciiString(sMsg);
		mplew.write(apPet.size());
		for(Item pPet : apPet){
			if(pPet != null){
				mplew.writeLong(pPet.getPetId());
				mplew.write(pPet.getPosition());
			}
		}
		return mplew.getPacket();
	}

	public static byte[] onAskPetAll(byte nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, List<Item> apPet, boolean bExceptionExist){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskPetAll.getMsgType());
		mplew.write(0);
		mplew.writeMapleAsciiString(sMsg);
		mplew.write(apPet.size());
		mplew.writeBoolean(bExceptionExist);
		for(Item pPet : apPet){
			if(pPet != null){
				mplew.writeLong(pPet.getPetId());
				mplew.write(pPet.getPosition());
			}
		}
		return mplew.getPacket();
	}

	public static byte[] onAskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskQuiz.getMsgType());
		mplew.write(0);
		mplew.write(nResCode);
		if(nResCode == NPCConversationManager.InitialQuizRes_Request){// fail has no bytes <3
			mplew.writeMapleAsciiString(sTitle);
			mplew.writeMapleAsciiString(sProblemText);
			mplew.writeMapleAsciiString(sHintText);
			mplew.writeShort(nMinInput);
			mplew.writeShort(nMaxInput);
			mplew.writeInt(tRemainInitialQuiz);
		}
		return mplew.getPacket();
	}

	public static byte[] onAskSpeedQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskSpeedQuiz.getMsgType());
		mplew.write(0);
		mplew.write(nResCode);
		if(nResCode == NPCConversationManager.InitialQuizRes_Request){// fail has no bytes <3
			mplew.writeInt(nType);
			mplew.writeInt(dwAnswer);
			mplew.writeInt(nCorrect);
			mplew.writeInt(nRemain);
			mplew.writeInt(tRemainInitialQuiz);
		}
		return mplew.getPacket();
	}

	public static byte[] onAskSlideMenu(int nSpeakerTypeID, int nSpeakerTemplateID, boolean bSlideDlgEX, int nIndex, String sMsg){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(nSpeakerTypeID);
		mplew.writeInt(nSpeakerTemplateID);
		mplew.write(ScriptMessageType.AskSlideMenu.getMsgType());
		mplew.write(0);
		mplew.writeInt(bSlideDlgEX ? 1 : 0);// Neo City
		mplew.writeInt(nIndex);// Dimensional Mirror.. There's also supportF for potions and such in higher versions.
		mplew.writeMapleAsciiString(sMsg);
		return mplew.getPacket();
	}

	public static byte[] getDimensionalMirror(String talk){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(9010022);
		mplew.write(0x0E);
		mplew.write(0);
		mplew.writeInt(0);
		mplew.writeMapleAsciiString(talk);
		return mplew.getPacket();
	}

	public static byte[] getSpeedQuiz(int npc, byte result, byte type, int objectID, int questionsCleared, int points, int timeLimit){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(1);
		mplew.writeInt(npc);
		mplew.write(6);
		mplew.write(0);
		mplew.write(result);
		mplew.writeInt(type);
		mplew.writeInt(objectID);
		mplew.writeInt(questionsCleared);
		mplew.writeInt(points);
		mplew.writeInt(timeLimit);
		return mplew.getPacket();
	}

	public static byte[] getNPCTalkStyle(int npc, String talk, int styles[]){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc);
		mplew.write(7);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.write(styles.length);
		for(int i = 0; i < styles.length; i++){
			mplew.writeInt(styles[i]);
		}
		return mplew.getPacket();
	}

	public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // ?
		mplew.writeInt(npc);
		mplew.write(3);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeInt(def);
		mplew.writeInt(min);
		mplew.writeInt(max);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] getNPCTalkText(int npc, String talk, String def){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_TALK.getValue());
		mplew.write(4); // Doesn't matter
		mplew.writeInt(npc);
		mplew.write(2);
		mplew.write(0); // speaker
		mplew.writeMapleAsciiString(talk);
		mplew.writeMapleAsciiString(def);// :D
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
		mplew.write(1);
		mplew.writeShort(1);
		mplew.writeInt(skillid);
		mplew.writeInt(level);
		mplew.writeInt(masterlevel);
		addExpirationTime(mplew, expiration);
		mplew.write(4);
		return mplew.getPacket();
	}

	public static byte[] getShowQuestCompletion(int id){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.QUEST_CLEAR.getValue());
		mplew.writeShort(id);
		return mplew.getPacket();
	}

	public static byte[] getInventoryFull(){
		return modifyInventory(true, Collections.<ModifyInventory> emptyList());
	}

	public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, int meso){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x16);
		mplew.writeInt(npcId);
		mplew.write(slots);
		mplew.writeShort(0x7E);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		mplew.writeShort(0);
		mplew.write((byte) items.size());
		for(Item item : items){
			addItemInfo(mplew, item, true);
		}
		mplew.writeShort(0);
		mplew.write(0);
		return mplew.getPacket();
	}

	/*
	 * 0x0A = Inv full
	 * 0x0B = You do not have enough mesos
	 * 0x0C = One-Of-A-Kind error
	 */
	public static byte[] getStorageError(byte i){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(i);
		return mplew.getPacket();
	}

	public static byte[] mesoStorage(byte slots, int meso){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x13);
		mplew.write(slots);
		mplew.writeShort(2);
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.writeInt(meso);
		return mplew.getPacket();
	}

	public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0xD);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for(Item item : items){
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STORAGE.getValue());
		mplew.write(0x9);
		mplew.write(slots);
		mplew.writeShort(type.getBitfieldEncoding());
		mplew.writeShort(0);
		mplew.writeInt(0);
		mplew.write(items.size());
		for(Item item : items){
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	/**
	 * @param oid
	 * @param remhppercentage
	 * @return
	 */
	public static byte[] showMonsterHP(int oid, int remhppercentage){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
		mplew.writeInt(oid);
		mplew.write(remhppercentage);
		return mplew.getPacket();
	}

	public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(5);
		mplew.writeInt(oid);
		mplew.writeInt(currHP);
		mplew.writeInt(maxHP);
		mplew.write(tagColor);
		mplew.write(tagBgColor);
		return mplew.getPacket();
	}

	public static byte[] giveFameResponse(int mode, String charname, int newfame){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(charname);
		mplew.write(mode);
		mplew.writeShort(newfame);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	/**
	 * status can be: <br>
	 * 0: ok, use giveFameResponse<br>
	 * 1: the username is
	 * incorrectly entered<br>
	 * 2: users under level 15 are unable to toggle with
	 * fame.<br>
	 * 3: can't raise or drop fame anymore today.<br>
	 * 4: can't raise
	 * or drop fame for this character for this month anymore.<br>
	 * 5: received
	 * fame, use receiveFame()<br>
	 * 6: level of fame neither has been raised nor
	 * dropped due to an unexpected error
	 *
	 * @param status
	 * @return
	 */
	public static byte[] giveFameErrorResponse(int status){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(status);
		return mplew.getPacket();
	}

	public static byte[] receiveFame(int mode, String charnameFrom){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
		mplew.write(5);
		mplew.writeMapleAsciiString(charnameFrom);
		mplew.write(mode);
		return mplew.getPacket();
	}

	/**
	 * mode: 0 buddychat; 1 partychat; 2 guildchat
	 *
	 * @param sender
	 * @param chattext
	 * @param mode
	 * @return
	 */
	public static byte[] multiChat(String sender, String chattext, int mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MULTICHAT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(sender);
		mplew.writeMapleAsciiString(chattext);
		return mplew.getPacket();
	}

	public static byte[] getClock(int time){ // time in seconds
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOCK.getValue());
		mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
		mplew.writeInt(time);
		return mplew.getPacket();
	}

	public static byte[] getClockTime(int hour, int min, int sec){ // Current Time
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CLOCK.getValue());
		mplew.write(1); // Clock-Type
		mplew.write(hour);
		mplew.write(min);
		mplew.write(sec);
		return mplew.getPacket();
	}

	public static byte[] removeClock(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STOP_CLOCK.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] damageMonster(int oid, int damage){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(oid);
		mplew.write(0);
		mplew.writeInt(damage);
		// if the mob is bDamageByMob
		mplew.writeInt(0);
		mplew.writeInt(0);
		// v6 = CInPacket::Decode4(iPacket);
		// v7 = CInPacket::Decode4(iPacket);
		// CMob::CreateHPIndicator(v2, (signed int)(100 * v6) / v7, 0xFFFF0000u);
		return mplew.getPacket();
	}

	public static byte[] healMonster(int oid, int heal){
		return damageMonster(oid, -heal);
	}

	public static byte[] showChair(int characterid, int itemid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_CHAIR.getValue());
		mplew.writeInt(characterid);
		mplew.writeInt(itemid);
		return mplew.getPacket();
	}

	public static byte[] cancelChair(int id){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
		if(id == -1){
			mplew.write(0);
		}else{
			mplew.write(1);
			mplew.writeShort(id);
		}
		return mplew.getPacket();
	}

	public static byte[] musicChange(String song){
		return environmentChange(song, 6);
	}

	public static byte[] showEffect(String effect){
		return environmentChange(effect, 3);
	}

	public static byte[] playSound(String sound){
		return environmentChange(sound, 4);
	}

	/**
	 * public enum FieldEffect {
	 * Summon(0x0),
	 * Tremble(0x1),
	 * Object(0x2),
	 * Object_Disable(0x3),
	 * Screen(0x3),
	 * Sound(0x4),
	 * MobHPTag(0x5),
	 * ChangeBGM(0x6),
	 * BGMVolumeOnly(0x8),
	 * BGMVolume(0x9),
	 * RewordRullet(0x7),
	 * TopScreen(0xB),
	 * Screen_Delayed(0xC),
	 * TopScreen_Delayed(0xD),
	 * Screen_AutoLetterBox(0xE),
	 * FloatingUI(0xF),
	 * Blind(0x10),
	 * GrayScale(0x11),
	 * OnOffLayer(0x12),
	 * Overlap(0x13),
	 * Overlap_Detail(0x14),
	 * Remove_Overlap_Detail(0x15),
	 * ColorChange(0x16),
	 * StageClear(0x17),
	 * TopScreen_WithOrigin(0x18),
	 * SpineScreen(0x19),
	 * OffSpineScreen(0x1A);
	 */
	public static byte[] environmentChange(String env, int mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(env);
		return mplew.getPacket();
	}

	public static byte[] environmentMove(String env, int mode){ // 2 if off, 1 is on
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF.getValue());
		mplew.writeMapleAsciiString(env);
		mplew.writeInt(mode);
		return mplew.getPacket();
	}

	public static byte[] environmentToggle(String env, int mode){ // I DON'T EVEN KNOW
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF_STATUS.getValue());
		mplew.writeInt(mode); // ???
		if(mode > 0){
			mplew.writeMapleAsciiString(env);
			mplew.writeInt(mode);
		}
		return mplew.getPacket();
	}

	public static byte[] startMapEffect(String msg, int itemid, boolean active){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
		mplew.write(active ? 0 : 1);
		mplew.writeInt(itemid);
		if(active){
			mplew.writeMapleAsciiString(msg);
		}
		return mplew.getPacket();
	}

	public static byte[] removeMapEffect(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
		mplew.write(0);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] mapEffect(String path){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(3);
		mplew.writeMapleAsciiString(path);
		return mplew.getPacket();
	}

	public static byte[] mapSound(String path){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(4);
		mplew.writeMapleAsciiString(path);
		return mplew.getPacket();
	}

	public static byte[] showGuildInfo(MapleCharacter c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x1A); // signature for showing guild info
		if(c == null){ // show empty guild (used for leaving, expelled)
			mplew.write(0);
			return mplew.getPacket();
		}
		MapleGuild g = null;
		try{
			g = ChannelServer.getInstance().getWorldInterface().getGuild(c.getMGC());
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		if(g == null){ // failed to read from DB - don't show a guild
			mplew.write(0);
			return mplew.getPacket();
		}else{
			c.setGuildRank(c.getGuildRank());
		}
		mplew.write(1); // bInGuild
		mplew.writeInt(g.getId());
		mplew.writeMapleAsciiString(g.getName());
		for(int i = 1; i <= 5; i++){
			mplew.writeMapleAsciiString(g.getRankTitle(i));
		}
		Collection<MapleGuildCharacter> members = g.getMembers();
		mplew.write(members.size()); // then it is the size of all the members
		for(MapleGuildCharacter mgc : members){// and each of their character ids o_O
			mplew.writeInt(mgc.getId());
		}
		for(MapleGuildCharacter mgc : members){
			mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(mgc.getGP());// people use to think this was guild 'signature'.. how retarded were they
			mplew.writeInt(mgc.getAllianceRank());
		}
		mplew.writeInt(g.getCapacity());
		mplew.writeShort(g.getLogoBG());
		mplew.write(g.getLogoBGColor());
		mplew.writeShort(g.getLogo());
		mplew.write(g.getLogoColor());
		mplew.writeMapleAsciiString(g.getNotice());
		mplew.writeInt(g.getGP());
		mplew.writeInt(g.getAllianceId());
		return mplew.getPacket();
	}

	public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3d);
		mplew.writeInt(gid);
		mplew.writeInt(cid);
		mplew.write(bOnline ? 1 : 0);
		return mplew.getPacket();
	}

	public static byte[] guildInvite(int gid, String charName, int level, int jobCode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x05);
		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(charName);
		mplew.writeInt(level);// nLevel
		mplew.writeInt(jobCode);// nJobCode
		return mplew.getPacket();
	}

	/**
	 * 'Char' has denied your guild invitation.
	 *
	 * @param charname
	 * @return
	 */
	public static byte[] denyGuildInvitation(String charname){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x37);
		mplew.writeMapleAsciiString(charname);
		return mplew.getPacket();
	}

	public static byte[] genericGuildMessage(byte code){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(code);
		return mplew.getPacket();
	}

	public static byte[] newGuildMember(MapleGuildCharacter mgc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x27);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
		mplew.writeInt(mgc.getJobId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getGuildRank()); // should be always 5 but whatevs
		mplew.writeInt(mgc.isOnline() ? 1 : 0); // should always be 1 too
		mplew.writeInt(1); // ? could be guild signature, but doesn't seem to matter
		mplew.writeInt(3);
		return mplew.getPacket();
	}

	// someone leaving, mode == 0x2c for leaving, 0x2f for expelled
	public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(bExpelled ? 0x2f : 0x2c);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeMapleAsciiString(mgc.getName());
		return mplew.getPacket();
	}

	// rank change
	public static byte[] changeRank(MapleGuildCharacter mgc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x40);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.write(mgc.getGuildRank());
		return mplew.getPacket();
	}

	public static byte[] guildNotice(int gid, String notice){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x44);
		mplew.writeInt(gid);
		mplew.writeMapleAsciiString(notice);
		return mplew.getPacket();
	}

	public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3C);
		mplew.writeInt(mgc.getGuildId());
		mplew.writeInt(mgc.getId());
		mplew.writeInt(mgc.getLevel());
		mplew.writeInt(mgc.getJobId());
		return mplew.getPacket();
	}

	public static byte[] rankTitleChange(int gid, String[] ranks){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3E);
		mplew.writeInt(gid);
		for(int i = 0; i < 5; i++){
			mplew.writeMapleAsciiString(ranks[i]);
		}
		return mplew.getPacket();
	}

	public static byte[] guildDisband(int gid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x32);
		mplew.writeInt(gid);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] guildQuestWaitingNotice(byte channel, int waitingPos){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x4C);
		mplew.write(channel);
		mplew.write(waitingPos);
		return mplew.getPacket();
	}

	public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x42);
		mplew.writeInt(gid);
		mplew.writeShort(bg);
		mplew.write(bgcolor);
		mplew.writeShort(logo);
		mplew.write(logocolor);
		return mplew.getPacket();
	}

	public static byte[] guildCapacityChange(int gid, int capacity){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x3A);
		mplew.writeInt(gid);
		mplew.write(capacity);
		return mplew.getPacket();
	}

	public static void addThread(final MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException{
		mplew.writeInt(rs.getInt("localthreadid"));
		mplew.writeInt(rs.getInt("postercid"));
		mplew.writeMapleAsciiString(rs.getString("name"));
		mplew.writeLong(getTime(rs.getLong("timestamp")));
		mplew.writeInt(rs.getInt("icon"));
		mplew.writeInt(rs.getInt("replycount"));
	}

	public static byte[] BBSThreadList(ResultSet rs, int start) throws SQLException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
		mplew.write(0x06);
		if(!rs.last()){
			mplew.write(0);
			mplew.writeInt(0);
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		int threadCount = rs.getRow();
		if(rs.getInt("localthreadid") == 0){ // has a notice
			mplew.write(1);
			addThread(mplew, rs);
			threadCount--; // one thread didn't count (because it's a notice)
		}else{
			mplew.write(0);
		}
		if(!rs.absolute(start + 1)){ // seek to the thread before where we start
			rs.first(); // uh, we're trying to start at a place past possible
			start = 0;
		}
		mplew.writeInt(threadCount);
		mplew.writeInt(Math.min(10, threadCount - start));
		for(int i = 0; i < Math.min(10, threadCount - start); i++){
			addThread(mplew, rs);
			rs.next();
		}
		return mplew.getPacket();
	}

	public static byte[] showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
		mplew.write(0x07);
		mplew.writeInt(localthreadid);
		mplew.writeInt(threadRS.getInt("postercid"));
		mplew.writeLong(getTime(threadRS.getLong("timestamp")));
		mplew.writeMapleAsciiString(threadRS.getString("name"));
		mplew.writeMapleAsciiString(threadRS.getString("startpost"));
		mplew.writeInt(threadRS.getInt("icon"));
		if(repliesRS != null){
			int replyCount = threadRS.getInt("replycount");
			mplew.writeInt(replyCount);
			int i;
			for(i = 0; i < replyCount && repliesRS.next(); i++){
				mplew.writeInt(repliesRS.getInt("replyid"));
				mplew.writeInt(repliesRS.getInt("postercid"));
				mplew.writeLong(getTime(repliesRS.getLong("timestamp")));
				mplew.writeMapleAsciiString(repliesRS.getString("content"));
			}
			if(i != replyCount || repliesRS.next()){ throw new RuntimeException(String.valueOf(threadRS.getInt("threadid"))); }
		}else{
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static byte[] showGuildRanks(int npcid, ResultSet rs) throws SQLException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x49);
		mplew.writeInt(npcid);
		if(!rs.last()){ // no guilds o.o
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		mplew.writeInt(rs.getRow()); // number of entries
		rs.beforeFirst();
		while(rs.next()){
			mplew.writeMapleAsciiString(rs.getString("name"));
			mplew.writeInt(rs.getInt("GP"));
			mplew.writeInt(rs.getInt("logo"));
			mplew.writeInt(rs.getInt("logoColor"));
			mplew.writeInt(rs.getInt("logoBG"));
			mplew.writeInt(rs.getInt("logoBGColor"));
		}
		return mplew.getPacket();
	}

	public static byte[] showPlayerRanks(int npcid, ResultSet rs) throws SQLException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x49);
		mplew.writeInt(npcid);
		if(!rs.last()){
			mplew.writeInt(0);
			return mplew.getPacket();
		}
		mplew.writeInt(rs.getRow());
		rs.beforeFirst();
		while(rs.next()){
			mplew.writeMapleAsciiString(rs.getString("name"));
			mplew.writeInt(rs.getInt("level"));
			mplew.writeInt(0);
			mplew.writeInt(0);
			mplew.writeInt(0);
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static byte[] updateGP(int gid, int GP){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
		mplew.write(0x48);
		mplew.writeInt(gid);
		mplew.writeInt(GP);
		return mplew.getPacket();
	}

	public static byte[] skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed, byte direction){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		mplew.write(level);
		mplew.write(flags);
		mplew.write(speed);
		mplew.write(direction); // Mmmk
		return mplew.getPacket();
	}

	public static byte[] skillCancel(MapleCharacter from, int skillId){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
		mplew.writeInt(from.getId());
		mplew.writeInt(skillId);
		return mplew.getPacket();
	}

	public static byte[] showMagnet(int mobid, byte success){ // Monster Magnet
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOW_MAGNET.getValue());
		mplew.writeInt(mobid);
		mplew.write(success);
		mplew.skip(10); // Mmmk
		return mplew.getPacket();
	}

	/**
	 * Sends a player hint.
	 *
	 * @param hint The hint it's going to send.
	 * @param width How tall the box is going to be.
	 * @param height How long the box is going to be.
	 * @return The player hint packet.
	 */
	public static byte[] sendHint(String hint, int width, int height){
		if(width < 1){
			width = hint.length() * 10;
			if(width < 40){
				width = 40;
			}
		}
		if(height < 5){
			height = 5;
		}
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
		mplew.writeMapleAsciiString(hint);
		mplew.writeShort(width);
		mplew.writeShort(height);
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] messengerInvite(String from, int messengerid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x03);
		mplew.writeMapleAsciiString(from);
		mplew.write(0);
		mplew.writeInt(messengerid);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] sendSpouseChat(MapleCharacter wife, String msg){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
		mplew.writeMapleAsciiString(wife.getName());
		mplew.writeMapleAsciiString(msg);
		return mplew.getPacket();
	}

	public static byte[] addMessengerPlayer(String from, MapleCharacterLook mcl, int position, int channel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x00);
		mplew.write(position);
		addCharLook(mplew, mcl, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x00);
		mplew.write(position);
		addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static byte[] removeMessengerPlayer(int position){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x02);
		mplew.write(position);
		return mplew.getPacket();
	}

	public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x07);
		mplew.write(position);
		addCharLook(mplew, chr, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static byte[] updateMessengerPlayer(String from, MapleCharacterLook mcl, int position, int channel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x07);
		mplew.write(position);
		addCharLook(mplew, mcl, true);
		mplew.writeMapleAsciiString(from);
		mplew.write(channel);
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static byte[] joinMessenger(int position){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x01);
		mplew.write(position);
		return mplew.getPacket();
	}

	public static byte[] messengerChat(String text){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(0x06);
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	public static byte[] messengerNote(String text, int mode, int mode2){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MESSENGER.getValue());
		mplew.write(mode);
		mplew.writeMapleAsciiString(text);
		mplew.write(mode2);
		return mplew.getPacket();
	}

	public static void addPetInfo(final MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet){// CPet::Init
		mplew.write(1);
		if(showpet){
			mplew.write(0);
		}
		mplew.writeInt(pet.getItemId());
		mplew.writeMapleAsciiString(pet.getName());
		mplew.writeInt(pet.getUniqueId());
		mplew.writeInt(0);// this is a long^
		mplew.writePos(pet.getPos());
		mplew.write(pet.getStance());
		mplew.writeShort(pet.getFh());
		mplew.write(0);// m_bNameTag
		mplew.write(0);// m_bChatBalloon
	}

	public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_PET.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getPetIndex(pet));
		if(remove){
			mplew.write(0);
			mplew.write(hunger ? 1 : 0);
		}else{
			addPetInfo(mplew, pet, true);
		}
		return mplew.getPacket();
	}

	public static byte[] movePet(int cid, byte slot, MovePath moves){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_PET.getValue());
		mplew.writeInt(cid);
		mplew.write(slot);
		moves.encode(mplew);
		return mplew.getPacket();
	}

	public static byte[] petChat(int cid, byte index, int n, int nAction, String text){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_CHAT.getValue());
		mplew.writeInt(cid);
		mplew.write(index);//
		mplew.write(n);
		mplew.write(nAction);
		mplew.writeMapleAsciiString(text);
		mplew.write(0);// bChatBalloon
		/*
		    COutPacket::COutPacket(&oPacket, 125, 0);
		    v6 = v2->m_pOwner->m_dwCharacterID;
		    LOBYTE(v13) = 3;
		    COutPacket::Encode4(&oPacket, v6);
		    COutPacket::Encode1(&oPacket, n[0]);
		    COutPacket::Encode1(&oPacket, nAction);
		    v6 = 0;
		    ZXString<char>::operator_((ZXString<char> *)&v6, &result);
		    COutPacket::EncodeStr(&oPacket, (ZXString<char>)v6);
		    CPet::UpdatePetAbility(v2);
		    COutPacket::Encode1(&oPacket, v2->m_bChatBalloon);
		
		 */
		return mplew.getPacket();
	}

	public static byte[] commandResponse(int cid, byte index, int animation, boolean success){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
		mplew.writeInt(cid);
		mplew.write(index);
		mplew.write(0);// v3, if set to 1 it requires a cc, map change, etc
		mplew.write(animation);// n[0]
		mplew.writeBoolean(success);// thisa
		mplew.writeBoolean(false);// m_bChatBalloon
		return mplew.getPacket();
	}

	public static byte[] changePetName(MapleCharacter chr, String newname, int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(0);
		mplew.writeMapleAsciiString(newname);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] petStatUpdate(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
		int mask = 0;
		mask |= MapleStat.PETSN.getValue();
		mplew.write(0);
		mplew.writeInt(mask);
		MaplePet[] pets = chr.getPets();
		for(int i = 0; i < 3; i++){
			if(pets[i] != null){
				mplew.writeInt(pets[i].getUniqueId());
				mplew.writeInt(0);
			}else{
				mplew.writeLong(0);
			}
		}
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] showForcedEquip(int team){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
		if(team > -1){
			mplew.write(team); // 00 = red, 01 = blue
		}
		return mplew.getPacket();
	}

	/**
	 * public static OutPacket OnSkill(int dwCharacterID, int dwSummonedID, int nAttackAction) {
	 * OutPacket oPacket = new OutPacket(LoopbackPacket.SummonedSkill, false);
	 * oPacket.Encode4(dwCharacterID);
	 * oPacket.Encode4(dwSummonedID);
	 * oPacket.Encode1(nAttackAction); //nAttackAction & 0x7F
	 * return oPacket;
	 * }
	 */
	public static byte[] skillCooldown(int sid, int time){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.COOLDOWN.getValue());
		mplew.writeInt(sid);
		mplew.writeShort(time);// Int in v97
		return mplew.getPacket();
	}

	public static byte[] skillBookSuccess(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
		mplew.writeBoolean(false);// bOnExclRequest
		mplew.writeInt(chr.getId());
		mplew.writeBoolean(true);// bIsMaterbook
		mplew.writeInt(skillid);
		mplew.writeInt(maxlevel);
		mplew.writeBoolean(canuse);// bUsed
		mplew.writeBoolean(success);// bSucceed
		return mplew.getPacket();
	}

	public static byte[] updateAriantPQRanking(String name, int score, boolean empty){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ARIANT_SCORE.getValue());
		mplew.write(empty ? 0 : 1);
		if(!empty){
			mplew.writeMapleAsciiString(name);
			mplew.writeInt(score);
		}
		return mplew.getPacket();
	}

	public static byte[] catchMonster(int monsobid, int itemid, byte success){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CATCH_MONSTER.getValue());
		mplew.writeInt(monsobid);
		mplew.writeInt(itemid);
		mplew.write(success);
		return mplew.getPacket();
	}

	public static byte[] catchMessage(int message){ // not done, I guess
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BRIDLE_MOB_CATCH_FAIL.getValue());
		mplew.write(message); // 1 = too strong, 2 = Elemental Rock
		mplew.writeInt(0);// Maybe itemid?
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] showAllCharacter(int chars, int unk){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
		mplew.write(1);
		mplew.writeInt(chars);
		mplew.writeInt(unk);
		return mplew.getPacket();
	}

	public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
		mplew.write(0);
		mplew.write(worldid);
		mplew.write(chars.size());
		for(MapleCharacter chr : chars){
			addCharEntry(mplew, chr, true);
		}
		return mplew.getPacket();
	}

	public static byte[] updateMount(int charid, MapleMount mount, boolean levelup){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
		mplew.writeInt(charid);
		mplew.writeInt(mount.getLevel());
		mplew.writeInt(mount.getExp());
		mplew.writeInt(mount.getTiredness());
		mplew.write(levelup ? (byte) 1 : (byte) 0);
		return mplew.getPacket();
	}

	public static byte[] boatPacket(boolean type){// don't think this is correct..
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CONTI_STATE.getValue());
		mplew.write(type ? 1 : 2);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] contiMoveShip(boolean show){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CONTI_MOVE.getValue());
		mplew.write(0x0A);
		mplew.write(show ? 4 : 5);
		return mplew.getPacket();
	}

	public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(1);// nMiniRoomType
		mplew.write(0);// nMaxUsers
		mplew.write(owner ? 0 : 1);// nMyPosition
		mplew.write(0);// for ( i = CInPacket::Decode1(iPacket); i >= 0; i = CInPacket::Decode1(iPacket) )
		addCharLook(mplew, minigame.getOwner(), false);
		mplew.writeMapleAsciiString(minigame.getOwner().getName());
		// job id short in later version
		if(minigame.getVisitor() != null){
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);// slot id
			addCharLook(mplew, visitor, false);
			mplew.writeMapleAsciiString(visitor.getName());
			// job id short in later version
		}
		mplew.write(0xFF);
		mplew.write(0);// slot of the person below.(owner is always 0)
		mplew.writeInt(1);// game type
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", true));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", true));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", true));
		mplew.writeInt(2000);// score
		if(minigame.getVisitor() != null){
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);// slot of the person below(omok only has 1 other player)
			mplew.writeInt(1);// game type
			mplew.writeInt(visitor.getMiniGamePoints("wins", true));
			mplew.writeInt(visitor.getMiniGamePoints("ties", true));
			mplew.writeInt(visitor.getMiniGamePoints("losses", true));
			mplew.writeInt(2000);// score
		}
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(minigame.getDescription());
		mplew.writeShort(piece);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameReady(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.READY.getCode());
		return mplew.getPacket();
	}

	public static byte[] getMiniGameUnReady(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
		return mplew.getPacket();
	}

	public static byte[] getMiniGameStart(MapleMiniGame game, int loser){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.START.getCode());
		mplew.write(loser);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameSkipOwner(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
		mplew.write(0x01);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameRequestTie(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
		return mplew.getPacket();
	}

	public static byte[] getMiniGameDenyTie(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
		return mplew.getPacket();
	}

	public static byte[] getMiniGameFull(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(0);
		mplew.write(2);
		return mplew.getPacket();
	}

	public static byte[] getMiniGamePassIncorrect(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(0);
		mplew.write(28);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameSkipVisitor(MapleMiniGame game){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
		mplew.write(0x00);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
		mplew.writeInt(move1);
		mplew.writeInt(move2);
		mplew.write(move3);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		mplew.writeInt(1);
		mplew.writeInt(c.getMiniGamePoints("wins", true));
		mplew.writeInt(c.getMiniGamePoints("ties", true));
		mplew.writeInt(c.getMiniGamePoints("losses", true));
		mplew.writeInt(2000);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameRemoveVisitor(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(1);
		return mplew.getPacket();
	}

	private static byte[] getMiniGameResult(MapleMiniGame game, int win, int lose, int tie, int result, int forfeit, boolean omok){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
		if(tie == 0 && forfeit != 1){
			mplew.write(0);
		}else if(tie == 1){
			mplew.write(1);
		}else if(forfeit == 1){
			mplew.write(2);
		}
		mplew.write(result - 1); // winner
		mplew.writeInt(1); // unknown
		mplew.writeInt(game.getOwner().getMiniGamePoints("wins", omok) + win); // wins
		mplew.writeInt(game.getOwner().getMiniGamePoints("ties", omok) + tie); // ties
		mplew.writeInt(game.getOwner().getMiniGamePoints("losses", omok) + lose); // losses
		mplew.writeInt(2000); // points
		mplew.writeInt(1); // start of visitor; unknown
		mplew.writeInt(game.getVisitor().getMiniGamePoints("wins", omok) + lose); // wins
		mplew.writeInt(game.getVisitor().getMiniGamePoints("ties", omok) + tie); // ties
		mplew.writeInt(game.getVisitor().getMiniGamePoints("losses", omok) + win); // losses
		mplew.writeInt(2000); // points
		game.getOwner().setMiniGamePoints(game.getVisitor(), result, omok);
		return mplew.getPacket();
	}

	public static byte[] getMiniGameOwnerWin(MapleMiniGame game){
		return getMiniGameResult(game, 1, 0, 0, 1, 0, true);
	}

	public static byte[] getMiniGameVisitorWin(MapleMiniGame game){
		return getMiniGameResult(game, 0, 1, 0, 2, 0, true);
	}

	public static byte[] getMiniGameTie(MapleMiniGame game){
		return getMiniGameResult(game, 0, 0, 1, 3, 0, true);
	}

	public static byte[] getMiniGameOwnerForfeit(MapleMiniGame game){
		return getMiniGameResult(game, 0, 1, 0, 2, 1, true);
	}

	public static byte[] getMiniGameVisitorForfeit(MapleMiniGame game){
		return getMiniGameResult(game, 1, 0, 0, 1, 1, true);
	}

	public static byte[] getMiniGameClose(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		mplew.write(1);
		mplew.write(3);
		return mplew.getPacket();
	}

	public static byte[] getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
		mplew.write(2);
		mplew.write(2);
		mplew.write(owner ? 0 : 1);
		mplew.write(0);
		addCharLook(mplew, minigame.getOwner(), false);
		mplew.writeMapleAsciiString(minigame.getOwner().getName());
		if(minigame.getVisitor() != null){
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			addCharLook(mplew, visitor, false);
			mplew.writeMapleAsciiString(visitor.getName());
		}
		mplew.write(0xFF);
		mplew.write(0);
		mplew.writeInt(2);
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("wins", false));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("ties", false));
		mplew.writeInt(minigame.getOwner().getMiniGamePoints("losses", false));
		mplew.writeInt(2000);
		if(minigame.getVisitor() != null){
			MapleCharacter visitor = minigame.getVisitor();
			mplew.write(1);
			mplew.writeInt(2);
			mplew.writeInt(visitor.getMiniGamePoints("wins", false));
			mplew.writeInt(visitor.getMiniGamePoints("ties", false));
			mplew.writeInt(visitor.getMiniGamePoints("losses", false));
			mplew.writeInt(2000);
		}
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(minigame.getDescription());
		mplew.write(piece);
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] getMatchCardStart(MapleMiniGame game, int loser){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.START.getCode());
		mplew.write(loser);
		int last = 13;
		if(game.getMatchesToWin() > 10){
			last = 31;
		}else if(game.getMatchesToWin() > 6){
			last = 21;
		}
		mplew.write(last - 1);
		for(int i = 1; i < last; i++){
			mplew.writeInt(game.getCardId(i));
		}
		return mplew.getPacket();
	}

	public static byte[] getMatchCardNewVisitor(MapleCharacter c, int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		mplew.writeInt(1);
		mplew.writeInt(c.getMiniGamePoints("wins", false));
		mplew.writeInt(c.getMiniGamePoints("ties", false));
		mplew.writeInt(c.getMiniGamePoints("losses", false));
		mplew.writeInt(2000);
		return mplew.getPacket();
	}

	public static byte[] getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
		mplew.write(turn);
		if(turn == 1){
			mplew.write(slot);
		}else if(turn == 0){
			mplew.write(slot);
			mplew.write(firstslot);
			mplew.write(type);
		}
		return mplew.getPacket();
	}

	public static byte[] getMatchCardOwnerWin(MapleMiniGame game){
		return getMiniGameResult(game, 1, 0, 0, 1, 0, false);
	}

	public static byte[] getMatchCardVisitorWin(MapleMiniGame game){
		return getMiniGameResult(game, 0, 1, 0, 2, 0, false);
	}

	public static byte[] getMatchCardTie(MapleMiniGame game){
		return getMiniGameResult(game, 0, 0, 1, 3, 0, false);
	}

	public static byte[] fredrickMessage(byte operation){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
		mplew.write(operation);
		return mplew.getPacket();
	}

	public static byte[] getFredrick(byte op){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK.getValue());
		mplew.write(op);
		switch (op){
			case 0x24:
				mplew.skip(8);
				break;
			default:
				mplew.write(0);
				break;
		}
		return mplew.getPacket();
	}

	public static byte[] getFredrick(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FREDRICK.getValue());
		mplew.write(0x23);
		mplew.writeInt(9030000); // Fredrick
		mplew.write(16);// slot count
		long flag = DBChar.Money | DBChar.ItemSlotEquip | DBChar.ItemSlotConsume | DBChar.ItemSlotInstall | DBChar.ItemSlotEtc;
		mplew.writeLong(flag);
		if((flag & DBChar.Money) > 0) mplew.writeInt(chr.getMerchantMeso());
		try{
			Map<MapleInventoryType, List<Item>> items = ItemFactory.MERCHANT.loadItems(chr.getId());
			for(byte mitT = MapleInventoryType.EQUIP.getType(); mitT <= MapleInventoryType.CASH.getType(); mitT++){
				MapleInventoryType mit = MapleInventoryType.getByType(mitT);
				if((flag & DBChar.getByInventoryType(mit)) > 0){
					List<Item> itemList = items.get(mit);
					if(itemList == null){
						mplew.write(0);
						continue;
					}
					mplew.write(itemList.size());
					for(int i = 0; i < itemList.size(); i++){
						addItemInfo(mplew, itemList.get(i), true, true);
					}
				}
			}
		}catch(SQLException e){
			mplew.skip(5);
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
		/**
		 * This packet has 2 other cases. ^ is OpenStoreBankDlg
		 * case StoreBankLoadFailed:
		 * oPacket.Encode4(nTemplateID);//npc
		 * oPacket.Encode4(dwFieldID);//mapid
		 * oPacket.Encode1(nChannelID);
		 * break;
		 * case StoreBankCalculateFee:
		 * oPacket.Encode4(nPassingDay);
		 * oPacket.Encode4(nFee);
		 * break;
		 */
		return mplew.getPacket();
	}

	public static byte[] addOmokBox(MapleCharacter c, int ammount, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getMiniGame(), 1, 0, ammount, type);
		return mplew.getPacket();
	}

	public static byte[] removeOmokBox(MapleCharacter c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] addMatchCardBox(MapleCharacter c, int ammount, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		addAnnounceBox(mplew, c.getMiniGame(), 2, 0, ammount, type);
		return mplew.getPacket();
	}

	public static byte[] removeMatchcardBox(MapleCharacter c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
		mplew.writeInt(c.getId());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] hiredMerchantBox(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
		mplew.write(0x07);
		return mplew.getPacket();
	}

	public static byte[] retrieveFirstMessage(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
		mplew.write(0x09);
		return mplew.getPacket();
	}

	public static byte[] remoteChannelChange(byte ch){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
		mplew.write(0x10);
		mplew.writeInt(0);// No idea yet
		mplew.write(ch);
		return mplew.getPacket();
	}

	public static byte[] spawnPlayerNPC(PlayerNPC npc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
		mplew.write(1);
		mplew.writeInt(npc.getObjectId());
		mplew.writeInt(npc.getId());
		mplew.writeShort(npc.getPosition().x);
		mplew.writeShort(npc.getCY());
		mplew.write(npc.getDirection());
		mplew.writeShort(npc.getFH());
		mplew.writeShort(npc.getRX0());
		mplew.writeShort(npc.getRX1());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] getPlayerNPC(PlayerNPC npc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
		mplew.write(0x01);
		mplew.writeInt(npc.getId());
		mplew.writeMapleAsciiString(npc.getName());
		mplew.write(npc.getGender());
		mplew.write(npc.getSkin());
		mplew.writeInt(npc.getFace());
		mplew.write(0);
		mplew.writeInt(npc.getHair());
		Map<Short, Integer> equip = npc.getEquips();
		Map<Short, Integer> myEquip = new LinkedHashMap<Short, Integer>();
		Map<Short, Integer> maskedEquip = new LinkedHashMap<Short, Integer>();
		for(short position : equip.keySet()){
			short pos = (byte) (position * -1);
			if(pos < 100 && myEquip.get(pos) == null){
				myEquip.put(pos, equip.get(position));
			}else if((pos > 100 || pos == -128) && pos != 111){ // don't ask. o.o
				pos -= 100;
				if(myEquip.get(pos) != null){
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, equip.get(position));
			}else if(myEquip.get(pos) != null){
				maskedEquip.put(pos, equip.get(position));
			}
		}
		for(Entry<Short, Integer> entry : myEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		for(Entry<Short, Integer> entry : maskedEquip.entrySet()){
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		Integer cWeapon = equip.get((byte) -111);
		if(cWeapon != null){
			mplew.writeInt(cWeapon);
		}else{
			mplew.writeInt(0);
		}
		for(int i = 0; i < 3; i++){
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static byte[] sendYellowTip(String tip){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
		mplew.write(0xFF);
		mplew.writeMapleAsciiString(tip);
		mplew.writeShort(0);
		return mplew.getPacket();
	}

	public static byte[] sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages, byte sortType, byte sortColumn){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x15); // operation
		mplew.writeInt(pages * 16); // testing, change to 10 if fails
		mplew.writeInt(items.size()); // number of items
		mplew.writeInt(tab);
		mplew.writeInt(type);
		mplew.writeInt(page);
		mplew.write(sortType);
		mplew.write(sortColumn);
		items.forEach(mii-> mii.encode(mplew));
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] noteSendMsg(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
		mplew.write(4);
		return mplew.getPacket();
	}

	/*
	 *  0 = Player online, use whisper
	 *  1 = Check player's name
	 *  2 = Receiver inbox full
	 */
	public static byte[] noteError(byte error){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
		mplew.write(5);
		mplew.write(error);
		return mplew.getPacket();
	}

	public static byte[] showNotes(ResultSet notes, int count) throws SQLException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
		mplew.write(3);
		mplew.write(count);
		for(int i = 0; i < count; i++){
			mplew.writeInt(notes.getInt("id"));
			mplew.writeMapleAsciiString(notes.getString("from") + " ");// Stupid nexon forgot space lol
			mplew.writeMapleAsciiString(notes.getString("message"));
			mplew.writeLong(getTime(notes.getLong("timestamp")));
			mplew.write(notes.getByte("fame"));// FAME :D
			notes.next();
		}
		return mplew.getPacket();
	}

	public static byte[] trockRefreshMapList(MapleCharacter chr, boolean delete, boolean vip){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAP_TRANSFER_RESULT.getValue());
		mplew.write(delete ? 2 : 3);
		if(vip){
			mplew.write(1);
			List<Integer> map = chr.getVipTrockMaps();
			for(int i = 0; i < 10; i++){
				mplew.writeInt(map.get(i));
			}
		}else{
			mplew.write(0);
			List<Integer> map = chr.getTrockMaps();
			for(int i = 0; i < 5; i++){
				mplew.writeInt(map.get(i));
			}
		}
		return mplew.getPacket();
	}

	public static byte[] showMTSCash(MapleCharacter p){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
		mplew.writeInt(p.getCashShop().getCash(4));
		mplew.writeInt(p.getCashShop().getCash(2));
		return mplew.getPacket();
	}

	public static byte[] MTSWantedListingOver(int nx, int items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x3D);
		mplew.writeInt(nx);
		mplew.writeInt(items);
		return mplew.getPacket();
	}

	public static byte[] MTSConfirmSell(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x1D);
		return mplew.getPacket();
	}

	public static byte[] MTSConfirmBuy(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x33);
		return mplew.getPacket();
	}

	public static byte[] MTSFailBuy(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x34);
		mplew.write(0x42);
		return mplew.getPacket();
	}

	public static byte[] MTSConfirmTransfer(int quantity, int pos){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x27);
		mplew.writeInt(quantity);
		mplew.writeInt(pos);
		return mplew.getPacket();
	}

	public static byte[] MTSOperation(ITCResCode code){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(code.getRes());
		return mplew.getPacket();
	}

	public static byte[] notYetSoldInv(List<MTSItemInfo> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x23);
		mplew.writeInt(items.size());
		if(!items.isEmpty()){
			items.forEach(mii-> mii.encode(mplew));
		}else{
			mplew.writeInt(0);
		}
		return mplew.getPacket();
	}

	public static byte[] transferInventory(List<MTSItemInfo> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
		mplew.write(0x21);
		mplew.writeInt(items.size());
		items.forEach(mii-> mii.encode(mplew));
		mplew.write(0xD0 + items.size());
		mplew.write(new byte[]{-1, -1, -1, 0});
		return mplew.getPacket();
	}

	public static byte[] enableCSUse(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.write(0x12);
		mplew.skip(6);
		return mplew.getPacket();
	}

	/**
	 * @param target
	 * @param mapid
	 * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
	 * @return
	 */
	public static byte[] getFindReply(String target, int mapid, int MTSmapCSchannel){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WHISPER.getValue());
		mplew.write(9);
		mplew.writeMapleAsciiString(target);
		mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
		mplew.writeInt(mapid); // -1 if mts, cs
		if(MTSmapCSchannel == 1){
			mplew.write(new byte[8]);
		}
		return mplew.getPacket();
	}

	public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
		mplew.write(askQuestion ? 1 : 0);
		mplew.write(questionSet);
		mplew.writeShort(questionId);
		return mplew.getPacket();
	}

	public static byte[] updateGender(MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SET_GENDER.getValue());
		mplew.write(chr.getGender());
		return mplew.getPacket();
	}

	public static byte[] enableReport(){ // by snow
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.CLAIM_STATUS_CHANGED.getValue());
		mplew.write(1);
		return mplew.getPacket();
	}

	public static byte[] giveFinalAttack(int skillid, int time){// packets found by lailainoob
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
		mplew.writeLong(0);
		mplew.writeShort(0);
		mplew.write(0);// some 80 and 0 bs DIRECTION
		mplew.write(0x80);// let's just do 80, then 0
		mplew.writeInt(0);
		mplew.writeShort(1);
		mplew.writeInt(skillid);
		mplew.writeInt(time);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] addCard(boolean full, int cardid, int level){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
		mplew.write(full ? 0 : 1);
		mplew.writeInt(cardid);
		mplew.writeInt(level);
		return mplew.getPacket();
	}

	public static byte[] changeCover(int cardid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
		mplew.writeInt(cardid);
		return mplew.getPacket();
	}

	public static byte[] aranGodlyStats(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
		mplew.write(new byte[]{(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
		return mplew.getPacket();
	}

	/**
	 * Sends a UI utility.
	 * 0x01 - Equipment Inventory.
	 * 0x02 - Stat Window.
	 * 0x03 - Skill Window.
	 * 0x05 - Keyboard Settings.
	 * 0x06 - Quest window.
	 * 0x09 - Monsterbook Window.
	 * 0x0A - Char Info
	 * 0x0B - Guild BBS
	 * 0x12 - Monster Carnival Window
	 * 0x16 - Party Search.
	 * 0x17 - Item Creation Window.
	 * 0x1A - My Ranking O.O
	 * 0x1B - Family Window
	 * 0x1C - Family Pedigree
	 * 0x1D - GM Story Board /funny shet
	 * 0x1E - Envelop saying you got mail from an admin. lmfao
	 * 0x1F - Medal Window
	 * 0x20 - Maple Event (???)
	 * 0x21 - Invalid Pointer Crash
	 *
	 * @param ui
	 * @return
	 */
	public static byte[] openUI(byte ui){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.OPEN_UI.getValue());
		mplew.write(ui);
		return mplew.getPacket();
	}

	public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(8);
		mplew.writeMapleAsciiString(msg);
		mplew.write(channel);
		mplew.write(whisper ? 1 : 0);
		if(item == null){
			mplew.write(0);
		}else{
			mplew.write(item.getPosition());
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static byte[] removeNPC(int npcid){ // Make npc's invisible
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_NPC.getValue());
		mplew.writeInt(npcid);
		return mplew.getPacket();
	}

	public static byte[] onLimitedNPCDisableInfo(List<Integer> disabledNpc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.LIMITED_NPC_DISABLE_INFO.getValue());
		mplew.write(disabledNpc.size());
		for(int nDisabledNpc : disabledNpc){
			mplew.writeInt(nDisabledNpc);
		}
		return mplew.getPacket();
	}

	/**
	 * Sends a report response
	 * Possible values for <code>mode</code>:<br>
	 * 0: You have succesfully
	 * reported the user.<br>
	 * 1: Unable to locate the user.<br>
	 * 2: You may only
	 * report users 10 times a day.<br>
	 * 3: You have been reported to the GM's by
	 * a user.<br>
	 * 4: Your request did not go through for unknown reasons.
	 * Please try again later.<br>
	 *
	 * @param mode The mode
	 * @return Report Reponse packet
	 */
	public static byte[] reportResponse(byte mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SUE_CHARACTER_RESULT.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static byte[] sendHammerData(int hammerUsed){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x39);
		mplew.writeInt(0);
		mplew.writeInt(hammerUsed);
		return mplew.getPacket();
	}

	public static byte[] sendHammerMessage(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
		mplew.write(0x3D);
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] makerResult(boolean success, int itemMade, int itemCount, int mesos, List<Pair<Integer, Integer>> itemsLost, boolean catalyst, int catalystID, int amtINCBuffGem, List<Integer> gems){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
		mplew.writeInt(success ? 0 : 1); // 0 = success, 1 = fail
		mplew.writeInt(1); // 1 or 2 doesn't matter, same methods
		mplew.writeBoolean(!success);
		if(success){
			mplew.writeInt(itemMade);
			mplew.writeInt(itemCount);
		}
		mplew.writeInt(itemsLost.size()); // Loop
		for(Pair<Integer, Integer> item : itemsLost){
			mplew.writeInt(item.getLeft());
			mplew.writeInt(item.getRight());
		}
		mplew.writeInt(amtINCBuffGem);
		for(int i = 0; i < amtINCBuffGem; i++){
			mplew.writeInt(gems.get(i));
		}
		mplew.write(catalyst ? 1 : 0); // stimulator
		if(catalyst){
			mplew.writeInt(catalystID);
		}
		mplew.writeInt(mesos);
		return mplew.getPacket();
	}

	public static byte[] makerResultCrystal(int itemIdGained, int itemIdLost){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
		mplew.writeInt(0); // Always successful!
		mplew.writeInt(3); // Monster Crystal
		mplew.writeInt(itemIdGained);
		mplew.writeInt(itemIdLost);
		return mplew.getPacket();
	}

	public static byte[] makerResultDesynth(int itemId, int mesos, List<Pair<Integer, Integer>> itemsGained){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
		mplew.writeInt(0); // Always successful!
		mplew.writeInt(4); // Mode Desynth
		mplew.writeInt(itemId); // Item desynthed
		mplew.writeInt(itemsGained.size()); // Loop of items gained, (int, int)
		for(Pair<Integer, Integer> item : itemsGained){
			mplew.writeInt(item.getLeft());
			mplew.writeInt(item.getRight());
		}
		mplew.writeInt(mesos); // Mesos spent.
		return mplew.getPacket();
	}

	public static byte[] updateQuestFinish(short quest, int npc, short nextquest){ // Check
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); // 0xF2 in v95
		mplew.write(8);// 0x0A in v95
		mplew.writeShort(quest);
		mplew.writeInt(npc);
		mplew.writeShort(nextquest);
		return mplew.getPacket();
	}

	public static byte[] questError(short quest){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(0x0A);
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static byte[] questFailure(byte type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(type);// 0x0B = No meso, 0x0D = Worn by character, 0x0E = Not having the item ?
		return mplew.getPacket();
	}

	public static byte[] questExpire(short quest){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
		mplew.write(0x0F);
		mplew.writeShort(quest);
		return mplew.getPacket();
	}

	public static byte[] getMultiMegaphone(String[] messages, int channel, boolean showEar){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
		mplew.write(0x0A);
		if(messages[0] != null){
			mplew.writeMapleAsciiString(messages[0]);
		}
		mplew.write(messages.length);
		for(int i = 1; i < messages.length; i++){
			if(messages[i] != null){
				mplew.writeMapleAsciiString(messages[i]);
			}
		}
		for(int i = 0; i < 10; i++){
			mplew.write(channel);
		}
		mplew.write(showEar ? 1 : 0);
		mplew.write(1);
		return mplew.getPacket();
	}

	/**
	 * Gets a gm effect packet (ie. hide, banned, etc.)
	 * Possible values for <code>type</code>:<br>
	 * 0x04: You have successfully
	 * blocked access.<br>
	 * 0x05: The unblocking has been successful.<br>
	 * 0x06 with Mode 0: You have
	 * successfully removed the name from the ranks.<br>
	 * 0x06 with Mode 1: You
	 * have entered an invalid character name.<br>
	 * 0x10: GM Hide, mode
	 * determines whether or not it is on.<br>
	 * 0x1E: Mode 0: Failed to send
	 * warning Mode 1: Sent warning<br>
	 * 0x13 with Mode 0: + mapid 0x13 with Mode
	 * 1: + ch (FF = Unable to find merchant)
	 *
	 * @param type The type
	 * @param mode The mode
	 * @return The gm effect packet
	 */
	public static byte[] getGMEffect(int type, byte mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
		mplew.write(type);
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static byte[] findMerchantResponse(boolean map, int extra){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
		mplew.write(0x13);
		mplew.write(map ? 0 : 1); // 00 = mapid, 01 = ch
		if(map){
			mplew.writeInt(extra);
		}else{
			mplew.write(extra); // -1 = unable to find
		}
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] disableMinimap(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
		mplew.writeShort(0x1C);
		return mplew.getPacket();
	}

	private static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, MapleGuild guild){
		mplew.writeInt(guild.getId());
		mplew.writeMapleAsciiString(guild.getName());
		for(int i = 1; i <= 5; i++){
			mplew.writeMapleAsciiString(guild.getRankTitle(i));
		}
		Collection<MapleGuildCharacter> members = guild.getMembers();
		mplew.write(members.size());
		for(MapleGuildCharacter mgc : members){
			mplew.writeInt(mgc.getId());
		}
		for(MapleGuildCharacter mgc : members){
			mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
			mplew.writeInt(mgc.getJobId());
			mplew.writeInt(mgc.getLevel());
			mplew.writeInt(mgc.getGuildRank());
			mplew.writeInt(mgc.isOnline() ? 1 : 0);
			mplew.writeInt(mgc.getGP());
			mplew.writeInt(mgc.getAllianceRank());
		}
		mplew.writeInt(guild.getCapacity());
		mplew.writeShort(guild.getLogoBG());
		mplew.write(guild.getLogoBGColor());
		mplew.writeShort(guild.getLogo());
		mplew.write(guild.getLogoColor());
		mplew.writeMapleAsciiString(guild.getNotice());
		mplew.writeInt(guild.getGP());
		mplew.writeInt(guild.getAllianceId());
	}

	public static byte[] getAllianceInfo(MapleAlliance alliance){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0C);
		mplew.write(1);
		addAllianceInfo(mplew, alliance);
		return mplew.getPacket();
	}

	public static byte[] makeNewAlliance(MapleAlliance alliance, MapleClient c) throws RemoteException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0F);
		addAllianceInfo(mplew, alliance);
		for(Integer guild : alliance.getGuilds()){
			getGuildInfo(mplew, ChannelServer.getInstance().getWorldInterface().getGuild(guild, c.getPlayer().getMGC()));
		}
		return mplew.getPacket();
	}

	public static byte[] getGuildAlliances(MapleAlliance alliance) throws RemoteException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0D);
		mplew.writeInt(alliance.getGuilds().size());
		for(Integer guild : alliance.getGuilds()){
			getGuildInfo(mplew, ChannelServer.getInstance().getWorldInterface().getGuild(guild, null));
		}
		return mplew.getPacket();
	}

	public static byte[] addGuildToAlliance(MapleAlliance alliance, int newGuild) throws RemoteException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x12);
		addAllianceInfo(mplew, alliance);
		mplew.writeInt(newGuild);
		getGuildInfo(mplew, WorldServer.getInstance().getGuild(newGuild, null));
		return mplew.getPacket();
	}

	private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleAlliance alliance){
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for(int i = 1; i <= 5; i++){
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		for(Integer guild : alliance.getGuilds()){
			mplew.writeInt(guild);
		}
		mplew.writeInt(alliance.getCapacity());
		mplew.writeMapleAsciiString(alliance.getNotice());
	}

	public static byte[] allianceMemberOnline(MapleCharacter mc, boolean online){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x0E);
		mplew.writeInt(mc.getGuild().getAllianceId());
		mplew.writeInt(mc.getGuildId());
		mplew.writeInt(mc.getId());
		mplew.write(online ? 1 : 0);
		return mplew.getPacket();
	}

	public static byte[] allianceNotice(int id, String notice){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1C);
		mplew.writeInt(id);
		mplew.writeMapleAsciiString(notice);
		return mplew.getPacket();
	}

	public static byte[] changeAllianceRankTitle(int alliance, String[] ranks){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1A);
		mplew.writeInt(alliance);
		for(int i = 0; i < 5; i++){
			mplew.writeMapleAsciiString(ranks[i]);
		}
		return mplew.getPacket();
	}

	public static byte[] updateAllianceJobLevel(MapleCharacter mc){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x18);
		mplew.writeInt(mc.getGuild().getAllianceId());
		mplew.writeInt(mc.getGuildId());
		mplew.writeInt(mc.getId());
		mplew.writeInt(mc.getLevel());
		mplew.writeInt(mc.getJob().getId());
		return mplew.getPacket();
	}

	public static byte[] removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild) throws RemoteException{
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x10);
		mplew.writeInt(alliance.getId());
		mplew.writeMapleAsciiString(alliance.getName());
		for(int i = 1; i <= 5; i++){
			mplew.writeMapleAsciiString(alliance.getRankTitle(i));
		}
		mplew.write(alliance.getGuilds().size());
		for(Integer guild : alliance.getGuilds()){
			mplew.writeInt(guild);
		}
		mplew.writeInt(2);
		mplew.writeMapleAsciiString(alliance.getNotice());
		mplew.writeInt(expelledGuild);
		getGuildInfo(mplew, WorldServer.getInstance().getGuild(expelledGuild, null));
		mplew.write(0x01);
		return mplew.getPacket();
	}

	public static byte[] disbandAlliance(int alliance){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x1D);
		mplew.writeInt(alliance);
		return mplew.getPacket();
	}

	public static byte[] sendEngagementRequest(String name){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue()); // <name> has requested engagement. Will you accept this proposal?
		mplew.write(0);
		mplew.writeMapleAsciiString(name); // name
		mplew.writeInt(10); // playerid
		return mplew.getPacket();
	}

	public static byte[] sendGroomWishlist(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue()); // <name> has requested engagement. Will you accept this proposal?
		mplew.write(9);
		return mplew.getPacket();
	}

	public static byte[] sendBrideWishList(List<Item> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
		mplew.write(0x0A);
		mplew.writeLong(-1); // ?
		mplew.writeInt(0); // ?
		mplew.write(items.size());
		for(Item item : items){
			addItemInfo(mplew, item, true);
		}
		return mplew.getPacket();
	}

	public static byte[] addItemToWeddingRegistry(MapleCharacter chr, Item item){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.WEDDING_GIFT_RESULT.getValue());
		mplew.write(0x0B);
		mplew.writeInt(0);
		for(int i = 0; i < 0; i++) // f4
		{
			mplew.write(0);
		}
		addItemInfo(mplew, item, true);
		return mplew.getPacket();
	}

	public static byte[] removeItemFromDuey(boolean remove, int Package){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARCEL.getValue());
		mplew.write(0x17);
		mplew.writeInt(Package);
		mplew.write(remove ? 3 : 4);
		return mplew.getPacket();
	}

	public static byte[] sendDueyMSG(byte operation){
		return sendDuey(operation, null);
	}

	public static byte[] sendDuey(byte operation, List<DueyPackages> packages){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PARCEL.getValue());
		mplew.write(operation);
		if(operation == 8){
			mplew.write(0);
			mplew.write(packages.size());
			for(DueyPackages dp : packages){
				mplew.writeInt(dp.getPackageId());
				mplew.writeAsciiString(dp.getSender());
				for(int i = dp.getSender().length(); i < 13; i++){
					mplew.write(0);
				}
				mplew.writeInt(dp.getMesos());
				mplew.writeLong(getTime(dp.sentTimeInMilliseconds()));
				mplew.writeLong(0); // Contains message o____o.
				for(int i = 0; i < 48; i++){
					mplew.writeInt(Randomizer.nextInt(Integer.MAX_VALUE));
				}
				mplew.writeInt(0);
				mplew.write(0);
				if(dp.getItem() != null){
					mplew.write(1);
					addItemInfo(mplew, dp.getItem(), true);
				}else{
					mplew.write(0);
				}
			}
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static byte[] sendDojoAnimation(byte firstByte, String animation){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(firstByte);
		mplew.writeMapleAsciiString(animation);
		return mplew.getPacket();
	}

	/**
	 * Gets a "block" packet (ie. the cash shop is unavailable, etc)
	 * Possible values for <code>type</code>:<br>
	 * 1: This portal is closed for now.<br>
	 * 2: You cannot go to that place.<br>
	 * 3: Unable to approach due to
	 * the force of the ground.<br>
	 * 4: You cannot teleport to or on this
	 * map.<br>
	 * 5: Unable to approach due to the force of the ground.<br>
	 * 6:
	 * This map can only be entered by party members.<br>
	 * 7: The Cash Shop is
	 * currently not available. Stay tuned...<br>
	 *
	 * @param type The type
	 * @return The "block" packet.
	 */
	public static byte[] blockedMessage(int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOCKED_MAP.getValue());
		mplew.write(type);
		return mplew.getPacket();
	}

	/**
	 * Gets a "block" packet (ie. the cash shop is unavailable, etc)
	 * Possible values for <code>type</code>:<br>
	 * 1: You cannot move that
	 * channel. Please try again later.<br>
	 * 2: You cannot go into the cash shop.
	 * Please try again later.<br>
	 * 3: The Item-Trading Shop is currently
	 * unavailable. Please try again later.<br>
	 * 4: You cannot go into the trade
	 * shop, due to limitation of user count.<br>
	 * 5: You do not meet the minimum
	 * level requirement to access the Trade Shop.<br>
	 *
	 * @param type The type
	 * @return The "block" packet.
	 */
	public static byte[] blockedMessage2(int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOCKED_SERVER.getValue());
		mplew.write(type);
		return mplew.getPacket();
	}

	/**
	 * Sends a "levelup" packet to the guild or family.
	 * Possible values for <code>type</code>:<br>
	 * 0: <Family> ? has reached Lv.
	 * ?.<br>
	 * - The Reps you have received from ? will be reduced in half. 1:
	 * <Family> ? has reached Lv. ?.<br>
	 * 2: <Guild> ? has reached Lv. ?.<br>
	 *
	 * @param type The type
	 * @return The "levelup" packet.
	 */
	public static byte[] levelUpMessage(int type, int level, String charname){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTIFY_LEVELUP.getValue());
		mplew.write(type);
		mplew.writeInt(level);
		mplew.writeMapleAsciiString(charname);
		return mplew.getPacket();
	}

	/**
	 * Sends a "married" packet to the guild or family.
	 * Possible values for <code>type</code>:<br>
	 * 0: <Guild ? is now married.
	 * Please congratulate them.<br>
	 * 1: <Family ? is now married. Please
	 * congratulate them.<br>
	 *
	 * @param type The type
	 * @return The "married" packet.
	 */
	public static byte[] marriageMessage(int type, String charname){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTIFY_MARRIAGE.getValue());
		mplew.write(type);
		mplew.writeMapleAsciiString("> " + charname); // To fix the stupid packet lol
		return mplew.getPacket();
	}

	/**
	 * Sends a "job advance" packet to the guild or family.
	 * Possible values for <code>type</code>:<br>
	 * 0: <Guild ? has advanced to
	 * a(an) ?.<br>
	 * 1: <Family ? has advanced to a(an) ?.<br>
	 *
	 * @param type The type
	 * @return The "job advance" packet.
	 */
	public static byte[] jobMessage(int type, int job, String charname){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTIFY_JOB_CHANGE.getValue());
		mplew.write(type);
		mplew.writeInt(job); // Why fking int?
		mplew.writeMapleAsciiString("> " + charname); // To fix the stupid packet lol
		return mplew.getPacket();
	}

	/**
	 * @param type - (0:Light&Long 1:Heavy&Short)
	 * @param delay - seconds
	 * @return
	 */
	public static byte[] trembleEffect(int type, int delay){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
		mplew.write(1);
		mplew.write(type);
		mplew.writeInt(delay);
		return mplew.getPacket();
	}

	public static byte[] getEnergy(String info, int amount){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SESSION_VALUE.getValue());
		mplew.writeMapleAsciiString(info);
		mplew.writeMapleAsciiString(Integer.toString(amount));
		return mplew.getPacket();
	}

	public static byte[] dojoWarpUp(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
		mplew.write(0);
		mplew.write(6);
		return mplew.getPacket();
	}

	public static byte[] portalTeleport(byte portalid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
		mplew.write(1);// enableActions
		mplew.write(portalid);
		return mplew.getPacket();
	}

	public static String getRightPaddedStr(String in, char padchar, int length){
		StringBuilder builder = new StringBuilder(in);
		for(int x = in.length(); x < length; x++){
			builder.append(padchar);
		}
		return builder.toString();
	}

	public static byte[] MobDamageMobFriendly(MapleMonster mob, int damage){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
		mplew.writeInt(mob.getObjectId());
		mplew.write(1); // direction ?
		mplew.writeInt(damage);
		int remainingHp = mob.getHp() - damage;
		if(remainingHp <= 1){
			remainingHp = 0;
			mob.getMap().removeMapObject(mob);
		}
		mob.setHp(remainingHp);
		mplew.writeInt(remainingHp);
		mplew.writeInt(mob.getMaxHp());
		return mplew.getPacket();
	}

	public static byte[] shopErrorMessage(int error, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(0x0A);
		mplew.write(type);
		mplew.write(error);
		return mplew.getPacket();
	}

	private static void addRingInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr){
		mplew.writeShort(chr.getCrushRings().size());
		for(MapleRing ring : chr.getCrushRings()){
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			mplew.writeInt(0);
		}
		mplew.writeShort(chr.getFriendshipRings().size());
		for(MapleRing ring : chr.getFriendshipRings()){
			mplew.writeInt(ring.getPartnerChrId());
			mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
			mplew.writeInt(ring.getRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getPartnerRingId());
			mplew.writeInt(0);
			mplew.writeInt(ring.getItemId());
		}
		boolean ring = chr.getMarriedTo() > 0 && chr.getMarriageRingID() > 0;
		mplew.writeShort(ring ? 1 : 0);// actually a loop like the rest.
		if(ring){
			mplew.writeInt(chr.getMarriageID());
			mplew.writeInt(chr.getGender() == 0 ? chr.getId() : chr.getMarriedTo());
			mplew.writeInt(chr.getGender() == 0 ? chr.getMarriedTo() : chr.getId());
			mplew.writeShort(chr.getMarriedTo() > 0 ? 3 : 1);
			mplew.writeInt(chr.getMarriageRingID());
			mplew.writeInt(chr.getMarriageRingID());
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? chr.getName() : MapleCharacter.getNameById(chr.getMarriedTo()), '\0', 13));
			mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? MapleCharacter.getNameById(chr.getMarriedTo()) : chr.getName(), '\0', 13));
		}
	}

	public static byte[] finishedGather(int inv){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
		mplew.write(0);
		mplew.write(inv);
		return mplew.getPacket();
	}

	public static byte[] finishedSort2(int inv){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
		mplew.write(0);
		mplew.write(inv);
		return mplew.getPacket();
	}

	public static byte[] hpqMessage(String text){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue()); // not 100% sure
		mplew.write(0);
		mplew.writeInt(5120016);
		mplew.writeAsciiString(text);
		return mplew.getPacket();
	}

	public static byte[] showEventInstructions(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
		mplew.write(0);
		return mplew.getPacket();
	}

	public static byte[] leftKnockBack(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
		return mplew.getPacket();
	}

	public static byte[] rollSnowBall(boolean entermap, int state, MapleSnowball ball0, MapleSnowball ball1){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SNOWBALL_STATE.getValue());
		if(entermap){
			mplew.skip(21);
		}else{
			mplew.write(state);// 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
			mplew.writeInt(ball0.getSnowmanHP() / 75);
			mplew.writeInt(ball1.getSnowmanHP() / 75);
			mplew.writeShort(ball0.getPosition());// distance snowball down, 84 03 = max
			mplew.write(-1);
			mplew.writeShort(ball1.getPosition());// distance snowball up, 84 03 = max
			mplew.write(-1);
		}
		return mplew.getPacket();
	}

	public static byte[] hitSnowBall(int what, int damage){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
		mplew.write(what);
		mplew.writeInt(damage);
		return mplew.getPacket();
	}

	/**
	 * Sends a Snowball Message<br>
	 * Possible values for <code>message</code>:<br>
	 * 1: ... Team's snowball has
	 * passed the stage 1.<br>
	 * 2: ... Team's snowball has passed the stage
	 * 2.<br>
	 * 3: ... Team's snowball has passed the stage 3.<br>
	 * 4: ... Team is
	 * attacking the snowman, stopping the progress<br>
	 * 5: ... Team is moving
	 * again<br>
	 *
	 * @param message
	 */
	public static byte[] snowballMessage(int team, int message){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
		mplew.write(team);// 0 is down, 1 is up
		mplew.writeInt(message);
		return mplew.getPacket();
	}

	public static byte[] coconutScore(int team1, int team2){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
		mplew.writeShort(team1);
		mplew.writeShort(team2);
		return mplew.getPacket();
	}

	public static byte[] hitCoconut(boolean spawn, int id, int type){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.COCONUT_HIT.getValue());
		if(spawn){
			mplew.writeShort(-1);
			mplew.writeShort(5000);
			mplew.write(0);
		}else{
			mplew.writeShort(id);
			mplew.writeShort(1000);// delay till you can attack again!
			mplew.write(type); // What action to do for the coconut.
		}
		return mplew.getPacket();
	}

	public static byte[] spawnGuide(boolean spawn){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
		mplew.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
		if(spawn){
			mplew.write(1);
		}else{
			mplew.write(0);
		}
		return mplew.getPacket();
	}

	public static byte[] talkGuide(String talk){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(0);
		mplew.writeMapleAsciiString(talk);
		mplew.write(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
		return mplew.getPacket();
	}

	public static byte[] guideHint(int hint){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
		mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
		mplew.write(1);
		mplew.writeInt(hint);
		mplew.writeInt(7000);
		return mplew.getPacket();
	}

	public static byte[] openITC(MapleClient c){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_ITC.getValue());
		addCharacterInfo(mplew, c.getPlayer());
		mplew.writeMapleAsciiString("Arnah");
		mplew.writeInt(GameConstants.nRegisterFeeMeso);
		mplew.writeInt(GameConstants.nCommissionRate);
		mplew.writeInt(GameConstants.nCommissionBase);
		mplew.writeInt(GameConstants.nAuctionDurationMin);
		mplew.writeInt(GameConstants.nAuctionDurationMax);
		mplew.writeLong(0);// gay time
		return mplew.getPacket();
	}

	public static byte[] resetForcedStats(){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
		mplew.writeShort(SendOpcode.FORCED_STAT_RESET.getValue());
		return mplew.getPacket();
	}

	public static byte[] earnTitleMessage(String msg){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
		mplew.writeMapleAsciiString(msg);
		return mplew.getPacket();
	}

	public static byte[] monsterCarnivalResult(byte mode){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.write(mode); // value is greater than or equal to 8
		return mplew.getPacket();
	}

	public static byte[] sheepRanchInfo(byte wolf, byte sheep){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
		mplew.write(wolf);
		mplew.write(sheep);
		return mplew.getPacket();
	}
	// Know what this is? ?? >=)

	public static byte[] sheepRanchClothes(int id, byte clothes){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
		mplew.writeInt(id); // Character id
		mplew.write(clothes); // 0 = sheep, 1 = wolf, 2 = Spectator (wolf without wool)
		return mplew.getPacket();
	}

	public static byte[] pyramidGauge(int gauge){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
		mplew.writeShort(SendOpcode.PYRAMID_GAUGE.getValue());
		mplew.writeInt(gauge);
		return mplew.getPacket();
	}
	// f2

	public static byte[] pyramidScore(byte score, int exp){// Type cannot be higher than 4 (Rank D), otherwise you'll crash
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
		mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue());
		mplew.write(score);
		mplew.writeInt(exp);
		return mplew.getPacket();
	}

	public static byte[] spawnDragon(MapleDragon dragon){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPAWN_DRAGON.getValue());
		mplew.writeInt(dragon.ownerid);
		mplew.writeInt(dragon.getPosition().x);
		mplew.writeInt(dragon.getPosition().y);
		mplew.write(dragon.getStance());
		mplew.writeShort(0);
		mplew.writeShort(dragon.jobid);
		return mplew.getPacket();
	}

	public static byte[] moveDragon(MapleDragon dragon, MovePath moves){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOVE_DRAGON.getValue());
		mplew.writeInt(dragon.ownerid);
		moves.encode(mplew);
		return mplew.getPacket();
	}

	/**
	 * Sends a request to remove Mir<br>
	 *
	 * @param chrid - Needs the specific Character ID
	 * @return The packet
	 */
	public static byte[] removeDragon(int chrid){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_DRAGON.getValue());
		mplew.writeInt(chrid);
		return mplew.getPacket();
	}

	/**
	 * Changes the current background effect to either being rendered or not.
	 * Data is still missing, so this is pretty binary at the moment in how it
	 * behaves.
	 *
	 * @param remove whether or not the remove or add the specified layer.
	 * @param layer the targeted layer for removal or addition.
	 * @param transition the time it takes to transition the effect.
	 * @return a packet to change the background effect of a specified layer.
	 */
	public static byte[] changeBackgroundEffect(boolean remove, int layer, int transition){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_BACK_EFFECT.getValue());
		mplew.writeBoolean(remove);
		mplew.writeInt(0); // not sure what this int32 does yet
		mplew.write(layer);
		mplew.writeInt(transition);
		return mplew.getPacket();
	}

	public static byte[] crcStatus(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MOB_CRC_KEY_CHANGED.getValue());
		mplew.writeInt(Randomizer.nextInt());
		return mplew.getPacket();
	}

	/**
	 * <name> has requested engagement. Will you accept this proposal?
	 * param name
	 * param playerid
	 */
	public static byte[] onMarriageRequest(String name, int playerid){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_REQUEST.getValue());
		mplew.write(0); // mode, 0 = engage, 1 = cancel, 2 = answer.. etc
		mplew.writeMapleAsciiString(name); // name
		mplew.writeInt(playerid); // playerid
		return mplew.getPacket();
	}

	/**
	 * Enable spouse chat and their engagement ring
	 */
	public static byte[] onMarriageResult(MapleCharacter chr, boolean wedding, int marriageid){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
		mplew.write(wedding ? 12 : 11);
		mplew.writeInt(marriageid);
		mplew.writeInt(chr.getGender() == 0 ? chr.getId() : chr.getMarriedTo());
		mplew.writeInt(chr.getGender() == 0 ? chr.getMarriedTo() : chr.getId());
		mplew.writeShort(wedding ? 3 : 1); // impossible, always 1
		if(wedding){
			mplew.writeInt(chr.getMarriageRingID());
			mplew.writeInt(chr.getMarriageRingID());
		}else{
			mplew.writeInt(chr.getEngagementRingID());
			mplew.writeInt(chr.getEngagementRingID());
		}
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? chr.getName() : MapleCharacter.getNameById(chr.getMarriedTo()), '\0', 13));
		mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? MapleCharacter.getNameById(chr.getMarriedTo()) : chr.getName(), '\0', 13));
		return mplew.getPacket();
	}

	/**
	 * To exit the Engagement Window (Waiting for her response...).
	 */
	public static byte[] onMarriageResult(final byte msg){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
		mplew.write(msg);
		if(msg == 36){
			mplew.write(1);
			mplew.writeMapleAsciiString("You are now engaged.");
		}
		return mplew.getPacket();
	}

	public static byte[] sendWeddingInvitation(String groom, String bride){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MARRIAGE_RESULT.getValue());
		mplew.write(15);
		mplew.writeMapleAsciiString(groom);
		mplew.writeMapleAsciiString(bride);
		mplew.writeShort(1); // 0 = Cathedral Normal?, 1 = Cathedral Premium?, 2 = Chapel Normal?
		return mplew.getPacket();
	}

	/**
	 * The World Map includes 'loverPos' in which this packet controls
	 */
	public static byte[] onNotifyWeddingPartnerTransfer(int partner, int mapid){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTIFY_MARRIED_PARTNER_MAP_TRANSFER.getValue());
		mplew.writeInt(mapid);
		mplew.writeInt(partner);
		return mplew.getPacket();
	}

	/**
	 * The wedding packet to display Pelvis Bebop and enable the Wedding Ceremony Effect between two characters
	 * CField_Wedding::OnWeddingProgress - Stages
	 * CField_Wedding::OnWeddingCeremonyEnd - Wedding Ceremony Effect
	 */
	public static byte[] onWeddingProgress(boolean SetBlessEffect, int groom, int bride, byte step){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SetBlessEffect ? SendOpcode.WEDDING_CEREMONY_END.getValue() : SendOpcode.WEDDING_PROGRESS.getValue());
		if(!SetBlessEffect){ // in order for ceremony packet to send, byte step = 2 must be sent first
			mplew.write(step);
		}
		mplew.writeInt(groom);
		mplew.writeInt(bride);
		return mplew.getPacket();
	}

	public static byte[] onCoupleMessage(String fiance, String text, boolean spouse){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
		mplew.write(spouse ? 5 : 4); // v2 = CInPacket::Decode1(a1) - 4;
		if(spouse){ // if ( v2 ) {
			mplew.writeMapleAsciiString(fiance);
		}
		mplew.write(spouse ? 5 : 1);
		mplew.writeMapleAsciiString(text);
		return mplew.getPacket();
	}

	/**
	 * Makes any NPC in the game scriptable.
	 * 
	 * @param npcId - The NPC's ID, found in WZ files/MCDB
	 * @param description - If the NPC has quests, this will be the text of the menu item
	 * @return
	 */
	public static byte[] setNPCScriptable(int npcId, String description){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
		mplew.write(1); // following structure is repeated n times
		mplew.writeInt(npcId);
		mplew.writeMapleAsciiString(description);
		mplew.writeInt(0); // start time
		mplew.writeInt(Integer.MAX_VALUE); // end time
		return mplew.getPacket();
	}

	/**
	 * Makes any NPC in the game scriptable.
	 * 
	 * @param npcId - The NPC's ID, found in WZ files/MCDB
	 * @param description - If the NPC has quests, this will be the text of the menu item
	 * @return
	 */
	public static byte[] setNPCScriptable(Set<Pair<Integer, String>> npcs){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
		for(Pair<Integer, String> npc : npcs){
			mplew.writeInt(npc.left);
			mplew.writeMapleAsciiString(npc.right);
			mplew.writeInt(0); // start time
			mplew.writeInt(Integer.MAX_VALUE); // end time
		}
		return mplew.getPacket();
	}

	public static byte[] setNpcSpecialAction(int oid, String action){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_SPECIAL_ACTION.getValue());
		mplew.writeInt(oid);
		mplew.writeMapleAsciiString(action);
		return mplew.getPacket();
	}

	public static byte[] openRPSNPC(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RPS_GAME.getValue());
		mplew.write(8);// open npc
		mplew.writeInt(9000019);
		return mplew.getPacket();
	}

	public static byte[] rpsMesoError(int mesos){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RPS_GAME.getValue());
		mplew.write(0x06);
		if(mesos != -1) mplew.writeInt(mesos);
		return mplew.getPacket();
	}

	public static byte[] rpsSelection(byte selection, byte answer){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RPS_GAME.getValue());
		mplew.write(0x0B);// 11l
		mplew.write(selection);
		mplew.write(answer);
		return mplew.getPacket();
	}

	public static byte[] rpsMode(byte mode){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.RPS_GAME.getValue());
		mplew.write(mode);
		return mplew.getPacket();
	}

	public static byte[] onSpecialEffectBySkill(int objectID, int skillID, int characterid){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MONSTER_SPECIAL_EFFECT_BY_SKILL.getValue());
		mplew.writeInt(objectID);
		mplew.writeInt(skillID);
		mplew.writeInt(characterid);
		mplew.writeShort(0);// tDelay
		return mplew.getPacket();
	}

	public static byte[] onNoticeMsg(String sMsg){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NOTICE_MSG.getValue());
		mplew.writeMapleAsciiString(sMsg);
		return mplew.getPacket();
	}

	/*
	 * 0: Normal Chat
	 * 1: Whisper
	 * 2: Party
	 * 3: Buddy
	 * 4: Guild
	 * 5: Alliance
	 * 6: Spouse [Dark Red]
	 * 7: Grey
	 * 8: Yellow
	 * 9: Light Yellow
	 * 10: Blue
	 * 11: White
	 * 12: Red
	 * 13: Light Blue
	 */
	public static byte[] onChatMsg(int nType, String sMsg){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHAT_MSG.getValue());
		mplew.writeShort(nType);
		mplew.writeMapleAsciiString(sMsg);
		return mplew.getPacket();
	}
}
