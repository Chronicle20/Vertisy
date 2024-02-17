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
package server.maps.objects;

import java.awt.Point;
import java.awt.Rectangle;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import constants.skills.*;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MobSkill;
import tools.packets.field.AffectedAreaPool;

/**
 * @author LaiLaiNoob
 */
public class MapleMist extends AbstractMapleMapObject{

	private Rectangle mistPosition;
	public int ownerid = -1, skilllevel;
	private MapleMonster mob = null;
	private MapleStatEffect source;
	private MobSkill skill;
	private boolean isMobMist, isPoisonMist, isRecoveryMist;
	private int skillDelay;
	public int duration;
	public long createTime = System.currentTimeMillis();

	/**
	 * Automatically sets isPoisonMist to true since all mobs give poison mist.
	 */
	public MapleMist(Rectangle mistPosition, MapleMonster mob, MobSkill skill, int duration){
		this.mistPosition = mistPosition;
		this.mob = mob;
		this.skill = skill;
		isMobMist = true;
		isPoisonMist = true;
		isRecoveryMist = false;
		skillDelay = 0;
		this.duration = duration;
	}

	public MapleMist(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source){
		this.mistPosition = mistPosition;
		this.ownerid = owner.getId();
		this.skilllevel = owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId()));
		this.source = source;
		this.skillDelay = 8;
		this.isMobMist = false;
		this.isRecoveryMist = false;
		this.isPoisonMist = false;
		this.duration = source.getDuration();
		switch (source.getSourceId()){
			case Evan.RECOVERY_AURA:
				isRecoveryMist = true;
				break;
			case Shadower.SMOKE_SCREEN: // Smoke Screen
				isPoisonMist = false;
				break;
			case FPMage.POISON_MIST: // FP mist
			case BlazeWizard.FLAME_GEAR: // Flame Gear
			case NightWalker.POISON_BOMB: // Poison Bomb
				isPoisonMist = true;
				break;
		}
	}

	@Override
	public MapleMapObjectType getType(){
		return MapleMapObjectType.MIST;
	}

	@Override
	public Point getPosition(){
		return mistPosition.getLocation();
	}

	public Skill getSourceSkill(){
		return SkillFactory.getSkill(source.getSourceId());
	}

	public boolean isMobMist(){
		return isMobMist;
	}

	public boolean isPoisonMist(){
		return isPoisonMist;
	}

	public boolean isRecoveryMist(){
		return isRecoveryMist;
	}

	public int getSkillDelay(){
		return skillDelay;
	}

	public MapleMonster getMobOwner(){
		return mob;
	}

	public Rectangle getBox(){
		return mistPosition;
	}

	@Override
	public void setPosition(Point position){
		throw new UnsupportedOperationException();
	}

	public final byte[] makeDestroyData(){
		return AffectedAreaPool.removeMist(getObjectId());
	}

	public final byte[] makeSpawnData(){
		if(ownerid != -1) return AffectedAreaPool.spawnMist(getObjectId(), ownerid, getSourceSkill().getId(), skilllevel, this);
		return AffectedAreaPool.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
	}

	public final byte[] makeFakeSpawnData(int level){
		if(ownerid != -1) return AffectedAreaPool.spawnMist(getObjectId(), ownerid, getSourceSkill().getId(), level, this);
		return AffectedAreaPool.spawnMist(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
	}

	@Override
	public void sendSpawnData(MapleClient client){
		client.announce(makeSpawnData());
	}

	@Override
	public void sendDestroyData(MapleClient client){
		client.announce(makeDestroyData());
	}

	public boolean makeChanceResult(){
		return source.makeChanceResult();
	}

	@Override
	public MapleMist clone(){
		return null;
	}
}
