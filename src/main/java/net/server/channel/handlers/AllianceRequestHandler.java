package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.MapleClient;
import client.MessageType;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @Author Arnah
 * @Website http://Vertisy.ca/
 * @since May 2, 2017
 */
public class AllianceRequestHandler extends AbstractMaplePacketHandler{

	public enum AllianceRequestType{
		LOGIN(1),
		LEAVE_ALLIANCE(2),
		INVITE_GUILD(3),
		JOIN_ALLIANCE(4),
		CHANGE_LEADER(7),
		UPDATE_HIERARCHY(8),
		UPDATE_NOTICE(10);

		private int type;

		private AllianceRequestType(int type){
			this.type = type;
		}

		public int getType(){
			return type;
		}

		public static AllianceRequestType getType(int type){
			for(AllianceRequestType requestType : values()){
				if(requestType.getType() == type) return requestType;
			}
			return null;
		}
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		try{
			if(c.getPlayer().getGuildId() <= 0) return;
			MapleAlliance alliance = null;
			MapleGuild guild = c.getPlayer().getGuild();
			if(guild != null && c.getPlayer().getGuild().getAllianceId() > 0){
				alliance = ChannelServer.getInstance().getWorldInterface().getAlliance(guild.getAllianceId());
			}
			byte mode = slea.readByte();
			AllianceRequestType type = AllianceRequestType.getType(mode);
			if(type == null){
				System.out.println("Unhandled type: " + mode + " - " + slea.toString());
				c.announce(CWvsContext.enableActions());
				return;
			}
			System.out.println("Alliance Request: " + mode + " (" + type + ")");
			switch (type){
				case LOGIN:
					if(alliance == null) return;
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendShowInfo(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId()), -1, -1);
					break;
				case LEAVE_ALLIANCE:
					if(alliance == null) return;// ban?
					if(c.getPlayer().getMGC().getAllianceRank() != 2) return;// ban?
					ChannelServer.getInstance().getWorldInterface().removeGuildFromAlliance(alliance.getId(), guild.getId());
					c.getPlayer().setAllianceRank(5, true);
					c.getPlayer().saveGuildStatus();
					break;
				case INVITE_GUILD:
					if(alliance == null) return;// ban?
					if(c.getPlayer().getMGC().getAllianceRank() != 1) return;// ban?
					String guildName = slea.readMapleAsciiString();
					MapleGuild targetGuild = ChannelServer.getInstance().getWorldInterface().getGuild(guildName);
					if(targetGuild != null){
						if(ChannelServer.getInstance().getWorldInterface().sendGuildInvitation(alliance.getId(), alliance.getName(), targetGuild.getLeaderId(), targetGuild.getName())){
							c.getPlayer().dropMessage(MessageType.SYSTEM, "Alliance invitation sent.");
						}else{
							c.getPlayer().dropMessage(MessageType.SYSTEM, "Alliance invitation failed. Make sure the guild leader is online.");
						}
					}else{
						c.getPlayer().dropMessage(MessageType.POPUP, "Unknown guild name.");
					}
					break;
				case JOIN_ALLIANCE:
					if(alliance != null) return;// ban?
					if(c.getPlayer().getGuildRank() != 1) return;
					int allianceid = slea.readInt();
					guildName = slea.readMapleAsciiString();
					if(ChannelServer.getInstance().getWorldInterface().isGuildInvited(allianceid, c.getPlayer().getId(), guildName)){
						if(ChannelServer.getInstance().getWorldInterface().addGuildToAlliance(guild.getId(), allianceid)){
							c.getPlayer().setAllianceRank(2, true);
							c.getPlayer().saveGuildStatus();
						}
					}
					break;
				case CHANGE_LEADER:
					if(alliance == null) return;// ban?
					if(c.getPlayer().getMGC().getAllianceRank() != 1) return;// ban?
					int newMaster = slea.readInt();
					ChannelServer.getInstance().getWorldInterface().setAllianceRank(guild.getId(), c.getPlayer().getId(), 2);// update clients & server
					ChannelServer.getInstance().getWorldInterface().setAllianceRank(guild.getId(), newMaster, 1);
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeLeader(alliance.getId(), c.getPlayer().getId(), newMaster), -1, -1);// update all clients
					break;
				case UPDATE_HIERARCHY:
					if(alliance == null) return;// ban?
					if(c.getPlayer().getMGC().getAllianceRank() != 1) return;// ban?
					String ranks[] = new String[5];
					for(int i = 0; i < 5; i++){
						ranks[i] = slea.readMapleAsciiString();
					}
					ChannelServer.getInstance().getWorldInterface().setAllianceRanks(alliance.getId(), ranks);
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), MaplePacketCreator.changeAllianceRankTitle(alliance.getId(), ranks), -1, -1);
					break;
				case UPDATE_NOTICE:
					if(c.getPlayer().getMGC().getAllianceRank() > 2) return;// ban?
					String notice = slea.readMapleAsciiString();
					ChannelServer.getInstance().getWorldInterface().setAllianceNotice(alliance.getId(), notice);
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), MaplePacketCreator.allianceNotice(alliance.getId(), notice), -1, -1);
					break;
				default:
					System.out.println("Unhandled alliance request type: " + type);
					System.out.println(slea.toString());
					c.announce(CWvsContext.enableActions());
					break;
			}
		}catch(RemoteException | NullPointerException rex){
			Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, rex);
			c.getPlayer().dropMessage(MessageType.ERROR, ServerConstants.WORLD_SERVER_ERROR);
			c.announce(CWvsContext.enableActions());
		}catch(Exception ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			c.announce(CWvsContext.enableActions());
		}
	}

	private static byte[] sendShowInfo(int allianceid, int playerid){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x02);
		mplew.writeInt(allianceid);
		mplew.writeInt(playerid);
		return mplew.getPacket();
	}

	public static byte[] sendInvitation(int allianceid, int playerid, final String allianceName){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(3);
		mplew.writeInt(allianceid);
		mplew.writeMapleAsciiString(allianceName);
		mplew.writeMapleAsciiString("Required");
		return mplew.getPacket();
	}

	private static byte[] sendChangeGuild(int allianceid, int playerid, int guildid, int option){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x07);
		mplew.writeInt(allianceid);
		mplew.writeInt(guildid);
		mplew.writeInt(playerid);
		mplew.write(option);
		return mplew.getPacket();
	}

	private static byte[] sendChangeLeader(int allianceid, int playerid, int victim){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x08);
		mplew.writeInt(allianceid);
		mplew.writeInt(playerid);
		mplew.writeInt(victim);
		return mplew.getPacket();
	}

	private static byte[] sendChangeRank(int allianceid, int playerid, int int1, byte byte1){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x09);
		mplew.writeInt(allianceid);
		mplew.writeInt(playerid);
		mplew.writeInt(int1);
		mplew.writeInt(byte1);
		return mplew.getPacket();
	}
}
