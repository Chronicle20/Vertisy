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
 *	@author Jvlaple
 *	@modified iPoopMagic (David)
 *	@description Nependeath Pot - Spawns Nependeath or Dark Nependeath
 */
 
function act() {
	var em = rm.getEventManager("OrbisPQ");
	if (Math.random() > .6 && em.getProperty("finished").equals("0")) {
		rm.spawnMonster(9300049);
		rm.spawnMonster(9300039, new java.awt.Point(-842, 563));
		em.setProperty("finished", "1");
		rm.mapMessage(6, "A Dark Nependeath has been spawned!");
	}
	else
		rm.spawnMonster(9300048);
}