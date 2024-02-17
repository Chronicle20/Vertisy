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
import java.util.*;

import client.*;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.EquipSlot;
import constants.GameConstants;
import constants.ItemConstants;
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemData.SkillData;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.*;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.partyquest.Pyramid;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CWvsContext;
import tools.packets.field.DropPool;

public abstract class AbstractDealDamageHandler extends AbstractMaplePacketHandler{

	public static class AttackInfo{

		public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, action, rangedirection, charge, display, lastAttackTime, mesoCount;
		public int projectile;
		public Map<Integer, List<Pair<Integer, Boolean>>> allDamage;
		public boolean isHH = false, isTempest = false, ranged, magic;
		public int speed = 4;
		public Point position = new Point();
		public Point attackerPosition = new Point();
		public List<Point> mobPositions = new ArrayList<>(), mobPositionPrev = new ArrayList<>();
		public List<Integer> mesos = new ArrayList<>();

		public MapleStatEffect getAttackEffect(MapleCharacter chr, Skill theSkill){
			Skill mySkill = theSkill;
			if(mySkill == null){
				mySkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(skill));
			}
			int skillLevel = chr.getSkillLevel(mySkill);
			if(mySkill.getId() % 10000000 == 1020){
				if(chr.getPartyQuest() instanceof Pyramid){
					if(((Pyramid) chr.getPartyQuest()).useSkill()){
						skillLevel = 1;
					}
				}
			}
			if(skillLevel == 0) return null;
			if(display > 80){ // Hmm
				if(!theSkill.getAction()){
					AutobanFactory.FAST_ATTACK.autoban(chr, "WZ Edit; adding action to a skill: " + display);
					return null;
				}
			}
			return mySkill.getEffect(skillLevel);
		}
	}

	// private int numRand = 11;// A number of random number for calculate damage
	// ??
	protected synchronized void applyAttack(AttackInfo attack, final MapleCharacter player, int attackCount){
		Skill skill = null;
		Skill theSkill = null;
		MapleStatEffect attackEffect = null;
		final int job = player.getJob().getId();
		if(player.isBanned()) return;
		if(!player.isAlive()) return;
		Item weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]);
		if(weapon == null && (!player.getJob().isA(MapleJob.PIRATE) && player.getJob().isA(MapleJob.THUNDERBREAKER1))){
			AutobanFactory.PACKET_EDIT.alert(player, "Tried to attack without a weapon");
			return;
		}
		if(weapon instanceof Equip){
			Equip eqp = (Equip) weapon;
			if(eqp.getDurability() == 0){
				AutobanFactory.PACKET_EDIT.alert(player, "Tried to attack with a weapon on 0 durability");
				return;
			}
		}
		if(attack.skill != 0){
			skill = SkillFactory.getSkill(attack.skill);
			theSkill = SkillFactory.getSkill(GameConstants.getHiddenSkill(attack.skill)); // returns back the skill id if its not a hidden skill so we are gucci
			attackEffect = attack.getAttackEffect(player, theSkill);
			if(attackEffect == null){
				player.getClient().announce(CWvsContext.enableActions());
				return;
			}
			if(player.getMp() < attackEffect.getMpCon()){
				AutobanFactory.MPCON.addPoint(player.getAutobanManager(), "Skill: " + attack.skill + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());
			}
			if(attack.skill != Cleric.HEAL){
				if(player.isAlive()){
					if(attack.skill == NightWalker.POISON_BOMB){ // Poison Bomb
						attackEffect.applyTo(player, new Point(attack.position.x, attack.position.y));
					}else if(attack.skill != Aran.BODY_PRESSURE){
						attackEffect.applyTo(player);
					}
				}else{
					player.getClient().announce(CWvsContext.enableActions());
				}
			}
			int mobCount = attackEffect.getMobCount();
			if(attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Page.FINAL_ATTACK_BW || attack.skill == Page.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_AXE || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM || attack.skill == WindArcher.FINAL_ATTACK || attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK){
				mobCount = 15;// :(
			}
			if(attack.skill == Aran.HIDDEN_FULL_DOUBLE || attack.skill == Aran.HIDDEN_FULL_TRIPLE || attack.skill == Aran.HIDDEN_OVER_DOUBLE || attack.skill == Aran.HIDDEN_OVER_TRIPLE){
				mobCount = 12;
			}
			if(attack.numAttacked > mobCount){
				AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skill + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
				return;
			}
		}
		// WTF IS THIS F3,1
		/*if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_PENRIL && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
			 return;
			 }*/
		int totDamage = 0;
		final MapleMap map = player.getMap();
		if(attack.skill == ChiefBandit.MESO_EXPLOSION){
			int delay = 0;
			for(int mesoOID : attack.mesos){
				MapleMapObject mapobject = map.getMapObject(mesoOID);
				if(mapobject != null){
					if(mapobject.getType() == MapleMapObjectType.ITEM){
						final MapleMapItem mapitem = (MapleMapItem) mapobject;
						if(mapitem.getMeso() == 0){
							AutobanFactory.PACKET_EDIT.alert(player, "Used meso explosion on a non-meso MapItem");
							return; // Maybe it is possible some how?
						}
						synchronized(mapitem){
							if(mapitem.isPickedUp()) return;
							TimerManager.getInstance().schedule("mesoExplosion", new Runnable(){

								@Override
								public void run(){
									map.removeMapObject(mapitem);
									map.broadcastMessage(DropPool.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem.getPosition());
									mapitem.setPickedUp(true);
								}
							}, delay);
							delay += 100;
						}
					}else{
						AutobanFactory.PACKET_EDIT.alert(player, "Used meso explosion on a non-item map object");
						return;
					}
				}
			}
		}
		for(Integer oned : attack.allDamage.keySet()){
			final MapleMonster monster = map.getMonsterByOid(oned.intValue());
			if(monster != null){
				int totDamageToOneMonster = 0;
				List<Pair<Integer, Boolean>> onedList = attack.allDamage.get(oned);
				for(Pair<Integer, Boolean> eachd : onedList){// TODO: Crit boolean?
					if(eachd.left < 0){
						eachd.left += Integer.MAX_VALUE;
					}
					totDamageToOneMonster += eachd.left;
				}
				totDamage += totDamageToOneMonster;
				player.checkMonsterAggro(monster);
				if(player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)){
					Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
					int delay = 0;
					final int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET).intValue();
					for(Pair<Integer, Boolean> eachd : onedList){// TODO: Crit boolean?
						eachd.left += Integer.MAX_VALUE;
						if(pickpocket.getEffect(player.getSkillLevel(pickpocket)).makeChanceResult()){
							final Integer eachdf;
							if(eachd.left < 0){
								eachdf = eachd.left + Integer.MAX_VALUE;
							}else{
								eachdf = eachd.left;
							}
							TimerManager.getInstance().schedule("pickPocket", new Runnable(){

								@Override
								public void run(){
									player.getMap().spawnMesoDrop(Math.min((int) Math.max(((double) eachdf / (double) 20000) * (double) maxmeso, (double) 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 2);
								}
							}, delay);
							delay += 100;
						}
					}
				}else if(attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN){
					player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getMaxHp() / 2)));
				}else if(attack.skill == Bandit.STEAL){
					Skill steal = SkillFactory.getSkill(Bandit.STEAL);
					if(steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()){
						List<MonsterDropEntry> toSteals = MapleMonsterInformationProvider.getInstance().retrieveDrop(monster.getId());
						Collections.shuffle(toSteals);
						int toSteal = toSteals.get(rand(0, (toSteals.size() - 1))).itemId;
						ItemInformationProvider ii = ItemInformationProvider.getInstance();
						Item item;
						if(ItemConstants.getInventoryType(toSteal).equals(MapleInventoryType.EQUIP)){
							item = ii.randomizeStats((Equip) ii.getEquipById(toSteal));
						}else{
							item = new Item(toSteal, (byte) 0, (short) 1, -1);
						}
						player.getMap().spawnItemDrop(monster, player, item, monster.getPosition(), false, false);
					}
					monster.setItemStolen();
				}else if(attack.skill == FPArchMage.FIRE_DEMON){
					monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration() * 1000);
				}else if(attack.skill == ILArchMage.ICE_DEMON){
					monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration() * 1000);
				}else if(attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE){
					player.setMarkedMonster(monster.getObjectId());
					player.announce(MaplePacketCreator.giveBuff(player, 1, attack.skill, Collections.singletonList(new Pair<>(MapleBuffStat.HOMING_BEACON, new BuffDataHolder(0, 0, monster.getObjectId())))));
				}
				if(job == 2111 || job == 2112){
					if(player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null){
						Skill snowCharge = SkillFactory.getSkill(Aran.SNOW_CHARGE);
						if(totDamageToOneMonster > 0){
							// MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX()), snowCharge, null, false);
							// monster.applyStatus(player, monsterStatusEffect, false, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY() * 1000);
							MobStatData data = new MobStatData(MobStat.Speed, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX(), snowCharge.id, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY() * 1000);
							monster.applyStatus(player, data, snowCharge, false, false);
						}
					}
				}
				if(player.getBuffedValue(MapleBuffStat.HAMSTRING) != null){
					Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
					if(hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()){
						// MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
						// monster.applyStatus(player, monsterStatusEffect, false, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
						MobStatData data = new MobStatData(MobStat.Speed, hamstring.getEffect(player.getSkillLevel(hamstring)).getX(), hamstring.id, hamstring.getEffect(player.getSkillLevel(hamstring)).getY() * 1000);
						monster.applyStatus(player, data, hamstring, false, false);
					}
				}
				if(player.getBuffedValue(MapleBuffStat.EVAN_SLOW) != null){
					Skill slow = SkillFactory.getSkill(Evan.SLOW);
					if(slow.getEffect(player.getSkillLevel(slow)).makeChanceResult()){
						// MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, slow.getEffect(player.getSkillLevel(slow)).getX()), slow, null, false);
						// monster.applyStatus(player, monsterStatusEffect, false, slow.getEffect(player.getSkillLevel(slow)).getY() * 60 * 1000);
						MobStatData data = new MobStatData(MobStat.Speed, slow.getEffect(player.getSkillLevel(slow)).getX(), slow.id, slow.getEffect(player.getSkillLevel(slow)).getY() * 60 * 1000);
						monster.applyStatus(player, data, slow, false, false);
					}
				}
				if(player.getBuffedValue(MapleBuffStat.BLIND) != null){
					Skill blind = SkillFactory.getSkill(Marksman.BLIND);
					if(blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()){
						MobStatData data = new MobStatData(MobStat.ACC, blind.getEffect(player.getSkillLevel(blind)).getX(), blind.id, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
						monster.applyStatus(player, data, blind, false, false);
						// MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
						// monster.applyStatus(player, monsterStatusEffect, false, blind.getEffect(player.getSkillLevel(blind)).getY() * 1000);
					}
				}
				if(job == 121 || job == 122){
					for(int charge = 1211005; charge < 1211007; charge++){
						Skill chargeSkill = SkillFactory.getSkill(charge);
						if(player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)){
							if(totDamageToOneMonster > 0){
								if(charge == WhiteKnight.BW_ICE_CHARGE || charge == WhiteKnight.SWORD_ICE_CHARGE){
									monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
									break;
								}
								if(charge == WhiteKnight.BW_FIRE_CHARGE || charge == WhiteKnight.SWORD_FIRE_CHARGE){
									monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
									break;
								}
							}
						}
					}
					if(job == 122){
						for(int charge = 1221003; charge < 1221004; charge++){
							Skill chargeSkill = SkillFactory.getSkill(charge);
							if(player.isBuffFrom(MapleBuffStat.WK_CHARGE, chargeSkill)){
								if(totDamageToOneMonster > 0){
									monster.setTempEffectiveness(Element.HOLY, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
									break;
								}
							}
						}
					}
				}else if(player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null){
					if(player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null){
						skill = SkillFactory.getSkill(21100005);
						player.setHp(player.getHp() + ((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100), true);
						player.updateSingleStat(MapleStat.HP, player.getHp());
					}
				}else if(job == 412 || job == 422 || job == 1411 || job == 1412 || job == MapleJob.BLADE_MASTER.getId()){
					int skillid = 0;
					switch (player.getJob()){
						case BLADE_MASTER:
							skillid = 4340001;
							break;
						case NIGHTLORD:
							skillid = 4120005;
							break;
						case SHADOWER:
							skillid = 4220005;
							break;
						case NIGHTWALKER3:
						case NIGHTWALKER4:
							skillid = 14110004;
							break;
						default:
							skillid = 0;
							break;
					}
					if(skillid != 0){
						Skill type = SkillFactory.getSkill(skillid);
						if(player.getSkillLevel(type) > 0){
							MapleStatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
							for(int i = 0; i < attackCount; i++){
								if(venomEffect.makeChanceResult()){
									if(monster.getVenomMulti() < 3){
										monster.setVenomMulti((monster.getVenomMulti() + 1));
										MobStatData data = new MobStatData(MobStat.Poison, venomEffect.getDuration());
										data.rOption = skillid;
										monster.applyStatus(player, data, type, false, true);
									}
								}
							}
						}
					}
				}else if(job == 521 || job == 522){ // from what I can gather this is how it should work
					if(!monster.isBoss() && attack.skill == Outlaw.FLAME_THROWER){
						Skill type = SkillFactory.getSkill(Outlaw.FLAME_THROWER);
						if(player.getSkillLevel(type) > 0){
							MapleStatEffect DoT = type.getEffect(player.getSkillLevel(type));
							MobStatData data = new MobStatData(MobStat.Poison, DoT.getDuration());
							data.rOption = Outlaw.FLAME_THROWER;
							monster.applyStatus(player, data, type, true, false);
						}
					}
				}else if(job >= 311 && job <= 322){
					if(!monster.isBoss()){
						Skill mortalBlow;
						if(job == 311 || job == 312){
							mortalBlow = SkillFactory.getSkill(Ranger.MORTAL_BLOW);
						}else{
							mortalBlow = SkillFactory.getSkill(Sniper.MORTAL_BLOW);
						}
						if(player.getSkillLevel(mortalBlow) > 0){
							MapleStatEffect mortal = mortalBlow.getEffect(player.getSkillLevel(mortalBlow));
							if(monster.getHp() <= (monster.getStats().getHp() * mortal.getX()) / 100){
								if(Randomizer.rand(1, 100) <= mortal.getY()){
									player.getMap().broadcastMessage(MaplePacketCreator.onSpecialEffectBySkill(monster.getObjectId(), mortalBlow.getId(), player.getId()));
									player.battleAnaylsis.addDamage(mortalBlow.id, monster.getHp());
									map.damageMonster(player, monster, monster.getHp());
								}
							}
						}
					}
				}else if(job >= 430 && job <= 434){
					if(attack.skill == BladeLord.OWL_SPIRIT){
						Skill owlSpirit = SkillFactory.getSkill(BladeLord.OWL_SPIRIT);
						if(player.getSkillLevel(owlSpirit) > 0){
							MapleStatEffect spirit = owlSpirit.getEffect(player.getSkillLevel(owlSpirit));
							if(!monster.isBoss() && spirit.makeChanceResult()){
								// Don't think this skill has a special effect by skill
								player.battleAnaylsis.addDamage(owlSpirit.id, monster.getHp());
								map.damageMonster(player, monster, monster.getHp());
								spirit.applyTo(player);
							}
						}
					}
				}
				if(attack.skill != 0){
					if(attackEffect.getFixDamage() != -1){
						if(totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0){
							AutobanFactory.FIX_DAMAGE.autoban(player, "Hit: " + String.valueOf(totDamageToOneMonster) + " damage. Calc: " + attackEffect.getFixDamage());
						}
					}
				}
				if(totDamageToOneMonster > 0 && attackEffect != null && attackEffect.getMonsterStati().size() > 0){
					if(attackEffect.makeChanceResult()){
						monster.applyStatus(player, attackEffect.getMonsterStati(), theSkill, attackEffect.isPoison(), false);
					}
				}
				if(attack.isHH && !monster.isBoss()){
					map.damageMonster(player, monster, monster.getHp() - 1);
				}else if(attack.isHH){
					int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
					int damage = (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8));
					player.battleAnaylsis.addDamage(attack.skill, damage);
					map.damageMonster(player, monster, damage);
				}else if(attack.isTempest && !monster.isBoss()){
					player.battleAnaylsis.addDamage(attack.skill, monster.getHp());
					map.damageMonster(player, monster, monster.getHp());
				}else if(attack.isTempest){
					int TmpDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Aran.COMBO_TEMPEST).getEffect(player.getSkillLevel(SkillFactory.getSkill(Aran.COMBO_TEMPEST))).getDamage() / 100));
					int damage = (int) (Math.floor(Math.random() * (TmpDmg / 5) + TmpDmg * .8));
					player.battleAnaylsis.addDamage(attack.skill, damage);
					map.damageMonster(player, monster, damage);
				}else{
					player.battleAnaylsis.addDamage(attack.skill, totDamageToOneMonster);
					map.damageMonster(player, monster, totDamageToOneMonster);
				}
				if(monster.isBuffed(MobStat.PCounter)){
					for(MobStatData data : monster.getMobStats().values()){
						if(data.stat.equals(MobStat.PCounter)){
							player.addHP(-data.pCounter);
							map.broadcastMessage(player, MaplePacketCreator.damagePlayer(0, monster.getId(), player.getId(), data.pCounter, 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
						}
					}
				}
				if(monster.isBuffed(MobStat.MCounter)){
					for(MobStatData data : monster.getMobStats().values()){
						if(data.stat.equals(MobStat.MCounter)){
							player.addHP(-data.mCounter);
							map.broadcastMessage(player, MaplePacketCreator.damagePlayer(0, monster.getId(), player.getId(), data.mCounter, 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
						}
					}
				}
			}
		}
	}

	protected AttackInfo parseDamage(LittleEndianAccessor lea, MapleCharacter chr, boolean ranged, boolean magic){
		AttackInfo ret = new AttackInfo();
		lea.readByte();
		if(lea.readByte() == 1) lea.skip(8);
		else lea.skip(7);// debug info
		ret.numAttackedAndDamage = lea.readByte();
		ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
		ret.numDamage = ret.numAttackedAndDamage & 0xF;
		ret.allDamage = new HashMap<>();
		lea.skip(8);// debug info
		ret.skill = lea.readInt();
		lea.skip(8);// debug info
		ret.ranged = ranged;
		ret.magic = magic;
		if(ret.skill > 0){
			ret.skilllevel = chr.getSkillLevel(ret.skill);
		}
		if(ret.skill == BladeMaster.FINAL_CUT || ret.skill == Evan.ICE_BREATH || ret.skill == Evan.FIRE_BREATH || ret.skill == FPArchMage.BIG_BANG || ret.skill == ILArchMage.BIG_BANG || ret.skill == Bishop.BIG_BANG || ret.skill == Gunslinger.GRENADE || ret.skill == Brawler.CORKSCREW_BLOW || ret.skill == ThunderBreaker.CORKSCREW_BLOW || ret.skill == NightWalker.POISON_BOMB){
			ret.charge = lea.readInt();// tKeyDown
		}else{
			ret.charge = -1;
		}
		if(ret.skill == Paladin.HEAVENS_HAMMER){
			ret.isHH = true;
		}else if(ret.skill == Aran.COMBO_TEMPEST){
			ret.isTempest = true;
		}
		// mage attacks have 6 * 4 debug bytes
		if(ret.magic) lea.skip(6 * 4);
		lea.readInt();// skill entry crc
		int crc = lea.readInt();// skill level data crc
		Skill skill = null;
		if(ret.skill > 0){
			skill = SkillFactory.getSkill(ret.skill);
			if(skill != null){
				Item item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.WEAPON.getSlots()[0]);
				if(item != null){
					Equip wep = (Equip) item;
					if(wep.hasLearnedSkills()){
						ItemData data = ItemInformationProvider.getInstance().getItemData(wep.getItemId());
						SkillData skillData = data.skillData.get((int) wep.getItemLevel());
						if(skillData != null){
							for(Pair<Integer, Integer> p : skillData.skills){
								if(p.left == ret.skill){
									ret.skilllevel += p.right;
								}
							}
						}
					}
				}
				ret.skilllevel = Math.min(ret.skilllevel, skill.getMaxLevel());
			}else Logger.log(LogType.INFO, LogFile.GENERAL_ERROR, null, "Gave skill %d but is null.", ret.skill);
		}
		ret.display = lea.readByte();// nOption
		ret.action = lea.readByte();// bLeft/nAction
		ret.stance = lea.readByte();// nAttackType
		boolean mesoExplosion = ret.skill == ChiefBandit.MESO_EXPLOSION;
		if(mesoExplosion){
			if(ret.numAttackedAndDamage == 0){// used to delete meso when not attacking anything
				lea.skip(10);// mob info? But we aren't hitting any mobs??
				int bullets = lea.readByte();
				for(int j = 0; j < bullets; j++){
					ret.mesos.add(lea.readInt());
					lea.skip(1);
				}
				return ret;
			}
		}
		if(ranged){
			if(ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE){
				lea.skip(4);
			}
			/*byte unk = */lea.readByte();
			ret.speed = lea.readByte();
			ret.lastAttackTime = lea.readInt();
			lea.readInt();
			/*short usableSlot = */lea.readShort();
			/*short cashSlot = */lea.readShort();
			/*byte unk1 = */lea.readByte();
		}else{
			/*byte b = */lea.readByte();// weapon class?
			ret.speed = lea.readByte();
			ret.lastAttackTime = lea.readInt();
			lea.readInt();// ?
		}
		if(chr.getStats().getAttackSpeed() != ret.speed){
			chr.getStats().recalcLocalStats(chr);
			if(chr.getStats().getAttackSpeed() != ret.speed){
				// Logger.log(LogType.INFO, LogFile.ATTACK_SPEED, chr.getName() + " has speed: " + chr.getStats().getAttackSpeed() + " but client gave: " + ret.speed + " Skill: " + ret.skill + " Level: " + ret.skilllevel);
				// AutobanFactory.WZ_EDIT.log(chr, "Calculated Attack Speed: " + chr.getStats().getAttackSpeed() + " Client Attack Speed: " + ret.speed);
			}
		}
		// System.out.println("Action: " + ret.action);
		int lastSkill = chr.getAutobanManager().getLastSkill();
		int tTotalDelay = GameConstants.getAttackDelay(ret.skill, skill);
		// System.out.println("tTotalDelay: " + tTotalDelay);
		if(lastSkill == -1 || lastSkill == ret.skill){
			int v9 = 6;
			if(chr.getJob().isA(MapleJob.MAGICIAN)){
				Item wep = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -EquipSlot.WEAPON.getSlots()[0]);
				if(wep != null) v9 = ItemInformationProvider.getInstance().getItemData(wep.getItemId()).attackSpeed;
			}
			Integer booster = chr.getBuffedValue(MapleBuffStat.BOOSTER);
			if(booster == null) booster = 0;
			int v10 = get_attack_speed_degree(v9, ret.skill, booster);
			long v11 = System.currentTimeMillis();
			int tDelay = (v10 + 10) * (tTotalDelay + 6 * ret.action) / 16;
			// v12 is some class to store data for past attacks
			// v7 is the CheatInspector for the chr.
			//
			// v12 = ZList<CCheatInspector::ATTACKSPEED::TIME>::AddTail(&v7->m_attackSpeeed.lAttackTime);
			// v12->tDelay = tDelay;
			// v12->tTime = v11;
			// v13 = v7->m_attackSpeeed.lAttackTime._m_pHead;
			/*long delay = ret.lastAttackTime - chr.getAutobanManager().getLastSpam(3);
			// System.out.println("Delay: " + delay);
			double delayExpected = GameConstants.getAttackDelay(ret.skill, skill);
			int attackSpeed = 0;
			int baseAttackSpeed = 0;
			if(GameConstants.isAffectedByAttackSpeed(ret.skill)){
				attackSpeed = chr.getStats().getAttackSpeed();
				baseAttackSpeed = attackSpeed;
				if(attackSpeed < 6){
					attackSpeed = Math.abs(attackSpeed - 6);
					attackSpeed += 3;
					delayExpected -= attackSpeed;
				}else if(attackSpeed > 6){
					if(attackSpeed == 7){
						delayExpected += 60;// idfk
					}else{
						attackSpeed -= 5;
						delayExpected += attackSpeed;
					}
				}
			}
			delayExpected = (int) delayExpected;
			// System.out.println("Delay: " + delay + " Expected: " + delayExpected + " AttackSpeed: " + baseAttackSpeed);
			if(delay < delayExpected && delay >= 0){// overlow makes it negative
				AutobanFactory.FAST_ATTACK.alert(chr, "delay of " + delay + " expected " + delayExpected + " with skill " + ret.skill + " baseAttackSpeed: " + baseAttackSpeed);
			}*/
		}
		chr.getAutobanManager().spam(3, ret.lastAttackTime);
		chr.getAutobanManager().setLastSkill(ret.skill);
		// CalcDamage::PDamage
		// CalcDamage::MDamage
		double calcDmgMax;
		// https://ayumilovemaple.wordpress.com/2009/09/06/maplestory-formula-compilation/
		// #Special Damage Formulas
		//
		// Find the base damage to base further calculations on.
		// Several skills have their own formula in this section.
		if(magic && ret.skill != 0){
			calcDmgMax = ((chr.getTotalMagic() * chr.getTotalMagic() / 1000D + chr.getTotalMagic()) / 30D + chr.getTotalInt() / 200D);
			// calcDmgMax = (chr.getTotalMagic() * chr.getTotalMagic() / 1000 + chr.getTotalMagic()) / 30 + chr.getTotalInt() / 200;
		}else if(ret.skill == 4001344 || ret.skill == NightWalker.LUCKY_SEVEN || ret.skill == NightLord.TRIPLE_THROW){
			calcDmgMax = (chr.getTotalLuk() * 5) * chr.getTotalWatk() / 100;
		}else if(ret.skill == DragonKnight.DRAGON_ROAR){// weapon type 43, 44
			calcDmgMax = (chr.getTotalStr() * 4 + chr.getTotalDex()) * chr.getTotalWatk() / 100;
		}else if(ret.skill == NightLord.VENOMOUS_STAR || ret.skill == Shadower.VENOMOUS_STAB){
			calcDmgMax = (int) (18.5 * (chr.getTotalStr() + chr.getTotalLuk()) + chr.getTotalDex() * 2) / 100 * chr.calculateMaxBaseDamage(chr.getTotalWatk());
		}else{
			calcDmgMax = chr.calculateMaxBaseDamage(chr.getTotalWatk());
		}
		double originalCalcMaxDmg = calcDmgMax;
		if(ret.skill != 0){// Calculates damage from specific skills
			if(ret.skilllevel <= 0) Logger.log(LogType.ERROR, LogFile.GENERAL_ERROR, chr.getName() + " used skill " + ret.skill + " with a skill level of " + ret.skilllevel);
			MapleStatEffect effect = skill.getEffect(ret.skilllevel);
			if(magic){
				// Since the skill is magic based, use the magic formula
				if(chr.getJob() == MapleJob.IL_ARCHMAGE || chr.getJob() == MapleJob.IL_MAGE){
					int skillLvl = chr.getSkillLevel(ILMage.ELEMENT_AMPLIFICATION);
					if(skillLvl > 0){
						calcDmgMax = calcDmgMax * SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
					}
				}else if(chr.getJob() == MapleJob.FP_ARCHMAGE || chr.getJob() == MapleJob.FP_MAGE){
					int skillLvl = chr.getSkillLevel(FPMage.ELEMENT_AMPLIFICATION);
					if(skillLvl > 0){
						calcDmgMax = calcDmgMax * SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
					}
				}else if(chr.getJob() == MapleJob.BLAZEWIZARD3 || chr.getJob() == MapleJob.BLAZEWIZARD4){
					int skillLvl = chr.getSkillLevel(BlazeWizard.ELEMENT_AMPLIFICATION);
					if(skillLvl > 0){
						calcDmgMax = calcDmgMax * SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
					}
				}else if(chr.getJob() == MapleJob.EVAN7 || chr.getJob() == MapleJob.EVAN8 || chr.getJob() == MapleJob.EVAN9 || chr.getJob() == MapleJob.EVAN10){
					int skillLvl = chr.getSkillLevel(Evan.MAGIC_AMPLIFICATION);
					if(skillLvl > 0){
						calcDmgMax = calcDmgMax * SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
					}
				}
				if(effect.getMatk() != 0) calcDmgMax *= effect.getMatk();
				if(ret.skill == Cleric.HEAL){
					// This formula is still a bit wonky, but it is fairly accurate.
					calcDmgMax = (int) Math.round((chr.getTotalInt() * 4.8 + chr.getTotalLuk() * 4) * chr.getTotalMagic() / 1000);
					calcDmgMax = calcDmgMax * effect.getHp() / 100;
				}
			}else if(ret.skill == Hermit.SHADOW_MESO){
				// Shadow Meso also has its own formula
				calcDmgMax = effect.getMoneyCon() * 10;
				calcDmgMax = (int) Math.floor(calcDmgMax * 1.5);
			}else{
				// Normal damage formula for skills
				switch (ret.skill){
					case Hunter.ARROW_BOMB:
						calcDmgMax = calcDmgMax * effect.getX() / 100;
						break;
					default:
						calcDmgMax = calcDmgMax * effect.getDamage() / 100;
						break;
				}
			}
		}
		Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);
		if(comboBuff != null && comboBuff > 0){
			int oid = chr.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
			int advcomboid = chr.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
			if(comboBuff > 6){// 150%
				// Advanced Combo
				MapleStatEffect ceffect = SkillFactory.getSkill(advcomboid).getEffect(chr.getSkillLevel(advcomboid));
				calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + 0.20 + (comboBuff - 5) * 0.04);
			}else{// 120%
			      // Normal Combo
				MapleStatEffect ceffect = SkillFactory.getSkill(oid).getEffect(chr.getSkillLevel(oid));
				calcDmgMax = (int) Math.floor(calcDmgMax * (ceffect.getDamage() + 20) / 100 + Math.floor((comboBuff - 1) * (chr.getSkillLevel(oid) / 6)) / 100);
			}
			if(GameConstants.isFinisherSkill(ret.skill)){
				// Finisher skills do more damage based on how many orbs the player has.
				int orbs = comboBuff - 1;
				if(orbs == 2){
					calcDmgMax *= 1.2;
				}else if(orbs == 3){
					calcDmgMax *= 1.54;
				}else if(orbs == 4){
					calcDmgMax *= 2;
				}else if(orbs >= 5){
					calcDmgMax *= 2.5;
				}
			}
		}
		if(chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null){
			calcDmgMax *= chr.getBuffEffect(MapleBuffStat.WIND_WALK).getDamage() / 100;
		}
		if(chr.getEnergyBar() == 15000){
			int energycharge = chr.isCygnus() ? ThunderBreaker.ENERGY_CHARGE : Marauder.ENERGY_CHARGE;
			MapleStatEffect ceffect = SkillFactory.getSkill(energycharge).getEffect(chr.getSkillLevel(energycharge));
			// calcDmgMax += calcDmgMax * (ceffect.getDamage() / 100D);
			// System.out.println(calcDmgMax);
		}
		if(chr.getMapId() >= 914000000 && chr.getMapId() <= 914000500){
			calcDmgMax += 80000; // Aran Tutorial.
		}
		boolean canCrit = false;
		if(chr.getJob().isA(MapleJob.THIEF) || chr.getJob().isA(MapleJob.NIGHTWALKER1) || chr.getJob().isA(MapleJob.WINDARCHER1) || chr.getJob() == MapleJob.ARAN3 || chr.getJob() == MapleJob.ARAN4 || chr.getJob() == MapleJob.MARAUDER || chr.getJob() == MapleJob.BUCCANEER){
			canCrit = true;
		}
		boolean shadowPartner = false;
		if(chr.getBuffEffect(MapleBuffStat.SHADOWPARTNER) != null){
			shadowPartner = true;
		}
		if(ret.skill == NightWalker.VAMPIRE){
			calcDmgMax *= 1.2; // I believe vampire ups the max crit damage, but I am not positive...
			shadowPartner = false; // SP doesn't add extra lines to Vampire...
		}
		if(chr.getJob().isA(MapleJob.NIGHTWALKER1) && chr.getBuffEffect(MapleBuffStat.DARKSIGHT) != null){
			int skillLvl = chr.getSkillLevel(NightWalker.VANISH);
			if(skillLvl > 0){
				MapleStatEffect ceffect = SkillFactory.getSkill(NightWalker.VANISH).getEffect(skillLvl);
				calcDmgMax = calcDmgMax * ceffect.getDamage() / 100;
			}
		}
		if(ret.skill != 0){
			int fixed = ret.getAttackEffect(chr, SkillFactory.getSkill(ret.skill)).getFixDamage();
			if(fixed > 0){
				calcDmgMax = fixed;
			}
		}
		for(int i = 0; i < ret.numAttacked; i++){
			int oid = lea.readInt();
			/*int nHitAction = */lea.readByte();
			/*byte v35 = */lea.readByte();
			// int nForeAction = v35 & 0x7F;
			// byte bLeft = (byte) ((v35 >> 7) & 1);// facing left?
			/*byte nFrameIdx = */lea.readByte();
			/*int v36 = */lea.readByte();
			// int nCalcDamageStatIndex = v36 & 0x7F;
			// boolean doomed = ((v36 >> 7) & 1) > 0;// sick doom skill
			Point mobPosition = new Point(lea.readShort(), lea.readShort());
			Point mobPositionPrev = new Point(lea.readShort(), lea.readShort());
			ret.mobPositions.add(i, mobPosition);
			ret.mobPositionPrev.add(i, mobPositionPrev);
			if(mesoExplosion){
				lea.readByte();
			}else{
				lea.readShort();// ?
			}
			List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<>();
			MapleMonster monster = chr.getMap().getMonsterByOid(oid);
			if(chr.getBuffEffect(MapleBuffStat.WK_CHARGE) != null){
				// Charge, so now we need to check elemental effectiveness
				int sourceID = chr.getBuffSource(MapleBuffStat.WK_CHARGE);
				int level = chr.getBuffedValue(MapleBuffStat.WK_CHARGE);
				if(monster != null){
					if(sourceID == WhiteKnight.BW_FIRE_CHARGE || sourceID == WhiteKnight.SWORD_FIRE_CHARGE){
						if(monster.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.WEAK){
							calcDmgMax *= 1.05 + level * 0.015;
						}
					}else if(sourceID == WhiteKnight.BW_ICE_CHARGE || sourceID == WhiteKnight.SWORD_ICE_CHARGE){
						if(monster.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.WEAK){
							calcDmgMax *= 1.05 + level * 0.015;
						}
					}else if(sourceID == WhiteKnight.BW_LIT_CHARGE || sourceID == WhiteKnight.SWORD_LIT_CHARGE){
						if(monster.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.WEAK){
							calcDmgMax *= 1.05 + level * 0.015;
						}
					}else if(sourceID == Paladin.BW_HOLY_CHARGE || sourceID == Paladin.SWORD_HOLY_CHARGE){
						if(monster.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.WEAK){
							calcDmgMax *= 1.2 + level * 0.015;
						}
					}
				}else{
					// Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
					// take the safe approach and just assume they are weak.
					calcDmgMax *= 1.5;
				}
			}
			if(ret.skill != 0){
				if(skill.getElement() != Element.NEUTRAL && chr.getBuffedValue(MapleBuffStat.ELEMENTAL_RESET) == null){
					// The skill has an element effect, so we need to factor that in.
					if(monster != null){
						ElementalEffectiveness eff = monster.getEffectiveness(skill.getElement());
						if(eff == ElementalEffectiveness.WEAK){
							calcDmgMax *= 1.5;
						}else if(eff == ElementalEffectiveness.STRONG){
							// calcDmgMax *= 0.5;
						}
					}else{
						// Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
						// take the safe approach and just assume they are weak.
						calcDmgMax *= 1.5;
					}
				}
				if(ret.skill == FPWizard.POISON_BREATH || ret.skill == FPMage.POISON_MIST || ret.skill == FPArchMage.FIRE_DEMON || ret.skill == ILArchMage.ICE_DEMON){
					if(monster != null){
						// Turns out poison is completely server side, so I don't know why I added this. >.<
						// calcDmgMax = monster.getHp() / (70 - chr.getSkillLevel(skill));
					}
				}else if(ret.skill == Hermit.SHADOW_WEB){
					if(monster != null){
						calcDmgMax = monster.getHp() / (50 - chr.getSkillLevel(skill));
					}
				}
			}
			// long rand[] = new long[numRand];// we need save it as long to store unsigned int
			// for(int in = 0; in < numRand; in++){
			// rand[in] = chr.getCRand().random();
			// }
			Skill shadowP = SkillFactory.getSkill(chr.getJob().isA(MapleJob.NIGHTWALKER1) ? 14111000 : 4111002);
			// int originalCalcMinDmg = chr.calculateMinBaseDamage(chr.getTotalWatk());
			int shadowPLevel = chr.getSkillLevel(shadowP);
			for(int j = 0; j < ret.numDamage; j++){
				int damage = lea.readInt();
				// System.out.println("minDmg(nocalc): " + originalCalcMinDmg);
				// System.out.println("MaxDmg(nocalc): " + originalCalcMaxDmg);
				// double dmgCalcOrg = chr.getCRand().RandomInRange(rand[j % numRand], originalCalcMaxDmg, originalCalcMinDmg);
				// double pd = (monster != null && monster.getStats() != null ? monster.getStats().getPDDamage() : 0);
				// double dmgCalc = Math.max(0, 100.0 - pd) / 100.0 * dmgCalcOrg;
				// System.out.println("Calculated: " + dmgCalc + "(" + dmgCalcOrg + ")");
				double hitDmgMax = calcDmgMax;
				if(ret.skill == Buccaneer.BARRAGE){
					if(j > 3){
						hitDmgMax *= Math.pow(2, (j - 3));
					}
				}
				if(ret.skill == NightWalker.VAMPIRE){
					hitDmgMax *= 3;
				}
				if(shadowPartner && shadowPLevel > 0){
					// For shadow partner, the second half of the hits only do 50% damage. So calc that
					// in for the crit effects.
					if(j >= ret.numDamage / 2){
						double y = ret.skill == 0 ? shadowP.getEffect(shadowPLevel).getX() : shadowP.getEffect(shadowPLevel).getY();
						hitDmgMax *= y / 100;
					}
				}
				if(damage > GameConstants.MAX_DAMAGE && !chr.isGM()){
					AutobanFactory.CLIENT_EDIT.alert(chr, "Increased Damage Cap Dealt: " + damage);
					damage = GameConstants.MAX_DAMAGE;
				}
				if(ret.skill == Marksman.SNIPE){
					damage = 195000 + Randomizer.nextInt(5000);
					hitDmgMax = 200000;
				}
				double maxWithCrit = hitDmgMax;
				if(canCrit){ // They can crit, so up the max.
					maxWithCrit *= 2;// Need more strict checks for crits.
				}else if(chr.playerStat.critDamage != 0){
					maxWithCrit *= chr.playerStat.critDamage;
				}
				if(hitDmgMax > GameConstants.MAX_DAMAGE){
					hitDmgMax = GameConstants.MAX_DAMAGE;
				}
				maxWithCrit = Math.round(maxWithCrit);
				// System.out.println("CDMG: " + damage + " maxWithCrit: " + maxWithCrit + " maxDmg: " + Math.round(hitDmgMax) + " calcBase: " + originalCalcMaxDmg);
				if(damage > maxWithCrit){
					AutobanFactory.DAMAGE_HACK.alert(chr, "CDMG: " + damage + " maxWithCrit: " + maxWithCrit + " maxDmg: " + Math.round(hitDmgMax) + " calcBase: " + originalCalcMaxDmg);
					// AutobanFactory.DAMAGE_HACK.alert(chr, "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " SLevel: " + ret.skilllevel + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapData().getMapName() + " (" + chr.getMapId() + ") calcDmgMaxFinal: " + calcDmgMax + " originalCalcDmgMax: " + originalCalcMaxDmg);
				}
				// Add a ab point if its over 4x what we calculated.
				/*if(damage > maxWithCrit * 4 && maxWithCrit != 0){
					AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutobanManager(), "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
				}*/
				boolean crit = ret.skill == Marksman.SNIPE || ((canCrit || chr.playerStat.critDamage != 0) && damage > hitDmgMax);
				// if(ret.skill == Marksman.SNIPE || (canCrit && damage > hitDmgMax)){
				// If the skill is a crit, inverse the damage to make it show up on clients.
				// damage = -Integer.MAX_VALUE + damage - 1;
				// }
				allDamageNumbers.add(new Pair<Integer, Boolean>(damage, crit));
			}
			if(!mesoExplosion || ret.skill != Corsair.RAPID_FIRE || ret.skill != Aran.HIDDEN_FULL_DOUBLE || ret.skill != Aran.HIDDEN_FULL_TRIPLE || ret.skill != Aran.HIDDEN_OVER_DOUBLE || ret.skill != Aran.HIDDEN_OVER_TRIPLE){
				/*int mobCrc = */lea.readInt();
			}
			ret.allDamage.put(Integer.valueOf(oid), allDamageNumbers);
		}
		if(ret.skill == NightWalker.POISON_BOMB){ // Poison Bomb
			lea.skip(4);
			ret.position.setLocation(lea.readShort(), lea.readShort());
		}
		ret.attackerPosition.x = lea.readShort();
		ret.attackerPosition.y = lea.readShort();
		if(mesoExplosion){
			ret.mesoCount = lea.readByte();
			for(int i = 0; i < ret.mesoCount; i++){
				ret.mesos.add(lea.readInt());
				lea.readByte();
			}
			lea.readShort();// ?
		}
		return ret;
	}

	int highest = 0;

	private static int rand(int l, int u){
		return (int) ((Math.random() * (u - l + 1)) + l);
	}

	public int get_attack_speed_degree(int nDegree, int nSkillID, int nWeaponBooster){// nWeaponBooster is the nX value of the Booster buffstat active
		int v3 = nDegree;
		int result;
		if(nSkillID == Rogue.DOUBLE_STAB) v3 = nDegree - 2;
		result = nWeaponBooster + v3;
		if(result <= 2) result = 2;
		if(result >= 10) result = 10;
		return result;
	}
}
