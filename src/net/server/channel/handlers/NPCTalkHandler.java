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
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.life.MapleNPCStats.NPCScriptData;
import server.maps.objects.MapleMapObject;
import server.maps.objects.PlayerNPC;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class NPCTalkHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!c.getPlayer().isAlive()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		int oid = slea.readInt();
		slea.readInt();
		MapleMapObject obj = c.getPlayer().getMap().getMapObject(oid);
		if(obj == null) return;
		if(obj instanceof MapleNPC){
			MapleNPC npc = (MapleNPC) obj;
			if(npc.getId() == 9010009){
				// c.announce(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(c.getPlayer())));
				c.announce(CWvsContext.enableActions());
			}else if(!npc.hasShop()){
				if(c.getCM() != null){
					if(c.getCM().getNpc() == npc.getId()){
						c.announce(CWvsContext.enableActions());
						return;
					}else c.getPlayer().dispose();
				}
				if(c.getQM() != null){
					c.announce(CWvsContext.enableActions());
					return;
				}
				if(npc.getId() >= 9100100 && npc.getId() < 9100120){
					// Custom handling for gachapon scripts to reduce the amount of scripts needed.
					NPCScriptManager.getInstance().start(c, npc.getId(), "gachapon", null);
				}else{
					if(npc.stats != null){
						NPCScriptData data = npc.stats.getScriptData().get(0);
						if(data != null && data.script != null){
							NPCScriptManager.getInstance().start(c, npc.getId(), data.script, null);
							return;
						}
					}
					try{
						NPCScriptManager.getInstance().start(c, npc.getId(), null);
					}catch(Exception ex){}
				}
			}else if(npc.hasShop()){
				if(c.getPlayer().getShop() != null) return;
				npc.sendShop(c);
			}
		}else if(obj instanceof PlayerNPC){
			NPCScriptManager.getInstance().start(c, ((PlayerNPC) obj).getId(), null);
		}
	}
}