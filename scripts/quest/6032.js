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
	Author : 		Twdtwd
	NPC Name: 		Moren
	Map(s): 		Magatia - Weapon & Armor Shop
	Description: 		Quest - Carson's Fundamentals of Alchemy
	Quest ID: 		6032
*/
var status = 0;

function end(mode, type, selection) {
    if (mode < 0 || status < 0)
        qm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1 && mode == 1) {
            qm.sendNext("Okay, time for lab! Today, we're going to make Weight Earrings! In order to make Weight Earrings, you'll need 4 bars of steel, 1 Weak Monster Crystal, and 1 bar of bronze. Now look closely. Making Weight Earrings involves the ductility theory of gravity.");
		} else if(status == 2) {
            qm.sendNextPrev("(When Meren completed the work, the Pentagram of Alchemy started emitting a bright beam of light and it blinded each and every one of us.)");
        } else if(status == 3) {
            qm.sendYesNo("Okay did you understand all that? Class dismissed.");
        } else {
            var info = qm.getPlayer().getQuestInfo(6029);
            info = info.substr(0, 2) + "1" + info.substr(3);
            qm.getPlayer().updateQuestInfo(6029, info);
            //qm.getPlayer().dropMessage(6, info + " " + info2);
            qm.completeQuest();
            qm.dispose();
        }
    }
 }