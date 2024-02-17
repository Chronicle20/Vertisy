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
import java.awt.Rectangle;
import java.util.*;

import client.MapleBuffStat;
import client.MapleCharacter;
import constants.skills.Bishop;
import server.MapleStatEffect;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import server.maps.objects.MapleMist;
import tools.Randomizer;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

/**
 * @author Danny (Leifde)
 */
public class MobSkill{

	private int skillId, skillLevel, mpCon;
	private List<Integer> toSummon = new ArrayList<Integer>();
	private int spawnEffect, hp, x, y;
	private long duration, cooltime;
	private float prop;
	private Point lt, rb;
	private int limit;
	private MapleStatEffect effect;

	public void save(LittleEndianWriter lew){
		lew.writeInt(skillId);
		lew.writeInt(skillLevel);
		lew.writeInt(mpCon);
		lew.writeInt(toSummon.size());
		for(int i : toSummon){
			lew.writeInt(i);
		}
		lew.writeInt(spawnEffect);
		lew.writeInt(hp);
		lew.writeInt(x);
		lew.writeInt(y);
		lew.writeLong(duration);
		lew.writeLong(cooltime);
		lew.writeDouble(prop);
		lew.writeBoolean(lt != null);
		if(lt != null) lew.writePos(lt);
		lew.writeBoolean(rb != null);
		if(rb != null) lew.writePos(rb);
		lew.writeInt(limit);
		getEffect().save(lew);
	}

	public void load(LittleEndianAccessor lea){
		skillId = lea.readInt();
		skillLevel = lea.readInt();
		mpCon = lea.readInt();
		int size = lea.readInt();
		for(int i = 0; i < size; i++){
			toSummon.add(lea.readInt());
		}
		spawnEffect = lea.readInt();
		hp = lea.readInt();
		x = lea.readInt();
		y = lea.readInt();
		duration = lea.readLong();
		cooltime = lea.readLong();
		prop = (float) lea.readDouble();
		if(lea.readBoolean()) lt = lea.readPos();
		if(lea.readBoolean()) rb = lea.readPos();
		limit = lea.readInt();
		effect = new MapleStatEffect();
		effect.load(lea);
	}

	public MobSkill(){
		super();
	}

	public MobSkill(int skillId, int level){
		this.skillId = skillId;
		this.skillLevel = level;
	}

	public MapleStatEffect getEffect(){
		// effect = null;
		if(effect == null){
			effect = MapleStatEffect.loadDebuffEffectFromMobSkill(this);
		}
		return effect;
	}

	public void setMpCon(int mpCon){
		this.mpCon = mpCon;
	}

	public void addSummons(List<Integer> toSummon){
		for(Integer summon : toSummon){
			this.toSummon.add(summon);
		}
	}

	public void setSpawnEffect(int spawnEffect){
		this.spawnEffect = spawnEffect;
	}

	public void setHp(int hp){
		this.hp = hp;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public void setDuration(long duration){
		this.duration = duration;
	}

	public void setCoolTime(long cooltime){
		this.cooltime = cooltime;
	}

	public void setProp(float prop){
		this.prop = prop;
	}

	public void setLtRb(Point lt, Point rb){
		this.lt = lt;
		this.rb = rb;
	}

	public void setLimit(int limit){
		this.limit = limit;
	}

	public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill){
		applyEffect(player, monster, skill, true);
	}

	public void applyEffect(MapleCharacter player, MapleMonster monster, boolean skill, boolean costMana){
		MapleBuffStat disease = null;
		Map<MobStat, MobStatData> stats = new HashMap<MobStat, MobStatData>();
		switch (skillId){
			case 100:
			case 110:
			case 150:
				stats.put(MobStat.PowerUp, new MobStatData(MobStat.PowerUp, x, skillId, skillLevel, duration));
				break;
			case 101:
			case 111:
			case 151:
				stats.put(MobStat.MagicUp, new MobStatData(MobStat.MagicUp, x, skillId, skillLevel, duration));
				break;
			case 102:
			case 112:
			case 152:
				stats.put(MobStat.PGuardUp, new MobStatData(MobStat.PGuardUp, x, skillId, skillLevel, duration));
				break;
			case 103:
			case 113:
			case 153:
				stats.put(MobStat.MGuardUp, new MobStatData(MobStat.MGuardUp, x, skillId, skillLevel, duration));
				break;
			case 114:
				if(lt != null && rb != null && skill){
					List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
					final int hps = (getX() / 1000) * (int) (950 + 1050 * Math.random());
					for(MapleMapObject mons : objects){
						((MapleMonster) mons).heal(hps, getY());
					}
				}else{
					monster.heal(getX(), getY());
				}
				break;
			case 120:
				disease = MapleBuffStat.SEAL;
				break;
			case 121:
				disease = MapleBuffStat.DARKNESS;
				break;
			case 122:
				disease = MapleBuffStat.WEAKEN;
				break;
			case 123:
				disease = MapleBuffStat.STUN;
				break;
			case 124:
				disease = MapleBuffStat.CURSE;
				break;
			case 125:
				disease = MapleBuffStat.POISON;
				break;
			case 126: // Slow
				disease = MapleBuffStat.SLOW;
				break;
			case 127:
				if(lt != null && rb != null && skill){
					for(MapleCharacter character : getPlayersInRange(monster, player)){
						character.dispel();
					}
				}else{
					player.dispel();
				}
				break;
			case 128: // Seduce
				disease = MapleBuffStat.SEDUCE;
				break;
			case 129: // Banish
				if(lt != null && rb != null && skill){
					for(MapleCharacter chr : getPlayersInRange(monster, player)){
						chr.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
					}
				}else{
					player.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
				}
				break;
			case 131: // Mist
				monster.getMap().spawnMist(new MapleMist(calculateBoundingBox(monster.getPosition(), true), monster, this, x * 10), false);
				break;
			case 132:
				disease = MapleBuffStat.CONFUSE;
				break;
			case 133: // zombify
				disease = MapleBuffStat.UNDEAD;
				break;
			case 140:
				if(makeChanceResult() && !monster.isBuffed(MobStat.MImmune)){
					stats.put(MobStat.PImmune, new MobStatData(MobStat.PImmune, x, skillId, skillLevel, duration));
				}
				break;
			case 141:
				if(makeChanceResult() && !monster.isBuffed(MobStat.PImmune)){
					stats.put(MobStat.MImmune, new MobStatData(MobStat.MImmune, x, skillId, skillLevel, duration));
				}
				break;
			case 143: // Weapon Reflect
				// has x and y, dunno if they are suppose to use each.
				stats.put(MobStat.PCounter, new MobStatData(MobStat.PCounter, x, skillId, skillLevel, duration));
				stats.put(MobStat.PImmune, new MobStatData(MobStat.PImmune, x, skillId, skillLevel, duration));
				break;
			case 144: // Magic Reflect
				stats.put(MobStat.MCounter, new MobStatData(MobStat.MCounter, y, skillId, skillLevel, duration));
				stats.put(MobStat.MImmune, new MobStatData(MobStat.MImmune, y, skillId, skillLevel, duration));
				break;
			case 145: // Weapon / Magic reflect
				stats.put(MobStat.PCounter, new MobStatData(MobStat.PCounter, x, skillId, skillLevel, duration));
				stats.put(MobStat.PImmune, new MobStatData(MobStat.PImmune, x, skillId, skillLevel, duration));
				stats.put(MobStat.MCounter, new MobStatData(MobStat.MCounter, y, skillId, skillLevel, duration));
				stats.put(MobStat.MImmune, new MobStatData(MobStat.MImmune, y, skillId, skillLevel, duration));
				break;
			case 154: // accuracy up
				stats.put(MobStat.ACC, new MobStatData(MobStat.ACC, x, skillId, skillLevel, duration));
				break;
			case 155: // avoid up
				stats.put(MobStat.EVA, new MobStatData(MobStat.EVA, x, skillId, skillLevel, duration));
				break;
			case 156: // speed up
				stats.put(MobStat.Speed, new MobStatData(MobStat.Speed, x, skillId, skillLevel, duration));
				break;
			case 157:
				stats.put(MobStat.SealSkill, new MobStatData(MobStat.SealSkill, x, skillId, skillLevel, duration));
				break;
			case 200:
				if(monster.getMap().getSpawnedMonstersOnMap() < 80){
					for(Integer mobId : getSummons()){
						MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
						toSpawn.setPosition(monster.getPosition());
						int ypos, xpos;
						xpos = (int) monster.getPosition().getX();
						ypos = (int) monster.getPosition().getY();
						switch (mobId){
							case 8500003: // Pap bomb high
								toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
								ypos = -590;
								break;
							case 8500004: // Pap bomb
								xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
								if(ypos != -590){
									ypos = (int) monster.getPosition().getY();
								}
								break;
							case 8510100: // Pianus bomb
								if(Math.ceil(Math.random() * 5) == 1){
									ypos = 78;
									xpos = Randomizer.nextInt(5) + (Randomizer.nextInt(2) == 1 ? 180 : 0);
								}else{
									xpos = (int) (monster.getPosition().getX() + Randomizer.nextInt(1000) - 500);
								}
								break;
						}
						switch (monster.getMap().getId()){
							case 220080001: // Pap map
								if(xpos < -890){
									xpos = (int) (Math.ceil(Math.random() * 150) - 890);
								}else if(xpos > 230){
									xpos = (int) (230 - Math.ceil(Math.random() * 150));
								}
								break;
							case 230040420: // Pianus map
								if(xpos < -239){
									xpos = (int) (Math.ceil(Math.random() * 150) - 239);
								}else if(xpos > 371){
									xpos = (int) (371 - Math.ceil(Math.random() * 150));
								}
								break;
						}
						toSpawn.setPosition(new Point(xpos, ypos));
						if(toSpawn.getId() == 8500004){
							monster.getMap().spawnFakeMonster(toSpawn);
						}else{
							monster.getMap().spawnMonsterWithEffect(toSpawn, getSpawnEffect(), toSpawn.getPosition());
						}
					}
				}
				break;
			default:
				Logger.log(LogType.INFO, LogFile.GENERAL_INFO, null, "Unhandled mobskill: %d from monster: %d in map: %d player: %s", skillId, monster.getId(), monster.getMap().getId(), player.getName());
				break;
		}
		if(stats.size() > 0){
			if(lt != null && rb != null && skill){
				for(MapleMapObject mons : getObjectsInRange(monster, MapleMapObjectType.MONSTER)){
					((MapleMonster) mons).registerMobStats(stats);
				}
			}else{
				monster.registerMobStats(stats);
			}
		}
		if(disease != null){
			if(lt != null && rb != null && skill){
				int i = 0;
				for(MapleCharacter character : getPlayersInRange(monster, player)){
					if(character.isActiveBuffedValue(Bishop.HOLY_SHIELD)) continue;
					if(disease.equals(MapleBuffStat.SEDUCE)){
						if(i < 10){
							getEffect().applyTo(character);
							i++;
						}
					}else{
						getEffect().applyTo(character);
					}
				}
			}else{
				if(player != null && !player.isActiveBuffedValue(Bishop.HOLY_SHIELD)){
					getEffect().applyTo(player);
				}
			}
		}
		monster.usedSkill(skillId, skillLevel, cooltime, duration);
		if(costMana) monster.setMp(monster.getMp() - getMpCon());
	}

	private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player){
		List<MapleCharacter> players = new ArrayList<MapleCharacter>();
		players.add(player);
		return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), players);
	}

	public int getSkillId(){
		return skillId;
	}

	public int getSkillLevel(){
		return skillLevel;
	}

	public int getMpCon(){
		return mpCon;
	}

	public List<Integer> getSummons(){
		return Collections.unmodifiableList(toSummon);
	}

	public int getSpawnEffect(){
		return spawnEffect;
	}

	public int getHP(){
		return hp;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public long getDuration(){
		return duration;
	}

	public long getCoolTime(){
		return cooltime;
	}

	public Point getLt(){
		return lt;
	}

	public Point getRb(){
		return rb;
	}

	public int getLimit(){
		return limit;
	}

	public boolean makeChanceResult(){
		return prop == 1.0 || Math.random() < prop;
	}

	private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft){
		int multiplier = facingLeft ? 1 : -1;
		Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
		Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
		return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
	}

	private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType){
		List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
		objectTypes.add(objectType);
		return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
	}
}
