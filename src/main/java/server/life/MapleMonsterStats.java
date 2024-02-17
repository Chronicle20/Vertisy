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
import java.util.*;

import server.life.MapleLifeFactory.BanishInfo;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleLifeFactory.selfDestruction;
import tools.Pair;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Frz
 */
public class MapleMonsterStats{

	private int link;
	private int exp, hp, mp, level, PADamage, PDDamage, MADamage, MDDamage, dropPeriod, cp, buffToGive, removeAfter, speed, fixedDamage, mobType, eva;
	private boolean boss, undead, ffaLoot, isExplosiveReward, firstAttack, removeOnMiss, hideName;
	private String name;
	private Map<String, Integer> animationTimes = new HashMap<String, Integer>();
	private Map<Element, ElementalEffectiveness> resistance = new HashMap<Element, ElementalEffectiveness>();
	private List<Integer> revives = new ArrayList<>();
	private byte tagColor, tagBgColor;
	private List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
	private Pair<Integer, Integer> cool = null;
	private BanishInfo banish = null;
	private List<loseItem> loseItem = null;
	private selfDestruction selfDestruction = null;
	private boolean friendly;
	private Point origin;
	private String defaultMove;
	private boolean canJump, canMove;
	private Map<Integer, MobAttackInfo> mobAttacks = new HashMap<>();
	public boolean bDamagedByMob;

	public void save(LittleEndianWriter lew){
		lew.writeInt(link);
		lew.writeInt(exp);
		lew.writeInt(hp);
		lew.writeInt(mp);
		lew.writeInt(level);
		lew.writeInt(PADamage);
		lew.writeInt(PDDamage);
		lew.writeInt(MADamage);
		lew.writeInt(MDDamage);
		lew.writeInt(dropPeriod);
		lew.writeInt(cp);
		lew.writeInt(buffToGive);
		lew.writeInt(removeAfter);
		lew.writeInt(speed);
		lew.writeInt(fixedDamage);
		lew.writeInt(mobType);
		lew.writeInt(eva);
		lew.writeBoolean(boss);
		lew.writeBoolean(undead);
		lew.writeBoolean(ffaLoot);
		lew.writeBoolean(isExplosiveReward);
		lew.writeBoolean(firstAttack);
		lew.writeBoolean(removeOnMiss);
		lew.writeBoolean(hideName);
		lew.writeMapleAsciiString(name);
		lew.writeInt(animationTimes.size());
		for(String key : animationTimes.keySet()){
			lew.writeMapleAsciiString(key);
			lew.writeInt(animationTimes.get(key));
		}
		lew.writeInt(resistance.size());
		for(Element ele : resistance.keySet()){
			lew.writeMapleAsciiString(ele.name());
			lew.writeMapleAsciiString(resistance.get(ele).name());
		}
		lew.writeInt(revives.size());
		for(int i : revives){
			lew.writeInt(i);
		}
		lew.write(tagColor);
		lew.write(tagBgColor);
		lew.writeInt(skills.size());
		for(Pair<Integer, Integer> skill : skills){
			lew.writeInt(skill.left);
			lew.writeInt(skill.right);
		}
		lew.writeBoolean(cool != null);
		if(cool != null){
			lew.writeInt(cool.left);
			lew.writeInt(cool.right);
		}
		lew.writeBoolean(banish != null);
		if(banish != null){
			banish.save(lew);
		}
		lew.writeBoolean(loseItem != null);
		if(loseItem != null){
			lew.writeInt(loseItem.size());
			for(loseItem item : loseItem){
				item.save(lew);
			}
		}
		lew.writeBoolean(selfDestruction != null);
		if(selfDestruction != null){
			selfDestruction.save(lew);
		}
		lew.writeBoolean(friendly);
		lew.writeBoolean(origin != null);
		if(origin != null) lew.writePos(origin);
		lew.writeBoolean(defaultMove != null);
		if(defaultMove != null) lew.writeMapleAsciiString(defaultMove);
		lew.writeBoolean(canJump);
		lew.writeBoolean(canMove);
		lew.writeInt(mobAttacks.size());
		for(int attack : mobAttacks.keySet()){
			lew.writeInt(attack);
			mobAttacks.get(attack).save(lew);
		}
	}

	public void load(LittleEndianAccessor lea){
		exp = lea.readInt();
		hp = lea.readInt();
		mp = lea.readInt();
		level = lea.readInt();
		PADamage = lea.readInt();
		PDDamage = lea.readInt();
		MADamage = lea.readInt();
		MDDamage = lea.readInt();
		dropPeriod = lea.readInt();
		cp = lea.readInt();
		buffToGive = lea.readInt();
		removeAfter = lea.readInt();
		speed = lea.readInt();
		fixedDamage = lea.readInt();
		mobType = lea.readInt();
		eva = lea.readInt();
		boss = lea.readBoolean();
		undead = lea.readBoolean();
		ffaLoot = lea.readBoolean();
		isExplosiveReward = lea.readBoolean();
		firstAttack = lea.readBoolean();
		removeOnMiss = lea.readBoolean();
		hideName = lea.readBoolean();
		name = lea.readMapleAsciiString();
		int size = lea.readInt();
		for(int i = 0; i < size; i++){
			animationTimes.put(lea.readMapleAsciiString(), lea.readInt());
		}
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			resistance.put(Element.valueOf(lea.readMapleAsciiString()), ElementalEffectiveness.valueOf(lea.readMapleAsciiString()));
		}
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			revives.add(lea.readInt());
		}
		tagColor = lea.readByte();
		tagBgColor = lea.readByte();
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			skills.add(new Pair<Integer, Integer>(lea.readInt(), lea.readInt()));
		}
		if(lea.readBoolean()){
			cool = new Pair<Integer, Integer>(lea.readInt(), lea.readInt());
		}
		if(lea.readBoolean()){
			banish = new BanishInfo();
			banish.load(lea);
		}
		if(lea.readBoolean()){
			size = lea.readInt();
			for(int i = 0; i < size; i++){
				loseItem item = new loseItem();
				item.load(lea);
				loseItem.add(item);
			}
		}
		if(lea.readBoolean()){
			selfDestruction = new selfDestruction();
			selfDestruction.load(lea);
		}
		friendly = lea.readBoolean();
		if(lea.readBoolean()) origin = lea.readPos();
		if(lea.readBoolean()) defaultMove = lea.readMapleAsciiString();
		canJump = lea.readBoolean();
		canMove = lea.readBoolean();
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			int attackID = lea.readInt();
			MobAttackInfo attack = new MobAttackInfo();
			attack.load(lea);
			mobAttacks.put(attackID, attack);
		}
	}

	public int getLink(){
		return link;
	}

	public void setLink(int link){
		this.link = link;
	}

	public int getExp(){
		return exp;
	}

	public void setExp(int exp){
		this.exp = exp;
	}

	public int getHp(){
		return hp;
	}

	public void setHp(int hp){
		this.hp = hp;
	}

	public int getMp(){
		return mp;
	}

	public void setMp(int mp){
		this.mp = mp;
	}

	public int getLevel(){
		return level;
	}

	public void setLevel(int level){
		this.level = level;
	}

	public int removeAfter(){
		return removeAfter;
	}

	public void setRemoveAfter(int removeAfter){
		this.removeAfter = removeAfter;
	}

	public int getDropPeriod(){
		return dropPeriod;
	}

	public void setDropPeriod(int dropPeriod){
		this.dropPeriod = dropPeriod;
	}

	public void setBoss(boolean boss){
		this.boss = boss;
	}

	public boolean isBoss(){
		return boss;
	}

	public void setFfaLoot(boolean ffaLoot){
		this.ffaLoot = ffaLoot;
	}

	public boolean isFfaLoot(){
		return ffaLoot;
	}

	public void setAnimationTime(String name, int delay){
		animationTimes.put(name, delay);
	}

	public int getAnimationTime(String name){
		Integer ret = animationTimes.get(name);
		if(ret == null) return 500;
		return ret.intValue();
	}

	public boolean isMobile(){
		return animationTimes.containsKey("move") || animationTimes.containsKey("fly");
	}

	public List<Integer> getRevives(){
		return revives;
	}

	public void setRevives(List<Integer> revives){
		this.revives = revives;
	}

	public void setUndead(boolean undead){
		this.undead = undead;
	}

	public boolean getUndead(){
		return undead;
	}

	public void setEffectiveness(Element e, ElementalEffectiveness ee){
		resistance.put(e, ee);
	}

	public ElementalEffectiveness getEffectiveness(Element e){
		ElementalEffectiveness elementalEffectiveness = resistance.get(e);
		if(elementalEffectiveness == null){
			return ElementalEffectiveness.NORMAL;
		}else{
			return elementalEffectiveness;
		}
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public byte getTagColor(){
		return tagColor;
	}

	public void setTagColor(int tagColor){
		this.tagColor = (byte) tagColor;
	}

	public byte getTagBgColor(){
		return tagBgColor;
	}

	public void setTagBgColor(int tagBgColor){
		this.tagBgColor = (byte) tagBgColor;
	}

	public void setSkills(List<Pair<Integer, Integer>> skills){
		for(Pair<Integer, Integer> skill : skills){
			this.skills.add(skill);
		}
	}

	public List<Pair<Integer, Integer>> getSkills(){
		return Collections.unmodifiableList(this.skills);
	}

	public int getNoSkills(){
		return this.skills.size();
	}

	public boolean hasSkill(int skillId, int level){
		for(Pair<Integer, Integer> skill : skills){
			if(skill.getLeft() == skillId && skill.getRight() == level) return true;
		}
		return false;
	}

	public void setFirstAttack(boolean firstAttack){
		this.firstAttack = firstAttack;
	}

	public boolean isFirstAttack(){
		return firstAttack;
	}

	public void setBuffToGive(int buff){
		this.buffToGive = buff;
	}

	public int getBuffToGive(){
		return buffToGive;
	}

	void removeEffectiveness(Element e){
		resistance.remove(e);
	}

	public BanishInfo getBanishInfo(){
		return banish;
	}

	public void setBanishInfo(BanishInfo banish){
		this.banish = banish;
	}

	public int getPADamage(){
		return PADamage;
	}

	public void setPADamage(int PADamage){
		this.PADamage = PADamage;
	}

	public int getCP(){
		return cp;
	}

	public void setCP(int cp){
		this.cp = cp;
	}

	public List<loseItem> loseItem(){
		return loseItem;
	}

	public void addLoseItem(loseItem li){
		if(loseItem == null){
			loseItem = new LinkedList<loseItem>();
		}
		loseItem.add(li);
	}

	public selfDestruction selfDestruction(){
		return selfDestruction;
	}

	public void setSelfDestruction(selfDestruction sd){
		this.selfDestruction = sd;
	}

	public void setExplosiveReward(boolean isExplosiveReward){
		this.isExplosiveReward = isExplosiveReward;
	}

	public boolean isExplosiveReward(){
		return isExplosiveReward;
	}

	public void setRemoveOnMiss(boolean removeOnMiss){
		this.removeOnMiss = removeOnMiss;
	}

	public boolean removeOnMiss(){
		return removeOnMiss;
	}

	public void setCool(Pair<Integer, Integer> cool){
		this.cool = cool;
	}

	public Pair<Integer, Integer> getCool(){
		return cool;
	}

	public int getPDDamage(){
		return PDDamage;
	}

	public int getMADamage(){
		return MADamage;
	}

	public int getMDDamage(){
		return MDDamage;
	}

	public boolean isFriendly(){
		return friendly;
	}

	public void setFriendly(boolean value){
		this.friendly = value;
	}

	public void setPDDamage(int PDDamage){
		this.PDDamage = PDDamage;
	}

	public void setMADamage(int MADamage){
		this.MADamage = MADamage;
	}

	public void setMDDamage(int MDDamage){
		this.MDDamage = MDDamage;
	}

	public void setOrigin(Point origin){
		this.origin = origin;
	}

	public Point getOrigin(){
		return origin;
	}

	public String getDefaultMoveType(){
		return defaultMove;
	}

	public void setDefaultMoveType(String defaultMoveType){
		this.defaultMove = defaultMoveType;
	}

	public int getSpeed(){
		return speed;
	}

	public void setSpeed(int speed){
		this.speed = speed;
	}

	public int getFixedDamage(){
		return fixedDamage;
	}

	public void setFixedDamage(int fixedDamage){
		this.fixedDamage = fixedDamage;
	}

	public boolean isNameHidden(){
		return hideName;
	}

	public void setHideName(boolean hidename){
		this.hideName = hidename;
	}

	public int getMobType(){
		return mobType;
	}

	public void setMobType(int mobType){
		this.mobType = mobType;
	}

	public boolean canJump(){
		return canJump;
	}

	public void setCanJump(boolean canJump){
		this.canJump = canJump;
	}

	public boolean canMove(){
		return canMove;
	}

	public void setCanMove(boolean canMove){
		this.canMove = canMove;
	}

	public int getEvade(){
		return eva;
	}

	public void setEvade(int evade){
		this.eva = evade;
	}

	public MobAttackInfo getMobAttack(int attack){
		return mobAttacks.get(attack);
	}

	public void addMobAttack(int attack, MobAttackInfo info){
		mobAttacks.put(attack, info);
	}
}
