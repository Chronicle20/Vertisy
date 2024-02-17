/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * @author iPoopMagic (David)
 */
function enter(pi) {
	if (pi.getPlayer().getMapId() == 240060000) {
		var nextMap = 240060100;
		var mob = pi.getPlayer().getMap().getMonsterById(8810000);
		var mob2 = pi.getPlayer().getMap().getMonsterById(8810024);
		var preheadCheck = "2";
	} else if (pi.getPlayer().getMapId() == 240060100) {
		var nextMap = 240060200;
		var mob = pi.getPlayer().getMap().getMonsterById(8810001);
		var mob2 = pi.getPlayer().getMap().getMonsterById(8810025);
		var preheadCheck = "4";
	}
	var eim = pi.getPlayer().getEventInstance();
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("sp");
	// only let people through if Horntail is gone
	if (pi.getEventManager("HorntailFight").getProperty("preheadCheck") != preheadCheck) {
		pi.getPlayer().dropMessage(6, "Horntail's Seal is blocking this door.");
		return false;
	} else {
		pi.getPlayer().changeMap(target, targetPortal);
		return true;
	}
	return true;	
}