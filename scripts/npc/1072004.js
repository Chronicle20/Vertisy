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


function start() {
    if (cm.haveItem(4031013,30)) {
        cm.sendNext("Ohhhhh.. you collected all 30 Dark Marbles!! It should have been difficult.. just incredible! Alright. You've passed the test and for that, I'll reward you #bThe Proof of a Hero#k. Take that and go back to Perion.");
    } else {
        cm.sendSimple("You will have to collect me #b30 #t4031013##k. Good luck. \r\n#b#L1#I would like to leave#l");
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
		if(selection == 1){
			cm.warp(102020300, 0);
		}else{
			cm.warp(102020300, 0);
			cm.removeAll(4031013);
			cm.gainItem(4031008, -1);
			cm.gainItem(4031012);
		}
	}
	cm.dispose();
}