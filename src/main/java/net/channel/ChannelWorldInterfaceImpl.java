package net.channel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import client.*;
import client.command.intern.CommandWarp;
import net.server.channel.Channel;
import net.server.guild.MapleGuild;
import net.server.world.*;
import server.cashshop.CashItemFactory;
import server.cashshop.LimitedGood;
import tools.MaplePacketCreator;
import tools.packets.CWvsContext;
import tools.packets.field.userpool.UserRemote;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class ChannelWorldInterfaceImpl extends UnicastRemoteObject implements ChannelWorldInterface{

	private static final long serialVersionUID = 5540131483186808595L;

	public ChannelWorldInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public boolean isConnected() throws RemoteException{
		return true;
	}

	@Override
	public void setExpRate(int expRate, int questExpRate, int dropRate, int mesoRate) throws RemoteException{
		ChannelServer.getInstance().setExpRate(expRate);
		ChannelServer.getInstance().setQuestExpRate(questExpRate);
		ChannelServer.getInstance().setDropRate(dropRate);
		ChannelServer.getInstance().setMesoRate(mesoRate);
	}

	@Override
	public void setServerMessage(String serverMessage) throws RemoteException{
		for(Channel ch : ChannelServer.getInstance().getChannels()){
			ch.setServerMessage(serverMessage);
		}
	}

	@Override
	public List<Integer> getAllChannels() throws RemoteException{
		return new ArrayList<>(ChannelServer.getInstance().getAllChannels().keySet());
	}

	@Override
	public int getChannelLoad(int channel) throws RemoteException{
		Channel ch = ChannelServer.getInstance().getChannel(channel);
		if(ch == null) return 0;
		return ch.getConnectedClients();
	}

	@Override
	public String getIP(int channel) throws RemoteException{
		Channel ch = ChannelServer.getInstance().getChannel(channel);
		if(ch == null) return null;
		return ch.getIP();
	}

	@Override
	public CharacterLocation find(int chrid) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null) return new CharacterLocation(chr);
		return null;
	}

	@Override
	public CharacterLocation find(String chrname) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterByName(chrname);
		if(chr != null) return new CharacterLocation(chr);
		return null;
	}

	@Override
	public void removeGP(int chrid, int gp) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null) chr.removeGP(gp);
	}

	@Override
	public void gainGP(int chrid, int gp) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null) chr.gainGP(gp);
	}

	@Override
	public String getGuildInviteStatus(String charname) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterByName(charname);
		if(chr != null){
			if(chr.getGuildId() == 0){
				return "The person you are trying to invite does not have a guild.";
			}else if(chr.getGuildRank() != 1) return "The player is not the leader of his/her guild.";
			else return "Good";
		}
		return null;
	}

	@Override
	public boolean setAllianceRank(int chrid, int rank) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null){
			chr.setAllianceRank(rank, false);
			chr.saveGuildStatus();
			return true;
		}else return false;
	}

	@Override
	public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException{
		MapleCharacter mc = ChannelServer.getInstance().getCharacterById(cid);
		if(mc == null) return;
		boolean bDifferentGuild;
		if(guildid == 0 && rank == -1){// Is this suppose to be a rank of -1?
			bDifferentGuild = true;
		}else{
			bDifferentGuild = guildid != mc.getGuildId();
			mc.setGuildId(guildid);
			mc.setGuildRank(rank);
			mc.saveGuildStatus();
		}
		if(bDifferentGuild){
			if(guildid == 0){
				mc.getMap().broadcastMessage(mc, UserRemote.guildNameChanged(mc.getId(), ""));
			}else{
				MapleGuild guild = ChannelServer.getInstance().getWorldInterface().getGuildIfExists(guildid);
				mc.getMap().broadcastMessage(mc, UserRemote.guildNameChanged(mc.getId(), guild != null ? guild.getName() : ""));
				if(guild != null) mc.getMap().broadcastMessage(mc, UserRemote.guildMarkChanged(mc.getId(), guild));
			}
		}
	}

	@Override
	public void setTopGuilds(int[] topGuilds) throws RemoteException{
		ChannelServer.getInstance().setTopGuilds(topGuilds);
	}

	@Override
	public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException{
		for(Channel ch : ChannelServer.getInstance().getChannels()){
			for(MaplePartyCharacter partychar : party.getMembers()){
				if(partychar.getChannel() == ch.getId()){
					MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(partychar.getName());
					if(chr != null){
						if(operation == PartyOperation.DISBAND){
							chr.setParty(null);
							chr.setMPC(null);
						}else{
							chr.setParty(party);
							chr.setMPC(partychar);
						}
						chr.getClient().announce(CWvsContext.Party.updateParty(chr.getClient().getChannel(), party, operation, target));
					}
				}
			}
			switch (operation){
				case LEAVE:
				case EXPEL:
					if(target.getChannel() == ch.getId()){
						MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(target.getName());
						if(chr != null){
							chr.getClient().announce(CWvsContext.Party.updateParty(chr.getClient().getChannel(), party, operation, target));
							chr.setParty(null);
							chr.setMPC(null);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public String getGuildInviteResponse(String charname) throws RemoteException{
		for(Channel ch : ChannelServer.getInstance().getChannels()){
			MapleCharacter chr = ch.getPlayerStorage().getCharacterByName(charname);
			if(chr != null){
				if(chr.getLevel() < 10) return "below10";
				if(chr.isIronMan()) return "ironman";
				if(chr.isInParty()) return "inparty";
				return "invite";
			}
		}
		return null;
	}

	@Override
	public BuddyList getBuddylist(int chrid) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null) return chr.getBuddylist();
		return null;
	}

	@Override
	public boolean addToBuddylist(int chrid, BuddylistEntry entry) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null){
			chr.getBuddylist().put(entry);
			return true;
		}
		return false;
	}

	@Override
	public boolean addBuddyRequest(int target, int cidFrom, String nameFrom, int channelFrom) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(target);
		if(chr != null){
			chr.getBuddylist().addBuddyRequest(target, cidFrom, nameFrom, channelFrom);
			return true;
		}
		return false;
	}

	@Override
	public void updateBuddies(int[] buddies, int characterId, int ch, boolean offline) throws RemoteException{
		for(int buddy : buddies){
			MapleCharacter chr = ChannelServer.getInstance().getCharacterById(buddy);
			if(chr != null){
				BuddylistEntry ble = chr.getBuddylist().get(characterId);
				if(ble != null && ble.isVisible()){
					if(offline){
						ble.setChannel((byte) -1);
					}else{
						ble.setChannel(ch);
					}
					chr.getBuddylist().put(ble);
					chr.getClient().announce(CWvsContext.Friend.updateBuddyChannel(ble));
				}
			}
		}
	}

	@Override
	public void buddyChat(int[] recipientCharacterIds, String source, int sourceid, String chattext) throws RemoteException{
		for(int characterId : recipientCharacterIds){
			MapleCharacter chr = ChannelServer.getInstance().getCharacterById(characterId);
			if(chr != null){
				if(chr.getBuddylist().containsVisible(sourceid)){
					chr.getClient().announce(MaplePacketCreator.multiChat(source, chattext, 0));
				}
			}
		}
	}

	@Override
	public int[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException{
		List<Integer> ret = new ArrayList<Integer>(characterIds.length);
		for(int characterId : characterIds){
			MapleCharacter chr = ChannelServer.getInstance().getCharacterById(characterId);
			if(chr != null){
				if(chr.getBuddylist().containsVisible(charIdFrom)){
					ret.add(characterId);
				}
			}
		}
		int[] retArr = new int[ret.size()];
		int pos = 0;
		for(Integer i : ret){
			retArr[pos++] = i.intValue();
		}
		return retArr;
	}

	@Override
	public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException{
		for(Channel channel : ChannelServer.getInstance().getChannels()){
			for(MapleMessengerCharacter messengerchar : messenger.getMembers()){
				if(messengerchar.getChannel() == channel.getId() && !(messengerchar.getName().equals(namefrom))){
					MapleCharacter chr = channel.getPlayerStorage().getCharacterByName(messengerchar.getName());
					if(chr != null){
						MapleCharacterLook mcl = ChannelServer.getInstance().getWorldInterface().getCharacterLooks(namefrom);
						if(mcl != null){
							chr.getClient().announce(MaplePacketCreator.addMessengerPlayer(namefrom, mcl, position, fromchannel));
							ChannelServer.getInstance().getWorldInterface().broadcastPacket(Arrays.asList(mcl.getId()), MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, messengerchar.getPosition(), messengerchar.getChannel()));
						}
					}
				}else if(messengerchar.getChannel() == channel.getId() && (messengerchar.getName().equals(namefrom))){
					MapleCharacter chr = channel.getPlayerStorage().getCharacterByName(messengerchar.getName());
					if(chr != null){
						chr.getClient().announce(MaplePacketCreator.joinMessenger(messengerchar.getPosition()));
					}
				}
			}
		}
	}

	@Override
	public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException{
		for(Channel channel : ChannelServer.getInstance().getChannels()){
			for(MapleMessengerCharacter messengerchar : messenger.getMembers()){
				if(messengerchar.getChannel() == channel.getId() && !(messengerchar.getName().equals(namefrom))){
					MapleCharacter chr = channel.getPlayerStorage().getCharacterByName(messengerchar.getName());
					if(chr != null){
						MapleCharacterLook from = ChannelServer.getInstance().getWorldInterface().getCharacterLooks(namefrom);
						chr.getClient().announce(MaplePacketCreator.updateMessengerPlayer(namefrom, from, position, fromchannel));
					}
				}
			}
		}
	}

	@Override
	public MapleCharacterLook getCharacterLooks(String character) throws RemoteException{
		MapleCharacter mc = ChannelServer.getInstance().getCharacterByName(character);
		if(mc != null) return new MapleCharacterLook(mc);
		return null;
	}

	@Override
	public boolean changeMap(String playerName, CharacterLocation target) throws RemoteException{
		MapleCharacter mc = ChannelServer.getInstance().getCharacterByName(playerName);
		if(mc != null){
			CommandWarp.changeMap(mc, target.channel, target.instanceMap, target.mapid, target.position, target.eventManager, target.eventInstance);
			return true;
		}
		return false;
	}

	@Override
	public boolean closeMerchant(String ownerName, int ownerID) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(ownerID);
		if(chr != null){
			chr.setHasMerchant(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean addMerchantMesos(int ownerID, int meso) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(ownerID);
		if(chr != null){
			chr.addMerchantMesos(meso);
			return true;
		}
		return false;
	}

	@Override
	public boolean setFamilyID(int chrid, int familyid) throws RemoteException{
		MapleCharacter chr = ChannelServer.getInstance().getCharacterById(chrid);
		if(chr != null){
			chr.setFamilyId(familyid);
			return true;
		}
		return false;
	}

	@Override
	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException{
		LimitedGood good = CashItemFactory.getGoodFromSN(nSN);
		if(good == null) return;
		good.nRemainCount = nRemainCount;
	}

	@Override
	public void broadcastPacket(byte[] packet) throws RemoteException{
		ChannelServer.getInstance().broadcastPacket(packet);
	}

	@Override
	public void broadcastGMPacket(byte[] packet) throws RemoteException{
		ChannelServer.getInstance().broadcastGMPacket(packet);
	}

	@Override
	public List<Integer> broadcastPacket(List<Integer> players, byte[] packet) throws RemoteException{
		return ChannelServer.getInstance().broadcastPacket(players, packet);
	}

	@Override
	public List<String> broadcastPacketToPlayers(List<String> players, byte[] packet) throws RemoteException{
		return ChannelServer.getInstance().broadcastPacketToPlayers(players, packet);
	}
}
