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
	Description: 		Quest - Meren's Second Round of Teaching
	Quest ID: 		6033
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
            qm.sendNext("I told you not to bother me unless you brought the item! Wait, you actually made it? Don't lie to me!");
		} else if(status == 2) {
            qm.sendNextPrev("I am completely shocked. This item is actually made of decent quality. Even I coulnd't make it properly! I think you have finally surpassed your master, I have now tought you everything I know.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#s1007# Maker (Level 3)\r\n#fUI/UIWindow.img/QuestIcon/8/0# 280,000");
        } else {
            if(qm.haveItem(4031980, 1)) {
                qm.gainItem(4031980, -1);
                qm.gainExp(280000);
                
                if(qm.getPlayer().getSkillLevel(1007) == 2)
                    qm.teachSkill(1007 , 3, 3, -1);
                if(qm.getPlayer().getSkillLevel(10001007) == 2)
                    qm.teachSkill(10001007 , 3, 3, -1);
                if(qm.getPlayer().getSkillLevel(20001007) == 2)
                    qm.teachSkill(20001007 , 3, 3, -1);
                if(qm.getPlayer().getSkillLevel(20011007) == 2)
                    qm.teachSkill(20011007 , 3, 3, -1);
                
                qm.completeQuest();
            } else {
                qm.sendOk("I thought you had the #r#v4031980##k with you! Bring it with you next time. I won't be satisfied until you do.");
            }
            //qm.completeQuest();
            qm.dispose();
        }
    }
 }