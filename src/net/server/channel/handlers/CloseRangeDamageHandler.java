/*
 * ara * This file is part of the OdinMS Maple Story Server
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import constants.GameConstants;
import constants.skills.*;
import server.MapleStatEffect;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CloseRangeDamageHandler extends AbstractDealDamageHandler{

	@Override
	public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c){
		MapleCharacter player = c.getPlayer();
		/*long timeElapsed = System.currentTimeMillis() - player.getAutobanManager().getLastSpam(8);
		if(timeElapsed < 300) {
			AutobanFactory.FAST_ATTACK.alert(player, "Time: " + timeElapsed);
		}
		player.getAutobanManager().spam(8);*/
		AttackInfo attack = parseDamage(slea, player, false, false);
		if(player.getBuffEffect(MapleBuffStat.MORPH) != null){
			if(player.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()){
				// How are they attacking when the client won't let them?
				player.getClient().disconnect(false, false);
				return;
			}
		}
		player.getMap().broadcastMessage(player, MaplePacketCreator.closeRangeAttack(player, attack), false, true);
		int numFinisherOrbs = 0;
		Integer comboBuff = player.getBuffedValue(MapleBuffStat.COMBO);
		if(GameConstants.isFinisherSkill(attack.skill)){
			if(comboBuff != null){
				numFinisherOrbs = comboBuff.intValue() - 1;
			}
			player.handleOrbconsume();
		}else if(attack.numAttacked > 0){
			if(attack.skill != 1111008 && comboBuff != null){
				int orbcount = player.getBuffedValue(MapleBuffStat.COMBO);
				int oid = player.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
				int advcomboid = player.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
				Skill combo = SkillFactory.getSkill(oid);
				Skill advcombo = SkillFactory.getSkill(advcomboid);
				MapleStatEffect ceffect;
				int advComboSkillLevel = player.getSkillLevel(advcombo);
				if(advComboSkillLevel > 0){
					ceffect = advcombo.getEffect(advComboSkillLevel);
				}else{
					ceffect = combo.getEffect(player.getSkillLevel(combo));
				}
				if(orbcount < ceffect.getX() + 1){
					int neworbcount = orbcount + 1;
					if(advComboSkillLevel > 0 && ceffect.makeChanceResult()){
						if(neworbcount <= ceffect.getX()){
							neworbcount++;
						}
					}
					int duration = combo.getEffect(player.getSkillLevel(oid)).getDuration();
					List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO, new BuffDataHolder(0, 0, neworbcount)));
					player.setBuffedValue(MapleBuffStat.COMBO, neworbcount);
					duration -= (int) (System.currentTimeMillis() - player.getBuffedStarttime(MapleBuffStat.COMBO));
					c.announce(MaplePacketCreator.giveBuff(player, oid, duration, stat));
					player.getMap().broadcastMessage(player, MaplePacketCreator.giveForeignBuff(player, stat), false);
				}
			}else if(player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100004) : SkillFactory.getSkill(5110001)) > 0 && (player.getJob().isA(MapleJob.MARAUDER) || player.getJob().isA(MapleJob.THUNDERBREAKER2))){
				for(int i = 0; i < attack.numAttacked; i++){
					player.handleEnergyChargeGain();
				}
			}
		}
		if(attack.numAttacked > 0 && attack.skill == DragonKnight.SACRIFICE){
			int totDamageToOneMonster = 0; // sacrifice attacks only 1 mob with 1 attack
			final Iterator<List<Pair<Integer, Boolean>>> dmgIt = attack.allDamage.values().iterator();
			if(dmgIt.hasNext()){// TODO: Crit boolean?
				totDamageToOneMonster = dmgIt.next().get(0).left;
			}
			int remainingHP = player.getHp() - totDamageToOneMonster * attack.getAttackEffect(player, null).getX() / 100;
			if(remainingHP > 1){
				player.setHp(remainingHP);
			}else{
				player.setHp(1);
			}
			player.updateSingleStat(MapleStat.HP, player.getHp());
		}
		if(attack.numAttacked > 0 && attack.skill == 1211002){
			boolean advcharge_prob = false;
			int advcharge_level = player.getSkillLevel(SkillFactory.getSkill(1220010));
			if(advcharge_level > 0){
				advcharge_prob = SkillFactory.getSkill(1220010).getEffect(advcharge_level).makeChanceResult();
			}
			if(!advcharge_prob){
				player.cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
			}
		}
		int attackCount = 1;
		if(attack.skill != 0){
			attackCount = attack.getAttackEffect(player, null).getAttackCount();
		}
		if(numFinisherOrbs == 0 && GameConstants.isFinisherSkill(attack.skill)) return;
		if(attack.skill > 0){
			Skill skill = SkillFactory.getSkill(attack.skill);
			MapleStatEffect effect_ = skill.getEffect(attack.skilllevel);
			if(effect_.getCooldown() > 0 && !player.isGM()){
				if(player.skillisCooling(attack.skill)){
					return;
				}else{
					c.announce(MaplePacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
					player.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule("crdh-cooldown", new CancelCooldownAction(player, attack.skill), effect_.getCooldown() * 1000));
				}
			}
		}
		boolean darksight = player.getBuffedValue(MapleBuffStat.DARKSIGHT) != null;
		if((player.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 || player.getSkillLevel(SkillFactory.getSkill(Rogue.DARK_SIGHT)) > 0) && darksight){// && player.getBuffSource(MapleBuffStat.DARKSIGHT) != 9101004
			Skill adsSkill = SkillFactory.getSkill(BladeLord.ADVANCED_DARK_SIGHT);
			int ads = player.getSkillLevel(adsSkill);
			if(ads > 0 && !adsSkill.getEffect(ads).makeChanceResult()){// hmm
				player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
				player.cancelBuffStats(MapleBuffStat.DARKSIGHT);
			}else if(ads <= 0){
				player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
				player.cancelBuffStats(MapleBuffStat.DARKSIGHT);
			}
		}
		applyAttack(attack, player, attackCount);
	}
}