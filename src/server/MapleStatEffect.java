/*
 * nawh This file is part of the OdinMS Maple Story Server
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
package server;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.Map.Entry;

import client.*;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.player.buffs.twostate.*;
import constants.ItemConstants;
import constants.SkillConstants;
import constants.skills.*;
import net.server.PlayerBuffValueHolder;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobStat;
import server.life.MobStatData;
import server.maps.MapleMap;
import server.maps.SummonMovementType;
import server.maps.objects.*;
import tools.*;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.packets.CUserPool;
import tools.packets.CWvsContext;
import tools.packets.UserLocal;
import tools.packets.field.userpool.UserRemote;

/**
 * @author Matze
 * @author Frz
 */
public class MapleStatEffect{

	private short watk = 0, matk = 0, wdef = 0, mdef = 0, acc = 0, avoid = 0, speed = 0, jump = 0;
	private short hp = 0, mp = 0;
	private double hpR = 0, mpR = 0;
	private short mpCon = 0, hpCon = 0;
	private int duration = -1;
	private boolean overTime/*, repeatEffect*/, disease = false;// TODO:
	private int sourceid, sourceLevel = 0;
	private Integer skillLevel;
	private int moveTo = -1;
	private boolean skill;
	private List<Pair<MapleBuffStat, BuffDataHolder>> statups = new ArrayList<>();
	private Map<MobStat, MobStatData> monsterStatus = new HashMap<>();
	private int x = 0, y = 0, mobCount = 1, moneyCon = 0, cooldown = 0, morphId = 0, ghost = 0, fatigue = 0, berserk, booster;
	private double prop = 0;
	private int itemCon = 0, itemConNo = 0;
	private int damage = 100, attackCount = 1, fixdamage = -1;
	private Point lt, rb;
	private byte bulletCount = 1, bulletConsume = 0;
	private int itemupbyitem; // boost to item drop rate, sometimes higher than 1.. TODO: Why?
	private int mesoupbyitem; // boost to meso drop rate, sometimes higher than 1.. TODO: Why?
	private int prob; // probability bonus for drops (30 = 1.30x)
	public boolean itemEffect;

	public void load(LittleEndianAccessor glea){
		watk = glea.readShort();
		matk = glea.readShort();
		wdef = glea.readShort();
		mdef = glea.readShort();
		acc = glea.readShort();
		avoid = glea.readShort();
		speed = glea.readShort();
		jump = glea.readShort();
		hp = glea.readShort();
		mp = glea.readShort();
		hpR = glea.readDouble();
		mpR = glea.readDouble();
		mpCon = glea.readShort();
		hpCon = glea.readShort();
		duration = glea.readInt();
		overTime = glea.readBoolean();
		disease = glea.readBoolean();
		sourceid = glea.readInt();
		sourceLevel = glea.readInt();
		if(glea.readBoolean()) skillLevel = glea.readInt();
		moveTo = glea.readInt();
		skill = glea.readBoolean();
		int statupSize = glea.readInt();
		for(int i = 0; i < statupSize; i++){
			statups.add(new Pair<MapleBuffStat, BuffDataHolder>(MapleBuffStat.getByShift(glea.readInt()), new BuffDataHolder(glea.readInt(), glea.readInt(), glea.readInt())));
		}
		statupSize = glea.readInt();
		for(int i = 0; i < statupSize; i++){
			MobStat mobStat = MobStat.getByValue(glea.readInt());
			MobStatData data = new MobStatData();
			data.decode(glea);
			monsterStatus.put(mobStat, data);
		}
		x = glea.readInt();
		y = glea.readInt();
		mobCount = glea.readInt();
		moneyCon = glea.readInt();
		cooldown = glea.readInt();
		morphId = glea.readInt();
		ghost = glea.readInt();
		fatigue = glea.readInt();
		berserk = glea.readInt();
		booster = glea.readInt();
		prop = glea.readDouble();
		itemCon = glea.readInt();
		itemConNo = glea.readInt();
		damage = glea.readInt();
		attackCount = glea.readInt();
		fixdamage = glea.readInt();
		if(glea.readBoolean()) lt = glea.readPos();
		if(glea.readBoolean()) rb = glea.readPos();
		bulletCount = glea.readByte();
		bulletConsume = glea.readByte();
		itemupbyitem = glea.readInt();
		mesoupbyitem = glea.readInt();
		prob = glea.readInt();
		itemEffect = glea.readBoolean();
	}

	public void save(LittleEndianWriter mplew){
		mplew.writeShort(watk);
		mplew.writeShort(matk);
		mplew.writeShort(wdef);
		mplew.writeShort(mdef);
		mplew.writeShort(acc);
		mplew.writeShort(avoid);
		mplew.writeShort(speed);
		mplew.writeShort(jump);
		mplew.writeShort(hp);
		mplew.writeShort(mp);
		mplew.writeDouble(hpR);
		mplew.writeDouble(mpR);
		mplew.writeShort(mpCon);
		mplew.writeShort(hpCon);
		mplew.writeInt(duration);
		mplew.writeBoolean(overTime);
		mplew.writeBoolean(disease);
		mplew.writeInt(sourceid);
		mplew.writeInt(sourceLevel);
		mplew.writeBoolean(skillLevel != null);
		if(skillLevel != null) mplew.writeInt(skillLevel);
		mplew.writeInt(moveTo);
		mplew.writeBoolean(skill);
		mplew.writeInt(statups.size());
		for(Pair<MapleBuffStat, BuffDataHolder> pair : statups){
			mplew.writeInt(pair.left.getShift());
			mplew.writeInt(pair.right.getSourceID());
			mplew.writeInt(pair.right.getSourceLevel());
			mplew.writeInt(pair.right.getValue());
		}
		mplew.writeInt(monsterStatus.size());
		for(Entry<MobStat, MobStatData> data : monsterStatus.entrySet()){
			mplew.writeInt(data.getKey().getShift());
			data.getValue().encode(mplew);
		}
		mplew.writeInt(x);
		mplew.writeInt(y);
		mplew.writeInt(mobCount);
		mplew.writeInt(moneyCon);
		mplew.writeInt(cooldown);
		mplew.writeInt(morphId);
		mplew.writeInt(ghost);
		mplew.writeInt(fatigue);
		mplew.writeInt(berserk);
		mplew.writeInt(booster);
		mplew.writeDouble(prop);
		mplew.writeInt(itemCon);
		mplew.writeInt(itemConNo);
		mplew.writeInt(damage);
		mplew.writeInt(attackCount);
		mplew.writeInt(fixdamage);
		mplew.writeBoolean(lt != null);
		if(lt != null) mplew.writePos(lt);
		mplew.writeBoolean(rb != null);
		if(rb != null) mplew.writePos(rb);
		mplew.write(bulletCount);
		mplew.write(bulletConsume);
		mplew.writeInt(itemupbyitem);
		mplew.writeInt(mesoupbyitem);
		mplew.writeInt(prob);
		mplew.writeBoolean(itemEffect);
	}

	public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime){
		return loadFromData(source, skillid, true, false, overtime);
	}

	public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid){
		return loadFromData(source, itemid, false, true, false);
	}

	private void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, BuffDataHolder>> list, MapleBuffStat buffstat, Integer val){
		if(val != 0){
			list.add(new Pair<>(buffstat, new BuffDataHolder(getSourceId(), getSourceLevel(), val)));
		}
	}

	public static MapleStatEffect loadDebuffEffectFromMobSkill(MobSkill source){
		MapleStatEffect ret = new MapleStatEffect();
		ret.skill = true;
		ret.sourceid = source.getSkillId();
		ret.sourceLevel = source.getSkillLevel();
		ret.disease = true;
		ret.overTime = true;
		ret.x = source.getX();
		ret.y = source.getY();
		ret.duration = (int) source.getDuration();
		ret.cooldown = (int) source.getCoolTime();
		ret.rb = source.getRb();
		ret.lt = source.getLt();
		ArrayList<Pair<MapleBuffStat, BuffDataHolder>> statups = new ArrayList<>();
		switch (ret.sourceid){
			case 120:
				statups.add(new Pair<>(MapleBuffStat.SEAL, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 121:
				statups.add(new Pair<>(MapleBuffStat.DARKNESS, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 122:
				statups.add(new Pair<>(MapleBuffStat.WEAKEN, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 123:
				statups.add(new Pair<>(MapleBuffStat.STUN, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 124:
				statups.add(new Pair<>(MapleBuffStat.CURSE, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 125:
				statups.add(new Pair<>(MapleBuffStat.POISON, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 126:
				statups.add(new Pair<>(MapleBuffStat.SLOW, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 128:
				statups.add(new Pair<>(MapleBuffStat.SEDUCE, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 132:
				statups.add(new Pair<>(MapleBuffStat.CONFUSE, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
			case 133:
				statups.add(new Pair<>(MapleBuffStat.UNDEAD, new BuffDataHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
				break;
		}
		statups.trimToSize();
		ret.statups = statups;
		return ret;
	}

	private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean itemEffect, boolean overTime){
		MapleStatEffect ret = new MapleStatEffect();
		ret.duration = MapleDataTool.getIntConvert("time", source, -1);
		ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
		ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
		ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
		ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
		ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
		ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
		int iprop = MapleDataTool.getInt("prop", source, 100);
		ret.prop = iprop / 100.0;
		ret.mobCount = MapleDataTool.getInt("mobCount", source, 1);
		ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
		ret.morphId = MapleDataTool.getInt("morph", source, 0);
		ret.ghost = MapleDataTool.getInt("ghost", source, 0);
		ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0);
		// ret.repeatEffect = MapleDataTool.getInt("repeatEffect", source, 0) > 0;
		ret.sourceid = sourceid;
		ret.skillLevel = ObjectParser.isInt(source.getName());
		if(ret.skillLevel == null) ret.skillLevel = 0;
		ret.skill = skill;
		ret.itemEffect = itemEffect;
		if(!ret.skill && ret.duration > -1){
			ret.overTime = true;
		}else{
			ret.duration *= 1000; // items have their times stored in ms, of course
			ret.overTime = overTime;
		}
		ArrayList<Pair<MapleBuffStat, BuffDataHolder>> statups = new ArrayList<>();
		ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
		ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
		ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
		ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
		ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
		ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
		ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
		ret.jump = (short) MapleDataTool.getInt("jump", source, 0);
		ret.berserk = MapleDataTool.getInt("berserk", source, 0);
		ret.booster = MapleDataTool.getInt("booster", source, 0);
		ret.mesoupbyitem = MapleDataTool.getInt("mesoupbyitem", source, 0);
		ret.itemupbyitem = MapleDataTool.getInt("itemupbyitem", source, 0);
		if(ret.overTime && ret.getSummonMovementType() == null){
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BERSERK_FURY, ret.berserk);
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BOOSTER, ret.booster);
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MESO_UP_BY_ITEM, ret.mesoupbyitem);
			ret.addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ITEM_UP_BY_ITEM, ret.itemupbyitem);
		}
		MapleData ltd = source.getChildByPath("lt");
		if(ltd != null){
			ret.lt = (Point) ltd.getData();
			ret.rb = (Point) source.getChildByPath("rb").getData();
		}
		ret.x = MapleDataTool.getInt("x", source, 0);
		ret.y = MapleDataTool.getInt("y", source, 0);
		ret.damage = MapleDataTool.getIntConvert("damage", source, 100);
		ret.fixdamage = MapleDataTool.getIntConvert("fixdamage", source, -1);
		ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1);
		ret.bulletCount = (byte) MapleDataTool.getIntConvert("bulletCount", source, 1);
		ret.bulletConsume = (byte) MapleDataTool.getIntConvert("bulletConsume", source, 0);
		ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
		ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
		ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
		ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);
		ret.prob = MapleDataTool.getInt("prob", source);
		Map<MobStat, MobStatData> monsterStatus = new ArrayMap<>();
		if(skill){
			switch (sourceid){
				// BEGINNER
				case Beginner.RECOVERY:
				case Noblesse.RECOVERY:
				case Legend.RECOVERY:
				case Evan.RECOVERY:
					statups.add(new Pair<>(MapleBuffStat.RECOVERY, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Beginner.ECHO_OF_HERO:
				case Noblesse.ECHO_OF_HERO:
				case Legend.ECHO_OF_HERO:
				case Evan.ECHO_OF_HERO:
					statups.add(new Pair<>(MapleBuffStat.ECHO_OF_HERO, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Beginner.MONSTER_RIDER:
				case Noblesse.MONSTER_RIDER:
				case Legend.MONSTER_RIDER:
				case Evan.MONSTER_RIDER:
				case Corsair.BATTLE_SHIP:
				case Beginner.SPACESHIP:
				case Noblesse.SPACESHIP:
				case Beginner.YETI_MOUNT1:
				case Beginner.YETI_MOUNT2:
				case Noblesse.YETI_MOUNT1:
				case Noblesse.YETI_MOUNT2:
				case Legend.YETI_MOUNT1:
				case Legend.YETI_MOUNT2:
				case Beginner.WITCH_BROOMSTICK:
				case Noblesse.WITCH_BROOMSTICK:
				case Legend.WITCH_BROOMSTICK:
				case Beginner.BALROG_MOUNT:
				case Noblesse.BALROG_MOUNT:
				case Legend.BALROG_MOUNT:
					statups.add(new Pair<>(MapleBuffStat.MONSTER_RIDING, new BuffDataHolder(sourceid, ret.skillLevel, sourceid)));
					break;
				case Beginner.BERSERK_FURY:
				case Noblesse.BERSERK_FURY:
				case Evan.BERSERK_FURY:
					statups.add(new Pair<>(MapleBuffStat.BERSERK_FURY, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				case Beginner.INVINCIBLE_BARRIER:
				case Noblesse.INVINCIBLE_BARRIER:
				case Legend.INVICIBLE_BARRIER:
				case Evan.INVINCIBLE_BARRIER:
					statups.add(new Pair<>(MapleBuffStat.DIVINE_BODY, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				case Fighter.POWER_GUARD:
				case Page.POWER_GUARD:
					statups.add(new Pair<>(MapleBuffStat.POWERGUARD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Spearman.HYPER_BODY:
				case GM.HYPER_BODY:
				case SuperGM.HYPER_BODY:
					statups.add(new Pair<>(MapleBuffStat.HYPERBODYHP, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					statups.add(new Pair<>(MapleBuffStat.HYPERBODYMP, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
					break;
				case Crusader.COMBO:
				case DawnWarrior.COMBO:
					statups.add(new Pair<>(MapleBuffStat.COMBO, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				case WhiteKnight.BW_FIRE_CHARGE:
				case WhiteKnight.BW_ICE_CHARGE:
				case WhiteKnight.BW_LIT_CHARGE:
				case WhiteKnight.SWORD_FIRE_CHARGE:
				case WhiteKnight.SWORD_ICE_CHARGE:
				case WhiteKnight.SWORD_LIT_CHARGE:
				case Paladin.BW_HOLY_CHARGE:
				case Paladin.SWORD_HOLY_CHARGE:
				case DawnWarrior.SOUL_CHARGE:
				case ThunderBreaker.LIGHTNING_CHARGE:
					statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case DragonKnight.DRAGON_BLOOD:
					statups.add(new Pair<>(MapleBuffStat.DRAGONBLOOD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case DragonKnight.DRAGON_ROAR:
					ret.hpR = -ret.x / 100.0;
					break;
				case Hero.STANCE:
				case Paladin.STANCE:
				case DarkKnight.STANCE:
				case Aran.FREEZE_STANDING:
					statups.add(new Pair<>(MapleBuffStat.STANCE, new BuffDataHolder(sourceid, ret.skillLevel, iprop)));
					break;
				case DawnWarrior.FINAL_ATTACK:
					statups.add(new Pair<>(MapleBuffStat.SOUL_MASTER_FINAL, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case WindArcher.FINAL_ATTACK:
					statups.add(new Pair<>(MapleBuffStat.WIND_BREAKER_FINAL, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				// MAGICIAN
				case Magician.MAGIC_GUARD:
				case BlazeWizard.MAGIC_GUARD:
				case Evan.MAGIC_GUARD:
					statups.add(new Pair<>(MapleBuffStat.MAGIC_GUARD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Cleric.INVINCIBLE:
					statups.add(new Pair<>(MapleBuffStat.INVINCIBLE, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Priest.HOLY_SYMBOL:
				case SuperGM.HOLY_SYMBOL:
					statups.add(new Pair<>(MapleBuffStat.HOLY_SYMBOL, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case FPArchMage.INFINITY:
				case ILArchMage.INFINITY:
				case Bishop.INFINITY:
					statups.add(new Pair<>(MapleBuffStat.INFINITY, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case FPArchMage.MANA_REFLECTION:
				case ILArchMage.MANA_REFLECTION:
				case Bishop.MANA_REFLECTION:
					statups.add(new Pair<>(MapleBuffStat.MANA_REFLECTION, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				case Bishop.HOLY_SHIELD:
					statups.add(new Pair<>(MapleBuffStat.HOLY_SHIELD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case BlazeWizard.ELEMENTAL_RESET:
					statups.add(new Pair<>(MapleBuffStat.ELEMENTAL_RESET, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Evan.ELEMENTAL_RESET:
					statups.add(new Pair<>(MapleBuffStat.ELEMENTAL_RESET, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Evan.MAGIC_SHIELD:
					statups.add(new Pair<>(MapleBuffStat.MAGIC_SHIELD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Evan.MAGIC_RESISTANCE:
					statups.add(new Pair<>(MapleBuffStat.MAGIC_RESIST, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				// case Evan.BLESSING_OF_THE_ONYX:
				// break;
				case Evan.SLOW:
					statups.add(new Pair<>(MapleBuffStat.EVAN_SLOW, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					// BOWMAN
				case Priest.MYSTIC_DOOR:
				case Hunter.SOUL_ARROW:
				case Crossbowman.SOUL_ARROW:
				case WindArcher.SOUL_ARROW:
					statups.add(new Pair<>(MapleBuffStat.SOULARROW, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Ranger.PUPPET:
				case Sniper.PUPPET:
				case WindArcher.PUPPET:
				case Outlaw.OCTOPUS:
				case Corsair.WRATH_OF_THE_OCTOPI:
					statups.add(new Pair<>(MapleBuffStat.PUPPET, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				case Bowmaster.CONCENTRATE:
					statups.add(new Pair<>(MapleBuffStat.CONCENTRATE, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Bowmaster.HAMSTRING:
					statups.add(new Pair<>(MapleBuffStat.HAMSTRING, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					monsterStatus.put(MobStat.Speed, new MobStatData(MobStat.Speed, ret.x, sourceid, ret.duration));
					break;
				case Marksman.BLIND:
					statups.add(new Pair<>(MapleBuffStat.BLIND, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					monsterStatus.put(MobStat.ACC, new MobStatData(MobStat.ACC, ret.x, sourceid, ret.duration));
					break;
				case Bowmaster.SHARP_EYES:
				case Marksman.SHARP_EYES:
					statups.add(new Pair<>(MapleBuffStat.SHARP_EYES, new BuffDataHolder(sourceid, ret.skillLevel, (ret.x << 8 | ret.y))));
					break;
				case WindArcher.WIND_WALK:
					statups.add(new Pair<>(MapleBuffStat.WIND_WALK, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				// THIEF
				case Rogue.DARK_SIGHT:
				case NightWalker.DARK_SIGHT:
					statups.add(new Pair<>(MapleBuffStat.DARKSIGHT, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Hermit.MESO_UP:
					statups.add(new Pair<>(MapleBuffStat.MESOUP, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Hermit.SHADOW_PARTNER:
				case NightWalker.SHADOW_PARTNER:
					statups.add(new Pair<>(MapleBuffStat.SHADOWPARTNER, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case ChiefBandit.MESO_GUARD:
					statups.add(new Pair<>(MapleBuffStat.MESOGUARD, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case ChiefBandit.PICKPOCKET:
					statups.add(new Pair<>(MapleBuffStat.PICKPOCKET, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case NightLord.SHADOW_STARS:
					statups.add(new Pair<>(MapleBuffStat.SHADOW_CLAW, new BuffDataHolder(sourceid, ret.skillLevel, 0)));
					break;
				case BladeLord.MIRROR_IMAGE:
					statups.add(new Pair<>(MapleBuffStat.MirrorImaging, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				// PIRATE
				case Pirate.DASH:
				case ThunderBreaker.DASH:
				case Beginner.SPACE_DASH:
				case Noblesse.SPACE_DASH:
					statups.add(new Pair<>(MapleBuffStat.DASH_SPEED, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					statups.add(new Pair<>(MapleBuffStat.DASH_JUMP, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
					break;
				case Corsair.SPEED_INFUSION:
				case Buccaneer.SPEED_INFUSION:
				case ThunderBreaker.SPEED_INFUSION:
					statups.add(new Pair<>(MapleBuffStat.SPEED_INFUSION, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Outlaw.HOMING_BEACON:
				case Corsair.BULLSEYE:
					statups.add(new Pair<>(MapleBuffStat.HOMING_BEACON, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case ThunderBreaker.SPARK:
					statups.add(new Pair<>(MapleBuffStat.SPARK, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case BladeSpecialist.TORNADO_SPIN:
					statups.add(new Pair<>(MapleBuffStat.DASH_SPEED, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					statups.add(new Pair<>(MapleBuffStat.DASH_JUMP, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
					break;
				case BladeLord.OWL_SPIRIT:
					statups.add(new Pair<>(MapleBuffStat.SuddenDeath, new BuffDataHolder(sourceid, ret.x, ret.y)));
					break;
				case BladeMaster.THORNS:
					statups.add(new Pair<>(MapleBuffStat.ThornsEffect, new BuffDataHolder(sourceid, ret.skillLevel, (ret.x << 8 | ret.y))));
					break;
				case BladeMaster.FINAL_CUT:
					ret.hpR = -ret.x / 100.0;
					statups.add(new Pair<>(MapleBuffStat.FinalCut, new BuffDataHolder(sourceid, ret.skillLevel, ret.y)));
					break;
				case BattleMage.DARK_AURA:
					statups.add(new Pair<>(MapleBuffStat.DarkAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case BattleMage.BLUE_AURA:
					statups.add(new Pair<>(MapleBuffStat.BlueAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case BattleMage.YELLOW_AURA:
					statups.add(new Pair<>(MapleBuffStat.YellowAura, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				// MULTIPLE
				case Aran.POLEARM_BOOSTER:
				case Fighter.AXE_BOOSTER:
				case Fighter.SWORD_BOOSTER:
				case Page.BW_BOOSTER:
				case Page.SWORD_BOOSTER:
				case Spearman.POLEARM_BOOSTER:
				case Spearman.SPEAR_BOOSTER:
				case Hunter.BOW_BOOSTER:
				case Crossbowman.CROSSBOW_BOOSTER:
				case Assassin.CLAW_BOOSTER:
				case Bandit.DAGGER_BOOSTER:
				case FPMage.SPELL_BOOSTER:
				case ILMage.SPELL_BOOSTER:
				case Brawler.KNUCKLER_BOOSTER:
				case Gunslinger.GUN_BOOSTER:
				case DawnWarrior.SWORD_BOOSTER:
				case BlazeWizard.SPELL_BOOSTER:
				case WindArcher.BOW_BOOSTER:
				case NightWalker.CLAW_BOOSTER:
				case ThunderBreaker.KNUCKLER_BOOSTER:
				case Evan.MAGIC_BOOSTER:
				case BladeRecruit.KATARA_BOOSTER:
				case BattleMage.STAFF_BOOST:
					statups.add(new Pair<>(MapleBuffStat.BOOSTER, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Hero.MAPLE_WARRIOR:
				case Paladin.MAPLE_WARRIOR:
				case DarkKnight.MAPLE_WARRIOR:
				case FPArchMage.MAPLE_WARRIOR:
				case ILArchMage.MAPLE_WARRIOR:
				case Bishop.MAPLE_WARRIOR:
				case Bowmaster.MAPLE_WARRIOR:
				case Marksman.MAPLE_WARRIOR:
				case NightLord.MAPLE_WARRIOR:
				case Shadower.MAPLE_WARRIOR:
				case Corsair.MAPLE_WARRIOR:
				case Buccaneer.MAPLE_WARRIOR:
				case Aran.MAPLE_WARRIOR:
				case Evan.MAPLE_WARRIOR:
					statups.add(new Pair<>(MapleBuffStat.MAPLE_WARRIOR, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				// SUMMON
				case Ranger.SILVER_HAWK:
				case Sniper.GOLDEN_EAGLE:
					statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					monsterStatus.put(MobStat.Stun, new MobStatData(MobStat.Stun, 1, sourceid, ret.duration));
					break;
				case FPArchMage.ELQUINES:
				case Marksman.FROST_PREY:
					statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
					break;
				case Priest.SUMMON_DRAGON:
				case Bowmaster.PHOENIX:
				case ILArchMage.IFRIT:
				case Bishop.BAHAMUT:
				case DarkKnight.BEHOLDER:
				case Outlaw.GAVIOTA:
				case DawnWarrior.SOUL:
				case BlazeWizard.FLAME:
				case WindArcher.STORM:
				case NightWalker.DARKNESS:
				case ThunderBreaker.LIGHTNING:
				case BlazeWizard.IFRIT:
				case BladeMaster.MIRRORED_TARGET:
					statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffDataHolder(sourceid, ret.skillLevel, 1)));
					break;
				// ----------------------------- MONSTER STATUS ---------------------------------- //
				case Crusader.ARMOR_CRASH:
				case DragonKnight.POWER_CRASH:
				case WhiteKnight.MAGIC_CRASH:
					monsterStatus.put(MobStat.SealSkill, new MobStatData(MobStat.SealSkill, 1, sourceid, ret.duration));
					break;
				case Rogue.DISORDER:
					monsterStatus.put(MobStat.PAD, new MobStatData(MobStat.PAD, ret.x, sourceid, ret.duration));
					monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.PDR, ret.y, sourceid, ret.duration));
					break;
				case Corsair.HYPNOTIZE:
					monsterStatus.put(MobStat.Dazzle, new MobStatData(MobStat.Dazzle, 1, sourceid, ret.duration));
					break;
				case NightLord.NINJA_AMBUSH:
				case Shadower.NINJA_AMBUSH:
					monsterStatus.put(MobStat.Ambush, new MobStatData(MobStat.PDR, ret.damage, sourceid, ret.duration));
					break;
				case Page.THREATEN:
					monsterStatus.put(MobStat.PAD, new MobStatData(MobStat.PAD, ret.x, sourceid, ret.duration));
					monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.PDR, ret.y, sourceid, ret.duration));
					break;
				case Crusader.AXE_COMA:
				case Crusader.SWORD_COMA:
				case Crusader.SHOUT:
				case WhiteKnight.CHARGE_BLOW:
				case Hunter.ARROW_BOMB:
				case ChiefBandit.ASSAULTER:
				case Shadower.BOOMERANG_STEP:
				case Brawler.BACK_SPIN_BLOW:
				case Brawler.DOUBLE_UPPERCUT:
				case Marauder.ENERGY_BLAST:
				case ThunderBreaker.ENERGY_BLAST:
				case Buccaneer.DEMOLITION:
				case Buccaneer.SNATCH:
				case Buccaneer.BARRAGE:
				case Gunslinger.BLANK_SHOT:
				case DawnWarrior.COMA:
				case Aran.ROLLING_SPIN:
				case Evan.FIRE_BREATH:
				case Evan.BLAZE:
				case BladeLord.FLYING_ASSAULTER:
					monsterStatus.put(MobStat.Stun, new MobStatData(MobStat.Stun, 1, sourceid, ret.duration));
					break;
				case NightLord.TAUNT:
				case Shadower.TAUNT:
					monsterStatus.put(MobStat.Darkness, new MobStatData(MobStat.Darkness, ret.x, sourceid, ret.duration));
					monsterStatus.put(MobStat.MDR, new MobStatData(MobStat.Poison, ret.x, sourceid, ret.duration));
					monsterStatus.put(MobStat.PDR, new MobStatData(MobStat.Poison, ret.x, sourceid, ret.duration));
					break;
				case ILWizard.COLD_BEAM:
				case ILMage.ICE_STRIKE:
				case ILArchMage.BLIZZARD:
				case ILMage.ELEMENT_COMPOSITION:
				case Sniper.BLIZZARD:
				case Outlaw.ICE_SPLITTER:
				case FPArchMage.PARALYZE:
				case Aran.COMBO_TEMPEST:
				case Evan.ICE_BREATH:
					ret.duration *= 2; // freezing skills are a little strange
					monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
					break;
				case FPWizard.SLOW:
				case ILWizard.SLOW:
				case BlazeWizard.SLOW:
					monsterStatus.put(MobStat.Speed, new MobStatData(MobStat.Speed, ret.x, sourceid, ret.duration));
					break;
				case FPWizard.POISON_BREATH:
				case FPMage.ELEMENT_COMPOSITION:
					monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
					break;
				case Priest.DOOM:
					monsterStatus.put(MobStat.Doom, new MobStatData(MobStat.Doom, 1, sourceid, ret.duration));
					break;
				case ILMage.SEAL:
				case FPMage.SEAL:
					monsterStatus.put(MobStat.Seal, new MobStatData(MobStat.Seal, 1, sourceid, ret.duration));
					break;
				case Hermit.SHADOW_WEB: // shadow web
				case NightWalker.SHADOW_WEB:
					monsterStatus.put(MobStat.Web, new MobStatData(MobStat.Web, 1, sourceid, ret.duration));
					break;
				case FPArchMage.FIRE_DEMON:
				case ILArchMage.ICE_DEMON:
					monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
					monsterStatus.put(MobStat.Freeze, new MobStatData(MobStat.Freeze, 1, sourceid, ret.duration));
					break;
				case Evan.PHANTOM_IMPRINT:
					// monsterStatus.put(MonsterStatus.PHANTOM_IMPRINT, ret.x);
					// monsterStatus.put(MobStat.Poison, new MobStatData(MobStat.Poison, 1, sourceid, ret.duration));
					// ARAN
				case Aran.COMBO_ABILITY:
					statups.add(new Pair<>(MapleBuffStat.ARAN_COMBO, new BuffDataHolder(sourceid, ret.skillLevel, 100)));
					break;
				case Aran.COMBO_BARRIER:
					statups.add(new Pair<>(MapleBuffStat.COMBO_BARRIER, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Aran.COMBO_DRAIN:
					statups.add(new Pair<>(MapleBuffStat.COMBO_DRAIN, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Aran.SMART_KNOCKBACK:
					statups.add(new Pair<>(MapleBuffStat.SMART_KNOCKBACK, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Aran.BODY_PRESSURE:
					statups.add(new Pair<>(MapleBuffStat.BODY_PRESSURE, new BuffDataHolder(sourceid, ret.skillLevel, ret.x)));
					break;
				case Aran.SNOW_CHARGE:
					statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, new BuffDataHolder(sourceid, ret.skillLevel, ret.duration)));
					break;
				default:
					break;
			}
		}
		if(ret.isMorph()){
			statups.add(new Pair<>(MapleBuffStat.MORPH, new BuffDataHolder(sourceid, ret.skillLevel, ret.getMorph())));
		}
		if(ret.ghost > 0 && !skill){
			statups.add(new Pair<>(MapleBuffStat.GHOST_MORPH, new BuffDataHolder(sourceid, ret.skillLevel, ret.ghost)));
		}
		ret.monsterStatus = monsterStatus;
		statups.trimToSize();
		ret.statups = statups;
		return ret;
	}

	/**
	 * @param applyto
	 * @param obj
	 * @param attack damage done by the skill
	 */
	public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack){
		if(makeChanceResult()){
			switch (sourceid){ // MP eater
				case FPWizard.MP_EATER:
				case ILWizard.MP_EATER:
				case Cleric.MP_EATER:
					if(obj == null || obj.getType() != MapleMapObjectType.MONSTER) return;
					MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
					if(!mob.isBoss()){
						int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
						if(absorbMp > 0){
							mob.setMp(mob.getMp() - absorbMp);
							applyto.addMP(absorbMp);
							applyto.getClient().announce(UserLocal.UserEffect.showOwnBuffEffect(sourceid, 1));
							applyto.getMap().broadcastMessage(applyto, UserRemote.UserEffect.showBuffeffect(applyto.getId(), sourceid, 1), false);
						}
					}
					break;
			}
		}
	}

	public boolean applyTo(MapleCharacter chr){
		return applyTo(chr, chr, true, null);
	}

	public boolean applyTo(MapleCharacter chr, Point pos){
		return applyTo(chr, chr, true, pos);
	}

	public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean firstApply, Point pos){
		return applyTo(applyfrom, applyto, firstApply, pos, duration, true);
	}

	/**
	 * @param firstApply If it's the first time applying it. false if you are reapplying it on cc, etc.
	 */
	public boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean firstApply, Point pos, long duration, boolean eliteEnhance){
		if(skill && (sourceid == GM.HIDE || sourceid == SuperGM.HIDE)){
			applyto.toggleHide(false);
			return true;
		}
		int hpchange = calcHPChange(applyfrom, firstApply);
		int mpchange = calcMPChange(applyfrom, firstApply);
		if(!skill){
			// hp is normal pots, hpR is %.-
			if((hp > 0 || hpR > 0.0) && hpchange > 0){
				int actualAmount = 0;
				if(applyto.getHp() + hpchange > applyto.getMaxHp()){
					actualAmount = applyto.getMaxHp() - applyto.getHp();
				}else{
					actualAmount = hpchange;
				}
				if(actualAmount > 0){
					applyto.gainRSSkillExp(RSSkill.Health, (long) (actualAmount * 0.05));
				}
			}
			if((mp > 0 || mpR > 0.0) && mpchange > 0){
				int actualAmount = 0;
				if(applyto.getMp() + mpchange > applyto.getMaxMp()){
					actualAmount = applyto.getMaxMp() - applyto.getMp();
				}else{
					actualAmount = mpchange;
				}
				if(actualAmount > 0) applyto.gainRSSkillExp(RSSkill.Mana, (long) (actualAmount * 0.05));
			}
		}
		if(firstApply){
			if(itemConNo != 0){
				MapleInventoryManipulator.removeById(applyto.getClient(), ItemInformationProvider.getInstance().getInventoryType(itemCon), itemCon, itemConNo, true, false);
			}
		}
		List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<>(2);
		if(!firstApply && isResurrection()){
			hpchange = applyto.getMaxHp();
			if(sourceid == Evan.SOUL_STONE){
				double hp = hpchange;
				hp *= this.getX() / 100D;
				hpchange = (int) Math.round(hp);
			}
			applyto.setStance(0);
			applyto.getMap().broadcastMessage(applyto, CUserPool.removePlayerFromMap(applyto.getId()), false);
			applyto.getMap().broadcastMessage(applyto, CUserPool.spawnPlayerMapobject(applyto), false);
		}
		if(isDispel() && makeChanceResult()){
			applyto.dispelDebuffs();
		}else if(isHeroWill()){
			// applyto.cancelBuffStats(MapleBuffStat.SEDUCE);
			applyto.cancelAllDebuffs();
		}
		if(isComboReset()){
			applyto.setCombo((short) 0);
		}
		/*if (applyfrom.getMp() < getMpCon()) {
		 AutobanFactory.MPCON.addPoint(applyfrom.getAutobanManager(), "mpCon hack for skill:" + sourceid + "; Player MP: " + applyto.getMp() + " MP Needed: " + getMpCon());
		 } */
		if(hpchange != 0){
			if(hpchange < 0 && (-hpchange) > applyto.getHp()) return false;
			int newHp = applyto.getHp() + hpchange;
			if(newHp < 1){
				newHp = 1;
			}
			applyto.setHp(newHp);
			hpmpupdate.add(new Pair<>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
		}
		int newMp = applyto.getMp() + mpchange;
		if(mpchange != 0){
			if(mpchange < 0 && -mpchange > applyto.getMp()) return false;
			applyto.setMp(newMp);
			hpmpupdate.add(new Pair<>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
		}
		applyto.getClient().announce(CWvsContext.updatePlayerStats(hpmpupdate, true, applyto));
		if(moveTo != -1){
			if(moveTo != applyto.getMapId()){
				MapleMap target;
				if(moveTo == 999999999){
					target = applyto.getMap().getReturnMap();
				}else{
					target = applyto.getClient().getChannelServer().getMap(moveTo);
					int targetid = target.getId() / 10000000;
					if(targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20 && targetid != 12 && (applyto.getMapId() / 10000000 != 10 && applyto.getMapId() / 10000000 != 12)) return false;
				}
				applyto.changeMap(target);
			}else{
				return false;
			}
		}
		if(isShadowClaw()){
			int projectile = 0;
			MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
			for(int i = 1; i <= use.getSlotLimit(); i++){ // impose order...
				Item item = use.getItem((short) i);
				if(item != null){
					if(ItemConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200){
						projectile = item.getItemId();
						break;
					}
				}
			}
			if(projectile == 0){
				return false;
			}else{
				MapleInventoryManipulator.removeStarById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, false);
			}
		}
		SummonMovementType summonMovementType = getSummonMovementType();
		if(overTime || isCygnusFA() || summonMovementType != null || sourceid == BladeMaster.FINAL_CUT){// find how to properly implement final cut buff?
			applyBuffEffect(applyfrom, applyto, (int) duration, System.currentTimeMillis(), firstApply, eliteEnhance);
		}
		if(firstApply && (overTime || isHeal())){
			applyPartyBuff(applyfrom);
		}
		if(firstApply && isMonsterBuff()){
			applyMonsterBuff(applyfrom);
		}
		if(this.getFatigue() != 0){
			applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
		}
		if(summonMovementType != null && pos != null){
			final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
			applyfrom.getMap().spawnSummon(tosummon);
			applyfrom.addSummon(sourceid, tosummon);
			tosummon.addHP(x);
			if(isBeholder()){
				tosummon.addHP(1);
			}
		}
		if(isMagicDoor() /*&& !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())*/){ // Magic Door
			int y = applyto.getFh() - 30;
			if(y == 0) y = applyto.getPosition().y - 10;
			Point doorPosition = new Point(applyto.getPosition().x, y);
			Point below = applyto.getMap().getGroundBelow(doorPosition);
			if(below != null){
				doorPosition = below;
			}else{
				y = applyto.getFh();
				if(y == 0) y = applyto.getPosition().y;
				doorPosition = new Point(applyto.getPosition().x, y);
			}
			MapleDoor door = new MapleDoor(applyto, sourceid, doorPosition);
			applyto.addDoor(door);
			applyto.getMap().spawnDoor(door);
			door = new MapleDoor(door); // The town door
			applyto.addDoor(door);
			door.getTown().spawnDoor(door);
			applyto.disableDoor();
		}else if(isMist()){
			Rectangle bounds = calculateBoundingBox(sourceid == NightWalker.POISON_BOMB ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
			MapleMist mist = new MapleMist(bounds, applyfrom, this);
			applyfrom.getMap().spawnMist(mist, false);
		}else if(isTimeLeap()){
			applyto.removeAllCooldownsExcept(Buccaneer.TIME_LEAP, true);
		}
		if(duration != -1 && ((sourceid >= 2022125 && sourceid <= 2022130) || sourceid == DarkKnight.HEX_OF_BEHOLDER)){
			applyto.registerEffect(this, System.currentTimeMillis(), duration);
		}
		auraCheck(applyto);
		return true;
	}

	public void remove(MapleCharacter from, MapleCharacter applyto, boolean firstUse){
		auraCheck(applyto);
		for(Pair<MapleBuffStat, BuffDataHolder> p : getStatups()){
			applyto.cancelEffectFromBuffStat(p.left);
		}
		if(firstUse){
			removePartyBuff(applyto);
		}
	}

	private void auraCheck(MapleCharacter applyto){
		int auras = 0, maxAuras = 2;
		for(PlayerBuffValueHolder buff : applyto.getAllBuffs()){
			if(buff.sourceid == BattleMage.DARK_AURA || buff.sourceid == BattleMage.BLUE_AURA || buff.sourceid == BattleMage.YELLOW_AURA){
				auras++;
			}
		}
		if(auras > maxAuras){
			PlayerBuffValueHolder aura = null;
			for(PlayerBuffValueHolder buff : applyto.getAllBuffs()){
				if(buff.sourceid == BattleMage.DARK_AURA || buff.sourceid == BattleMage.BLUE_AURA || buff.sourceid == BattleMage.YELLOW_AURA){
					Optional<Pair<MapleBuffStat, BuffDataHolder>> check = getStatups().stream().findFirst();
					if(check.get().right.getSourceID() != buff.sourceid){
						aura = buff;
						break;
					}
				}
			}
			if(aura != null){
				applyto.cancelEffectFromBuffStat(aura.getEffect().getStatups().stream().findFirst().get().left);
			}
		}
	}

	/**
	 * Gets a list of character objects that are in range.
	 * Calls {@link MapleStatEffect#applyTo(MapleCharacter, MapleCharacter, boolean, Point)} for each character object found.
	 */
	private void applyPartyBuff(MapleCharacter applyfrom){
		if(isPartyBuff() && (applyfrom.isInParty() || isGmBuff())){
			Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
			List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
			List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
			int maxPlayers = this.getSourceId() == Evan.SOUL_STONE ? Randomizer.nextInt(getY()) + 1 : this.getSourceId() == Bishop.RESURRECTION ? 1 : 6;
			for(MapleMapObject affectedmo : affecteds){
				MapleCharacter affected = (MapleCharacter) affectedmo;
				if(affected == null) continue;
				if(applyfrom == null) continue;
				if(affected.getId() != applyfrom.getId() && (isGmBuff() || (applyfrom.getPartyId() != -1 && applyfrom.getPartyId() == affected.getPartyId()))){
					if((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())){
						affectedp.add(affected);
					}
				}
			}
			int totalAffected = 0;
			for(MapleCharacter affected : affectedp){
				if(totalAffected++ >= maxPlayers) continue;
				applyTo(applyfrom, affected, false, null);
				affected.getClient().announce(UserLocal.UserEffect.showOwnBuffEffect(sourceid, 2));
				affected.getMap().broadcastMessage(affected, UserRemote.UserEffect.showBuffeffect(affected.getId(), sourceid, 2), false);
			}
		}
	}

	private void removePartyBuff(MapleCharacter from){
		if(isPartyBuff() && (from.isInParty() || isGmBuff())){
			Rectangle bounds = calculateBoundingBox(from.getPosition(), from.isFacingLeft());
			List<MapleMapObject> affecteds = from.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
			List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
			for(MapleMapObject affectedmo : affecteds){
				MapleCharacter affected = (MapleCharacter) affectedmo;
				if(affected == null) continue;
				if(affected.getId() != from.getId() && (isGmBuff() || from.getParty().getId() == affected.getParty().getId())){
					if((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())){
						affectedp.add(affected);
					}
				}
			}
			for(MapleCharacter affected : affectedp){
				remove(from, affected, false);
			}
		}
	}

	private void applyMonsterBuff(MapleCharacter applyfrom){
		Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
		List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
		Skill skill_ = SkillFactory.getSkill(sourceid);
		int i = 0;
		for(MapleMapObject mo : affected){
			i++;
			MapleMonster monster = (MapleMonster) mo;
			if(isDispel()){
				if(i >= mobCount) continue;
				monster.debuffMob(skill_.getId());
			}else{
				if(sourceid == Page.THREATEN){// Custom, make threaten switch controller
					MapleCharacter controller = monster.getController();
					if(controller == null || controller.getId() != applyfrom.getId()) monster.switchController(applyfrom, true);
					else{
						applyfrom.controlMonster(monster, true);
						monster.setControllerHasAggro(true);
						monster.setControllerKnowsAboutAggro(false);
					}
				}
				if(i >= mobCount) continue;
				if(makeChanceResult()){
					monster.applyStatus(applyfrom, getMonsterStati(), skill_, false, isPoison());
					if(isCrash()){
						monster.debuffMob(skill_.getId());
					}
				}
			}
			if(i >= mobCount && sourceid != Page.THREATEN) break;
		}
	}

	private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft){
		Point mylt;
		Point myrb;
		if(facingLeft){
			mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
			myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
		}else{
			myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
			mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
		}
		Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
		return bounds;
	}

	public void silentApplyBuff(MapleCharacter chr, long starttime, long duration){
		silentApplyBuff(chr, starttime, duration, false);
	}

	public void silentApplyBuff(MapleCharacter chr, long starttime, long duration, boolean eliteEnhance){
		this.applyBuffEffect(chr, chr, duration, starttime, false, eliteEnhance);
		SummonMovementType summonMovementType = getSummonMovementType();
		if(summonMovementType != null){
			final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
			if(!tosummon.isStationary()){
				chr.addSummon(sourceid, tosummon);
				tosummon.addHP(x);
			}
		}
	}

	public final void applyComboBuff(final MapleCharacter applyto, int combo){
		final List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ARAN_COMBO, new BuffDataHolder(sourceid, 0, combo)));
		applyto.getClient().announce(MaplePacketCreator.giveBuff(applyto, sourceid, 99999, stat));
		final long starttime = System.currentTimeMillis();
		applyto.registerEffect(this, starttime, -1);
	}

	public void applyBuffEffect(MapleCharacter chr, int duration){
		applyBuffEffect(chr, chr, duration, false);
	}

	public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary){
		applyBuffEffect(applyfrom, applyto, duration, primary);
	}

	public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, int duration, boolean primary){
		applyBuffEffect(applyfrom, applyto, duration, System.currentTimeMillis(), primary, true);
	}

	/**
	 * Calls {@link MapleCharacter#registerEffect(MapleStatEffect, long, long)} to register the active buff.
	 * Gets the list of statups and sends the buff packet to the client.
	 */
	public void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, long duration, long starttime, boolean primary, boolean eliteEnhance){
		if(!isMonsterRiding()){
			applyto.cancelEffect(this, true, -1);
		}
		List<Pair<MapleBuffStat, BuffDataHolder>> localstatups = statups;
		// Duration sent in packets, we reduce this later for times when you cc so on the players screen it visually expires on same time.
		int packetDuration = (int) duration;
		// Buff Duration is the duration used to check if the buff should expire server-side.
		// Since starttime never changes we can't re-use packetDuration and have to keep the original full duration of the buff.
		int buffDuration = packetDuration;
		// System.out.println("original: " + duration);
		int localsourceid = sourceid;
		int seconds = packetDuration / 1000;
		MapleMount givemount = null;
		if(isMonsterRiding()){
			int ridingLevel = 0;
			Item mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
			if(mount != null){
				ridingLevel = mount.getItemId();
			}
			if(sourceid == Corsair.BATTLE_SHIP){
				ridingLevel = 1932000;
			}else if(sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP){
				ridingLevel = 1932000 + applyto.getSkillLevel(sourceid);
			}else if(sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1){
				ridingLevel = 1932003;
			}else if(sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2){
				ridingLevel = 1932004;
			}else if(sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK){
				ridingLevel = 1932005;
			}else if(sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT){
				ridingLevel = 1932010;
			}else{
				if(applyto.getMount() == null){
					applyto.mount(ridingLevel, sourceid);
				}
				applyto.getMount().startSchedule();
			}
			if(sourceid == Corsair.BATTLE_SHIP){
				givemount = new MapleMount(applyto, 1932000, sourceid);
			}else if(sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP){
				givemount = new MapleMount(applyto, 1932000 + applyto.getSkillLevel(sourceid), sourceid);
			}else if(sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1){
				givemount = new MapleMount(applyto, 1932003, sourceid);
			}else if(sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2){
				givemount = new MapleMount(applyto, 1932004, sourceid);
			}else if(sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK){
				givemount = new MapleMount(applyto, 1932005, sourceid);
			}else if(sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT){
				givemount = new MapleMount(applyto, 1932010, sourceid);
			}else{
				givemount = applyto.getMount();
			}
			packetDuration = sourceid;
			buffDuration = sourceid;
			localsourceid = ridingLevel;
			localstatups = Collections.singletonList(new Pair<>(MapleBuffStat.MONSTER_RIDING, new BuffDataHolder(0, 0, 0)));// first 0 was sourceid
		}else if(isSkillMorph()){
			localstatups = Collections.singletonList(new Pair<>(MapleBuffStat.MORPH, new BuffDataHolder(sourceid, 0, getMorph(applyto))));
		}
		if(primary){
			packetDuration = alchemistModifyVal(applyfrom, packetDuration, false);
			buffDuration = alchemistModifyVal(applyfrom, buffDuration, false);
			applyto.getMap().broadcastMessage(applyto, UserRemote.UserEffect.showBuffeffect(applyto.getId(), sourceid, 1, (byte) 3), false);
		}
		packetDuration -= (System.currentTimeMillis() - starttime);
		// System.out.println("buffDuration: " + packetDuration);
		if(packetDuration <= 0) return;
		if(!isDisease() && !SkillConstants.isEliteExempted(sourceid) && eliteEnhance){
			if(sourceid != FPArchMage.BIG_BANG && sourceid != ILArchMage.BIG_BANG && sourceid != Bishop.BIG_BANG){
				if(applyto.getClient().checkEliteStatus()){
					if(packetDuration * 2 > Integer.MAX_VALUE || packetDuration * 2 < 0){
						packetDuration = Integer.MAX_VALUE;
						buffDuration = Integer.MAX_VALUE;
					}else{
						packetDuration *= 2;
						buffDuration *= 2;
					}
					seconds = packetDuration / 1000;
				}
			}else{
				// localDuration = Math.abs(localDuration);
				// use to be -1000 causing it to be perm, Math.abs set it to 1000 but its too short for it to be at all useful.
				packetDuration = 5000;
				seconds = packetDuration / 1000;
			}
		}
		for(Pair<MapleBuffStat, BuffDataHolder> p : localstatups){
			if(p.left.equals(MapleBuffStat.ENERGY_CHARGE)){
				TemporaryStatBase pStat = applyto.secondaryStat.getTemporaryState(TSIndex.EnergyCharged.getIndex());
				pStat.nOption = localsourceid;
				pStat.rOption = packetDuration;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.EnergyCharged.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.DASH_SPEED)){
				TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.DashSpeed.getIndex());
				pStat.nOption = p.right.getValue();
				pStat.rOption = localsourceid;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.DashSpeed.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.DASH_JUMP)){
				TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.DashJump.getIndex());
				pStat.nOption = p.right.getValue();
				pStat.rOption = localsourceid;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.DashJump.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.MONSTER_RIDING)){// doesn't seem to work
				TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.RideVehicle.getIndex());
				pStat.nOption = localsourceid;
				pStat.rOption = packetDuration;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.RideVehicle.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.SPEED_INFUSION)){
				PartyBooster pStat = (PartyBooster) applyto.secondaryStat.getTemporaryState(TSIndex.PartyBooster.getIndex());
				pStat.nOption = p.right.getValue();
				pStat.rOption = localsourceid;
				pStat.tLastUpdated = System.currentTimeMillis();
				pStat.tCurrentTime = (int) System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.PartyBooster.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.HOMING_BEACON)){
				GuidedBullet pStat = (GuidedBullet) applyto.secondaryStat.getTemporaryState(TSIndex.GuidedBullet.getIndex());
				pStat.nOption = p.right.getSourceID();
				pStat.rOption = p.right.getSourceLevel();
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.GuidedBullet.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.UNDEAD)){
				TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.Undead.getIndex());
				pStat.nOption = localsourceid;
				pStat.rOption = packetDuration;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.Undead.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.UNDEAD)){
				TwoStateTemporaryStat pStat = (TwoStateTemporaryStat) applyto.secondaryStat.getTemporaryState(TSIndex.Undead.getIndex());
				pStat.nOption = localsourceid;
				pStat.rOption = packetDuration;
				pStat.tLastUpdated = System.currentTimeMillis();
				applyto.secondaryStat.setTemporaryState(TSIndex.Undead.getIndex(), pStat);
			}else if(p.left.equals(MapleBuffStat.MORPH)){
				// fix morph
			}
		}
		if(localstatups.size() > 0){
			byte[] buff = null;
			byte[] mbuff = null;
			if(isDisease()){
				buff = MaplePacketCreator.giveBuff(applyto, (sourceLevel << 16 | sourceid), packetDuration, localstatups);
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, localstatups);
			}else if(getSummonMovementType() == null){
				buff = MaplePacketCreator.giveBuff(applyto, (skill ? sourceid : -sourceid), packetDuration, localstatups);
			}
			if(isDash()){
				buff = MaplePacketCreator.giveBuff(applyto, localsourceid, packetDuration, localstatups);
				mbuff = MaplePacketCreator.giveForgeinPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
			}else if(isInfusion()){
				buff = MaplePacketCreator.giveBuff(applyto, localsourceid, packetDuration, localstatups);
				mbuff = MaplePacketCreator.giveForgeinPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
			}else if(isWindWalk()){
				List<Pair<MapleBuffStat, BuffDataHolder>> wwstat = Collections.singletonList(new Pair<>(MapleBuffStat.WIND_WALK, new BuffDataHolder(0, 0, 0)));
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, wwstat);
			}else if(isDs()){
				List<Pair<MapleBuffStat, BuffDataHolder>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT, new BuffDataHolder(0, 0, 0)));
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, dsstat);
			}else if(isCombo()){
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, statups);
			}else if(isMonsterRiding()){
				applyto.getMount().setItemId(givemount.getItemId());
				applyto.getMount().setActive(true);
				applyto.getMount().setExp(givemount.getExp());
				applyto.getMount().setLevel(givemount.getLevel());
				applyto.getMount().setTiredness(givemount.getTiredness());
				buff = MaplePacketCreator.giveBuff(applyto, localsourceid, packetDuration, localstatups);
				mbuff = MaplePacketCreator.showMonsterRiding(applyto.getId(), applyto.getMount());
				packetDuration = (int) duration;
				if(sourceid == Corsair.BATTLE_SHIP){// hp
					if(applyto.getBattleshipHp() == 0){
						applyto.resetBattleshipHp();
					}
				}
			}else if(isShadowPartner()){
				List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SHADOWPARTNER, new BuffDataHolder(0, 0, 0)));
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, stat);
			}else if(isSoulArrow()){
				List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SOULARROW, new BuffDataHolder(0, 0, 0)));
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, stat);
			}else if(isEnrage()){
				applyto.handleOrbconsume();
			}else if(isMorph()){
				List<Pair<MapleBuffStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.MORPH, new BuffDataHolder(sourceid, 0, getMorph(applyto))));
				mbuff = MaplePacketCreator.giveForeignBuff(applyto, stat);
			}
			// System.out.println("registering duration: " + buffDuration);
			applyto.registerEffect(this, starttime, buffDuration);
			if(buff != null){
				if(!hasNoIcon()){ // Thanks flav for such a simple release! :)
					applyto.getClient().announce(buff);
				}
			}
			if(mbuff != null){
				applyto.getMap().broadcastMessage(applyto, mbuff, false);
			}
			if(sourceid == Corsair.BATTLE_SHIP){
				applyto.announce(MaplePacketCreator.skillCooldown(5221999, applyto.getBattleshipHp() / 10));
			}
		}
	}

	private int calcHPChange(MapleCharacter applyfrom, boolean firstApply){
		int hpchange = 0;
		if(hp != 0){
			if(!skill){
				if(firstApply){
					hpchange += alchemistModifyVal(applyfrom, hp, true);
				}else{
					hpchange += hp;
				}
			}else{
				hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
			}
		}
		if(hpR != 0){
			hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
		}
		if(firstApply){
			if(hpCon != 0){
				hpchange -= hpCon;
			}
		}
		if(isChakra()){
			hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
		}else if(sourceid == SuperGM.HEAL_PLUS_DISPEL){
			hpchange += (applyfrom.getMaxHp() - applyfrom.getHp());
		}
		return hpchange;
	}

	private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor){
		return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
	}

	private int calcMPChange(MapleCharacter applyfrom, boolean firstApply){
		int mpchange = 0;
		if(mp != 0){
			if(firstApply){
				mpchange += alchemistModifyVal(applyfrom, mp, true);
			}else{
				mpchange += mp;
			}
		}
		if(mpR != 0){
			mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
		}
		if(firstApply){
			if(mpCon != 0){
				double mod = 1.0;
				boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
				boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
				boolean isEvan = applyfrom.getJob().isA(MapleJob.EVAN7);
				if(isAFpMage || isCygnus || isEvan || applyfrom.getJob().isA(MapleJob.IL_MAGE)){
					Skill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : (isEvan ? SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION)));
					int ampLevel = applyfrom.getSkillLevel(amp);
					if(ampLevel > 0){
						mod = amp.getEffect(ampLevel).getX() / 100.0;
					}
				}
				mpchange -= mpCon * mod;
				if(applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null){
					mpchange = 0;
				}else if(applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null){
					mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE).doubleValue() / 100));
				}
			}
		}
		if(sourceid == SuperGM.HEAL_PLUS_DISPEL){
			mpchange += (applyfrom.getMaxMp() - applyfrom.getMp());
		}
		return mpchange;
	}

	private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX){
		if(!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))){
			MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
			if(alchemistEffect != null) return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
		}
		return val;
	}

	private MapleStatEffect getAlchemistEffect(MapleCharacter chr){
		int id = Hermit.ALCHEMIST;
		if(chr.isCygnus()){
			id = NightWalker.ALCHEMIST;
		}
		int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
		return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
	}

	private boolean isGmBuff(){
		switch (sourceid){
			case Beginner.ECHO_OF_HERO:
			case Noblesse.ECHO_OF_HERO:
			case Legend.ECHO_OF_HERO:
			case Evan.ECHO_OF_HERO:
			case SuperGM.HEAL_PLUS_DISPEL:
			case SuperGM.HASTE:
			case SuperGM.HOLY_SYMBOL:
			case SuperGM.BLESS:
			case SuperGM.RESURRECTION:
			case SuperGM.HYPER_BODY:
				return true;
			default:
				return false;
		}
	}

	private boolean isMonsterBuff(){
		if(!skill) return false;
		switch (sourceid){
			case Page.THREATEN:
			case FPWizard.SLOW:
			case ILWizard.SLOW:
			case FPMage.SEAL:
			case ILMage.SEAL:
			case Priest.DOOM:
			case Hermit.SHADOW_WEB:
			case NightLord.NINJA_AMBUSH:
			case Shadower.NINJA_AMBUSH:
			case BlazeWizard.SLOW:
			case BlazeWizard.SEAL:
			case NightWalker.SHADOW_WEB:
			case Crusader.ARMOR_CRASH:
			case DragonKnight.POWER_CRASH:
			case WhiteKnight.MAGIC_CRASH:
			case Priest.DISPEL:
			case SuperGM.HEAL_PLUS_DISPEL:
				return true;
		}
		return false;
	}

	private boolean isPartyBuff(){
		if(lt == null || rb == null) return false;
		if((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE){// wk charges have lt and rb set but are neither player nor monster buffs
			return false;
		}
		return true;
	}

	private boolean isHeal(){
		return sourceid == Cleric.HEAL || sourceid == SuperGM.HEAL_PLUS_DISPEL;
	}

	private boolean isResurrection(){
		return sourceid == Bishop.RESURRECTION || sourceid == Evan.SOUL_STONE || sourceid == GM.RESURRECTION || sourceid == SuperGM.RESURRECTION;
	}

	private boolean isTimeLeap(){
		return sourceid == Buccaneer.TIME_LEAP;
	}

	public boolean isDragonBlood(){
		return skill && sourceid == DragonKnight.DRAGON_BLOOD;
	}

	public boolean isBerserk(){
		return skill && sourceid == DarkKnight.BERSERK;
	}

	public boolean isRecovery(){
		return sourceid == Beginner.RECOVERY || sourceid == Noblesse.RECOVERY || sourceid == Legend.RECOVERY;
	}

	private boolean isWindWalk(){
		return sourceid == WindArcher.WIND_WALK;
	}

	private boolean isDs(){
		return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == NightWalker.DARK_SIGHT);
	}

	private boolean isCombo(){
		return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
	}

	private boolean isEnrage(){
		return skill && sourceid == Hero.ENRAGE;
	}

	public boolean isBeholder(){
		return skill && sourceid == DarkKnight.BEHOLDER;
	}

	private boolean isShadowPartner(){
		return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER);
	}

	private boolean isChakra(){
		return skill && sourceid == ChiefBandit.CHAKRA;
	}

	public boolean isMonsterRiding(){
		return skill && (sourceid % 10000000 == 1004 || sourceid % 10000000 == 11004/*Evan*/ || sourceid == Corsair.BATTLE_SHIP || sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP || sourceid == Beginner.YETI_MOUNT1 || sourceid == Beginner.YETI_MOUNT2 || sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT2 || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Legend.BALROG_MOUNT);
	}

	public boolean isMagicDoor(){
		return skill && sourceid == Priest.MYSTIC_DOOR;
	}

	public boolean isPoison(){
		return skill && (sourceid == FPMage.POISON_MIST || sourceid == FPWizard.POISON_BREATH || sourceid == FPMage.ELEMENT_COMPOSITION || sourceid == NightWalker.POISON_BOMB || sourceid == BlazeWizard.FLAME_GEAR);
	}

	public boolean isMorph(){
		return morphId > 0;
	}

	public boolean isMorphWithoutAttack(){
		return morphId > 0 && morphId < 100; // Every morph item I have found has been under 100, pirate skill transforms start at 1000.
	}

	private boolean isMist(){
		return skill && (sourceid == FPMage.POISON_MIST || sourceid == Shadower.SMOKE_SCREEN || sourceid == BlazeWizard.FLAME_GEAR || sourceid == NightWalker.POISON_BOMB || sourceid == Evan.RECOVERY_AURA);
	}

	private boolean isSoulArrow(){
		return skill && (sourceid == Hunter.SOUL_ARROW || sourceid == Crossbowman.SOUL_ARROW || sourceid == WindArcher.SOUL_ARROW);
	}

	private boolean isShadowClaw(){
		return skill && sourceid == NightLord.SHADOW_STARS;
	}

	private boolean isCrash(){
		return skill && (sourceid == DragonKnight.POWER_CRASH || sourceid == Crusader.ARMOR_CRASH || sourceid == WhiteKnight.MAGIC_CRASH);
	}

	private boolean isDispel(){
		return skill && (sourceid == Priest.DISPEL || sourceid == SuperGM.HEAL_PLUS_DISPEL);
	}

	private boolean isHeroWill(){
		if(skill){
			switch (sourceid){
				case Hero.HEROS_WILL:
				case Paladin.HEROS_WILL:
				case DarkKnight.HEROS_WILL:
				case FPArchMage.HEROS_WILL:
				case ILArchMage.HEROS_WILL:
				case Bishop.HEROS_WILL:
				case Bowmaster.HEROS_WILL:
				case Marksman.HEROS_WILL:
				case NightLord.HEROS_WILL:
				case Shadower.HEROS_WILL:
				case Buccaneer.PIRATES_RAGE:
				case Aran.HEROS_WILL:
					return true;
				default:
					return false;
			}
		}
		return false;
	}

	private boolean isDash(){
		return skill && (sourceid == Pirate.DASH || sourceid == ThunderBreaker.DASH || sourceid == Beginner.SPACE_DASH || sourceid == Noblesse.SPACE_DASH);
	}

	private boolean isSkillMorph(){
		return skill && (sourceid == Buccaneer.SUPER_TRANSFORMATION || sourceid == Marauder.TRANSFORMATION || sourceid == WindArcher.EAGLE_EYE || sourceid == ThunderBreaker.TRANSFORMATION);
	}

	private boolean isInfusion(){
		return skill && (sourceid == Buccaneer.SPEED_INFUSION || sourceid == Corsair.SPEED_INFUSION || sourceid == ThunderBreaker.SPEED_INFUSION);
	}

	private boolean isCygnusFA(){
		return skill && (sourceid == DawnWarrior.FINAL_ATTACK || sourceid == WindArcher.FINAL_ATTACK);
	}

	private boolean isComboReset(){
		return sourceid == Aran.COMBO_BARRIER || sourceid == Aran.COMBO_DRAIN;
	}

	private int getFatigue(){
		return fatigue;
	}

	public int getMorph(){
		return morphId;
	}

	private int getMorph(MapleCharacter chr){
		if(morphId >= 1000) return morphId + (100 * chr.getGender());
		if(morphId % 10 == 0) return morphId + chr.getGender();
		return morphId + (100 * chr.getGender());
	}

	private SummonMovementType getSummonMovementType(){
		if(!skill) return null;
		switch (sourceid){
			case Ranger.PUPPET:
			case Sniper.PUPPET:
			case WindArcher.PUPPET:
			case Outlaw.OCTOPUS:
			case Corsair.WRATH_OF_THE_OCTOPI:
			case BladeMaster.MIRRORED_TARGET:
				// case WildHunter.MineDummySummoned:
				// case WildHunter.Trap:
				// case Mechanic.TeslaCoil:
				// case Mechanic.VelocityControler:
				// case Mechanic.HealingRobot_H_LX:
				// case Mechanic.SG88:
				// case Mechanic.AR01:
				// case Mechanic.RoboRoboDummy:
				return SummonMovementType.STATIONARY;
			case Ranger.SILVER_HAWK:
			case Sniper.GOLDEN_EAGLE:
			case Priest.SUMMON_DRAGON:
			case Marksman.FROST_PREY:
			case Bowmaster.PHOENIX:
			case Outlaw.GAVIOTA:
				return SummonMovementType.CIRCLE_FOLLOW;
			case DarkKnight.BEHOLDER:
			case FPArchMage.ELQUINES:
			case ILArchMage.IFRIT:
			case Bishop.BAHAMUT:
			case DawnWarrior.SOUL:
			case BlazeWizard.FLAME:
			case BlazeWizard.IFRIT:
			case WindArcher.STORM:
			case NightWalker.DARKNESS:
			case ThunderBreaker.LIGHTNING:
				// case Mechanic.Satelite:
				// case Mechanic.Satelite2:
				// case Mechanic.Satelite3:
				// case Mechanic.RoboRobo:
				return SummonMovementType.FOLLOW;
			// case Valkyrie.Gabiota:
			// return SummonMovementType.FLY_RANDOM;
			//
			// case BMage.Revive:
			// return SummonMovementType.WALK_RANDOM;
			//
		}
		return null;
	}

	public boolean hasNoIcon(){
		return hasNoIcon(sourceid);
	}

	public static boolean hasNoIcon(int sourceid){
		return(sourceid == 3111002 || sourceid == 3211002 || + // puppet, puppet
		sourceid == 3211005 || sourceid == 2311002 || + // golden eagle, mystic door
		sourceid == 2121005 || sourceid == 2221005 || + // elquines, ifrit
		sourceid == 2321003 || sourceid == 3121006 || + // bahamut, phoenix
		sourceid == 3221005 || sourceid == 3111005 || + // frostprey, silver hawk
		sourceid == 2311006 || sourceid == 5220002 || + // summon dragon, wrath of the octopi
		sourceid == 5211001 || sourceid == 5211002 || +sourceid == BladeMaster.MIRRORED_TARGET); // octopus, gaviota
	}

	public boolean isSkill(){
		return skill;
	}

	public int getSourceId(){
		return sourceid;
	}

	public int getSourceLevel(){
		return sourceLevel;
	}

	public boolean makeChanceResult(){
		return prop == 1.0 || Math.random() < prop;
	}

	public boolean isDisease(){
		return disease;
	}

	public short getHp(){
		return hp;
	}

	public short getMp(){
		return mp;
	}

	public short getHpCon(){
		return hpCon;
	}

	public short getMpCon(){
		return mpCon;
	}

	public short getMatk(){
		return matk;
	}

	public short getWatk(){
		return watk;
	}

	public int getDuration(){
		return duration;
	}

	public List<Pair<MapleBuffStat, BuffDataHolder>> getStatups(){
		return statups;
	}

	public boolean sameSource(MapleStatEffect effect){
		return this.sourceid == effect.sourceid && this.skill == effect.skill;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public int getDamage(){
		return damage;
	}

	public int getAttackCount(){
		return attackCount;
	}

	public int getMobCount(){
		return mobCount;
	}

	public int getFixDamage(){
		return fixdamage;
	}

	public byte getBulletCount(){
		return bulletCount;
	}

	public byte getBulletConsume(){
		return bulletConsume;
	}

	public int getMoneyCon(){
		return moneyCon;
	}

	public int getCooldown(){
		return cooldown;
	}

	/**
	 * @return If > 0 buff gives bonus item drop rate.
	 */
	public int getItemupbyitem(){
		return itemupbyitem;
	}

	/**
	 * @return If > 0 buff gives bonus meso rate.
	 */
	public int getMesoupbyitem(){
		return mesoupbyitem;
	}

	/**
	 * Used to get Meso & Drop bonus from {@link getItemupbyitem()} and {@link getMesoupbyitem()}
	 * 
	 * @return Meso & Drop bonus, EG: 30 = 1.30x
	 */
	public int getProb(){
		return prob;
	}

	public Map<MobStat, MobStatData> getMonsterStati(){
		return monsterStatus;
	}

	public Integer getSkilLevel(){
		return skillLevel;
	}

	public short getWdef(){
		return wdef;
	}

	public short getMdef(){
		return mdef;
	}

	public short getAcc(){
		return acc;
	}

	public short getAvoid(){
		return avoid;
	}
}
