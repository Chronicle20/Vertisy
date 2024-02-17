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

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import client.autoban.AutobanFactory;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CWvsContext;
import tools.packets.field.userpool.UserRemote;

public final class SpecialMoveHandler extends AbstractMaplePacketHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter chr = c.getPlayer();
		if(chr == null) return;
		chr.getAutobanManager().setTimestamp(4, slea.readInt(), 3);
		int skillid = slea.readInt();
		if(!chr.isGM()){
			if((!GameConstants.isPQSkillMap(chr.getMapId()) && GameConstants.isPqSkill(skillid)) || GameConstants.isGMSkills(skillid) || (!GameConstants.is_correct_job_for_skill_root(chr.getJob().getId(), skillid / 10000) && !GameConstants.isBeginnerSkill(skillid))){
				AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit skills. Using skill: " + skillid + " without it being in their job.");
				c.disconnect(true, false);
				return;
			}
		}
		Point pos = null;
		int __skillLevel = slea.readByte();
		Skill skill = SkillFactory.getSkill(skillid);
		int skillLevel = chr.getSkillLevel(skill);
		if(skillid % 10000000 == 1010 || skillid % 10000000 == 1011){
			skillLevel = 1;
			chr.setDojoEnergy(0);
			c.announce(MaplePacketCreator.getEnergy("energy", 0));
		}
		if(skillLevel == 0 || skillLevel != __skillLevel) return;
		MapleStatEffect effect = skill.getEffect(skillLevel);
		if(effect.getCooldown() > 0){
			if(chr.skillisCooling(skillid)){
				return;
			}else if(skillid != Corsair.BATTLE_SHIP){
				if(FeatureSettings.COOLDOWNS){
					c.announce(MaplePacketCreator.skillCooldown(skillid, effect.getCooldown()));
					ScheduledFuture<?> timer = TimerManager.getInstance().schedule("smh-cancel", new CancelCooldownAction(chr, skillid), effect.getCooldown() * 1000);
					chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
				}
			}
		}
		if(skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET){ // Monster Magnet
			int num = slea.readInt();
			for(int i = 0; i < num; i++){
				int mobId = slea.readInt();
				byte success = slea.readByte();
				chr.getMap().broadcastMessage(chr, MaplePacketCreator.showMagnet(mobId, success), false);
				MapleMonster monster = chr.getMap().getMonsterByOid(mobId);
				if(monster != null){
					if(!monster.isBoss()){
						monster.switchController(chr, monster.isControllerHasAggro());
					}
				}
			}
			byte direction = slea.readByte();
			chr.getMap().broadcastMessage(chr, UserRemote.UserEffect.showBuffeffect(chr.getId(), skillid, chr.getSkillLevel(skillid), direction), false);
			c.announce(CWvsContext.enableActions());
			return;
		}else if(skillid == Brawler.MP_RECOVERY){// MP Recovery
			Skill s = SkillFactory.getSkill(skillid);
			MapleStatEffect ef = s.getEffect(chr.getSkillLevel(s));
			double x = ef.getX();
			int lose = (int) (chr.getMaxHp() / x);
			chr.setHp(chr.getHp() - lose);
			chr.updateSingleStat(MapleStat.HP, chr.getHp());
			double y = ef.getY();// someone needs to test properly..
			int gain = (int) (lose * (y / 100));
			chr.setMp(chr.getMp() + gain);
			chr.updateSingleStat(MapleStat.MP, chr.getMp());
		}else if(skillid % 10000000 == 1004){
			slea.readShort();
		}
		if(slea.available() == 6){// was 5, they added an extra byte after pos
			pos = new Point(slea.readShort(), slea.readShort());
		}
		if(chr.isAlive()){
			MapleBuffStat toggle = null;
			switch (skill.getId()){
				case BattleMage.BLUE_AURA:
					toggle = MapleBuffStat.BlueAura;
					break;
				case BattleMage.DARK_AURA:
					toggle = MapleBuffStat.DarkAura;
					break;
				case BattleMage.YELLOW_AURA:
					toggle = MapleBuffStat.YellowAura;
					break;
			}
			if(toggle != null){
				if(chr.getBuffEffect(toggle) != null){
					skill.getEffect(skillLevel).remove(chr, chr, true);
					// chr.cancelEffectFromBuffStat(toggle);
					c.announce(CWvsContext.enableActions());
					return;
				}
			}
			if(skill.getId() != Priest.MYSTIC_DOOR){
				skill.getEffect(skillLevel).applyTo(chr, pos);
				c.announce(CWvsContext.enableActions());
			}else if(skill.getId() == Priest.MYSTIC_DOOR && chr.canDoor()){
				skill.getEffect(skillLevel).applyTo(chr, pos);
			}else{
				chr.message("Please wait 5 seconds before casting Mystic Door again.");
				c.announce(CWvsContext.enableActions());
			}
		}else{
			c.announce(CWvsContext.enableActions());
		}
	}
}
