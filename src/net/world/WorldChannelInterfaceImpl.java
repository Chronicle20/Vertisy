package net.world;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.CharacterLocation;
import client.MapleCharacterLook;
import constants.WorldConstants.WorldInfo;
import net.channel.ChannelWorldInterface;
import net.server.PlayerBuffValueHolder;
import net.server.channel.CharacterIdChannelPair;
import net.server.channel.handlers.AllianceRequestHandler;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import net.server.world.*;
import net.world.family.Family;
import net.world.family.FamilyCharacter;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since Feb 2, 2017
 */
public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface{

	private static final long serialVersionUID = -5163994554887909013L;

	public WorldChannelInterfaceImpl() throws RemoteException{
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}

	@Override
	public boolean isConnected() throws RemoteException{
		return true;
	}

	@Override
	public void saveWorldData() throws RemoteException{
		WorldServer.getInstance().saveWorldData();
	}

	@Override
	public void setExpRate(int expRate, int questExpRate, int dropRate, int mesoRate) throws RemoteException{
		WorldInfo info = WorldInfo.values()[WorldServer.getInstance().getID()];
		info.setExpRate(expRate);
		info.setQuestExpRate(questExpRate);
		info.setDropRate(dropRate);
		info.setMesoRate(mesoRate);
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.setExpRate(expRate, questExpRate, dropRate, mesoRate);
		}
	}

	@Override
	public void setServerMessage(String serverMessage) throws RemoteException{
		WorldServer.getInstance().serverMessage = serverMessage;
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.setServerMessage(serverMessage);
		}
	}

	@Override
	public String getServerMessage() throws RemoteException{
		return WorldServer.getInstance().serverMessage;
	}

	@Override
	public List<Integer> getAllChannels() throws RemoteException{
		List<Integer> channels = new ArrayList<>();
		for(int ch : WorldRegistryImpl.getInstance().getChannelServers().keySet()){
			ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannelServers().get(ch);
			channels.addAll(cwi.getAllChannels());
		}
		return channels;
	}

	@Override
	public void updateConnectedClients(int channel, int connectedClients) throws RemoteException{
		WorldServer.getInstance().getCenterInterface().updateConnectedClients(WorldServer.getInstance().getID(), channel, connectedClients);
	}

	@Override
	public CharacterLocation find(int chrid) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			CharacterLocation result = cwi.find(chrid);
			if(result != null) return result;
		}
		return null;
	}

	@Override
	public CharacterLocation find(String chrname) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			CharacterLocation result = cwi.find(chrname);
			if(result != null) return result;
		}
		return null;
	}

	@Override
	public String getIP(int channel) throws RemoteException{
		return WorldRegistryImpl.getInstance().getChannelServers().get(channel).getIP(channel);
	}

	@Override
	public int createGuild(int leaderId, String name) throws RemoteException{
		return WorldServer.getInstance().createGuild(leaderId, name);
	}

	@Override
	public MapleGuild getGuild(String name) throws RemoteException{
		return WorldServer.getInstance().getGuild(name);
	}

	@Override
	public MapleGuild getGuild(int id, MapleGuildCharacter mgc) throws RemoteException{
		return WorldServer.getInstance().getGuild(id, mgc);
	}

	@Override
	public MapleGuild getGuild(MapleGuildCharacter mgc) throws RemoteException{
		return WorldServer.getInstance().getGuild(mgc);
	}

	@Override
	public MapleGuild getGuildIfExists(int id) throws RemoteException{
		return WorldServer.getInstance().getGuildIfExists(id);
	}

	@Override
	public void disbandGuild(int gid) throws RemoteException{
		WorldServer.getInstance().disbandGuild(gid);
		WorldServer.getInstance().updateTopGuilds();
	}

	@Override
	public MapleGuildSummary getGuildSummary(int gid) throws RemoteException{
		return WorldServer.getInstance().getGuildSummary(gid);
	}

	@Override
	public void setGuildMemberOnline(MapleGuildCharacter mgc, boolean bOnline, int channel) throws RemoteException{
		WorldServer.getInstance().setGuildMemberOnline(mgc, bOnline, channel);
	}

	@Override
	public void setGuildNotice(int gid, String notice) throws RemoteException{
		WorldServer.getInstance().setGuildNotice(gid, notice);
	}

	@Override
	public void memberLevelJobUpdate(MapleGuildCharacter mgc) throws RemoteException{
		WorldServer.getInstance().memberLevelJobUpdate(mgc);
	}

	@Override
	public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException{
		WorldServer.getInstance().setGuildAndRank(cids, guildid, rank, exception);
	}

	@Override
	public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException{
		WorldServer.getInstance().changeEmblem(gid, affectedPlayers, mgs);
	}

	@Override
	public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) throws RemoteException{
		WorldServer.getInstance().setGuildEmblem(gid, bg, bgcolor, logo, logocolor);
	}

	@Override
	public int addGuildMember(MapleGuildCharacter mgc) throws RemoteException{
		return WorldServer.getInstance().addGuildMember(mgc);
	}

	@Override
	public void leaveGuild(MapleGuildCharacter mgc) throws RemoteException{
		WorldServer.getInstance().leaveGuild(mgc);
		WorldServer.getInstance().updateTopGuilds();
	}

	@Override
	public void expelMember(MapleGuildCharacter initiator, String name, int cid) throws RemoteException{
		WorldServer.getInstance().expelMember(initiator, name, cid);
	}

	@Override
	public void changeRank(int gid, int cid, int newRank) throws RemoteException{
		WorldServer.getInstance().changeRank(gid, cid, newRank);
	}

	@Override
	public void changeRankTitle(int gid, String[] ranks) throws RemoteException{
		WorldServer.getInstance().changeRankTitle(gid, ranks);
	}

	@Override
	public String increaseGuildCapacity(int gid) throws RemoteException{
		return WorldServer.getInstance().increaseGuildCapacity(gid);
	}

	@Override
	public void addGuildCoins(int gid, long coins) throws RemoteException{
		MapleGuild g = WorldServer.getInstance().getGuild(gid, null);
		if(g != null) g.addCoins(coins);
	}

	@Override
	public void addGuildMeso(int gid, long meso) throws RemoteException{
		MapleGuild g = WorldServer.getInstance().getGuild(gid, null);
		if(g != null) g.addMeso(meso);
	}

	@Override
	public String getGuildInviteStatus(String charname) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			String result = cwi.getGuildInviteStatus(charname);
			if(result != null) return result;
		}
		return null;
	}

	@Override
	public String getGuildInviteResponse(String charname) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			String result = cwi.getGuildInviteResponse(charname);
			if(result != null) return result;
		}
		return null;
	}

	@Override
	public void guildChat(int gid, String sourceName, int sourceid, String msg) throws RemoteException{
		WorldServer.getInstance().guildChat(gid, sourceName, sourceid, msg);
	}

	@Override
	public void gainGP(int gid, int chrid, int gp) throws RemoteException{
		WorldServer.getInstance().gainGP(gid, chrid, gp);
		WorldServer.getInstance().updateTopGuilds();
	}

	@Override
	public void removeGP(int gid, int chrid, int gp) throws RemoteException{
		WorldServer.getInstance().removeGP(gid, chrid, gp);
		WorldServer.getInstance().updateTopGuilds();
	}

	@Override
	public void guildMessage(int gid, final byte[] packet) throws RemoteException{
		WorldServer.getInstance().getGuild(gid, null).guildMessage(packet);
	}

	@Override
	public void guildMessage(int gid, final byte[] packet, int exception) throws RemoteException{
		WorldServer.getInstance().getGuild(gid, null).broadcast(packet, exception);
	}

	@Override
	public MapleAlliance getAlliance(int id) throws RemoteException{
		return WorldServer.getInstance().getAlliance(id);
	}

	@Override
	public void addAlliance(int id, MapleAlliance alliance) throws RemoteException{
		WorldServer.getInstance().addAlliance(id, alliance);
	}

	@Override
	public void disbandAlliance(int id) throws RemoteException{
		WorldServer.getInstance().disbandAlliance(id);
	}

	@Override
	public void allianceMessage(int id, final byte[] packet, int exception, int guildex) throws RemoteException{
		WorldServer.getInstance().allianceMessage(id, packet, exception, guildex);
	}

	@Override
	public boolean setAllianceRanks(int aId, String[] ranks) throws RemoteException{
		return WorldServer.getInstance().setAllianceRanks(aId, ranks);
	}

	@Override
	public boolean sendGuildInvitation(int allianceid, String allianceName, int guildLeader, String guildName) throws RemoteException{
		MapleAlliance alliance = WorldServer.getInstance().getAlliance(allianceid);
		if(alliance != null){
			if(broadcastPacket(Arrays.asList(guildLeader), AllianceRequestHandler.sendInvitation(alliance.getId(), 0, allianceName))){
				alliance.addInvitation(guildLeader, guildName);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isGuildInvited(int allianceid, int guildLeader, String guildName) throws RemoteException{
		MapleAlliance alliance = WorldServer.getInstance().getAlliance(allianceid);
		if(alliance != null) return alliance.isInvited(guildLeader, guildName);
		return false;
	}

	@Override
	public boolean setAllianceNotice(int aId, String notice) throws RemoteException{
		return WorldServer.getInstance().setAllianceNotice(aId, notice);
	}

	@Override
	public boolean setGuildAllianceId(int gId, int aId) throws RemoteException{
		return WorldServer.getInstance().setGuildAllianceId(gId, aId);
	}

	@Override
	public void setAllianceRank(int gid, int id, int rank) throws RemoteException{
		WorldServer.getInstance().getGuild(gid, null).getMGC(id).setAllianceRank(rank);
		WorldServer.getInstance().setAllianceRank(id, rank);
	}

	@Override
	public boolean addGuildToAlliance(int gid, int allianceid) throws RemoteException{
		MapleAlliance alliance = WorldServer.getInstance().getAlliance(allianceid);
		MapleGuild adding = WorldServer.getInstance().getGuild(gid, null);
		if(alliance != null && adding != null){
			if(alliance.getGuilds().size() >= alliance.getCapacity()) return false;
			if(alliance.addGuild(gid)){
				adding.setAllianceId(allianceid);
				setAllianceRank(gid, adding.getLeaderId(), 2);
				adding.guildMessage(MaplePacketCreator.getAllianceInfo(alliance));
				adding.guildMessage(MaplePacketCreator.getGuildAlliances(alliance));
				alliance.getGuilds().forEach(id-> {
					MapleGuild guild = WorldServer.getInstance().getGuild(id, null);
					if(guild != null){
						try{
							guild.guildMessage(MaplePacketCreator.addGuildToAlliance(alliance, gid));
						}catch(RemoteException | NullPointerException e){
							Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
						}
					}
				});
				alliance.removeInvited(gid);
				return true;
			}
		}
		return false;
	}

	@Override
	public void removeGuildFromAlliance(int allianceid, int guildid) throws RemoteException{
		MapleAlliance alliance = WorldServer.getInstance().getAlliance(allianceid);
		MapleGuild guild = WorldServer.getInstance().getGuild(guildid, null);
		if(guild != null && alliance != null){
			setAllianceRank(guildid, guild.getLeaderId(), 5);
			guild.setAllianceId(0);
			for(int mg : alliance.getGuilds()){
				MapleGuild g = WorldServer.getInstance().getGuild(mg, null);
				if(g != null){
					g.broadcast(MaplePacketCreator.removeGuildFromAlliance(alliance, guildid));
				}
			}
			alliance.removeGuild(guildid);
		}
	}

	@Override
	public MapleParty createParty(MaplePartyCharacter chrfor) throws RemoteException{
		return WorldServer.getInstance().createParty(chrfor);
	}

	@Override
	public MapleParty getParty(int partyid) throws RemoteException{
		return WorldServer.getInstance().getParty(partyid);
	}

	@Override
	public MapleParty disbandParty(int partyid) throws RemoteException{
		return WorldServer.getInstance().disbandParty(partyid);
	}

	@Override
	public void updateParty(int partyid, PartyOperation operation, MaplePartyCharacter target) throws RemoteException{
		WorldServer.getInstance().updateParty(partyid, operation, target);
	}

	@Override
	public void partyChat(MapleParty party, String chattext, String source) throws RemoteException{
		WorldServer.getInstance().partyChat(party, chattext, source);
	}

	@Override
	public void addPartyInvited(int partyid, String name) throws RemoteException{
		MapleParty party = WorldServer.getInstance().getParty(partyid);
		if(party != null) party.addInvited(name);
	}

	@Override
	public void removePartyInvited(int partyid, String name) throws RemoteException{
		WorldServer.getInstance().getParty(partyid).removeInvited(name);
	}

	@Override
	public boolean isPartyInvited(int partyid, String name) throws RemoteException{
		return WorldServer.getInstance().getParty(partyid).isInvited(name);
	}

	@Override
	public void incrementMonsterKills(int partyid) throws RemoteException{
		WorldServer.getInstance().getParty(partyid).incrementMonsterKills();
	}

	@Override
	public void resetMonsterKills(int partyid) throws RemoteException{
		WorldServer.getInstance().getParty(partyid).setMonsterKills(0);
	}

	@Override
	public int getMonsterKillsWorld(int partyid) throws RemoteException{
		return WorldServer.getInstance().getParty(partyid).getMonsterKills();
	}

	@Override
	public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException{
		WorldServer.getInstance().updateBuddies(characterId, channel, buddies, true);
	}

	@Override
	public void loggedOn(String name, int characterId, int channel, int[] buddies) throws RemoteException{
		WorldServer.getInstance().updateBuddies(characterId, channel, buddies, false);
	}

	@Override
	public CharacterIdChannelPair[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException{
		return WorldServer.getInstance().multiBuddyFind(charIdFrom, characterIds);
	}

	@Override
	public void buddyChat(int[] recipientCharacterIds, String source, int sourceid, String chattext) throws RemoteException{
		WorldServer.getInstance().buddyChat(recipientCharacterIds, source, sourceid, chattext);
	}

	@Override
	public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) throws RemoteException{
		WorldServer.getInstance().buddyChanged(cid, cidFrom, name, channel, operation);
	}

	@Override
	public BuddyAddResult requestBuddyAdd(int chrid, int channelFrom, int cidFrom, String nameFrom) throws RemoteException{
		return WorldServer.getInstance().requestBuddyAdd(chrid, channelFrom, cidFrom, nameFrom);
	}

	@Override
	public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) throws RemoteException{
		WorldServer.getInstance().getPlayerBuffStorage().addBuffsToStorage(chrid, toStore);
	}

	@Override
	public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) throws RemoteException{
		return WorldServer.getInstance().getPlayerBuffStorage().getBuffsFromStorage(chrid);
	}

	@Override
	public MapleMessenger createMessenger(MapleMessengerCharacter chrfor) throws RemoteException{
		return WorldServer.getInstance().createMessenger(chrfor);
	}

	@Override
	public MapleMessenger getMessenger(int messengerid) throws RemoteException{
		return WorldServer.getInstance().getMessenger(messengerid);
	}

	@Override
	public void joinMessenger(int messengerid, MapleMessengerCharacter target, String from, int fromchannel) throws RemoteException{
		WorldServer.getInstance().joinMessenger(messengerid, target, from, fromchannel);
	}

	@Override
	public void leaveMessenger(int messengerid, MapleMessengerCharacter target) throws RemoteException{
		WorldServer.getInstance().leaveMessenger(messengerid, target);
	}

	@Override
	public void declineChat(String target, String namefrom) throws RemoteException{
		WorldServer.getInstance().declineChat(target, namefrom);
	}

	@Override
	public void messengerChat(int messengerid, String chattext, String namefrom) throws RemoteException{
		WorldServer.getInstance().messengerChat(messengerid, chattext, namefrom);
	}

	@Override
	public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException{
		WorldServer.getInstance().messengerInvite(sender, messengerid, target, fromchannel);
	}

	@Override
	public void silentJoinMessenger(int messengerid, MapleMessengerCharacter target, int position) throws RemoteException{
		WorldServer.getInstance().silentJoinMessenger(messengerid, target, position);
	}

	@Override
	public void updateMessenger(int messengerid, String namefrom, int fromchannel) throws RemoteException{
		WorldServer.getInstance().updateMessenger(messengerid, namefrom, fromchannel);
	}

	@Override
	public void updateMessenger(int messengerid, String namefrom, int position, int fromchannel) throws RemoteException{
		WorldServer.getInstance().updateMessenger(WorldServer.getInstance().getMessenger(messengerid), namefrom, position, fromchannel);
	}

	@Override
	public MapleCharacterLook getCharacterLooks(String character) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			MapleCharacterLook mcl = cwi.getCharacterLooks(character);
			if(mcl != null) return mcl;
		}
		return null;
	}

	@Override
	public boolean changeMap(String playerName, CharacterLocation target) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			boolean result = cwi.changeMap(playerName, target);
			if(result) return result;
		}
		return false;
	}

	@Override
	public void closeMerchant(String ownerName, int ownerID) throws RemoteException{
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?", java.sql.Statement.RETURN_GENERATED_KEYS)){
			ps.setInt(1, ownerID);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			boolean result = cwi.closeMerchant(ownerName, ownerID);
			if(result) return;
		}
	}

	@Override
	public void addMerchantMesos(int ownerID, int meso) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			boolean result = cwi.addMerchantMesos(ownerID, meso);
			if(result) return;
		}
		try(PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + meso + " WHERE id = ?", java.sql.Statement.RETURN_GENERATED_KEYS)){
			ps.setInt(1, ownerID);
			ps.executeUpdate();
		}catch(SQLException ex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
		}
	}

	@Override
	public Family getFamily(int familyid) throws RemoteException{
		return WorldServer.getInstance().getFamily(familyid);
	}

	@Override
	public Family createFamily(int world, int bossID, String familyName) throws RemoteException{
		Family family = new Family();
		family.world = world;
		family.bossID = bossID;
		family.familyName = familyName;
		family.create();
		return family;
	}

	@Override
	public void setFamilyID(int chrid, int familyid) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			if(cwi.setFamilyID(chrid, familyid)) return;
		}
	}

	@Override
	public boolean joinFamily(int familyid, int ownerid, int id) throws RemoteException{
		Family family = getFamily(familyid);
		if(familyid <= 0 || family == null){
			CharacterLocation loc = find(ownerid);
			if(loc == null) return false;
			family = createFamily(WorldServer.getInstance().getID(), ownerid, loc.charName);
			FamilyCharacter owner = new FamilyCharacter();
			owner.characterID = loc.chrid;
			owner.characterName = loc.charName;
			owner.familyID = family.familyID;
			owner.online = true;
			owner.parent = owner;
			family.members.put(owner.characterID, owner);
			setFamilyID(owner.characterID, family.familyID);
		}
		CharacterLocation cl = find(id);
		// check if they are in a family.
		FamilyCharacter fc = new FamilyCharacter();
		fc.characterID = cl.chrid;
		fc.characterName = cl.charName;
		fc.familyID = family.familyID;
		fc.online = true;
		fc.parent = family.members.get(ownerid);
		family.members.put(fc.characterID, fc);
		family.save();
		setFamilyID(fc.characterID, family.familyID);
		WorldServer.getInstance().addFamily(family);
		return true;
	}

	@Override
	public void updateLimitedGood(int nSN, int nRemainCount) throws RemoteException{
		WorldServer.getInstance().getCenterInterface().updateLimitedGood(nSN, nRemainCount);
	}

	@Override
	public void changeName(MapleGuildCharacter mgc) throws RemoteException{
		if(mgc != null){
			MapleGuild guild = getGuildIfExists(mgc.getGuildId());
			if(guild != null){
				guild.memberLevelJobUpdate(mgc);
			}
		}
	}

	@Override
	public void broadcastPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastPacket(packet);
		}
	}

	@Override
	public boolean broadcastPacket(List<Integer> players, byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			if(cwi.broadcastPacket(players, packet).size() == players.size()) return true;
		}
		return false;
	}

	@Override
	public boolean broadcastPacketToPlayers(List<String> players, byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			if(cwi.broadcastPacketToPlayers(players, packet).size() == players.size()) return true;
		}
		return true;
	}

	@Override
	public void broadcastGMPacket(byte[] packet) throws RemoteException{
		for(ChannelWorldInterface cwi : WorldRegistryImpl.getInstance().getChannelServers().values()){
			cwi.broadcastGMPacket(packet);
		}
	}
}
