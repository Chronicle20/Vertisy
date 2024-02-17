/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 *	@NPC: Muirhat
 *	@Author: iPoopMagic
 *	@Function: Disciples of the Black Magician Quest (2175)
 */

function start() {
    if (cm.isQuestStarted(2175)) {
		cm.sendOk("Are you ready? Good, I'll send you to where the disciples of the Black Magician are. Look for the pigs around the area where I'll be sending you. You'll be able to find it by tracking them.");
    } else {
		cm.sendOk("The Black Magician and his followers. Kyrin and the Crew of Nautilus. \n They'll be chasing one another until one of them doesn't exist, that's for sure.");
		cm.dispose();
    }
}

function action(mode, type, selection) {
    cm.warp(912000000, 0);
    cm.dispose();
}
