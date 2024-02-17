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

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class SkillBookHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		if(!c.getPlayer().isAlive()){
			c.announce(CWvsContext.enableActions());
			return;
		}
		slea.readInt();
		short slot = slea.readShort();
		int itemId = slea.readInt();
		MapleCharacter player = c.getPlayer();
		Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if(toUse != null && toUse.getQuantity() > 0){
			if(toUse.getItemId() != itemId){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use mismatched item in SkillBookHandler");
				return;
			}
			ItemData skilldata = ItemInformationProvider.getInstance().getItemData(toUse.getItemId());
			boolean canuse;
			boolean success = false;
			int skill = 0;
			int maxlevel = 0;
			if(skilldata == null){
				AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a non-skillbook in SkillBookhandler");
				return;
			}
			if(skilldata.skills.isEmpty()){
				player.getClient().announce(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, false, success));
				return;
			}
			Skill skill2 = null;
			if(c.getPlayer().getJob() != null && c.getPlayer().getJob().getJobTree() != null){
				top: for(MapleJob job : c.getPlayer().getJob().getJobTree()){
					for(int id : skilldata.skills){
						if(id / 10000 == job.getId()){
							skill2 = SkillFactory.getSkill(id);
							break top;
						}
					}
				}
			}
			if(skill2 == null){
				canuse = false;
			}else if(player.getReincarnations() < skilldata.rcount){
				canuse = false;
			}else if((player.getSkillLevel(skill2) >= skilldata.reqSkillLevel || skilldata.reqSkillLevel == 0) && player.getMasterLevel(skill2) < skilldata.masterLevel){
				canuse = true;
				if(Randomizer.nextInt(101) < skilldata.success && skilldata.success != 0){
					success = true;
					player.changeSkillLevel(skill2, player.getSkillLevel(skill2), Math.max(skilldata.masterLevel, player.getMasterLevel(skill2)), -1);
				}else{
					success = false;
					// player.dropMessage("The skill book lights up, but the skill winds up as if nothing happened.");
				}
				MapleInventoryManipulator.removeItem(c, MapleInventoryType.USE, slot, (short) 1, true, false);
			}else{
				canuse = false;
			}
			player.getClient().announce(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, canuse, success));
		}else{
			AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SkillBookHandler");
		}
	}
}
