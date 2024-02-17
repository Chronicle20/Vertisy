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
/*
 *@Author:  Moogra
 *@NPC:     4th Job Warrior Advancement NPC
 *@Purpose: Handles 4th job.
 */

function start() {
	if(cm.getLevel() < 120 || Math.round(cm.getJobId() / 100) != 1) {
		cm.sendOk("Please don't bother me right now, I am trying to concentrate.");
		cm.dispose();
    } else if (!cm.isQuestCompleted(6904)) {
        cm.sendOk("You have not yet passed my trials. I can not advance you until you do so.");
        cm.dispose();
    } else if ( cm.getJobId() % 100 % 10 != 2) {
        cm.sendYesNo("You did a marvellous job passing my test. Are you ready to advance to your 4th job?");
	} else {
		cm.sendOk("Please don't bother me right now, I am trying to concentrate.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
    if (mode >= 1 && cm.getJobId() % 100 % 10 != 2) {
		cm.changeJobById(cm.getJobId() + 1);
		cm.gainSP(3);
		cm.gainItem(2280003, 1);
	}
    cm.dispose();
}