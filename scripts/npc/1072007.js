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
-- Odin JavaScript --------------------------------------------------------------------------------
	Thief Job Instructor - Thief's Construction Site (108000400)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
*/
function start() {
    if (cm.haveItem(4031013,30)) {
        cm.sendSay("You're a true hero! Take this and the Dark Lord will acknowledge you.", false, true);
    } else {
		cm.sendSimple("You will have to collect me #b30 #t4031013##k. Good luck. \r\n#b#L1#I would like to leave#l");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
		if(selection == 1){
			cm.warp(102040000, 1);
		}else{
			cm.warp(102040000, 1);
			cm.removeAll(4031013);
			cm.gainItem(4031009, -1);
			cm.gainItem(4031012);
			cm.completeQuest(100010);
			cm.startQuest(100011);
		}
	}
    cm.dispose();
}