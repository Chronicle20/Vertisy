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

import tools.data.input.LittleEndianAccessor;
import tools.data.output.LittleEndianWriter;

/**
 * @author Danny (Leifde)
 */
public class MobAttackInfo{

	private boolean isDeadlyAttack;
	private int mpBurn;
	private int diseaseSkill;
	private int diseaseLevel;
	private int mpCon;

	public MobAttackInfo(){}

	public void setDeadlyAttack(boolean isDeadlyAttack){
		this.isDeadlyAttack = isDeadlyAttack;
	}

	public boolean isDeadlyAttack(){
		return isDeadlyAttack;
	}

	public void setMpBurn(int mpBurn){
		this.mpBurn = mpBurn;
	}

	public int getMpBurn(){
		return mpBurn;
	}

	public void setDiseaseSkill(int diseaseSkill){
		this.diseaseSkill = diseaseSkill;
	}

	public int getDiseaseSkill(){
		return diseaseSkill;
	}

	public void setDiseaseLevel(int diseaseLevel){
		this.diseaseLevel = diseaseLevel;
	}

	public int getDiseaseLevel(){
		return diseaseLevel;
	}

	public void setMpCon(int mpCon){
		this.mpCon = mpCon;
	}

	public int getMpCon(){
		return mpCon;
	}

	public void save(LittleEndianWriter lew){
		lew.writeBoolean(isDeadlyAttack);
		lew.writeInt(mpBurn);
		lew.writeInt(diseaseSkill);
		lew.writeInt(diseaseLevel);
		lew.writeInt(mpCon);
	}

	public void load(LittleEndianAccessor lea){
		isDeadlyAttack = lea.readBoolean();
		mpBurn = lea.readInt();
		diseaseSkill = lea.readInt();
		diseaseLevel = lea.readInt();
		mpCon = lea.readInt();
	}
}
