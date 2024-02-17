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
package net.server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import client.SkillFactory;
import server.ItemInformationProvider;
import server.MapleStatEffect;
import server.life.MobSkill;

/**
 * @author Danny
 */
public class PlayerBuffValueHolder implements Externalizable{

	public long startTime, duration;
	public int sourceid, sourcelevel;
	public Integer skillLevel;
	public boolean disease, skill, itemEffect;
	private MapleStatEffect effect;

	public PlayerBuffValueHolder(){
		super();
	}

	public PlayerBuffValueHolder(long startTime, long duration, MapleStatEffect effect){
		this.startTime = startTime;
		this.duration = duration;
		this.effect = effect;
		this.sourceid = effect.getSourceId();
		this.sourcelevel = effect.getSourceLevel();
		this.skillLevel = effect.getSkilLevel();
		this.disease = effect.isDisease();
		this.skill = effect.isSkill();
		this.itemEffect = effect.itemEffect;
	}

	public MapleStatEffect getEffect(){
		if(effect != null) return effect;
		if(disease){
			effect = MapleStatEffect.loadDebuffEffectFromMobSkill(new MobSkill(sourceid, sourcelevel));
		}else if(skill){
			effect = SkillFactory.getSkill(sourceid).getEffect(skillLevel);
		}else if(itemEffect){
			effect = ItemInformationProvider.getInstance().getItemData(sourceid).itemEffect;
		}
		return effect;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException{
		out.writeLong(startTime);
		out.writeLong(duration);
		out.writeInt(sourceid);
		out.writeInt(sourcelevel);
		if(skillLevel != null) out.writeInt(skillLevel);
		else out.writeInt(1);
		out.writeBoolean(disease);
		out.writeBoolean(skill);
		out.writeBoolean(itemEffect);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
		startTime = in.readLong();
		duration = in.readLong();
		sourceid = in.readInt();
		sourcelevel = in.readInt();
		skillLevel = in.readInt();
		disease = in.readBoolean();
		skill = in.readBoolean();
		itemEffect = in.readBoolean();
	}
}
