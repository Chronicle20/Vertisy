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
/* NPC Base
 Map Name (Map ID)
 LPQ boss stage
 */

var prizes = Array(2040602, 2040602, 2040602, 2040601, 2040601, 2040601, 2040601, 2040802, 2040002, 2040402,
        2040505, 2040505, 2040505, 2040505, 2040502, 2044501, 2044601, 2044701,
        2041019, 2041016, 2041022, 2041013, 2041007, 2043301, 2040301, 2040801, 2040001, 2040504,
        2040501, 2040513, 2043101, 2044001, 2044401, 2040701, 2044301, 2043801, 2043701, 2040401,
        2040803, 2040804, 1102000, 1102001, 1102003, 1102004, 1102011, 1102012, 1102013, 1102014);
var item;
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode === 1)
            status++;
        else
            status--;
        if (status === 0 && mode === 1) {
            cm.sendNext("Congratulations on sealing the dimensional crack!");
        } else if (status === 1) {
            item = prizes[Math.floor(Math.random() * prizes.length)];
            if (cm.beforeGetItem(item)) {
                cm.sendNextPrev("For all of your hard work, I have a gift for you! Here take this prize.");
            }
//            if (cm.canHold(1102003) && cm.canHold(2040602)) { //ori, this doesnt cover every scenario, i like it tho :p
//                item = prizes[Math.floor(Math.random() * prizes.length)];
//                cm.sendNextPrev("For all of your hard work, I have a gift for you! Here take this prize.");
//            } else {
//                cm.sendOk("It seems you don't have a free slot in your #rEquip#k and #rUse#k inventories. Please make room and try again.");
//                cm.dispose();
//            }
        } else if (status === 2) {
            if (cm.getPlayer().checkEquippedFor(1022073)) { //shhh :p
                //randomize items if the player is wearing the glasses
                cm.gainItem(false, item, 1, true, true);
            } else {
                cm.gainItem(false, item, 1);
            }
//            cm.gainItem(item, 1); //ori
            cm.warp(221024500);
        }
    }
}