package tools.packets.field;

import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import constants.ItemConstants;
import net.SendOpcode;
import net.server.channel.handlers.PlayerInteractionHandler;
import server.MaplePlayerShop;
import server.MaplePlayerShopItem;
import server.MapleTrade;
import server.maps.objects.HiredMerchant;
import server.maps.objects.HiredMerchant.SoldItem;
import tools.MaplePacketCreator;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Aug 21, 2017
 */
public class MiniRoomBase{

	public class MiniRoomType{// CMiniRoomBaseDlg::MiniRoomFactory

		public static final int OMOK = 1, MEMORY_GAME = 2, TRADE = 3, PERSONAL_SHOP = 4, ENTRUSTED_SHOP = 5, CASH_TRADING = 6;
	}

	public static void addAvatar(LittleEndianWriter lew, int index, MapleCharacter chr){// CMiniRoomBaseDlg::DecodeAvatar
		lew.write(index);
		MaplePacketCreator.addCharLook(lew, chr, false);
		lew.writeMapleAsciiString(chr.getName());
		lew.writeShort(chr.getJob().getId());
	}

	public static class Omok{
		//
	}

	public static class MemoryGame{
		//
	}

	public static class Trade{

		public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
			mplew.write(MiniRoomType.TRADE);
			mplew.write(2);// maxUsers
			mplew.write(number);// myPosition
			if(number == 1){
				addAvatar(mplew, 0, trade.getPartner().getChr());
			}
			addAvatar(mplew, number, c.getPlayer());
			mplew.write(-1);// Client does a loop looking for index to add more avatars, -1 ends the loop.
			return mplew.getPacket();
		}

		public static byte[] getTradeConfirmation(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
			return mplew.getPacket();
		}

		public static byte[] getTradeCompletion(byte number){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
			mplew.write(number);
			mplew.write(6);
			return mplew.getPacket();
		}

		public static byte[] getTradeCancel(byte number){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
			mplew.write(number);
			mplew.write(2);
			return mplew.getPacket();
		}

		public static byte[] getTradeChat(MapleCharacter c, String chat, boolean owner){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
			mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
			mplew.write(owner ? 0 : 1);
			mplew.writeMapleAsciiString(c.getName() + " : " + chat);
			return mplew.getPacket();
		}

		public static byte[] sendMesoLimit(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue()); // Players under level 15 can only trade 1m per day
			return mplew.getPacket();
		}

		public static byte[] getTradePartnerAdd(MapleCharacter c){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
			addAvatar(mplew, 1, c);
			return mplew.getPacket();
		}

		public static byte[] getTradeInvite(MapleCharacter c){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
			mplew.write(MiniRoomType.TRADE);
			mplew.writeMapleAsciiString(c.getName());
			mplew.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});// SN for CMiniRoomBaseDlg::SendInviteResult, just sent back
			return mplew.getPacket();
		}

		public static byte[] getTradeMesoSet(byte number, int meso){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
			mplew.write(number);
			mplew.writeInt(meso);
			return mplew.getPacket();
		}

		public static byte[] getTradeItemAdd(byte number, Item item){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
			mplew.write(number);
			mplew.write(item.getPosition());
			MaplePacketCreator.addItemInfo(mplew, item, true);
			return mplew.getPacket();
		}
	}

	public static class PersonalShop{

		/**
		 * @param c
		 * @param shop
		 * @param owner
		 * @return
		 */
		public static byte[] getPlayerShop(MapleClient c, MaplePlayerShop shop, boolean owner){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
			mplew.write(MiniRoomType.PERSONAL_SHOP);
			mplew.write(4);
			mplew.write(owner ? 0 : 1);
			/*mplew.write(0);
			addCharLook(mplew, shop.getOwner(), false);
			mplew.writeMapleAsciiString(shop.getOwner().getName());
			mplew.write(1);
			addCharLook(mplew, shop.getOwner(), false);
			mplew.writeMapleAsciiString(shop.getOwner().getName());*/
			addAvatar(mplew, 0, shop.getOwner());
			addAvatar(mplew, 1, shop.getOwner());
			mplew.write(-1);
			mplew.writeMapleAsciiString(shop.getDescription());
			List<MaplePlayerShopItem> items = shop.getItems();
			mplew.write(0x10);// slot max
			mplew.write(items.size());
			for(MaplePlayerShopItem item : items){
				mplew.writeShort(item.getItem().getPerBundle());
				mplew.writeShort(item.getItem().getQuantity());
				mplew.writeInt(item.getPrice());
				MaplePacketCreator.addItemInfo(mplew, item.getItem(), true);
			}
			return mplew.getPacket();
		}
	}

	public static class EntrustedShop{

		/*
		 * Possible things for ENTRUSTED_SHOP_CHECK_RESULT
		 * 0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming succesful
		 * 0x10 = Changes channel to the store (Store is open at Channel 1, do you want to change channels?)
		 * 0x11 = You cannot sell any items when managing.. blabla
		 * 0x12 = FKING POPUP LOL
		 */
		public static byte[] getHiredMerchant(MapleCharacter chr, HiredMerchant hm, boolean firstTime){// Thanks Dustin
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
			mplew.write(MiniRoomType.ENTRUSTED_SHOP);
			mplew.write(4);
			mplew.writeShort(hm.getVisitorSlot(chr) + 1);
			mplew.writeInt(hm.getItemId());
			mplew.writeMapleAsciiString("Hired Merchant");// hm.getDescription()???
			for(int i = 0; i < 3; i++){
				if(hm.getVisitors()[i] != null){
					addAvatar(mplew, i + 1, hm.getVisitors()[i]);
				}
			}
			mplew.write(-1);
			// CEntrustedShopDlg::OnEnterResult
			if(hm.isOwner(chr)){
				mplew.writeShort(hm.getMessages().size());
				for(int i = 0; i < hm.getMessages().size(); i++){
					mplew.writeMapleAsciiString(hm.getMessages().get(i).getLeft());
					mplew.write(hm.getMessages().get(i).getRight());
				}
			}else{
				mplew.writeShort(0);
			}
			mplew.writeMapleAsciiString(hm.getOwnerName());
			if(hm.isOwner(chr)){
				mplew.writeInt(hm.getTimeLeft());// m_tPass
				mplew.write(firstTime ? 1 : 0);
				List<SoldItem> sold = hm.getSold();
				mplew.write(sold.size());
				for(SoldItem s : sold){
					mplew.writeInt(s.getItemId());
					mplew.writeShort(s.getQuantity());
					mplew.writeInt(s.getMesos());
					mplew.writeMapleAsciiString(s.getBuyer());
				}
				mplew.writeLong(chr.getMerchantMeso());
			}
			mplew.writeMapleAsciiString(hm.getDescription());// sTitle
			mplew.write(0x10); // TODO SLOTS, which is 16 for most stores...slotMax
			// Goes to some update method or something.
			mplew.writeInt(chr.getMeso());
			mplew.write(hm.getItems().size());
			if(hm.getItems().isEmpty()){
				mplew.write(0);// Hmm??
			}else{
				for(MaplePlayerShopItem item : hm.getItems()){
					mplew.writeShort(item.getBundles() + (ItemConstants.isRechargable(item.getItem().getItemId()) && item.getItem().getQuantity() == 0 ? 1 : 0));
					mplew.writeShort(item.getPerBundle());
					mplew.writeInt(item.getPrice());
					MaplePacketCreator.addItemInfo(mplew, item.getItem(), true);
				}
			}
			return mplew.getPacket();
		}

		public static byte[] updateHiredMerchant(HiredMerchant hm, MapleCharacter chr){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
			mplew.writeInt(chr.getMeso());
			mplew.write(hm.getItems().size());
			for(MaplePlayerShopItem item : hm.getItems()){
				mplew.writeShort(item.getBundles() + (ItemConstants.isRechargable(item.getItem().getItemId()) && item.getItem().getQuantity() == 0 ? 1 : 0));
				mplew.writeShort(item.getPerBundle());
				mplew.writeInt(item.getPrice());
				MaplePacketCreator.addItemInfo(mplew, item.getItem(), true);
			}
			return mplew.getPacket();
		}

		public static byte[] hiredMerchantChat(String message, byte slot){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
			mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
			mplew.write(slot);
			mplew.writeMapleAsciiString(message);
			return mplew.getPacket();
		}

		public static byte[] hiredMerchantVisitorLeave(int slot){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
			if(slot != 0){
				mplew.write(slot);
			}
			return mplew.getPacket();
		}

		public static byte[] hiredMerchantOwnerLeave(){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
			mplew.write(0);
			return mplew.getPacket();
		}

		public static byte[] leaveHiredMerchant(int slot, int status2){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
			mplew.write(slot);
			mplew.write(status2);
			return mplew.getPacket();
		}

		public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
			mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
			addAvatar(mplew, slot, chr);
			// mplew.write(slot);
			// addCharLook(mplew, chr, false);
			// mplew.writeMapleAsciiString(chr.getName());
			return mplew.getPacket();
		}

		public static byte[] spawnHiredMerchant(HiredMerchant hm){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
			mplew.writeInt(hm.getOwnerId());
			mplew.writeInt(hm.getItemId());
			mplew.writeShort((short) hm.getPosition().getX());
			mplew.writeShort((short) hm.getPosition().getY());
			mplew.writeShort(0);
			mplew.writeMapleAsciiString(hm.getOwnerName());
			mplew.write(0x05);
			mplew.writeInt(hm.getObjectId());
			mplew.writeMapleAsciiString(hm.getDescription());
			mplew.write(hm.getItemId() % 10);
			mplew.write(new byte[]{1, 4});
			return mplew.getPacket();
		}

		public static byte[] destroyHiredMerchant(int id){
			final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
			mplew.writeInt(id);
			return mplew.getPacket();
		}
	}

	// idk where these belong
	public static byte[] getPlayerShopChat(MapleCharacter c, String chat, boolean owner){// used in MapleMiniGame
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(owner ? 0 : 1);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}

	public static byte[] getPlayerShopNewVisitor(MapleCharacter c, int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
		mplew.write(slot);
		MaplePacketCreator.addCharLook(mplew, c, false);
		mplew.writeMapleAsciiString(c.getName());
		return mplew.getPacket();
	}

	public static byte[] getPlayerShopRemoveVisitor(int slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
		if(slot > 0){
			mplew.write(slot);
		}
		return mplew.getPacket();
	}

	public static byte[] getPlayerShopItemUpdate(MaplePlayerShop shop){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
		mplew.write(shop.getItems().size());
		for(MaplePlayerShopItem item : shop.getItems()){
			mplew.writeShort(item.getItem().getPerBundle());
			mplew.writeShort(item.getItem().getQuantity());
			mplew.writeInt(item.getPrice());
			MaplePacketCreator.addItemInfo(mplew, item.getItem(), true);
		}
		return mplew.getPacket();
	}

	public static byte[] getPlayerShopChat(MapleCharacter c, String chat, byte slot){
		final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
		mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
		mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
		mplew.write(slot);
		mplew.writeMapleAsciiString(c.getName() + " : " + chat);
		return mplew.getPacket();
	}
}
