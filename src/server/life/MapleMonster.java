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
package server.life;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import client.*;
import client.status.MonsterStatusEffect;
import constants.skills.*;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.propertybuilder.ExpProperty;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.packets.field.MobPool;
import tools.packets.field.NpcPool;

public class MapleMonster extends AbstractLoadedMapleLife{

	private MapleMonsterStats stats;
	private int hp, mp;
	private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
	private boolean controllerHasAggro, controllerKnowsAboutAggro;
	private int mobVacController;
	private EventInstanceManager eventInstance = null;
	private Collection<MonsterListener> listeners = new LinkedList<>();
	private Map<MobStat, MobStatData> mobStats = new LinkedHashMap<>();
	private ArrayList<MobStat> alreadyBuffed = new ArrayList<>();
	private Collection<AttackerEntry> attackers = new LinkedList<>();
	private MapleCharacter highestDamageChar;
	private MapleMap map;
	private int VenomMultiplier = 0;
	private boolean fake = false;
	private boolean dropsDisabled = false;
	private List<Pair<Integer, Integer>> usedSkills = new ArrayList<>();
	private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
	private boolean itemStolen;
	private int team = -1;
	private final HashMap<Integer, AtomicInteger> takenDamage = new HashMap<>();
	public boolean quickInstance = false;
	private String owner;
	public ReentrantLock monsterLock = new ReentrantLock();
	private int mobTime;// TODO: Move to MapleMonsterStats?
	private Point spawnPosition;
	private int itemEffect;
	private int parentMob;
	//
	private int poisonDamage, poisonType = -1, poisonFrom;
	private long lastPoisonDamage = -1;

	public MapleMonster(int id, MapleMonsterStats stats){
		super(id);
		initWithStats(stats);
	}

	public MapleMonster(MapleMonster monster){
		super(monster);
		initWithStats(monster.stats);
	}

	private void initWithStats(MapleMonsterStats stats){
		setStance(5);
		this.stats = stats;
		hp = stats.getHp();
		mp = stats.getMp();
	}

	public void update(){
		long updateTime = System.currentTimeMillis();
		synchronized(mobStats){
			Map<MobStat, MobStatData> remove = new HashMap<>();
			for(Entry<MobStat, MobStatData> data : mobStats.entrySet()){
				if(System.currentTimeMillis() > data.getValue().endTime){
					remove.put(data.getKey(), data.getValue());
					if(data.getKey().equals(MobStat.Poison)){
						this.setVenomMulti(0);
						poisonFrom = -1;
						poisonType = -1;
						poisonDamage = 0;
					}
				}else{
					if(updateTime - lastPoisonDamage >= 1000 && poisonType != -1){
						MapleCharacter chr = this.map.getCharacterById(poisonFrom);
						if(chr == null){
							lastPoisonDamage = System.currentTimeMillis();
							continue;
						}
						// poison damage
						int damage = poisonDamage;
						if(damage >= hp){
							damage = hp - 1;
							if(poisonType == 1 || poisonType == 2){
								map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
							}
						}
						if(hp > 1 && damage > 0){
							damage(chr, damage, false);
							if(poisonType == 1){
								map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
							}
						}
						lastPoisonDamage = System.currentTimeMillis();
					}
				}
			}
			if(!remove.isEmpty()) removeMobStats(remove);
		}
	}

	public void disableDrops(){
		this.dropsDisabled = true;
	}

	public boolean dropsDisabled(){
		return dropsDisabled;
	}

	public void setMap(MapleMap map){
		this.map = map;
	}

	public int getHp(){
		return hp;
	}

	public void setHp(int hp){
		this.hp = hp;
	}

	public int getMaxHp(){
		return stats.getHp();
	}

	public int getMp(){
		return mp;
	}

	public void setMp(int mp){
		if(mp < 0){
			mp = 0;
		}
		this.mp = mp;
	}

	public int getMaxMp(){
		return stats.getMp();
	}

	public int getExp(){
		return stats.getExp();
	}

	public int getLevel(){
		return stats.getLevel();
	}

	public int getCP(){
		return stats.getCP();
	}

	public int getTeam(){
		return team;
	}

	public void setTeam(int team){
		this.team = team;
	}

	public int getVenomMulti(){
		return this.VenomMultiplier;
	}

	public void setVenomMulti(int multiplier){
		this.VenomMultiplier = multiplier;
	}

	public MapleMonsterStats getStats(){
		return stats;
	}

	public boolean isBoss(){
		return stats.isBoss() || isHT();
	}

	public int getAnimationTime(String name){
		return stats.getAnimationTime(name);
	}

	private List<Integer> getRevives(){
		return stats.getRevives();
	}

	private byte getTagColor(){
		return stats.getTagColor();
	}

	private byte getTagBgColor(){
		return stats.getTagBgColor();
	}

	public MobSkill getMobSkill(int skillId){
		return MobSkillFactory.getMobSkill(skillId, 1);
	}

	/**
	 * @param from the player that dealt the damage
	 * @param damage
	 */
	public void damage(MapleCharacter from, int damage, boolean updateAttackTime){
		AttackerEntry attacker;
		if(from.isInParty()){
			attacker = new PartyAttackerEntry(from.getParty().getId(), from.getClient().getChannelServer());
		}else{
			attacker = new SingleAttackerEntry(from, from.getClient().getChannelServer());
		}
		boolean replaced = false;
		for(AttackerEntry aentry : attackers){
			if(aentry.equals(attacker)){
				attacker = aentry;
				replaced = true;
				break;
			}
		}
		if(!replaced){
			attackers.add(attacker);
		}
		int rDamage = Math.max(0, Math.min(damage, this.hp));
		attacker.addDamage(from, rDamage, updateAttackTime);
		this.hp -= rDamage;
		int remhppercentage = (int) Math.ceil((this.hp * 100.0) / getMaxHp());
		if(remhppercentage < 1){
			remhppercentage = 1;
		}
		long okTime = System.currentTimeMillis() - 4000;
		if(hasBossHPBar()){
			from.getMap().broadcastMessage(makeBossHPBarPacket(), getPosition());
		}else if(!isBoss()){
			for(AttackerEntry mattacker : attackers){
				for(AttackingMapleCharacter cattacker : mattacker.getAttackers()){
					if(cattacker.getAttacker().getMap() == from.getMap()){
						if(cattacker.getLastAttackTime() >= okTime){
							cattacker.getAttacker().getClient().announce(MaplePacketCreator.showMonsterHP(getObjectId(), remhppercentage));
						}
					}
				}
			}
		}
	}

	public void heal(int hp, int mp){
		int hp2Heal = getHp() + hp;
		int mp2Heal = getMp() + mp;
		if(hp2Heal >= getMaxHp()){
			hp2Heal = getMaxHp();
		}
		if(mp2Heal >= getMaxMp()){
			mp2Heal = getMaxMp();
		}
		setHp(hp2Heal);
		setMp(mp2Heal);
		getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp));
	}

	public boolean isAttackedBy(MapleCharacter chr){
		return takenDamage.containsKey(chr.getId());
	}

	public void giveExpToCharacter(MapleCharacter attacker, int exp, boolean isKiller, int numExpSharers){
		if(isKiller){
			if(eventInstance != null){
				eventInstance.monsterKilled(attacker, this);
			}
		}
		// final int partyModifier = 0;// numExpSharers > 1 ? (110 + (5 * (numExpSharers - 2))) : 0;
		int partyExp = 0;
		if(attacker.getHp() > 0){
			int personalExp = exp;
			if(exp > 0){
				/*if(partyModifier > 0){
					partyExp = (int) (personalExp * ServerConstants.PARTY_EXPERIENCE_MOD * partyModifier / 1000f);
				}*/
				if(mobStats.containsKey(MobStat.Showdown)){
					personalExp *= (mobStats.get(MobStat.Showdown).nOption / 100.0 + 1.0);
				}
			}
			if(exp < 0){// O.O ><
				personalExp = Integer.MAX_VALUE;
			}
			attacker.gainExp(new ExpProperty(ExpGainType.MONSTER).gain(personalExp).party(partyExp).show().white(isKiller));
			attacker.mobKilled(getId());
			float pExp = personalExp;
			attacker.increaseEquipExp(pExp / 1000F);// better place
		}
	}

	public MapleCharacter killBy(MapleCharacter killer){
		long totalBaseExpL = (long) (this.getExp() * killer.getClient().getPlayer().getStats().getExpRate());
		int totalBaseExp = (int) (Math.min(Integer.MAX_VALUE, totalBaseExpL));
		AttackerEntry highest = null;
		int highdamage = 0;
		for(AttackerEntry attackEntry : attackers){
			if(attackEntry.getDamage() > highdamage){
				highest = attackEntry;
				highdamage = attackEntry.getDamage();
			}
		}
		for(AttackerEntry attackEntry : attackers){
			attackEntry.killedMob(killer.getMap(), (int) Math.ceil(totalBaseExp * ((double) attackEntry.getDamage() / getMaxHp())), attackEntry == highest);
		}
		MapleCharacter controller = getController();
		if(controller != null){ // this can/should only happen when a hidden gm attacks the monster
			controller.getClient().announce(MobPool.stopControllingMonster(this.getObjectId()));
			controller.stopControllingMonster(this);
		}
		final List<Integer> toSpawn = this.getRevives();
		if(toSpawn != null){
			final MapleMap reviveMap = killer.getMap();
			if(toSpawn.contains(9300216) && reviveMap.getId() > 925000000 && reviveMap.getId() < 926000000){
				reviveMap.broadcastMessage(MaplePacketCreator.playSound("Dojang/clear"));
				reviveMap.broadcastMessage(MaplePacketCreator.showEffect("dojang/end/clear"));
			}
			Pair<Integer, String> timeMob = reviveMap.getMapData().getTimeMob();
			if(timeMob != null){
				if(toSpawn.contains(timeMob.getLeft())){
					reviveMap.broadcastMessage(MaplePacketCreator.serverNotice(6, timeMob.getRight()));
				}
				if(timeMob.getLeft() == 9300338 && (reviveMap.getId() >= 922240100 && reviveMap.getId() <= 922240119)){
					if(!reviveMap.containsNPC(9001108)){
						MapleNPC npc = MapleLifeFactory.getNPC(9001108);
						npc.setPosition(new Point(172, 9));
						npc.setCy(9);
						npc.setRx0(172 + 50);
						npc.setRx1(172 - 50);
						npc.setFh(27);
						reviveMap.addMapObject(npc);
						reviveMap.broadcastMessage(NpcPool.spawnNPC(npc));
					}else{
						reviveMap.toggleHiddenNPC(9001108);
					}
				}
			}
			for(Integer mid : toSpawn){
				final MapleMonster mob = MapleLifeFactory.getMonster(mid);
				mob.setParentMob(getObjectId());
				if(eventInstance != null){
					eventInstance.registerMonster(mob);
				}
				mob.setPosition(getPosition());
				if(dropsDisabled()){
					mob.disableDrops();
				}
				reviveMap.spawnMonster(mob, -3);
			}
		}
		if(eventInstance != null){
			eventInstance.monsterKilled(this);
		}
		for(MonsterListener listener : listeners.toArray(new MonsterListener[listeners.size()])){
			listener.monsterKilled(getAnimationTime("die1"));
		}
		MapleCharacter ret = highestDamageChar;
		highestDamageChar = null; // may not keep hard references to chars outside of PlayerStorage or MapleMap
		return ret;
	}

	public boolean isAlive(){
		return this.hp > 0;
	}

	public MapleCharacter getController(){
		return controller.get();
	}

	public void setController(MapleCharacter controller){
		this.controller = new WeakReference<>(controller);
	}

	public void switchController(MapleCharacter newController, boolean immediateAggro){
		MapleCharacter controllers = getController();
		if(controllers != null && controllers.getId() == newController.getId()) return;
		if(controllers != null){
			controllers.stopControllingMonster(this);
			controllers.getClient().announce(MobPool.stopControllingMonster(getObjectId()));
		}
		newController.controlMonster(this, immediateAggro);
		setController(newController);
		if(immediateAggro){
			setControllerHasAggro(true);
		}
		setControllerKnowsAboutAggro(false);
	}

	public void addListener(MonsterListener listener){
		listeners.add(listener);
	}

	public boolean isControllerHasAggro(){
		return fake ? false : controllerHasAggro;
	}

	public void setControllerHasAggro(boolean controllerHasAggro){
		if(fake) return;
		this.controllerHasAggro = controllerHasAggro;
	}

	public boolean isControllerKnowsAboutAggro(){
		return fake ? false : controllerKnowsAboutAggro;
	}

	public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro){
		if(fake) return;
		this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
	}

	public byte[] makeBossHPBarPacket(){
		return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
	}

	public boolean hasBossHPBar(){
		return (isBoss() && getTagColor() > 0) || isHT();
	}

	private boolean isHT(){
		return getId() == 8810018;
	}

	@Override
	public void sendSpawnData(MapleClient c){
		if(!isAlive()) return;
		if(isFake()){
			c.announce(MobPool.spawnFakeMonster(this, 0));
		}else{
			c.announce(MobPool.spawnMonster(this, false));
		}
		if(hasBossHPBar()){
			if(this.getMap().countMonster(8810026) > 0 && this.getMap().getId() == 240060200){
				this.getMap().killAllMonsters();
				return;
			}
			c.announce(makeBossHPBarPacket());
		}
	}

	@Override
	public void sendDestroyData(MapleClient client){
		client.announce(MobPool.killMonster(getObjectId(), false));
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.MONSTER;
	}

	public void setEventInstance(EventInstanceManager eventInstance){
		this.eventInstance = eventInstance;
	}

	public EventInstanceManager getEventInstance(){
		return eventInstance;
	}

	public boolean isMobile(){
		return stats.isMobile();
	}

	public ElementalEffectiveness getEffectiveness(Element e){
		if(mobStats.size() > 0 && mobStats.containsKey(MobStat.Doom)) return ElementalEffectiveness.NORMAL; // like blue snails
		return stats.getEffectiveness(e);
	}

	public boolean applyStatus(MapleCharacter from, MobStatData data, Skill skill, boolean poison, boolean venom){
		Map<MobStat, MobStatData> stats = new HashMap<>();
		stats.put(data.stat, data);
		return applyStatus(from, stats, skill, poison, venom);
	}

	public boolean applyStatus(MapleCharacter from, Map<MobStat, MobStatData> stats, Skill skill, boolean poison, boolean venom){
		switch (this.stats.getEffectiveness(skill.getElement())){
			case IMMUNE:
			case STRONG:
			case NEUTRAL:
				return false;
			case NORMAL:
			case WEAK:
				break;
			default:{
				System.out.println("Unknown elemental effectiveness: " + this.stats.getEffectiveness(skill.getElement()));
				return false;
			}
		}
		if(skill.getId() == FPMage.ELEMENT_COMPOSITION){ // fp compo
			ElementalEffectiveness effectiveness = this.stats.getEffectiveness(Element.POISON);
			if(effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) return false;
		}else if(skill.getId() == ILMage.ELEMENT_COMPOSITION){ // il compo
			ElementalEffectiveness effectiveness = this.stats.getEffectiveness(Element.ICE);
			if(effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) return false;
		}else if(skill.getId() == NightLord.VENOMOUS_STAR || skill.getId() == Shadower.VENOMOUS_STAB || skill.getId() == NightWalker.VENOM){// venom
			if(this.stats.getEffectiveness(Element.POISON) == ElementalEffectiveness.WEAK) return false;
		}
		if(poison && getHp() <= 1) return false;
		if(this.stats.isBoss()){
			if(!(stats.containsKey(MobStat.Speed) && stats.containsKey(MobStat.Ambush) && stats.containsKey(MobStat.PAD))) return false;
		}
		if(poison){
			int poisonLevel = from.getSkillLevel(skill);
			poisonDamage = Math.min(Short.MAX_VALUE, (int) Math.round(getMaxHp() / (70.0 - poisonLevel) + 0.999));
			if(isBuffed(MobStat.Poison)) stats.get(MobStat.Poison).nOption = (short) poisonDamage;
			lastPoisonDamage = System.currentTimeMillis() + 1000;
			poisonType = 0;
			poisonFrom = from.getId();
			// status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
			// status.setDamageSchedule(timerManager.register("damageTaska", new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
		}else if(venom){
			if(from.getJob() == MapleJob.NIGHTLORD || from.getJob() == MapleJob.SHADOWER || from.getJob().isA(MapleJob.NIGHTWALKER3)){
				int poisonLevel, matk, id = from.getJob().getId();
				int skilll = (id == 412 ? NightLord.VENOMOUS_STAR : (id == 422 ? Shadower.VENOMOUS_STAB : NightWalker.VENOM));
				poisonLevel = from.getSkillLevel(SkillFactory.getSkill(skilll));
				if(poisonLevel <= 0) return false;
				matk = SkillFactory.getSkill(skilll).getEffect(poisonLevel).getMatk();
				int luk = from.getLuk();
				int maxDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.2 * luk * matk));
				int minDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.1 * luk * matk));
				int gap = maxDmg - minDmg;
				if(gap == 0){
					gap = 1;
				}
				int poisonDamage = 0;
				for(int i = 0; i < getVenomMulti(); i++){
					poisonDamage += (Randomizer.nextInt(gap) + minDmg);
				}
				poisonDamage = Math.min(Short.MAX_VALUE, poisonDamage);
				if(isBuffed(MobStat.Venom)) stats.get(MobStat.Venom).nOption = (short) poisonDamage;
				if(isBuffed(MobStat.Poison)) stats.get(MobStat.Poison).nOption = (short) poisonDamage;
				lastPoisonDamage = System.currentTimeMillis() + 1000;
				poisonType = 0;
				poisonFrom = from.getId();
				// status.setValue(MonsterStatus.VENOMOUS_WEAPON, Integer.valueOf(poisonDamage));
				// status.setDamageSchedule(timerManager.register("damagetaskb", new DamageTask(poisonDamage, from, status, cancelTask, 0), 1000, 1000));
			}else return false;
		}else if(skill.getId() == Hermit.SHADOW_WEB || skill.getId() == NightWalker.SHADOW_WEB){ // Shadow Web
			poisonDamage = (int) (getMaxHp() / 50.0 + 0.999);
			if(isBuffed(MobStat.Web)) stats.get(MobStat.Web).nOption = (short) poisonDamage;
			lastPoisonDamage = System.currentTimeMillis() + 1000;
			poisonType = 1;
			poisonFrom = from.getId();
			// status.setDamageSchedule(timerManager.schedule("damagetask1", new DamageTask((int) (getMaxHp() / 50.0 + 0.999), from, status, cancelTask, 1), 3500));
		}else if(skill.getId() == 4121004 || skill.getId() == 4221004){ // Ninja Ambush
			final byte level = from.getSkillLevel(skill);
			if(isBuffed(MobStat.Ambush)) stats.get(MobStat.Ambush).nOption = (short) poisonDamage;
			poisonDamage = (int) ((from.getStr() + from.getLuk()) * (1.5 + (level * 0.05)) * (skill.getEffect(level).getDamage() / 100));
			lastPoisonDamage = System.currentTimeMillis() + 1000;
			poisonType = 2;
			poisonFrom = from.getId();
			/*if (getHp() - damage <= 1)  { make hp 1 betch
			 damage = getHp() - (getHp() - 1);
			 }*/
			// status.setValue(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(damage));
			// status.setDamageSchedule(timerManager.register("damagetaskc", new DamageTask(damage, from, status, cancelTask, 2), 1000, 1000));
		}
		for(MobStat stat : stats.keySet()){
			alreadyBuffed.add(stat);
		}
		this.registerMobStats(stats);
		// int animationTime = skill.getAnimationTime();
		// byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status, null);
		// map.broadcastMessage(packet, getPosition());
		// if(getController() != null && !getController().isMapObjectVisible(this)){
		// getController().getClient().announce(packet);
		// }
		// status.setCancelTask(timerManager.schedule("mob-canceltask", cancelTask, duration + animationTime));
		return true;
	}

	public void debuffMob(int skillid){
		// skillid is not going to be used for now until I get warrior debuff working
		MobStat[] stats = {MobStat.PAD, MobStat.PDR, MobStat.MAD, MobStat.MDR};
		for(int i = 0; i < stats.length; i++){
			if(isBuffed(stats[i])){
				MobStat stat = stats[i];
				// byte[] packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), oldEffect.getStati());
				// map.broadcastMessage(packet, getPosition());
				// if(getController() != null && !getController().isMapObjectVisible(MapleMonster.this)){
				// getController().getClient().announce(packet);
				// }
				this.removeMobStat(stat);
			}
		}
	}

	public final void dispelSkill(final MobSkill skillId){
		List<MobStat> toCancel = new ArrayList<>();
		for(Entry<MobStat, MobStatData> effects : mobStats.entrySet()){
			MobStatData mse = effects.getValue();
			if(mse.mobSkill && mse.skillLevel == skillId.getSkillId()){ // not checking for level.
				toCancel.add(effects.getKey());
			}
		}
		for(MobStat stat : toCancel){
			removeMobStat(stat);
		}
	}

	public boolean isBuffed(MobStat status){
		return this.mobStats.containsKey(status);
	}

	public void setFake(boolean fake){
		this.fake = fake;
	}

	public boolean isFake(){
		return fake;
	}

	public MapleMap getMap(){
		return map;
	}

	public List<Pair<Integer, Integer>> getSkills(){
		return stats.getSkills();
	}

	public boolean hasSkill(int skillId, int level){
		return stats.hasSkill(skillId, level);
	}

	public boolean canUseSkill(MobSkill toUse){
		if(toUse == null) return false;
		if(isBuffed(MobStat.SealSkill)) return false;
		for(Pair<Integer, Integer> skill : usedSkills){
			if(skill.getLeft() == toUse.getSkillId() && skill.getRight() == toUse.getSkillLevel()) return false;
		}
		if(toUse.getLimit() > 0){
			if(this.skillsUsed.containsKey(new Pair<>(toUse.getSkillId(), toUse.getSkillLevel()))){
				int times = this.skillsUsed.get(new Pair<>(toUse.getSkillId(), toUse.getSkillLevel()));
				if(times >= toUse.getLimit()) return false;
			}
		}
		if(toUse.getSkillId() == 200){
			Collection<MapleMapObject> mmo = getMap().getMapObjects();
			int i = 0;
			for(MapleMapObject mo : mmo){
				if(mo.getType() == MapleMapObjectType.MONSTER){
					i++;
				}
			}
			if(i > 100) return false;
		}
		return true;
	}

	public void usedSkill(final int skillId, final int level, long cooltime, long duration){
		this.usedSkills.add(new Pair<>(skillId, level));
		if(this.skillsUsed.containsKey(new Pair<>(skillId, level))){
			int times = this.skillsUsed.get(new Pair<>(skillId, level)) + 1;
			this.skillsUsed.remove(new Pair<>(skillId, level));
			this.skillsUsed.put(new Pair<>(skillId, level), times);
		}else{
			this.skillsUsed.put(new Pair<>(skillId, level), 1);
		}
		final MapleMonster mons = this;
		TimerManager tMan = TimerManager.getInstance();
		tMan.schedule("usedSkill", new Runnable(){

			@Override
			public void run(){
				mons.clearSkill(skillId, level);
			}
		}, cooltime + duration);
	}

	public synchronized void clearSkill(int skillId, int level){
		int index = -1;
		for(Pair<Integer, Integer> skill : usedSkills){
			if(skill.getLeft() == skillId && skill.getRight() == level){
				index = usedSkills.indexOf(skill);
				break;
			}
		}
		if(index != -1){
			usedSkills.remove(index);
		}
	}

	public int getNoSkills(){
		return this.stats.getNoSkills();
	}

	public boolean isFirstAttack(){
		return this.stats.isFirstAttack();
	}

	public int getBuffToGive(){
		return this.stats.getBuffToGive();
	}

	private final class DamageTask implements Runnable{

		private final int dealDamage;
		private final MapleCharacter chr;
		private final MonsterStatusEffect status;
		private final Runnable cancelTask;
		private final int type;
		private final MapleMap map;

		private DamageTask(int dealDamage, MapleCharacter chr, MonsterStatusEffect status, Runnable cancelTask, int type){
			this.dealDamage = dealDamage;
			this.chr = chr;
			this.status = status;
			this.cancelTask = cancelTask;
			this.type = type;
			this.map = chr.getMap();
		}

		@Override
		public void run(){
			int damage = dealDamage;
			if(damage >= hp){
				damage = hp - 1;
				if(type == 1 || type == 2){
					map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
					cancelTask.run();
					status.getCancelTask().cancel(false);
				}
			}
			if(hp > 1 && damage > 0){
				damage(chr, damage, false);
				if(type == 1){
					map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
				}
			}
		}
	}

	public String getName(){
		return stats.getName();
	}

	public void setItemStolen(){
		itemStolen = true;
	}

	public boolean hasItemBeenStolen(){
		return itemStolen;
	}

	public void setTempEffectiveness(Element e, ElementalEffectiveness ee, long milli){
		final Element fE = e;
		final ElementalEffectiveness fEE = stats.getEffectiveness(e);
		if(!stats.getEffectiveness(e).equals(ElementalEffectiveness.WEAK)){
			stats.setEffectiveness(e, ee);
			TimerManager.getInstance().schedule("tempEffectiveness", new Runnable(){

				@Override
				public void run(){
					stats.removeEffectiveness(fE);
					stats.setEffectiveness(fE, fEE);
				}
			}, milli);
		}
	}

	public Collection<MobStat> alreadyBuffedStats(){
		return Collections.unmodifiableCollection(alreadyBuffed);
	}

	public BanishInfo getBanish(){
		return stats.getBanishInfo();
	}

	public void setBoss(boolean boss){
		this.stats.setBoss(boss);
	}

	public int getDropPeriodTime(){
		return stats.getDropPeriod();
	}

	public int getPADamage(){
		return stats.getPADamage();
	}

	public class AttackingMapleCharacter{

		private MapleCharacter attacker;
		private long lastAttackTime;

		public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime){
			super();
			this.attacker = attacker;
			this.lastAttackTime = lastAttackTime;
		}

		public long getLastAttackTime(){
			return lastAttackTime;
		}

		public MapleCharacter getAttacker(){
			return attacker;
		}
	}

	public interface AttackerEntry{

		List<AttackingMapleCharacter> getAttackers();

		public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime);

		public int getDamage();

		public boolean contains(MapleCharacter chr);

		public void killedMob(MapleMap map, int baseExp, boolean mostDamage);
	}

	private class SingleAttackerEntry implements AttackerEntry{

		private int damage;
		private int chrid;
		private long lastAttackTime;
		private Channel cserv;

		public SingleAttackerEntry(MapleCharacter from, Channel cserv){
			this.chrid = from.getId();
			this.cserv = cserv;
		}

		@Override
		public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime){
			if(chrid == from.getId()){
				this.damage += damage;
			}else{
				throw new IllegalArgumentException("Not the attacker of this entry");
			}
			if(updateAttackTime){
				lastAttackTime = System.currentTimeMillis();
			}
		}

		@Override
		public List<AttackingMapleCharacter> getAttackers(){
			MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(chrid);
			if(chr != null){
				return Collections.singletonList(new AttackingMapleCharacter(chr, lastAttackTime));
			}else{
				return Collections.emptyList();
			}
		}

		@Override
		public boolean contains(MapleCharacter chr){
			return chrid == chr.getId();
		}

		@Override
		public int getDamage(){
			return damage;
		}

		@Override
		public void killedMob(MapleMap map, int baseExp, boolean mostDamage){
			MapleCharacter chr = map.getCharacterById(chrid);
			if(chr != null){
				giveExpToCharacter(chr, baseExp, mostDamage, 1);
			}
		}

		@Override
		public int hashCode(){
			return chrid;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			final SingleAttackerEntry other = (SingleAttackerEntry) obj;
			return chrid == other.chrid;
		}
	}

	private static class OnePartyAttacker{

		public MapleParty lastKnownParty;
		public int damage;
		public long lastAttackTime;

		public OnePartyAttacker(MapleParty lastKnownParty, int damage){
			this.lastKnownParty = lastKnownParty;
			this.damage = damage;
			this.lastAttackTime = System.currentTimeMillis();
		}
	}

	private class PartyAttackerEntry implements AttackerEntry{

		private int totDamage;
		private Map<Integer, OnePartyAttacker> attackers;
		private Channel cserv;
		private int partyid;

		public PartyAttackerEntry(int partyid, Channel cserv){
			this.partyid = partyid;
			this.cserv = cserv;
			attackers = new HashMap<>(6);
		}

		@Override
		public List<AttackingMapleCharacter> getAttackers(){
			List<AttackingMapleCharacter> ret = new ArrayList<>(attackers.size());
			for(Entry<Integer, OnePartyAttacker> entry : attackers.entrySet()){
				MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(entry.getKey());
				if(chr != null){
					ret.add(new AttackingMapleCharacter(chr, entry.getValue().lastAttackTime));
				}
			}
			return ret;
		}

		private Map<MapleCharacter, OnePartyAttacker> resolveAttackers(){
			Map<MapleCharacter, OnePartyAttacker> ret = new HashMap<>(attackers.size());
			for(Entry<Integer, OnePartyAttacker> aentry : attackers.entrySet()){
				MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(aentry.getKey());
				if(chr != null){
					ret.put(chr, aentry.getValue());
				}
			}
			return ret;
		}

		@Override
		public boolean contains(MapleCharacter chr){
			return attackers.containsKey(chr.getId());
		}

		@Override
		public int getDamage(){
			return totDamage;
		}

		@Override
		public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime){
			OnePartyAttacker oldPartyAttacker = attackers.get(from.getId());
			if(oldPartyAttacker != null){
				oldPartyAttacker.damage += damage;
				oldPartyAttacker.lastKnownParty = from.getParty();
				if(updateAttackTime){
					oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
				}
			}else{
				// TODO actually this causes wrong behaviour when the party changes between attacks
				// only the last setup will get exp - but otherwise we'd have to store the full party
				// constellation for every attack/everytime it changes, might be wanted/needed in the
				// future but not now
				OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty(), damage);
				attackers.put(from.getId(), onePartyAttacker);
				if(!updateAttackTime){
					onePartyAttacker.lastAttackTime = 0;
				}
			}
			totDamage += damage;
		}

		@Override
		public void killedMob(MapleMap map, int baseExp, boolean mostDamage){
			Map<MapleCharacter, OnePartyAttacker> attackers_ = resolveAttackers();
			MapleCharacter highest = null;
			int highestDamage = 0;
			Map<MapleCharacter, Integer> expMap = new ArrayMap<>(6);
			for(Entry<MapleCharacter, OnePartyAttacker> attacker : attackers_.entrySet()){
				MapleParty party = attacker.getValue().lastKnownParty;
				// double averagePartyLevel = 0;
				List<MapleCharacter> expApplicable = new ArrayList<>();
				for(MaplePartyCharacter partychar : party.getMembers()){
					if(attacker.getKey().getLevel() - partychar.getLevel() <= 5 || getLevel() - partychar.getLevel() <= 5){
						MapleCharacter pchr = cserv.getPlayerStorage().getCharacterByName(partychar.getName());
						if(pchr != null){
							if(pchr.isAlive() && pchr.getMap() == map){
								expApplicable.add(pchr);
								// averagePartyLevel += pchr.getLevel();
							}
						}
					}
				}
				double expBonus = 1.0;
				if(expApplicable.size() > 1){
					expBonus = 1.10 + 0.05 * expApplicable.size();
					// averagePartyLevel /= expApplicable.size();
				}
				int iDamage = attacker.getValue().damage;
				if(iDamage > highestDamage){
					highest = attacker.getKey();
					highestDamage = iDamage;
				}
				double innerBaseExp = baseExp * ((double) iDamage / totDamage);
				double expFraction = (innerBaseExp * expBonus) / (expApplicable.size() + 1);
				for(MapleCharacter expReceiver : expApplicable){
					Integer oexp = expMap.get(expReceiver);
					int iexp;
					if(oexp == null){
						iexp = 0;
					}else{
						iexp = oexp.intValue();
					}
					double expWeight = (expReceiver == attacker.getKey() ? 2.0 : 1.0);
					iexp += (int) Math.round(expFraction * expWeight);
					expMap.put(expReceiver, Integer.valueOf(iexp));
				}
			}
			for(Entry<MapleCharacter, Integer> expReceiver : expMap.entrySet()){
				giveExpToCharacter(expReceiver.getKey(), expReceiver.getValue(), mostDamage ? expReceiver.getKey() == highest : false, expMap.size());
			}
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + partyid;
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			final PartyAttackerEntry other = (PartyAttackerEntry) obj;
			if(partyid != other.partyid) return false;
			return true;
		}
	}

	/*
	 * The person to resurrect on kill.
	 */
	public void setOwner(String owner){
		this.owner = owner;
	}

	/*
	 * The person to resurrect on kill.
	 */
	public String getOwner(){
		return owner;
	}

	public int getMobTime(){
		return mobTime;
	}

	public void setMobTime(int time){
		this.mobTime = time;
	}

	public Point getSpawnPosition(){
		return spawnPosition;
	}

	public void setSpawnPosition(Point pos){
		this.spawnPosition = pos;
	}

	public int getMobVacController(){
		return mobVacController;
	}

	public void setMobVacController(int controller){
		this.mobVacController = controller;
	}

	public Collection<AttackerEntry> getAttackEntries(){
		return this.attackers;
	}

	public int getItemEffect(){
		return itemEffect;
	}

	public void setItemEffect(int itemEffect){
		this.itemEffect = itemEffect;
	}

	public int getParentMob(){
		return parentMob;
	}

	public void setParentMob(int parentMob){
		this.parentMob = parentMob;
	}

	public Map<MobStat, MobStatData> getMobStats(){
		return this.mobStats;
	}

	public void removeMobStat(MobStat stat){
		Map<MobStat, MobStatData> stats = new HashMap<>();
		stats.put(stat, mobStats.get(stat));
		removeMobStats(stats);
	}

	public void removeMobStats(Map<MobStat, MobStatData> stats){
		stats.keySet().forEach(d-> {
			mobStats.remove(d);
		});
		map.broadcastMessage(MobPool.onStatReset(this, stats));
	}

	public void registerMobStats(Map<MobStat, MobStatData> stats){
		for(Entry<MobStat, MobStatData> entry : stats.entrySet()){
			registerMobStat(entry.getKey(), entry.getValue());
		}
		if(map != null && !stats.isEmpty()){
			map.broadcastMessage(MobPool.onStatSet(this, stats));
		}
	}

	private void registerMobStat(MobStat stat, MobStatData data){
		data.setEndTime();// just incase idfk
		mobStats.put(stat, data);
		// Do rest
	}

	@Override
	public MapleMonster clone(){
		MapleMonster clone = new MapleMonster(this);
		clone.setPosition((Point) this.getPosition().clone());
		return clone;
	}
}
