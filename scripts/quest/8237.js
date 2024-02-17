/*
 * @quest: Catch a bigfoot by a toe (8237)
 * @npc: Lita Lawless
 */
var status = -1;

function start(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendSimple("Ah! A fellow hunter, eh? Well, we've gotten some strange reports of all this stuff going on in the Phantom Forest - people being attacked by bags of money, creepy ghosts, even a strange sighting of a Yeti-like creature. To help put a stop to this, Icebyrd and I are offering bounties upon certain creatures that are troublesome. Complete all of the Bounties, and I'll deputize you! Are you interested?\r\n#b#L0#You bet! What do you have for me?#l\r\n");
	} else if (selection == 0) {
		qm.sendSimple("Well, I'll be honest. This one is hard. You're going up against Big Foot, one of the toughest mobs in New Leaf City. Are you ready?\r\n#b#L10#I'm up for it. Now way I'll end up like those other noobs.#l\r\n#L11#On second though, maybe I'd better see Miki for some potions first...#l\r\n");
	} else if (selection == 10) {
		qm.sendSimple("Famous last words, my friend. It is a very strong mob. I need a Big Foot Toe as proof.\r\n#b#L20#I'll do it.#l\r\n");
	} else if (selection == 11) {
		qm.sendOk("Come back when you're ready...");
		qm.dispose();
	} else if (selection == 20) {
		qm.sendOk("Go and good luck!");
		qm.forceStartQuest();
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
	if (mode != 1) {
	    if(type == 1 && mode == 0)
		    status -= 2;
		else{
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendOk("You're alive! I mean, you made it back! That's great...and there's only a few dozen scratches on you-nothing a Ginger Ale won't fix in a jiffy. And you brought Bigfoot's Toe! How was the fight? Take this reward-you've certainly earned it!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/7/0# 3000000 mesos\r\n#fUI/UIWindow.img/QuestIcon/8/0# " + 200000 + " EXP");
		qm.gainItem(4032013, -1);
		qm.gainExp(200000);
		qm.forceCompleteQuest();
		qm.dispose();
	}
}