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
            qm.sendNext("Welcome, welcome. Meren told me the story. I'm a busy man, I don't know why he's asking me to do this. What do you think about science I've never thought about it, and for him to ask me to teach you the fundamentals of science is ludicrous, no?");
		} else if(status == 2) {
            qm.sendNextPrev("Science is really not a hard concept to understand. It's all blended in our daily lives. Look at this mechine. Does it look complex? No! Science is the same! It's the same as me being a normal human being! Too bad the general population is scared of it. They have this perception that science is difficult to understand. I say that's a no no!!!");
        } else if(status == 3) {
            qm.sendNextPrev("Think about it. It's all about understanding, and working towards understanding the concept. It's all about you understanding the concept, then overcoming it, and then understanding it once more! That's the essence of science! Understand? Really, do you?");
        } else if(status == 4) {
            qm.sendYesNo("Physics theory? Equation? Expriment? That's all nonsense. That's not what's important! Ahhh- I've talked so much I feel like I'm going to faint. Go on now. Class dismissed.");
        } else {
            var info = qm.getPlayer().getQuestInfo(6029);
            info = info.substr(0, 1) + "1" + info.substr(2);
            qm.getPlayer().updateQuestInfo(6029, info);
            qm.completeQuest();
            qm.dispose();
        }
    }
 }