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
/*  Stump at the Room of Maze
    Room of Maze (240050100)
    
*/

var status;
var numbers = Array("second", "third", "fourth", "fifth");

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
            var eim = cm.getPlayer().getEventInstance();
            if(eim == null) {
                cm.sendOk("Error!");
                cm.dispose();
                return;
            }
            var itemId = 4001088;
            for(var i = 0; i < 4; i++) {
                if(cm.haveItem(itemId + i, 1)) {
                    eim.setProperty((i + 2) + "stageclear", "true");
                    eim.broadcastPlayerMsg(6, "The " + numbers[i] + " door of the maze room has opened.");
                    cm.gainItem(itemId + i, -1);
                    
                    cm.dispose();
                    return;
                }
            }
            cm.sendOk("Please bring me the keys to open the path forward.");
            cm.dispose();
		} else {
            cm.dispose();
        }
    }
}