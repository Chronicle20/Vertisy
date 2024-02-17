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
import java.util.Iterator;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildResponse;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext.Guild;
import tools.packets.field.userpool.UserRemote;

public final class GuildOperationHandler extends AbstractMaplePacketHandler{

	private boolean isGuildNameAcceptable(String name){
		if(name.length() < 3 || name.length() > 12) return false;
		for(int i = 0; i < name.length(); i++){
			if(!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) return false;
		}
		return true;
	}


	private class Invited{

		public String name;
		public int gid;
		public long expiration;

		public Invited(String n, int id){
			name = n.toLowerCase();
			gid = id;
			expiration = System.currentTimeMillis() + 60 * 60 * 1000;
		}

		@Override
		public boolean equals(Object other){
			if(!(other instanceof Invited)) return false;
			Invited oth = (Invited) other;
			return(gid == oth.gid && name.equals(oth));
		}

		@Override
		public int hashCode(){
			int hash = 3;
			hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
			hash = 83 * hash + this.gid;
			return hash;
		}
	}

	private java.util.List<Invited> invited = new java.util.LinkedList<Invited>();
	private long nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(System.currentTimeMillis() >= nextPruneTime){
			Iterator<Invited> itr = invited.iterator();
			Invited inv;
			while(itr.hasNext()){
				inv = itr.next();
				if(System.currentTimeMillis() >= inv.expiration){
					itr.remove();
				}
			}
			nextPruneTime = System.currentTimeMillis() + 20 * 60 * 1000;
		}
		MapleCharacter mc = c.getPlayer();
		byte type = slea.readByte();
		switch (type){
			case Guild.Request.LoadGuild:
				// c.announce(MaplePacketCreator.showGuildInfo(mc));
				break;
			case Guild.Request.CheckGuildName:// Create, should be CreateGuildAgree or CreateNewGuild but we don't do those checks
				if(mc.getGuildId() > 0 || mc.getMapId() != 200000301){
					c.getPlayer().dropMessage(1, "You cannot create a new Guild while in one.");
					return;
				}
				if(mc.getMeso() < MapleGuild.CREATE_GUILD_COST){
					c.getPlayer().dropMessage(1, "You do not have enough mesos to create a Guild.");
					return;
				}
				String guildName = slea.readMapleAsciiString();
				if(!isGuildNameAcceptable(guildName)){
					c.getPlayer().dropMessage(1, "The Guild name you have chosen is not accepted.");
					return;
				}
				int gid = 0;
				try{
					gid = ChannelServer.getInstance().getWorldInterface().createGuild(mc.getId(), guildName);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				if(gid == 0){
					c.announce(MaplePacketCreator.genericGuildMessage((byte) 0x1c));
					return;
				}
				mc.gainMeso(-MapleGuild.CREATE_GUILD_COST, true, false, true);
				mc.setGuildId(gid);
				mc.setGuildRank(1);
				mc.saveGuildStatus();
				c.announce(MaplePacketCreator.showGuildInfo(mc));
				try{
					ChannelServer.getInstance().getWorldInterface().gainGP(gid, mc.getId(), 500);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				c.getPlayer().dropMessage(1, "You have successfully created a Guild.");
				mc.getMap().broadcastMessage(mc, UserRemote.guildNameChanged(mc.getId(), guildName));
				break;
			case Guild.Request.InviteGuild:// Invite
				if(mc.getGuildId() <= 0 || mc.getGuildRank() > 2) return;
				String name = slea.readMapleAsciiString();
				MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
				if(mgr != null){
					c.announce(mgr.getPacket());
				}else{
					Invited inv = new Invited(name, mc.getGuildId());
					if(!invited.contains(inv)){
						invited.add(inv);
					}
				}
				break;
			case Guild.Request.JoinGuild:// Join
				if(mc.getGuildId() > 0){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " attempted to join a guild when s/he is already in one.");
					return;
				}
				gid = slea.readInt();
				int cid = slea.readInt();
				if(cid != mc.getId()){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " attempted to join a guild with a different character id.");
					return;
				}
				name = mc.getName().toLowerCase();
				Iterator<Invited> itr = invited.iterator();
				boolean bOnList = true;
				while(itr.hasNext()){
					Invited inv = itr.next();
					if(gid == inv.gid && name.equals(inv.name)){
						bOnList = true;
						itr.remove();
						break;
					}
				}
				if(!bOnList){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " is trying to join a guild that never invited him/her (or that the invitation has expired)");
					return;
				}
				mc.setGuildId(gid); // joins the guild
				mc.setGuildRank(5); // start at lowest rank
				int s = 0;
				try{
					s = ChannelServer.getInstance().getWorldInterface().addGuildMember(mc.getMGC());
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				if(s == 0){
					c.getPlayer().dropMessage(1, "The Guild you are trying to join is already full.");
					mc.setGuildId(0);
					return;
				}
				c.announce(MaplePacketCreator.showGuildInfo(mc));
				try{
					ChannelServer.getInstance().getWorldInterface().gainGP(gid, mc.getId(), 500);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				mc.saveGuildStatus(); // update database
				try{
					mc.getMap().broadcastMessage(mc, UserRemote.guildNameChanged(mc.getId(), ""));
					mc.getMap().broadcastMessage(mc, UserRemote.guildMarkChanged(mc.getId(), ChannelServer.getInstance().getWorldInterface().getGuild(gid, null)));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case Guild.Request.WithdrawGuild:// Quit
				cid = slea.readInt();
				name = slea.readMapleAsciiString();
				if(cid != mc.getId() || !name.equals(mc.getName()) || mc.getGuildId() <= 0){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " tried to quit guild under the name \"" + name + "\" and current guild id of " + mc.getGuildId() + ".");
					return;
				}
				try{
					ChannelServer.getInstance().getWorldInterface().removeGP(mc.getGuildId(), mc.getId(), mc.getGP());
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				c.announce(MaplePacketCreator.updateGP(mc.getGuildId(), 0));
				try{
					ChannelServer.getInstance().getWorldInterface().leaveGuild(mc.getMGC());
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				c.announce(MaplePacketCreator.showGuildInfo(null));
				mc.setGuildId(0);
				mc.saveGuildStatus();
				mc.getMap().broadcastMessage(mc, UserRemote.guildNameChanged(mc.getId(), ""));
				break;
			case Guild.Request.KickGuild:// Expel
				cid = slea.readInt();
				name = slea.readMapleAsciiString();
				if(mc.getGuildRank() > 2 || mc.getGuildId() <= 0){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " is trying to expel without rank 1 or 2.");
					return;
				}
				try{
					ChannelServer.getInstance().getWorldInterface().expelMember(mc.getMGC(), name, cid);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case Guild.Request.SetGradeName:
				if(mc.getGuildId() <= 0 || mc.getGuildRank() != 1){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " tried to change guild rank titles when s/he does not have permission.");
					return;
				}
				String ranks[] = new String[5];
				for(int i = 0; i < 5; i++){
					ranks[i] = slea.readMapleAsciiString();
				}
				try{
					ChannelServer.getInstance().getWorldInterface().changeRankTitle(mc.getGuildId(), ranks);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case Guild.Request.SetMemberGrade:
				cid = slea.readInt();
				byte newRank = slea.readByte();
				if(mc.getGuildRank() > 2 || (newRank <= 2 && mc.getGuildRank() != 1) || mc.getGuildId() <= 0){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " is trying to change rank outside of his/her permissions.");
					return;
				}
				if(newRank <= 1 || newRank > 5) return;
				try{
					ChannelServer.getInstance().getWorldInterface().changeRank(mc.getGuildId(), cid, newRank);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case Guild.Request.SetMark:
				if(mc.getGuildId() <= 0 || mc.getGuildRank() != 1 || mc.getMapId() != 200000301){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " tried to change guild emblem without being the guild leader.");
					return;
				}
				if(mc.getGuild().getMeso() < MapleGuild.CHANGE_EMBLEM_COST){
					c.announce(MaplePacketCreator.serverNotice(1, "You're guild does not have enough mesos to create a Guild."));
					return;
				}
				short bg = slea.readShort();
				byte bgcolor = slea.readByte();
				short logo = slea.readShort();
				byte logocolor = slea.readByte();
				try{
					ChannelServer.getInstance().getWorldInterface().setGuildEmblem(mc.getGuildId(), bg, bgcolor, logo, logocolor);
					mc.getGuild().removeMeso(MapleGuild.CHANGE_EMBLEM_COST);
					mc.getMap().broadcastMessage(mc, UserRemote.guildMarkChanged(mc.getId(), bg, bgcolor, logo, logocolor));
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			case Guild.Request.SetNotice:
				if(mc.getGuildId() <= 0 || mc.getGuildRank() > 2){
					Logger.log(LogType.WARNING, LogFile.ANTICHEAT, "[hax] " + mc.getName() + " tried to change guild notice while not in a guild.");
					return;
				}
				String notice = slea.readMapleAsciiString();
				if(notice.length() > 100) return;
				try{
					ChannelServer.getInstance().getWorldInterface().setGuildNotice(mc.getGuildId(), notice);
				}catch(RemoteException | NullPointerException ex){
					Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
				}
				break;
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, "Unhandled GUILD_OPERATION packet: \n" + slea.toString());
		}
	}
}
