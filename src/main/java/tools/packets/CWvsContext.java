package tools.packets;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.*;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ItemConstants;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuildSummary;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import server.ItemInformationProvider;
import server.MaplePlayerShopItem;
import server.maps.objects.MapleDoor;
import server.maps.objects.PlayerShop;
import server.propertybuilder.ExpProperty;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Jun 21, 2017
 */
public class CWvsContext{

	public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

	/**
	 * Gets an empty stat update.
	 *
	 * @return The empy stat update packet.
	 */
	public static byte[] enableActions(){
		return updatePlayerStats(EMPTY_STATUPDATE, true, null);
	}

	/**
	 * Gets an update for specified stats.
	 *
	 * @param stats The stats to update.
	 * @return The stat update packet.
	 */
	public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, MapleCharacter chr){
		return updatePlayerStats(stats, false, chr);
	}

	/**
	 * Gets an update for specified stats.
	 *
	 * @param stats The list of stats to update.
	 * @param itemReaction Result of an item reaction(?)
	 * @return The stat update packet.
	 */
	public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean itemReaction, MapleCharacter chr){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
		mplew.write(itemReaction ? 1 : 0);
		int updateMask = 0;
		for(Pair<MapleStat, Integer> statupdate : stats){
			updateMask |= statupdate.getLeft().getValue();
		}
		List<Pair<MapleStat, Integer>> mystats = stats;
		if(mystats.size() > 1){
			Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>(){

				@Override
				public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2){
					int val1 = o1.getLeft().getValue();
					int val2 = o2.getLeft().getValue();
					return(val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
				}
			});
		}
		mplew.writeInt(updateMask);
		for(Pair<MapleStat, Integer> statupdate : mystats){
			switch (statupdate.getLeft()){
				case SKIN:
				case LEVEL:
					mplew.write(statupdate.getRight().byteValue());
					break;
				case JOB:
				case STR:
				case DEX:
				case INT:
				case LUK:
				case HP:
				case MAXHP:
				case MP:
				case MAXMP:
				case AVAILABLEAP:
				case FAME:
					mplew.writeShort(statupdate.getRight().shortValue());
					break;
				case AVAILABLESP:
					if(GameConstants.hasExtendedSPTable(chr.getJob())){
						mplew.write(chr.getRemainingSpSize());
						for(int i = 0; i < chr.getRemainingSps().length; i++){
							if(chr.getRemainingSpBySkill(i) > 0){
								mplew.write(i);
								mplew.write(chr.getRemainingSpBySkill(i));
							}
						}
					}else{
						mplew.writeShort(statupdate.getRight().shortValue());
					}
					break;
				case FACE:
				case HAIR:
				case EXP:
				case MESO:
				case GACHAEXP:
					mplew.writeInt(statupdate.getRight());
					break;
				case PETSN:
					mplew.writeLong(0);
					break;
				case PETSN2:
					mplew.writeLong(0);
					break;
				case PETSN3:
					mplew.writeLong(0);
					break;
			}
		}
		mplew.write(0);// Boolean, if true decode 1 and CUserLocal::SetSecondaryStatChangedPoint
		return mplew.getPacket();
	}

	public static byte[] bonusExpRateChanged(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.BONUS_EXP_RATE_CHANGED);
		mplew.writeInt(17);
		mplew.writeInt(1);// nHour
		mplew.writeInt(0);
		return mplew.getPacket();
	}

	public static byte[] openFullClientDownloadLink(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.OPEN_FULL_CLIENT_DOWNLOAD_LINK);
		return mplew.getPacket();
	}

	public static byte[] incubatorResult(Item item){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
		mplew.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
		mplew.writeInt(item.getItemId());
		mplew.writeShort(item.getQuantity());
		return mplew.getPacket();
	}

	public static byte[] owlOfMinerva(MapleClient c, byte sortByPrice, int itemid, List<PlayerShop> shops, List<MaplePlayerShopItem> items){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue());
		mplew.write(6);
		mplew.writeInt(0);// nNpcShopPrice
		mplew.writeInt(itemid);
		mplew.writeInt(items.size());
		for(PlayerShop hm : shops){
			for(MaplePlayerShopItem item : hm.getItems()){
				if(item.getItem().getItemId() == itemid && item.isExist()){
					mplew.writeMapleAsciiString(hm.getOwnerName());
					mplew.writeInt(hm.getMapId());
					mplew.writeMapleAsciiString(hm.getDescription());
					mplew.writeInt(item.getItem().getQuantity());
					mplew.writeInt(item.getItem().getPerBundle());
					mplew.writeInt(item.getPrice());
					mplew.writeInt(hm.getOwnerId());
					mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
					mplew.write(ItemInformationProvider.getInstance().getInventoryType(item.getItem().getItemId()).getType());
					if(item.getItem().getItemId() / 1000000 == 1){
						MaplePacketCreator.addItemInfo(mplew, item.getItem(), true);
					}
				}
			}
		}
		return mplew.getPacket();
	}

	public static byte[] getMacros(SkillMacro[] macros){//
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.MACRO_SYS_DATA_INIT.getValue());
		int count = 0;
		for(int i = 0; i < 5; i++){
			if(macros[i] != null){
				count++;
			}
		}
		mplew.write(count);
		for(int i = 0; i < 5; i++){
			SkillMacro macro = macros[i];
			if(macro != null){
				mplew.writeMapleAsciiString(macro.getName());
				mplew.write(macro.getShout());
				mplew.writeInt(macro.getSkill1());
				mplew.writeInt(macro.getSkill2());
				mplew.writeInt(macro.getSkill3());
			}
		}
		return mplew.getPacket();
	}

	public static byte[] OnCharacterInfo(MapleCharacter chr){// CWvsContext::OnCharacterInfo
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
		mplew.writeInt(chr.getId());
		mplew.write(chr.getLevel());
		mplew.writeShort(chr.getJob().getId());
		mplew.writeShort(chr.getFame());
		mplew.writeBoolean(chr.getMarriedTo() > 0 && chr.getMarriageRingID() > 0);
		String guildName = "";
		String allianceName = "";
		MapleGuildSummary gs = null;
		try{
			gs = ChannelServer.getInstance().getWorldInterface().getGuildSummary(chr.getGuildId());
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		if(chr.getGuildId() > 0 && gs != null){
			guildName = gs.getName();
			MapleAlliance alliance = null;
			try{
				alliance = ChannelServer.getInstance().getWorldInterface().getAlliance(gs.getAllianceId());
			}catch(RemoteException | NullPointerException ex){
				Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
			}
			if(alliance != null){
				allianceName = alliance.getName();
			}
		}
		mplew.writeMapleAsciiString(guildName);
		mplew.writeMapleAsciiString(allianceName);
		mplew.write(0);// pMedalInfo
		MaplePet[] pets = chr.getPets();
		Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -114);
		// mplew.writeBool(true);// bPetActivated
		for(int i = 0; i < 3; i++){
			if(pets[i] != null){
				mplew.write(pets[i].getUniqueId());
				mplew.writeInt(pets[i].getItemId()); // dwTemplateID
				mplew.writeMapleAsciiString(pets[i].getName());// sName
				mplew.write(pets[i].getLevel()); // nLevel
				mplew.writeShort(pets[i].getCloseness()); // nTameness
				mplew.write(pets[i].getFullness()); // nRepleteness
				mplew.writeShort(0);// usPetSkill
				mplew.writeInt(inv != null ? inv.getItemId() : 0);// nItemID
				// mplew.write(i == 2 ? 0 : 1);//
			}
		}
		mplew.write(0); // end of pets
		if(chr.getMount() != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18) != null){
			mplew.writeBoolean(true);
			mplew.writeInt(chr.getMount().getLevel()); // level
			mplew.writeInt(chr.getMount().getExp()); // exp
			mplew.writeInt(chr.getMount().getTiredness()); // tiredness
		}else{
			mplew.writeBoolean(false);
		}
		mplew.write(chr.getCashShop().getWishList().size());
		for(int sn : chr.getCashShop().getWishList()){
			mplew.writeInt(sn);
		}
		mplew.writeInt(chr.getMonsterBook().getBookLevel());
		mplew.writeInt(chr.getMonsterBook().getNormalCard());
		mplew.writeInt(chr.getMonsterBook().getSpecialCard());
		mplew.writeInt(chr.getMonsterBook().getTotalCards());
		mplew.writeInt(chr.getMonsterBookCover() > 0 ? ItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
		Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
		if(medal != null){
			mplew.writeInt(medal.getItemId());
		}else{
			mplew.writeInt(0);
		}
		ArrayList<Short> medalQuests = new ArrayList<>();
		List<MapleQuestStatus> completed = chr.getCompletedQuests();
		for(MapleQuestStatus q : completed){
			if(q.getQuest().getId() >= 29000){ // && q.getQuest().getId() <= 29923
				medalQuests.add(q.getQuest().getId());
			}
		}
		Collections.sort(medalQuests);
		mplew.writeShort(medalQuests.size());
		for(Short s : medalQuests){
			mplew.writeShort(s);
		}
		List<Integer> chairs = new ArrayList<>();
		for(Item item : chr.getInventory(MapleInventoryType.SETUP).list()){
			if(ItemConstants.is_chair(item.getItemId())) chairs.add(item.getItemId());
		}
		mplew.writeInt(chairs.size());
		for(int itemid : chairs){
			mplew.writeInt(itemid);
		}
		return mplew.getPacket();
	}

	public static byte[] SetPassengerRequest(int nPassengerID){//
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.SET_PASSENGER_REQUEST.getValue());
		mplew.writeInt(nPassengerID);
		return mplew.getPacket();
	}

	public static class OnMessage{

		public static class MessageType{

			public static final int DropPickUpMessage = 0x0, QuestRecordMessage = 0x1, CashItemExpireMessage = 0x2, IncEXPMessage = 0x3, IncSPMessage = 0x4, IncPOPMessage = 0x5, IncMoneyMessage = 0x6, IncGPMessage = 0x7, GiveBuffMessage = 0x8, GeneralItemExpireMessage = 0x9, SystemMessage = 10, QuestRecordExMessage = 11, ItemProtectExpireMessage = 12, ItemExpireReplaceMessage = 13, SkillExpireMessage = 14;
		}

		public static byte[] getShowInventoryFull(){
			return getShowInventoryStatus(0xff);
		}

		public static byte[] showItemUnavailable(){
			return getShowInventoryStatus(0xfe);
		}

		public static byte[] getShowInventoryStatus(int mode){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.DropPickUpMessage);
			mplew.write(mode);
			mplew.writeInt(0);
			mplew.writeInt(0);
			return mplew.getPacket();
		}

		/**
		 * @param c
		 * @param quest
		 * @return
		 */
		public static byte[] forfeitQuest(short quest){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordMessage);
			mplew.writeShort(quest);
			mplew.write(0);
			return mplew.getPacket();
		}

		/**
		 * @param c
		 * @param quest
		 * @return
		 */
		public static byte[] completeQuest(short quest, long time){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordMessage);
			mplew.writeShort(quest);
			mplew.write(2);
			mplew.writeLong(MaplePacketCreator.getTime(time));
			return mplew.getPacket();
		}

		public static byte[] updateQuest(MapleQuestStatus q, int infoNumber){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordMessage);
			mplew.writeShort(infoNumber != 0 ? infoNumber : q.getQuest().getId());
			if(infoNumber != 0){
				mplew.write(1);
			}else{
				mplew.write(q.getStatus().getId());
			}
			mplew.writeMapleAsciiString(q.getQuestData());
			mplew.writeLong(0);
			return mplew.getPacket();
		}

		public static byte[] itemExpired(int itemid){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.CashItemExpireMessage);
			mplew.writeInt(itemid);
			return mplew.getPacket();
		}

		/**
		 * Gets a packet telling the client to show an EXP increase.
		 *
		 * @param gain The amount of EXP gained.
		 * @param inChat In the chat box?
		 * @param white White text or yellow?
		 * @return The exp gained packet.
		 */
		public static byte[] getShowExpGain(ExpProperty property){// CWvsContext::OnIncEXPMessage
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.IncEXPMessage); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
			mplew.writeBoolean(property.white);
			mplew.writeInt(property.gain);
			mplew.writeBoolean(property.inChat);
			mplew.writeInt(property.bonusEvent); // monster book bonus (Bonus Event Exp)
			mplew.write(property.nEventPercentage);
			mplew.write(property.nPartyBonusPercentage);
			mplew.writeInt(property.wedding); // wedding bonus
			if(property.nEventPercentage > 0){
				mplew.write(property.nPlayTimeHour);
			}
			if(property.inChat){ // quest bonus rate stuff
				mplew.write(property.nQuestBonusRate);
				if(property.nQuestBonusRate > 0){
					mplew.write(property.nQuestBonusRemainCount);
				}
			}
			mplew.write(property.nPartyBonusEventRate); // 0 = party bonus, 100 = 1x Bonus EXP, 200 = 2x Bonus EXP
			mplew.writeInt(property.party); // party bonus
			mplew.writeInt(property.equip); // equip bonus
			mplew.writeInt(property.cafe); // Internet Cafe Bonus
			mplew.writeInt(property.rainbow); // Rainbow Week Bonus
			mplew.writeInt(property.nPartyExpRingExp);
			mplew.writeInt(property.nCakePieEventBonus);
			return mplew.getPacket();
		}

		public static byte[] incSPMessage(short nJob, byte sp){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.IncSPMessage);
			mplew.writeShort(nJob);
			mplew.write(sp);
			return mplew.getPacket();
		}

		/**
		 * Gets a packet telling the client to show a fame gain.
		 *
		 * @param gain How many fame gained.
		 * @return The meso gain packet.
		 */
		public static byte[] getShowFameGain(int gain){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.IncPOPMessage);
			mplew.writeInt(gain);
			return mplew.getPacket();
		}

		/**
		 * Gets a packet telling the client to show a meso gain.
		 *
		 * @param gain How many mesos gained.
		 * @return The meso gain packet.
		 */
		public static byte[] getShowMesoGain(int gain){
			return getShowMesoGain(gain, false);
		}

		/**
		 * Gets a packet telling the client to show a meso gain.
		 *
		 * @param gain How many mesos gained.
		 * @param inChat Show in the chat window?
		 * @return The meso gain packet.
		 */
		public static byte[] getShowMesoGain(int gain, boolean inChat){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			if(!inChat){
				mplew.write(0);
				mplew.writeShort(1); // v83
			}else{
				mplew.write(MessageType.IncMoneyMessage);
			}
			mplew.writeInt(gain);
			mplew.writeShort(0);
			return mplew.getPacket();
		}

		public static byte[] getGPMessage(int gpChange){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.IncGPMessage);
			mplew.writeInt(gpChange);
			return mplew.getPacket();
		}

		public static byte[] getItemMessage(int itemid){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.GiveBuffMessage);// was 7
			mplew.writeInt(itemid);
			return mplew.getPacket();
		}

		public static byte[] showInfoText(String text){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.SystemMessage);// was 9
			mplew.writeMapleAsciiString(text);
			return mplew.getPacket();
		}

		public static byte[] getDojoInfoMessage(String message){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.SystemMessage);
			mplew.writeMapleAsciiString(message);
			return mplew.getPacket();
		}

		public static byte[] bunnyPacket(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.SystemMessage);
			mplew.writeAsciiString("Protect the Moon Bunny!!!");
			return mplew.getPacket();
		}

		public static byte[] getDojoInfo(String info){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordExMessage);
			mplew.write(new byte[]{(byte) 0xB7, 4});// QUEST ID f5
			mplew.writeMapleAsciiString(info);
			return mplew.getPacket();
		}

		public static byte[] updateDojoStats(MapleCharacter chr, int belt){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordExMessage);
			mplew.write(new byte[]{(byte) 0xB7, 4}); // ?
			mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
			return mplew.getPacket();
		}

		public static byte[] updateAreaInfo(int area, String info){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
			mplew.write(MessageType.QuestRecordExMessage); // 0x0B in v95
			mplew.writeShort(area);// infoNumber
			mplew.writeMapleAsciiString(info);
			return mplew.getPacket();
		}
	}

	public static class Party{

		public static class Request{

			public static final int LoadParty = 0, CreateNewParty = 1, WithdrawParty = 2, JoinParty = 3, InviteParty = 4, KickParty = 5, ChangePartyBoss = 6;
		}

		public static class Result{

			public static final int LoadParty_Done = 7, CreateNewParty_Done = 8, CreateNewParty_AlreayJoined = 9, CreateNewParty_Beginner = 10, CreateNewParty_Unknown = 11, WithdrawParty_Done = 12, WithdrawParty_NotJoined = 13, WithdrawParty_Unknown = 14, JoinParty_Done = 15, JoinParty_Done2 = 16, JoinParty_AlreadyJoined = 17, JoinParty_AlreadyFull = 18, JoinParty_OverDesiredSize = 19, JoinParty_UnknownUser = 20, JoinParty_Unknown = 21, InviteParty_Sent = 22, InviteParty_BlockedUser = 23, InviteParty_AlreadyInvited = 24, InviteParty_AlreadyInvitedByInviter = 25, InviteParty_Rejected = 26, InviteParty_Accepted = 27, KickParty_Done = 28, KickParty_FieldLimit = 29, KickParty_Unknown = 30, ChangePartyBoss_Done = 31, ChangePartyBoss_NotSameField = 32, ChangePartyBoss_NoMemberInSameField = 33, ChangePartyBoss_NotSameChannel = 34, ChangePartyBoss_Unknown = 35, AdminCannotCreate = 36, AdminCannotInvite = 37, UserMigration = 38, ChangeLevelOrJob = 39, CanNotInThisField = 40, // Correct
			        ServerMsg = 41, PartyInfo_TownPortalChanged = 42, PartyInfo_OpenGate = 43;
			/*SuccessToSelectPQReward = 40,
			FailToSelectPQReward = 41,
			ReceivePQReward = 42,
			FailToRequestPQReward = 43,
			CanNotInThisField = 44,
			ServerMsg = 45,
			PartyInfo_TownPortalChanged = 46,
			PartyInfo_OpenGate = 47;*/
		}

		public static byte[] partyCreated(MapleParty party, MaplePartyCharacter partychar){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			mplew.write(Party.Result.CreateNewParty_Done);
			mplew.writeInt(party.getId());
			// in v95 x,y is shorts, not sure if in v90.
			if(!partychar.getDoors().isEmpty()){
				MapleDoor door = partychar.getDoors().get(0);
				if(door.getTown().getId() == partychar.getMapId()){
					mplew.writeInt(partychar.getDoors().get(0).getTarget().getId());
					mplew.writeInt(partychar.getDoors().get(0).getTown().getId());
					mplew.writeInt(partychar.getDoors().get(0).getSkillID());
					mplew.writeInt(partychar.getDoors().get(0).getTownPortal().getPosition().x);
					mplew.writeInt(partychar.getDoors().get(0).getTownPortal().getPosition().y);
					// mplew.writePos(partychar.getDoors().get(0).getTownPortal().getPosition());
				}else{
					mplew.writeInt(partychar.getDoors().get(0).getTown().getId());
					mplew.writeInt(partychar.getDoors().get(0).getTarget().getId());
					mplew.writeInt(partychar.getDoors().get(0).getSkillID());
					// mplew.writePos(partychar.getDoors().get(0).getPosition());
					mplew.writeInt(partychar.getDoors().get(0).getPosition().x);
					mplew.writeInt(partychar.getDoors().get(0).getPosition().y);
				}
			}else{
				mplew.writeInt(999999999);
				mplew.writeInt(999999999);
				mplew.writeInt(0);// nSkillID
				mplew.writeInt(0);
				mplew.writeInt(0);
			}
			return mplew.getPacket();
		}

		public static byte[] partyInvite(MapleCharacter from){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			mplew.write(Party.Request.InviteParty);
			mplew.writeInt(from.getParty().getId());
			mplew.writeMapleAsciiString(from.getName());
			mplew.writeInt(from.getLevel());
			mplew.writeInt(from.getJob().getId());
			mplew.write(0);
			return mplew.getPacket();
		}

		/**
		 * 10: A beginner can't create a party.
		 * 1/11/14/19: Your request for a party didn't work due to an unexpected error.
		 * 13: You have yet to join a party.
		 * 16: Already have joined a party.
		 * 17: The party you're trying to join is already in full capacity.
		 * 19: Unable to find the requested character in this channel.
		 *
		 * @param message
		 * @return
		 */
		public static byte[] partyStatusMessage(int message){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			mplew.write(message);
			return mplew.getPacket();
		}

		/**
		 * 23: 'Char' have denied request to the party.
		 *
		 * @param message
		 * @param charname
		 * @return
		 */
		public static byte[] partyStatusMessage(int message, String charname){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			mplew.write(message);
			mplew.writeMapleAsciiString(charname);
			return mplew.getPacket();
		}

		// PARTYDATA::Decode
		private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving){
			List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
			while(partymembers.size() < 6){
				partymembers.add(new MaplePartyCharacter());
			}
			for(MaplePartyCharacter partychar : partymembers){
				lew.writeInt(partychar.getId());
			}
			for(MaplePartyCharacter partychar : partymembers){
				lew.writeAsciiString(MaplePacketCreator.getRightPaddedStr(partychar.getName(), '\0', 13));
			}
			for(MaplePartyCharacter partychar : partymembers){
				lew.writeInt(partychar.getJobId());
			}
			for(MaplePartyCharacter partychar : partymembers){
				lew.writeInt(partychar.getLevel());
			}
			for(MaplePartyCharacter partychar : partymembers){
				if(partychar.isOnline()){
					lew.writeInt(partychar.getChannel());
				}else{
					lew.writeInt(-2);
				}
			}
			lew.writeInt(party.getLeader().getId());
			for(MaplePartyCharacter partychar : partymembers){
				if(partychar.getChannel() == forchannel){
					lew.writeInt(partychar.getMapId());
				}else{
					lew.writeInt(0);
				}
			}
			for(MaplePartyCharacter partychar : partymembers){
				if(partychar.getChannel() == forchannel && !leaving){
					if(!partychar.getDoors().isEmpty()){
						lew.writeInt(partychar.getDoors().get(0).getTown().getId());
						lew.writeInt(partychar.getDoors().get(0).getTarget().getId());
						lew.writeInt(partychar.getDoors().get(0).getSkillID());
						lew.writeInt(partychar.getDoors().get(0).getPosition().x);
						lew.writeInt(partychar.getDoors().get(0).getPosition().y);
					}else{
						lew.writeInt(999999999);
						lew.writeInt(999999999);
						lew.writeInt(0);// nSkillID
						lew.writeInt(0);
						lew.writeInt(0);
					}
				}else{
					lew.writeInt(999999999);
					lew.writeInt(999999999);
					lew.writeInt(0);// nSkillID
					lew.writeInt(0);
					lew.writeInt(0);
				}
			}
		}

		public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			switch (op){
				case DISBAND:
				case EXPEL:
				case LEAVE:
					mplew.write(Party.Result.WithdrawParty_Done);
					mplew.writeInt(party.getId());
					mplew.writeInt(target.getId());
					if(op == PartyOperation.DISBAND){
						mplew.write(0);
						mplew.writeInt(party.getId());
					}else{
						mplew.write(1);
						if(op == PartyOperation.EXPEL){
							mplew.write(1);
						}else{
							mplew.write(0);
						}
						mplew.writeMapleAsciiString(target.getName());
						addPartyStatus(forChannel, party, mplew, false);
					}
					break;
				case JOIN:
					mplew.write(Party.Result.JoinParty_Done);
					mplew.writeInt(party.getId());
					mplew.writeMapleAsciiString(target.getName());
					addPartyStatus(forChannel, party, mplew, false);
					break;
				case SILENT_UPDATE:
				case LOG_ONOFF:
					mplew.write(Party.Result.LoadParty_Done);
					mplew.writeInt(party.getId());
					addPartyStatus(forChannel, party, mplew, false);
					break;
				case CHANGE_LEADER:
					mplew.write(Party.Result.ChangePartyBoss_Done);
					mplew.writeInt(target.getId());
					mplew.write(0);
					break;
				case UPDATE_DOOR:
					mplew.write(Party.Result.PartyInfo_TownPortalChanged);
					mplew.write(party.getIndex(target));
					if(target.getDoors().isEmpty()){
						mplew.writeInt(999999999);
						mplew.writeInt(999999999);
						mplew.writeInt(0);// skillid
						mplew.writeInt(0);
					}else{
						mplew.writeInt(target.getDoors().get(0).getTown().getId());
						mplew.writeInt(target.getDoors().get(0).getTarget().getId());
						mplew.writeInt(target.getDoors().get(0).getSkillID());
						mplew.writePos(target.getDoors().get(0).getPosition());
						// mplew.writeInt(target.getDoors().get(0).getPosition().x);
						// mplew.writeInt(target.getDoors().get(0).getPosition().y);
					}
					break;
			}
			return mplew.getPacket();
		}

		// Doesn't exist in v90/v83, it was sending the level/job update back in v83.
		@Deprecated
		public static byte[] partyPortal(int townId, int targetId, int nSkillID, Point position){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
			mplew.write(Party.Result.PartyInfo_OpenGate);
			mplew.writeInt(townId);
			mplew.writeInt(targetId);
			mplew.writeInt(nSkillID);
			mplew.writePos(position);
			return mplew.getPacket();
		}

		public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
			mplew.writeInt(cid);
			mplew.writeInt(curhp);
			mplew.writeInt(maxhp);
			return mplew.getPacket();
		}
	}

	public static class Friend{

		public static class AccountMoreInfo{

			public static final int First = 0x0, LoadRequest = 0x1, LoadResult = 0x2, SaveRequest = 0x3, SaveResult = 0x4;
		}

		public static class FindFriend{

			public static final int MyInfoRequest = 0x5, MyInfoResult = 0x6, SearchRequest = 0x7, SearchResult = 0x8, SearchResult_Error = 0x9, DetailRequest = 0xA, DetailResult = 0xB, ErrorCode_OverflowQueue = 0xC;
		}

		public static class FriendRequest{

			public static final int LoadFriend = 0x0, SetFriend = 0x1, AcceptFriend = 0x2, DeleteFriend = 0x3, NotifyLogin = 0x4, NotifyLogout = 0x5, IncMaxCount = 0x6;
		}

		public static class FriendResult{

			public static final int LoadFriend_Done = 0x7, NotifyChange_FriendInfo = 0x8, Invite = 0x9, SetFriend_Done = 0xA, SetFriend_FullMe = 0xB, SetFriend_FullOther = 0xC, SetFriend_AlreadySet = 0xD, SetFriend_Master = 0xE, SetFriend_UnknownUser = 0xF, SetFriend_Unknown = 0x10, AcceptFriend_Unknown = 0x11, DeleteFriend_Done = 0x12, DeleteFriend_Unknown = 0x13, Notify = 0x14, IncMaxCount_Done = 0x15, IncMaxCount_Unknown = 0x16, PleaseWait = 0x17;
		}

		// FriendRes, FriendReq
		public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
			mplew.write(FriendResult.LoadFriend_Done);// CWvsContext::CFriend::Reset
			mplew.write((int) buddylist.stream().filter(BuddylistEntry::isVisible).count());
			for(BuddylistEntry buddy : buddylist){
				if(buddy.isVisible()){
					buddy.encode(mplew);
				}
			}
			for(BuddylistEntry buddy : buddylist){
				if(buddy.isVisible()){
					mplew.writeInt(buddy.inShop ? 1 : 0);
				}
			}
			return mplew.getPacket();
		}

		public static byte[] buddylistMessage(byte message){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
			mplew.write(message);
			return mplew.getPacket();
		}

		public static byte[] requestBuddylistAdd(int cidFrom, int cid, String nameFrom){// Make this use a BuddylistEntry?
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
			mplew.write(FriendResult.Invite);
			mplew.writeInt(cidFrom);// dwFriendID
			mplew.writeMapleAsciiString(nameFrom);
			mplew.writeInt(0);// nLevel
			mplew.writeInt(0);// nJobCode
			// GW_Friend::Decode
			// struct GW_Friend
			mplew.writeInt(cid);// dwFriendID
			mplew.writeNullTerminatedAsciiString(nameFrom, 13);
			mplew.write(0);// nFlag
			mplew.writeInt(0);// nChannelID
			mplew.writeNullTerminatedAsciiString("Default Group", 17);
			// CWvsContext::CFriend::Insert
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] updateBuddyChannel(BuddylistEntry entry){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
			mplew.write(FriendResult.Notify);
			mplew.writeInt(entry.getCharacterId());
			mplew.writeBoolean(entry.inShop);
			mplew.writeInt(entry.getChannel());
			return mplew.getPacket();
		}

		public static byte[] updateBuddyCapacity(int capacity){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
			mplew.write(FriendResult.IncMaxCount_Done);
			mplew.write(capacity);
			return mplew.getPacket();
		}
	}

	public static class Guild{

		//
		public static class Request{

			public static final int LoadGuild = 0x0, InputGuildName = 0x1, CheckGuildName = 0x2, CreateGuildAgree = 0x3, CreateNewGuild = 0x4, InviteGuild = 0x5, JoinGuild = 0x6, WithdrawGuild = 0x7, KickGuild = 0x8, RemoveGuild = 0x9, IncMaxMemberNum = 0xA, ChangeLevel = 0xB, ChangeJob = 0xC, SetGradeName = 0xD, SetMemberGrade = 0xE, SetMark = 0xF, SetNotice = 0x10, InputMark = 0x11, CheckQuestWaiting = 0x12, CheckQuestWaiting2 = 0x13, InsertQuestWaiting = 0x14, CancelQuestWaiting = 0x15, RemoveQuestCompleteGuild = 0x16, IncPoint = 0x17, IncCommitment = 0x18, SetQuestTime = 0x19, ShowGuildRanking = 0x1A, SetSkill = 0x1B;
		}

		public static class Send{

			public static final int LoadGuild_Done = 0x1C, CheckGuildName_Available = 0x1D, CheckGuildName_AlreadyUsed = 0x1E, CheckGuildName_Unknown = 0x1F, CreateGuildAgree_Reply = 0x20, CreateGuildAgree_Unknown = 0x21, CreateNewGuild_Done = 0x22, CreateNewGuild_AlreayJoined = 0x23, CreateNewGuild_GuildNameAlreayExist = 0x24, CreateNewGuild_Beginner = 0x25, CreateNewGuild_Disagree = 0x26, CreateNewGuild_NotFullParty = 0x27, CreateNewGuild_Unknown = 0x28, JoinGuild_Done = 0x29, JoinGuild_AlreadyJoined = 0x2A, JoinGuild_AlreadyFull = 0x2B, JoinGuild_UnknownUser = 0x2C, JoinGuild_Unknown = 0x2D, WithdrawGuild_Done = 0x2E, WithdrawGuild_NotJoined = 0x2F, WithdrawGuild_Unknown = 0x30, KickGuild_Done = 0x31, KickGuild_NotJoined = 0x32, KickGuild_Unknown = 0x33, RemoveGuild_Done = 0x34, RemoveGuild_NotExist = 0x35, RemoveGuild_Unknown = 0x36, InviteGuild_BlockedUser = 0x37, InviteGuild_AlreadyInvited = 0x38, InviteGuild_Rejected = 0x39, AdminCannotCreate = 0x3A, AdminCannotInvite = 0x3B, IncMaxMemberNum_Done = 0x3C, IncMaxMemberNum_Unknown = 0x3D, ChangeLevelOrJob = 0x3E, NotifyLoginOrLogout = 0x3F, SetGradeName_Done = 0x40, SetGradeName_Unknown = 0x41, SetMemberGrade_Done = 0x42, SetMemberGrade_Unknown = 0x43, SetMemberCommitment_Done = 0x44, SetMark_Done = 0x45, SetMark_Unknown = 0x46, SetNotice_Done = 0x47, InsertQuest = 0x48, NoticeQuestWaitingOrder = 0x49, SetGuildCanEnterQuest = 0x4A, IncPoint_Done = 0x4B, ShowGuildRanking = 0x4C, GuildQuest_NotEnoughUser = 0x4D, GuildQuest_RegisterDisconnected = 0x4E, GuildQuest_NoticeOrder = 0x4F, Authkey_Update = 0x50, SetSkill_Done = 0x51, ServerMsg = 0x52;
		}
	}
}
