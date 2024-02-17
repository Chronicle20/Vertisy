/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server.maps;

/**
 * @author AngelSL
 */
public enum FieldLimit{
	JUMP(0x01),
	MOVEMENTSKILLS(0x02),
	SUMMON(0x04),
	DOOR(0x08),
	CHANGECHANNEL(0x10),
	EXPLOSS_PORTALSCROLL(0x20), // Not sure. Used in boss rooms
	CANNOTVIPROCK(0x40),
	CANNOTMINIGAME(0x80),
	SPECIFICPORTALSCROLLLIMIT(0x100),
	CANNOTUSEMOUNTS(0x200),
	STATCHANGEITEMCONSUMELIMIT(0x400),
	CANTSWITCHPARTYLEADER(0x800),
	CANNOTUSEPOTION(0x1000),
	CANTWEDDINGINVITE(0x2000),
	CASHWEATHER(0x4000),
	CANTUSEPET(0x8000), // Ariant colosseum-related?
	CANTUSEMACRO(0x10000), // No notes
	CANNOTJUMPDOWN(0x20000),
	SUMMONNPCLIMIT(0x40000),
	NOEXPDECREASE(0x80000),
	NOFALLDAMAGE(0x100000),
	SHOPS(0x200000),
	CANTDROP(0x400000),
	ROCKETBOOSTER_LIMIT(0x800000),// v95
	;

	private long i;

	private FieldLimit(long i){
		this.i = i;
	}

	public long getValue(){
		return i;
	}

	public boolean check(int fieldlimit){
		return (fieldlimit & i) == i;
	}
}
