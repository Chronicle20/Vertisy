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

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class NPCAnimation extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		// TODO: Npc Controllers
		/*MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.NPC_ACTION.getValue());
		int objectid = slea.readInt();
		mplew.writeInt(objectid);
		byte unk = slea.readByte();
		mplew.write(unk);// aAct
		byte unk2 = slea.readByte();
		mplew.write(unk2);// nChatIdx
		MapleNPC npc = c.getPlayer().getMap().getNPCByObjectId(objectid);
		if(npc != null && npc.stats.move){
			// System.out.println("Decoding move");
			MovePath path = new MovePath();
			path.decode(slea);
			path.encode(mplew);
			
			// mplew.writeInt(slea.readInt());//pos
			// mplew.writeInt(slea.readInt());//pos
			// System.out.println("A: " + slea.toString());
			// System.out.println("B: " + mplew.toString());
		}
		if(slea.available() > 0){// has 2 positions and a random byte?
			mplew.write(slea.read((int) slea.available()));
		}
		c.announce(mplew.getPacket());*/
	}
}
