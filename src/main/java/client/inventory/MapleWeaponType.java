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
package client.inventory;

public enum MapleWeaponType{
	NOT_A_WEAPON(0, 0),
	GENERAL1H_SWING(4.4, 0.9),
	GENERAL1H_STAB(3.2, 0.9),
	GENERAL2H_SWING(4.8, 0.9),
	GENERAL2H_STAB(3.4, 0.9),
	BOW(3.4, 0.9),
	CLAW(3.6, 2.5), // ?
	CROSSBOW(3.6, 0.9),
	DAGGER_THIEVES(3.6, 0.9),
	DAGGER_OTHER(4, 0.9),
	GUN(3.6, 0.9),
	KNUCKLE(4.8, 0.9),
	POLE_ARM_SWING(5.0, 3.0),
	POLE_ARM_STAB(3.0, 3.0),
	SPEAR_STAB(5.0, 3.0),
	SPEAR_SWING(3.0, 3.0),
	STAFF(3.6, 4.6),
	SWORD1H(4.0, 0.9),
	SWORD2H(4.6, 0.9),
	WAND(3.6, 0);

	private double damageMultiplier, minDamageMultiplier;

	private MapleWeaponType(double maxDamageMultiplier, double minDamageMultiplier){
		this.damageMultiplier = maxDamageMultiplier;
		this.minDamageMultiplier = minDamageMultiplier;
	}

	public double getMaxDamageMultiplier(){
		return damageMultiplier;
	}

	public double getMinDamageMultiplier(){
		return minDamageMultiplier;
	}
}
