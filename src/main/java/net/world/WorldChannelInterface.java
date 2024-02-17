package net.world;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.CharacterLocation;
import client.MapleCharacterLook;
import net.server.PlayerBuffValueHolder;
import net.server.channel.CharacterIdChannelPair;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import net.server.world.*;
import net.world.family.Family;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public interface WorldChannelInterface extends Remote{

	public boolean isConnected() throws RemoteException;

	public void saveWorldData() throws RemoteException;

	public void setExpRate(int expRate, int questExpRate, int dropRate, int mesoRate) throws RemoteException;

	public void setServerMessage(String serverMessage) throws RemoteException;

	public String getServerMessage() throws RemoteException;

	public String getIP(int channel) throws RemoteException;

	public List<Integer> getAllChannels() throws RemoteException;

	public void updateConnectedClients(int channel, int connectedClients) throws RemoteException;

	public CharacterLocation find(int chrid) throws RemoteException;

	public CharacterLocation find(String chrname) throws RemoteException;

	public int createGuild(int leaderId, String name) throws RemoteException;

	public MapleGuild getGuild(String name) throws RemoteException;

	public MapleGuild getGuild(int id, MapleGuildCharacter mgc) throws RemoteException;

	public MapleGuild getGuild(MapleGuildCharacter mgc) throws RemoteException;

	public MapleGuild getGuildIfExists(int id) throws RemoteException;

	public void disbandGuild(int gid) throws RemoteException;

	public MapleGuildSummary getGuildSummary(int gid) throws RemoteException;

	public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException;

	public void setGuildNotice(int gid, String notice) throws RemoteException;

	public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException;

	public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException;

	public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException;

	public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException;

	public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException;

	public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException;

	public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException;

	public void changeRank(int gid, int cid, int newRank) throws RemoteException;

	public void changeRankTitle(int gid, String[] ranks) throws RemoteException;

	public String increaseGuildCapacity(int gid) throws RemoteException;

	public void addGuildCoins(int gid, long coins) throws RemoteException;

	public void addGuildMeso(int gid, long meso) throws RemoteException;

	public String getGuildInviteStatus(String charname) throws RemoteException;

	public String getGuildInviteResponse(String charname) throws RemoteException;

	public void guildChat(int gid, String sourceName, int sourceid, String msg) throws RemoteException;

	public void gainGP(int gid, int chrid, int gp) throws RemoteException;

	public void removeGP(int gid, int chrid, int gp) throws RemoteException;

	public void guildMessage(int gid, final byte[] packet) throws RemoteException;

	public void guildMessage(int gid, final byte[] packet, int exception) throws RemoteException;

	public MapleAlliance getAlliance(int id) throws RemoteException;

	public void addAlliance(int id, MapleAlliance alliance) throws RemoteException;

	public void disbandAlliance(int id) throws RemoteException;

	public void allianceMessage(int id, final byte[] packet, int exception, int guildex) throws RemoteException;

	public boolean setAllianceRanks(int aId, String[] ranks) throws RemoteException;

	public boolean setAllianceNotice(int aId, String notice) throws RemoteException;

	public boolean setGuildAllianceId(int gId, int aId) throws RemoteException;

	public void setAllianceRank(int gid, int id, int rank) throws RemoteException;

	public boolean addGuildToAlliance(int gid, int allianceid) throws RemoteException;

	public boolean sendGuildInvitation(int allianceid, String allianceName, int guildLeader, String guildName) throws RemoteException;

	public boolean isGuildInvited(int allianceid, int guildLeader, String guildName) throws RemoteException;

	public void removeGuildFromAlliance(int allianceid, int guildid) throws RemoteException;

	public MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException;

	public MapleParty getParty(int partyid) throws RemoteException;

	public MapleParty disbandParty(int partyid) throws RemoteException;

	public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;

	public void partyChat(MapleParty party, String chattext, String source) throws RemoteException;

	public void addPartyInvited(int partyid, String name) throws RemoteException;

	public void removePartyInvited(int partyid, String name) throws RemoteException;

	public boolean isPartyInvited(int partyid, String name) throws RemoteException;

	public void incrementMonsterKills(int partyid) throws RemoteException;

	public void resetMonsterKills(int partyid) throws RemoteException;

	public int getMonsterKillsWorld(int partyid) throws RemoteException;

	public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException;

	public void loggedOn(String name, int characterId, int channel, int buddies[]) throws RemoteException;

	public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException;

	public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) throws RemoteException;

	public BuddyAddResult requestBuddyAdd(int chrid, int channelFrom, int cidFrom, String nameFrom) throws RemoteException;

	public void buddyChat(int[] recipientCharacterIds, String source, int sourceid, String chattext) throws RemoteException;

	public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException;

	public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException;

	public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException;

	public MapleMessenger getMessenger(int messengerid) throws RemoteException;

	public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException;

	public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException;

	public void declineChat(String target, String namefrom) throws RemoteException;

	public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException;

	public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException;

	public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException;

	public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException;

	public void updateMessenger(int messengerid, String namefrom, int position, int fromchannel) throws RemoteException;

	public MapleCharacterLook getCharacterLooks(String character) throws RemoteException;

	public boolean changeMap(String playerName, CharacterLocation target) throws RemoteException;

	public void closeMerchant(String ownerName, int ownerID) throws RemoteException;

	public void addMerchantMesos(int ownerID, int meso) throws RemoteException;

	public Family getFamily(int familyid) throws RemoteException;

	public Family createFamily(int world, int bossID, String familyName) throws RemoteException;

	public void setFamilyID(int chrid, int familyid) throws RemoteException;

	public boolean joinFamily(int familyid, int ownerid, int id) throws RemoteException;

	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException;

	public void changeName(MapleGuildCharacter mgc) throws RemoteException;

	public void broadcastPacket(byte[] packet) throws RemoteException;

	public boolean broadcastPacket(List<Integer> players, byte[] packet) throws RemoteException;

	public boolean broadcastPacketToPlayers(List<String> players, byte[] packet) throws RemoteException;

	public void broadcastGMPacket(byte[] packet) throws RemoteException;
}
