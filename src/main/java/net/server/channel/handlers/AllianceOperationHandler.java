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
package net.server.channel.handlers;

import java.rmi.RemoteException;

import client.CharacterLocation;
import client.MapleClient;
import client.MessageType;
import net.AbstractMaplePacketHandler;
import net.SendOpcode;
import net.channel.ChannelServer;
import net.server.guild.MapleAlliance;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;

/**
 * @author XoticStory
 */
public final class AllianceOperationHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		try{
			System.out.println("Alliance operation");
			MapleAlliance alliance = null;
			if(c.getPlayer().getGuild() != null && c.getPlayer().getGuild().getAllianceId() > 0){
				alliance = ChannelServer.getInstance().getWorldInterface().getAlliance(c.getPlayer().getGuild().getAllianceId());
			}
			if(alliance == null){
				c.getPlayer().dropMessage("You are not in an alliance.");
				c.announce(CWvsContext.enableActions());
				return;
			}else if(c.getPlayer().getMGC().getAllianceRank() > 2 || !alliance.getGuilds().contains(c.getPlayer().getGuildId())){
				c.announce(CWvsContext.enableActions());
				return;
			}
			switch (slea.readByte()){
				case 0x01:
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendShowInfo(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId()), -1, -1);
					break;
				case 0x02:{ // Leave Alliance
					if(c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuildId() < 1 || c.getPlayer().getGuildRank() != 1) return;
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeGuild(c.getPlayer().getGuildId(), c.getPlayer().getId(), c.getPlayer().getGuildId(), 2), -1, -1);
					break;
				}
				case 0x03: // send alliance invite
					String charName = slea.readMapleAsciiString();
					CharacterLocation location = ChannelServer.getInstance().getWorldInterface().find(charName);
					if(location == null){
						c.getPlayer().dropMessage("The player is not online.");
					}else{
						String status = ChannelServer.getInstance().getWorldInterface().getGuildInviteStatus(charName);
						if(status != null){
							if(status.equals("Good")){
								ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendInvitation(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), "test"), -1, -1);
							}else{
								c.getPlayer().dropMessage(status);
							}
						}
					}
					break;
				case 0x04:{
					int guildid = slea.readInt();
					// slea.readMapleAsciiString();//guild name
					if(c.getPlayer().getGuild().getAllianceId() != 0 || c.getPlayer().getGuildRank() != 1 || c.getPlayer().getGuildId() < 1) return;
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeGuild(guildid, c.getPlayer().getId(), c.getPlayer().getGuildId(), 0), -1, -1);
					break;
				}
				case 0x06:{ // Expel Guild
					int guildid = slea.readInt();
					int allianceid = slea.readInt();
					if(c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuild().getAllianceId() != allianceid) return;
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeGuild(allianceid, c.getPlayer().getId(), guildid, 1), -1, -1);
					break;
				}
				case 0x07:{ // Change Alliance Leader
					if(c.getPlayer().getGuild().getAllianceId() == 0 || c.getPlayer().getGuildId() < 1) return;
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeLeader(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), slea.readInt()), -1, -1);
					break;
				}
				case 0x08:
					String ranks[] = new String[5];
					for(int i = 0; i < 5; i++){
						ranks[i] = slea.readMapleAsciiString();
					}
					ChannelServer.getInstance().getWorldInterface().setAllianceRanks(alliance.getId(), ranks);
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), MaplePacketCreator.changeAllianceRankTitle(alliance.getId(), ranks), -1, -1);
					break;
				case 0x09:{
					int int1 = slea.readInt();
					byte byte1 = slea.readByte();
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), sendChangeRank(c.getPlayer().getGuild().getAllianceId(), c.getPlayer().getId(), int1, byte1), -1, -1);
					break;
				}
				case 0x0A:
					String notice = slea.readMapleAsciiString();
					ChannelServer.getInstance().getWorldInterface().setAllianceNotice(alliance.getId(), notice);
					ChannelServer.getInstance().getWorldInterface().allianceMessage(alliance.getId(), MaplePacketCreator.allianceNotice(alliance.getId(), notice), -1, -1);
					break;
				default:
					Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Unhandled AllIANCE_OPERATION packet: \n" + slea.toString());
			}
			alliance.saveToDB();
		}catch(RemoteException | NullPointerException ex){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
			c.announce(CWvsContext.enableActions());
			c.getPlayer().dropMessage(MessageType.ERROR, "Error communicating if the WorldServer.");
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

	private static byte[] sendInvitation(int allianceid, int playerid, final String guildname){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
		mplew.write(0x05);
		mplew.writeInt(allianceid);
		mplew.writeInt(playerid);
		mplew.writeMapleAsciiString(guildname);
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
