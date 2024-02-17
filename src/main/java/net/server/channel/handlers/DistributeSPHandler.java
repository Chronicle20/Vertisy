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
import constants.GameConstants;
import constants.JobConstants;
import constants.skills.Aran;
import constants.skills.BladeSpecialist;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;

public final class DistributeSPHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		slea.readInt();
		int skillid = slea.readInt();
		MapleCharacter player = c.getPlayer();
		if(GameConstants.isHiddenSkills(skillid)){
			AutobanFactory.PACKET_EDIT.alert(player, "tried to packet edit in distributing sp. Tried to add sp to hidden skill " + skillid);
			c.disconnect(true, false);
			return;
		}
		int remainingSp = player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillid / 10000));
		boolean isBeginnerSkill = false;
		if((!GameConstants.isPQSkillMap(player.getMapId()) && GameConstants.isPqSkill(skillid)) || (!player.isGM() && GameConstants.isGMSkills(skillid)) || (!GameConstants.is_correct_job_for_skill_root(player.getJob().getId(), skillid / 10000) && !GameConstants.isBeginnerSkill(skillid))){
			AutobanFactory.PACKET_EDIT.alert(player, "tried to packet edit in distributing sp. Tried to use skill " + skillid + " without it being in their job.");
			c.disconnect(true, false);
			return;
		}
		if(skillid % 10000000 > 999 && skillid % 10000000 < 1003){
			int total = 0;
			for(int i = 0; i < 3; i++){
				total += player.getSkillLevel(SkillFactory.getSkill(player.getJobType() * 10000000 + 1000 + i));
			}
			remainingSp = Math.min((player.getLevel() - 1), 6) - total;
			isBeginnerSkill = true;
		}
		Skill skill = SkillFactory.getSkill(skillid);
		int curLevel = player.getSkillLevel(skill);
		int maxLevel = skill.getMaxLevel();
		int masterLevel = skill.getMasterLevel();
		// System.out.println("CurLevel: " + curLevel + " maxLevel: " + maxLevel + " masterLevel: " + masterLevel + " playerMasterLevel: " + player.getMasterLevel(skill));
		if(masterLevel > 0 && player.getMasterLevel(skill) <= masterLevel) maxLevel = masterLevel;
		if((remainingSp > 0 && curLevel + 1 <= maxLevel)){
			if(!isBeginnerSkill){
				player.setRemainingSp(player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillid / 10000)) - 1, JobConstants.getSkillBookIndex(skillid / 10000));
			}
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSpBySkill(JobConstants.getSkillBookIndex(skillid / 10000)));
			if(skill.getId() == Aran.FULL_SWING){
				player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
				player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_DOUBLE), (byte) player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
				player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_FULL_TRIPLE), (byte) player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
			}else if(skill.getId() == Aran.OVER_SWING){
				player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
				player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_DOUBLE), (byte) player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
				player.changeSkillLevel(SkillFactory.getSkill(Aran.HIDDEN_OVER_TRIPLE), (byte) player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
			}else if(skill.getId() == BladeSpecialist.TORNADO_SPIN){
				player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
				player.changeSkillLevel(SkillFactory.getSkill(BladeSpecialist.TORNADO_SPIN_TWIRL), (byte) player.getSkillLevel(skill), player.getMasterLevel(skill), player.getSkillExpiration(skill));
			}else{
				player.changeSkillLevel(skill, (byte) (curLevel + 1), player.getMasterLevel(skill), player.getSkillExpiration(skill));
			}
		}else player.announce(CWvsContext.enableActions());
	}
}