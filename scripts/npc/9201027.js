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
 * 9201027 - Nana(P)
 * 
 * 9201001 = 4031367
 * 9201023 = 4031368
 * 9201024 = 4031369
 * 9201025 = 4031370
 * 9201026 = 4031371
 * 9201027 = 4031372
 *
 */
var itemid = 4000018;
var amount = 40;
var proofoflove = 4031372;

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (!cm.haveItem(itemid, amount)) {
            if (status == 0) {
                cm.sendNext("Hey, you look like you need proofs of love? I can get them for you.");
            } else if (status == 1) {
                cm.sendNext("All you have to do is bring me " + amount + " #b#t" + itemid + "##k");
                cm.dispose();
            }
        } else {
            if (status == 0) {
            	if(cm.canHold(proofoflove)){
	                cm.sendNext("Wow, you were quick! Heres the proof of love...");
	                cm.gainItem(itemid, -amount)
	                cm.gainItem(proofoflove, 1);
	                cm.dispose();
                }else{
                	cm.sendOk("Make room in your ETC tab.");
                	cm.dispose(); 
                }
            }
        }
    }
}