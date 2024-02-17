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

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import server.maps.objects.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.field.MiniRoomBase;

/**
 * @author kevintjuh93 :3
 */
public class RemoteStoreHandler extends AbstractMaplePacketHandler{

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		HiredMerchant hm = getMerchant(c);
		if(chr.hasMerchant() && hm != null){
			if(hm.getChannel() == chr.getClient().getChannel()){
				hm.setOpen(false);
				hm.removeAllVisitors("");
				chr.setHiredMerchant(hm);
				chr.announce(MiniRoomBase.EntrustedShop.getHiredMerchant(chr, hm, false));
			}else{
				c.announce(MaplePacketCreator.remoteChannelChange((byte) hm.getChannel()));
			}
			return;
		}else{
			chr.dropMessage(1, "You don't have a Merchant open");
		}
		c.announce(CWvsContext.enableActions());
	}

	public HiredMerchant getMerchant(MapleClient c){
		if(c.getPlayer().hasMerchant()){
			for(Channel cserv : ChannelServer.getInstance().getChannels()){
				if(cserv.getHiredMerchants().get(c.getPlayer().getId()) != null) return cserv.getHiredMerchants().get(c.getPlayer().getId());
			}
		}
		return null;
	}
}
