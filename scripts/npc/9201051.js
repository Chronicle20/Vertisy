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
 *	@Name: John Barricade
 *	@Map: Lobby (Bigger Ben)
 *	@Description: Forging (Crystal Ilbis, etc.) - unfinished
 */

var status;
 
function start() {
    status = -1;
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
        if (status == 0) {
			cm.sendSimple("#b#L0#I think I found an Artifact. Can you take a look at it?#l\r\n#k");
		} else if (selection == 0) {
			cm.sendNext("Did you find an ancient item?");
		} else if (status == 1) {
			cm.sendSimple("#b#L10#Ancient Artifacts#l\r\n#k");
		} else if (selection == 10) {
			cm.sendSimple("What have you got there?! I see you've been exploring MapleStory offline! Good work! You've found a very valuable object! The item you've found is from a bygone era... we treasure hunters call them Artifacts.#b\r\n#L20#Tell me more about Artifacts!#l\r\n#L21#This old thing? I'm sure it's worth as much as my sock!#l\r\n#k");
		} else if (selection == 20) {
			cm.sendNext("Artifacts are ancient items of power, usually created by an ancient civilization for some special purpose. There are several types of them. I've been exploring the breadth of Maple World, trying to learn what each of them was used for. While their original use currently still eludes us, with the right know-how, the energies inside an Artifact can be unlocked and transferred into the formation of a mundane object such as a sword or a helmet, imbuing it with great power.");
		} else if (selection == 21) {
			cm.sendOk("Fine then. I guess you do not wish to know about artifacts. Your loss.");
			cm.dispose();
		} else if (status == 2) {
			cm.sendYesNo("Say, are you interested on making one for yourself?");
		} else if (status == 3) {
			cm.sendSimple("Artifacts are ancient items of power, usually created by an ancient civilization for some special purpose. There are several types of them. I've been exploring the breadth of Maple World, trying to learn what each of them was used for. While their original use currently still eludes us, with the right know-how, the energies inside an Artifact can be unlocked and transferred into the formation of a mundane object such as a sword or a helmet, imbuing it with great power.\r\n#b#L30#Can you tell me more about the Artifacts that I have?#l\r\n#L31#Can I make anything with the Artifacts in my inventory?#l\r\n#k");
		} else if (selection == 30) {
			cm.sendOk("I've stated so much about them already. Unfortunately, at the moment the only item I know how to make are crystal illbis.");
			cm.dispose();
		} else if (selection == 31) {
			if (cm.haveItem(4031758) && cm.haveItem(4031917)) {
				cm.sendSimple("Zounds! You've got something! With the Artifacts in your possession (Naricain Jewel Crystal Shard ) I think you can use them to synthesize items below. If you want to know more about the inventions, you can click on the names."
				+ "\r\n#b#L40#Crystal Illbi Throwing-Stars#l\r\n" + 
				"\r\n#L41#Skip that...I'm read to make an item from my Artifacts!#l#k\r\n");
			} else {
				cm.sendOk("Hm, seems like you don't have an Artifact in possession.");
				cm.dispose();
			}
		} else if (selection == 40) {
			cm.sendNext("Even more lethal than the already deadly Ilbi throwing stars are the Crystal Ilbis. It's sad that these were created by accident when a Hermit fought an Elder Wraith, which to tell you, is nothing like a Jr. Wraith. After defeating the ghost, the thief was amazed to find that the deathly coldness of the wraith's incorporeal form had turned the steel of his stars into frozen crystal. If I guess correctly, the dark energies trapped within a #bNaricain Jewel#k and a #bCrystal Shard#k can be used to duplicate this effect and transform a set of Ilbis into these deadly crystal stars.");
		} else if (selection == 41 || status == 4) {
			status = 5;
			cm.sendSimple("Do you have both of these Artifacts? If so, I can draw up a Manual to create a set of Crystal Ilbis for you! Just a warning though, I will have to tinker with the Artifacts to do this, so if you decide to do this, there's no turning back! \r\n#b#L50#Create Crystal Ilbi Throwing-Stars.#l\r\n#L51#No...I haven't made my mind up yet. I think I'll hold on to these Artifacts for now and see what else I can make.#l#k");
		} else if (status == 5) {
			if (selection == 50) {
				cm.sendNext("Let me see those Artifacts!\r\nBarricade takes the #bNaricain Jewel#k and #bCrystal Shard#k from you. As John excitedly examines and fiddles with the Artifacts, he does some quick calculations and scribbles up a rough manual for building the throwing stars.\r\nHere you go... take this #bCrystal Ilbi Forging Manual#k! This will show you how to build it! You're also going to need 1 set of Ilbi Throwing Stars, 7 LUK Crystals, and 1 Dark Crystal. Unfortunately, my knowledge ends at how ancient objects were created, and not at making the item itself. But don't worry, I know exactly the person who can do that. Take these items along with the Manual to my friend Spindle in Omega Sector! That kid can make anything! From this manual, he'll know how to build these stars from you! Good luck and let me know how it turns out.");
				if (cm.canHold(4031912)) {
					cm.gainItem(4031758, -1);
					cm.gainItem(4031917, -1);
					cm.gainItem(4031912, 1);
				} else {
					cm.playerMessage(1, "Your inventory is full.");
				}
			} else if (selection == 51) {
				cm.sendOk("Alright then, but you're truly missing out on the powers ancient items hold!");
			}
			cm.dispose();
		}
    }
}