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
/* Casey
	Henesys Game Park (100000203)
	Used to exchange game pieces for game sets.
 */

var status;
var game = 0;
var gametype = 0;

var pieces = [[4030000, 4030001], [4030000, 4030010], [4030000, 4030011], [4030001, 4030010], [4030010, 4030011],
              [4030011, 4030001], [4030013, 4030014], [4030013, 4030016], [4030014, 4030016], [4030015, 4030013],
              [4030015, 4030014], [4030015, 4030016]];

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
            cm.sendSimple("Hello there. I am the Minigame Master, Casey. What can I help you with today?\r\n#b#L0#I would like to make an Omok set.#l\r\n#b#L1#I would like to make a Match Card set.#l\r\n#b#L2#Nothing, I think I will just leave now.#l");
		} else if(status == 1) {
            game = selection;
            if(game == 0) {
                var outStr = "What type of omok game would you like to make?\r\n#b";
                for(var i = 0; i < 12; i++) {
                    outStr += "#L" + i + "##t" + (4080000 + i) + "#\r\n";
                }
                cm.sendSimple(outStr);
            } else if(game == 1) {
                cm.sendYesNo("Would you like to make a set of match cards? To make a set of match cards I will need the following materials.\r\n\r\n#i4030012# 25 #t4030012#s\r\n#i4031138# 50,000 Mesos\r\n\r\nWould you like to make a set of match cards?");
            } else {
                cm.dispose();
            }
        } else if(status == 2) {
            if(game == 0) {
                if(selection > 11 || selection < 0) {
                    cm.dispose();
                    return;
                }
                gametype = selection;
                var piece = pieces[gametype];
                
                cm.sendYesNo("Would you like to make a #t" + (4080000 + gametype) + "#? To make it I will need the following materials.\r\n\r\n#i" + piece[0] + "# 10 #t" + piece[0] + "#\r\n#i" + piece[1] + "# 10 #t" + piece[1] + "#\r\n#i4030009# 1 #t4030009#\r\n#i4031138# 50,000 Mesos\r\n\r\nWould you like to make the omok set?");
               
            } else if(game == 1) {
                if(cm.haveItem(4030012, 25) && cm.getMeso() >= 50000) {
                    cm.gainItem(4030012, -25);
                    cm.gainMeso(-50000);
                    cm.gainItem(4080100);
                    cm.sendOk("There you go, 1 set of match cards. I wish you luck while playing.");
                } else {
                    cm.sendOk("I'm sorry, but I can't make a set of match cards for you unless you bring me all of the required materials.");
                }
                cm.dispose();
            }
        } else if(status == 3) {
            if(game != 0 ) {
                cm.dispose();
                return;
            }
            var piece = pieces[gametype];
            
            if(cm.haveItem(piece[0], 10) && cm.haveItem(piece[1], 10) && cm.haveItem(4030009, 1) && cm.getMeso() >= 50000) {
                cm.gainItem(piece[0], -10);
                cm.gainItem(piece[1], -10);
                cm.gainItem(4030009, -1);
                cm.gainMeso(-50000);
                cm.gainItem(4080000 + gametype);
                cm.sendOk("There you go, one brand new #t" + (4080000 + gametype) + "#. I wish you luck while playing.");
            } else {
                cm.sendOk("I'm sorry, but I can't make an omok set for you unless you bring me all of the required materials.");
            }
        } else {
            cm.dispose();
        }
    }
}