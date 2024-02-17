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
package client;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.JobConstants;
import server.MapleStatEffect;
import server.life.Element;
import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

public class Skill{

	public int id;
	public List<MapleStatEffect> effects = new ArrayList<>();
	public Element element;
	public int animationTime, skillType, delay, masterLevel;
	public int job;
	public boolean action;
	public Map<Integer, Point> lt = new HashMap<Integer, Point>();
	public Map<Integer, Point> rb = new HashMap<Integer, Point>();
	public Map<Integer, Integer> range = new HashMap<Integer, Integer>();

	public Skill(int id){
		this.id = id;
		this.job = id / 10000;
	}

	public void save(LittleEndianWriter lew){
		lew.writeInt(effects.size());
		for(MapleStatEffect mse : effects){
			mse.save(lew);
		}
		lew.writeMapleAsciiString(element.name());
		lew.writeInt(animationTime);
		lew.writeInt(skillType);
		lew.writeInt(delay);
		lew.writeInt(masterLevel);
		lew.writeInt(job);
		lew.writeBoolean(action);
		lew.writeInt(lt.size());
		for(int i : lt.keySet()){
			lew.writeInt(i);
			lew.writePos(lt.get(i));
		}
		lew.writeInt(rb.size());
		for(int i : rb.keySet()){
			lew.writeInt(i);
			lew.writePos(rb.get(i));
		}
		lew.writeInt(range.size());
		for(int i : range.keySet()){
			lew.writeInt(i);
			lew.writeInt(range.get(i));
		}
	}

	public void load(LittleEndianAccessor lea){
		int size = lea.readInt();
		for(int i = 0; i < size; i++){
			MapleStatEffect mse = new MapleStatEffect();
			mse.load(lea);
			effects.add(mse);
		}
		element = Element.valueOf(lea.readMapleAsciiString());
		animationTime = lea.readInt();
		skillType = lea.readInt();
		delay = lea.readInt();
		masterLevel = lea.readInt();
		job = lea.readInt();
		action = lea.readBoolean();
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			lt.put(lea.readInt(), lea.readPos());
		}
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			rb.put(lea.readInt(), lea.readPos());
		}
		size = lea.readInt();
		for(int i = 0; i < size; i++){
			range.put(lea.readInt(), lea.readInt());
		}
	}

	public int getId(){
		return id;
	}

	public MapleStatEffect getEffect(int level){
		return effects.get(level - 1);
	}

	public int getMaxLevel(){
		return effects.size();
	}

	public int getMasterLevel(){
		return masterLevel;
	}

	public boolean isFourthJob(){
		if(job == 2212) return false;
		if(id == 22170001 || id == 22171003 || id == 22171004 || id == 22181002 || id == 22181003) return true;
		return job % 10 == 2;
	}

	public boolean is_skill_need_master_level(){
		int v1; // esi@1
		boolean v2; // zf@7
		int nSkillID = id;
		v1 = nSkillID / 10000;
		if(nSkillID / 10000 / 100 == 22 || v1 == 2001){
			if(JobConstants.getJobIndex(nSkillID / 10000) != 9 && JobConstants.getJobIndex(v1) != 10 && nSkillID != 22111001 && nSkillID != 22141002){
				v2 = nSkillID == 22140000;
				if(!v2) return false;
			}
			return true;
		}
		if(v1 / 10 == 43){
			if(JobConstants.getJobIndex(nSkillID / 10000) != 4 && nSkillID != 4311003 && nSkillID != 4321000 && nSkillID != 4331002){
				v2 = nSkillID == 4331005;
				if(!v2) return false;
			}
			return true;
		}
		if(!(v1 % 100 != 0)) return false;
		return v1 % 10 == 2;
	}

	public Element getElement(){
		return element;
	}

	public int getAnimationTime(){
		return animationTime;
	}

	public int getDelay(){
		return delay;
	}

	public int getSkillType(){
		return skillType;
	}

	public boolean isBeginnerSkill(){
		return id % 10000000 < 10000;
	}

	public boolean getAction(){
		return action;
	}
}