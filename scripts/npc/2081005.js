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
/* Keroben
	Cave of Life - Entrance (240040700)
	Checks if the player is currently transformed, and if so lets them inside the Cave of Life.
 */

var status;
var allow = false;
 
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
        var buff = cm.getPlayer().getBuffSource(Packages.client.MapleBuffStat.MORPH);
        if(buff != 2210003) {
          // They are not morphed. Warp them out.
          cm.sendNext("Human! You can't be here. I am going to have to kick you out.");
        } else {
          // They are morphed. Let them in.
          allow = true;
          cm.sendNext("Oh, my brother! Don't worry about human invasion's. I'll protect you all. Come on inside!");
        }
      } else if(status == 1) {
        if(allow) {
          cm.cancelItem(2210003);
          cm.warp(240050000, "out00");
        } else {
          cm.warp(240040600, "east00");
        }
        cm.dispose();
      } else {
        cm.dispose();
      }
    }
}