package net.channel;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterLocation;
import client.MapleCharacterLook;
import net.server.world.MapleMessenger;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public interface ChannelWorldInterface extends Remote{

	public boolean isConnected() throws RemoteException;

	public void setExpRate(int expRate, int questExpRate, int dropRate, int mesoRate) throws RemoteException;

	public void setServerMessage(String serverMessage) throws RemoteException;

	public List<Integer> getAllChannels() throws RemoteException;

	public int getChannelLoad(int channel) throws RemoteException;

	public String getIP(int channel) throws RemoteException;

	public CharacterLocation find(int chrid) throws RemoteException;

	public CharacterLocation find(String chrname) throws RemoteException;

	public void gainGP(int chrid, int gp) throws RemoteException;

	public void removeGP(int chrid, int gp) throws RemoteException;

	public String getGuildInviteStatus(String charname) throws RemoteException;

	public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException;

	public void setTopGuilds(int[] topGuilds) throws RemoteException;

	public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;

	public String getGuildInviteResponse(String charname) throws RemoteException;

	public boolean setAllianceRank(int chrid, int rank) throws RemoteException;

	public BuddyList getBuddylist(int chrid) throws RemoteException;

	public boolean addToBuddylist(int chrid, BuddylistEntry entry) throws RemoteException;

	public boolean addBuddyRequest(int target, int cidFrom, String nameFrom, int channelFrom) throws RemoteException;

	public void updateBuddies(int[] buddies, int characterId, int ch, boolean offline) throws RemoteException;

	public void buddyChat(int[] recipientCharacterIds, String source, int sourceid, String chattext) throws RemoteException;

	public int[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;

	public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException;

	public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException;

	public MapleCharacterLook getCharacterLooks(String character) throws RemoteException;

	public boolean changeMap(String playerName, CharacterLocation target) throws RemoteException;

	public boolean closeMerchant(String ownerName, int ownerID) throws RemoteException;

	public boolean addMerchantMesos(int ownerID, int meso) throws RemoteException;

	public boolean setFamilyID(int chrid, int familyid) throws RemoteException;

	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException;

	public void broadcastPacket(byte[] packet) throws RemoteException;

	public List<Integer> broadcastPacket(List<Integer> players, byte[] packet) throws RemoteException;

	public List<String> broadcastPacketToPlayers(List<String> players, byte[] packet) throws RemoteException;

	public void broadcastGMPacket(byte[] packet) throws RemoteException;
}
