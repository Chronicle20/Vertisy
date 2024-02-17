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
 *	@Name: Arec (Thief 3rd Job Instructor)
 *	@Modified: iPoopMagic (David) - GMS-like text
 */
status = -1;
var job;
var sel;
actionx = {"Mental" : false, "Physical" : false};

function start() {
    if (!(cm.getPlayer().getLevel() >= 70 && parseInt(cm.getJobId() / 100) == 4)){
        cm.sendNext("Hi there.");
        cm.dispose();
        return;
    }
    if (cm.haveItem(4031058))
        actionx["Mental"] = true;
    else if (cm.haveItem(4031057))
        actionx["Physical"] = true;
    if(cm.getJobId() == 432){
		cm.sendSimple("Anything you want from me?\r\n#b#L0#I want to become a Blade Lord.#l\r\n#L1#Please allow me to do the Zakum Dungeon Quest.");
	}else cm.sendSimple("Anything you want from me?#b" + (cm.getJobId() % 10 == 0 ? "\r\n#L0#I want to make the 3th job advancement." : "") + "\r\n#L1#Please allow me to do the Zakum Dungeon Quest.");
}

//433: https://www.youtube.com/watch?v=tr8dQADYkQ4
function action(mode, type, selection){
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if(mode != 1 || (status > 2 && !actionx["Mental"]) || status > 3){
        if (mode == 0 && type == 1)
            cm.sendNext("Make up your mind.");
        cm.dispose();
        return;
    }
    if (actionx["Mental"]){
        if (status == 0)
			if(cm.getJobId() == 432){
				cm.sendSay("Ah... You found the Holy Ground, I see. I... I didn't think you would pass the second test. I will take the necklace now before you advance to your 3rd job.", false, true);
			}else cm.sendNext("Great job completing the mental part of the test. You have wisely answered all the questions correctly. I must say, I am quite impressed with the level of wisdom you have displayed there. Please hand me the necklace first, before we takeon the next step.");
        else if (status == 1)
            if(cm.getJobId() == 432){
				cm.sendYesNo("Okay, you will now become an even stronger thief. Since you have already chosen the path of a Dual Blade, there is no need for you to choose a job to advance to. Do you wish to make your job advancement now?");
			}else cm.sendYesNo("Okay! Now, you'll be transformed into a much more powerful thieve through me. Before doing that, though, please make sure your SP has been thoroughly used, You'll need to use up at least all of SP's gained until level 70 to make the 3rd job advancement. Oh, and since you have already chosen your path of the occupation by the 2nd job adv., you won't have to choose again for the 3rd job adv. Do you want to do it right now?");
        else if (status == 2) {
            /*if (cm.getPlayer().getRemainingSp() > 0)
                if (cm.getPlayer().getRemainingSp() > (cm.getLevel() - 70) * 3) {
                    cm.sendNext("Please, use all your SP before contining.");
                    cm.dispose();
                    return;
                }*/
            if (cm.getJobId() % 10 == 0 || cm.getJobId() == 432) {
                cm.gainItem(4031058, -1);
                cm.changeJobById(cm.getJobId() + 1);
                cm.gainSP(1);
                cm.getPlayer().removePartyQuestItem("JBQ");
				if(cm.getJobId() == 433){
					cm.teachSkill(4331002, 0, 30);
					cm.teachSkill(4331005, 0, 20);
				}
            }
			if(cm.getJobId() == 433){
				cm.sendSay("You have officially been anointed as a #bBlade Lord#k. You'll be able to learn various advanced attack skills, like #bOwl Spirit#k, which can one-shot kill enemies and #bBlood Storm#k, which can attack multiple enemies quickly.", false, true);
			}else if (cm.getJobId() % 100 / 10 == 1) {
				cm.sendNext("You have officially been anointed as a #bHermit#k from this point forward. The skill book introduces a slew of new offensive skills for Hermits, using shadows as a way of duplication and replacement. You'll learn skills like #bShadow Meso#k (replace MP with mesos and attack monsters with the damage based on the amount of mesos thrown) and #bCopycat#k (create a shadow that mimics your every move, enabling you to attack twice). Use those skills to take on monsters that may have been difficult to conquer before.");
			} else {
				cm.sendNext("You have officially been anointed as a #bChief Bandit#k from this point forward. One of the new additions to the skill book is a skill called #bBand of Thieves#k, which lets you summon fellow Bandits to attack multiple monsters at once. Chief Bandits can also utilize mesos in numerous ways, from attacking monsters (#bMeso Explosion#k, which explodes mesos on the ground) to defending yourself (#bMeso Guard#k, which decreases damage done to you).");
			}
        } else if (status == 3) {
            cm.sendNextPrev("Here is some SP and AP to get you started. You have now become a powerful, powerful thief, indeed. Remember, though, that the real world will be awaiting your arrival with even tougher obstavles to overcome. Once you feel like you cannot train yourself to reach a higher place, then, and only then, come see me. I'll be here waiting.");
        }
    } else if (actionx["Physical"]){
        if (status == 0)
            cm.sendNext("Great job completing the physical part of the test. I knew you could do it. Now that you have passed the first half of the test, here's the second half. Please give me the necklace fist");
        else if (status == 1){
            if (cm.haveItem(4031057)){
                cm.gainItem(4031057, -1);
                cm.getPlayer().setPartyQuestItemObtained("JBQ");
            }
            cm.sendNextPrev("Here's the 2nd half of the test. This test will determine whether you are smart enough to take the next step towards greatness. There is a dark, snow-covered area called the Holy Ground at the snowfield in Ossyria, where even the monsters can't reach. On the center of the area lies a huge stone called the Holy Stone. You'll need to offer a special item as the sacrifice, then the Holy Stone will test your wisdom right there on the spot.");
        } else if (status == 2)
            cm.sendNextPrev("You'll need to answer each and every question given to you with honesty and conviction. If you correctly answer all the questions, then the Holy Stone will formally accept you and hand you #b#t4031058##k. Bring back the necklace, and I will help you to the next step forward. Good luck.");
    } else if (cm.getPlayer().gotPartyQuestItem("JB3") && selection == 0 && status != 2){
        cm.sendNext("Go, talk with #b#p1052001##k and bring me #b#t4031057##k.");
        cm.dispose();
    } else if (cm.getPlayer().gotPartyQuestItem("JBQ") && selection == 0){
        cm.sendNext("Go, talk with #b#p2030006##k and bring me #b#t4031058##k.");
        cm.dispose();
    } else {
        if (sel == undefined)
            sel = selection;
        if (sel == 0){
            if (cm.getPlayer().getLevel() >= 70){
				if(cm.getJobId() % 10 == 0){
					if (status == 0)
						cm.sendYesNo("Welcome. I'm #b#p2020011##k, the chief of all thieves, ready to share my street knowledge and hard knock life to those willing to listen. You seem ready to make the leap forward, the one ready to take on the challenges of the 3rd job advancement. Too many thieves have come and gone, unable to meet the standards of achieving the 3rd job advancement. What about you? Are you ready to be tested and make the 3th job advancemente?");
					else if (status == 1){
						cm.getPlayer().setPartyQuestItemObtained("JB3");
						cm.sendNext("Good. You will be tested on two important aspects of the thief: strength and wisdom. I'll now explain to you the physical half of the test. Remember #b#p1052001##k from Kerning City? Go see him, and he'll give you the details on the first half of the test. Please complete the mission, and get #b#t4031057##k from #p1052001#.");
					} else if (status == 2)
						cm.sendNextPrev("The mental half of the test can only start after you pass the physical part of the test. #b#t4031057##k will be the proof that you have indeed passed the test. I'll let #b#p1052001##k in advance that you're making your way there, so get ready. It won't be easy, but I have the utmost faith in you. Good luck.");
				}else if(cm.getJobId() == 432){
					if(status == 0){
						cm.sendYesNo("Ah, you wish to make your 3rd job advancement? I have the power to make you stronger but first I must test your persistence. Do you wish to take my test?");
					}else if(status == 1){
						cm.getPlayer().setPartyQuestItemObtained("JB3");
						cm.sendSay("There are two things you must prove you possess: strength and intelligence. First, I shall test your strength. do you remember #bDark Lord#k in Kerning City, who gave you your 1st job advancement? He will give you a mission. Complete that mission and bring back #bThe Necklace of Strength#k from Dark Lord.", false, true);
					}else if(status == 2){
						cm.sendSay("The second test will determine your intelligence, but first pass this one and bring back #bThe Necklace of Strength#k. I will let #bDark Lord#k know that you are coming. Good luck...", true, false);
					}
				}
			}
        } else {
            if (cm.getPlayer().getLevel() >= 50){
            	cm.sendNext("Ok, go.");
            	cm.getPlayer().setProgressValue("zakpermission", "true");
            }else
                cm.sendNext("You're weak.");
            cm.dispose();
        }
    }
}