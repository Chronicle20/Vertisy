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
	NPC Name: 		Carson
	Map(s): 		Magatia - Weapon & Armor Shop
	Description: 		Quest - Carson's Fundamentals of Alchemy
	Quest ID: 		6030
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
            qm.sendYesNo("Welcome, welcome. I have already received a letter from Meren that you would be on your way. Meren told me that you needed to take a class on Basic Alchemy. Is that right?");
		} else if(status == 2) {
            qm.sendNextPrev("To define the basics of Alchemy, these two terms collide in play: #bcirculation#k and #bexchange#k. Circulation is the theory of the elements that compose the materiel changes unknowingly follow the same set of rules. Like water turns into a tree, the tree turns into fire, fire into sand, sand into gold, and gold back to water. This is the very basic part of alchemy, one that defines the basic composition for everything in this world.");
        } else if(status == 3) {
            qm.sendNextPrev("Exchange is a little bit different from circulation. Circulation deals with the power that changes the characteristics with the boundaries, but exchange deals with the concept of a constant amount of a particular composition. No item can be created from nothing, and the same goes true for alchemy in that alchemy can't be used to create something that never existed before. The core of alchemy deals with processing the existing materials with the theory of circulation to change into something new.");
        } else if(status == 4) {
            qm.sendYesNo("Your facial expression tells me I am not sure if what I just told you fully registered into your head. It's true that if you understood alchemy in that short of time, then you're the kind of genius that comes in once every 1,000 years. Anyway, the class is over, I'll let Meren know you've taken the class.");
        } else {
            var info = qm.getPlayer().getQuestInfo(6029);
            info = "1" + info.substr(1);
            qm.getPlayer().updateQuestInfo(6029, info);
            qm.completeQuest();
            qm.dispose();
        }
    }
 }