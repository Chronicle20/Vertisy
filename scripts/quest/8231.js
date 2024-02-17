/*
 * @quest: Fool's Gold (8231)
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
		qm.sendSimple("Ah! A fellow hunter, eh? Well, we've gotten some strange reports of all this stuff going on in the Phantom Forest - people being attacked by bags of money, creepy ghosts, even a strange sighting of a Yeti-like creature. To help put a stop to this, Icebyrd and I are offering bounties upon certain creatures that are troublesome. Complete all of the Bounties, and I'll deputize you! Are you interested?\r\n#b#L1#Absoultely. Monsters are born to perish by my weapon.#l\r\n");
	} else if (selection == 1) { 
		qm.sendSimple("That's the spirit I like to see! I'll warn you now though, this won't be easy--the little buggers I'm about to send  you after are tricky to beat. ARe you sure you're ready to brave the dangers of the Phantom Forest and hunt these little devils?\r\n#b#L10#I know what I'm in for. Tell me what I need to know to kill these things.#l\r\n#L11#On second thought, maybe I'd better see Miki for some potions first...#l\r\n");
	} else if (selection == 10) {
		qm.sendSimple("That's what I like to hear! You remind me a bit about myself when I was still training. Being a Thief is no easy task, and in my youth I scammed and took quite a few mesos in my time-this is my way of making up for it. Protecting others and helping the city grow, even helping travelers improve their skills, it all adds up in the end. I'd rather protect the weak than prey on them.\r\n#b#L20#I can understand that. So, what's the deal with these Leprechauns?#l\r\n");
	} else if (selection == 11) {
		qm.sendNext("Come see me when you are ready.");
		qm.dispose();
	} else if (selection == 20) {
		qm.sendNext("Well, I ventured out to the Phantom Forest on a mission to see what was out there. We'd gotten some strange reports about what was going on, and Icebyrd was rather worried. There's been some strange things reported recently, and I think he's getting a bit nervous. At any rate, I saw many, many strange things out there in the forest. In particular, I remember coming across a rather large bag of mesos, ripe for taking. I assumed someone had left it and moved on.");
	} else if (status == 4) {
		qm.sendSimple("As I bent down to pick it up, I thought I heard laughter. Very faint, but I know it was there. I immediately faded from view, just in case there was anything around. I was alert, ready to strike, and then... nothing. No creature came. Now at ease, I returned to normal sight and reached for the meso bag. That's when it hit me. Literally!\r\n#b#L30#What do you mean, 'literally'?#l\r\n");
	} else if (selection == 30) {
		qm.sendNext("The second I touched the bag, this thing...a legeless devil in a raggedy top-hat with a ..lollipop for a weapon sprung out! I immediately leapt back, weapon up. It seemed to float on air, disappearing and reappearing, as if it were toying with me. And the one thing I do not like to be, is toyed with.");
	} else if (status == 5) {
		qm.sendNextPrev("I focused myself, feeling my body grow lighter, increasing my speed and agility. I began attacking, making the devilish thing flinch. It seemed hurt, then I saw it swing its candy-like weapon at me- at first it seemed easy to dodge, but then, to my horror, it grew larger as it approached, until I realized it was too large. I had taken the wrong angle-it struck me! I was dazed for a moment. And then I saw it inhale and blow this strange energy at me. I rolled to the side, barely avoiding it! But I had recovered my strength now. I sent a flurry of stars at it, and used my claw to finally vanquish the monster...it left no trace save for a few mesos, and a strange silver clover on the round. A rather surprising fight to say the least.");
	} else if (status == 6) {
		qm.sendSimple("It's a good thing I remembered the best advice the Dark Lord gave me. True warriors wait for the perfect time to strike.\r\n#b#L40#Wow. Sounds like a crazy fight, I can see why you'd be worried about those things!#l\r\n");
	} else if (selection == 40) {
		qm.sendNext("Precisely. The trickery that this creature employs is nefarious in its execution. I don't want those things anywhere near New Leaf City. Thus the bounty. The requirement for this one is simple. You must bring me #b30 of those strange silver clovers#k... hmm, that's sort of a mouthful- lets just call them #bLucky Charms#k. When you're done, be sure to return to me!\r\n");
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
		qm.sendOk("Back already, I see! Let's see how you did... 10,20...30! Excellent work! You've done an excellent service to the citizens of New Leaf City. Please take these mesos as a token of appreciation! Be sure to return once you rest- I have plenty of bounties available!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/7/0# 1000000 mesos\r\n#fUI/UIWindow.img/QuestIcon/8/0# 5400 exp");
		qm.gainExp(5400 * 5);
		qm.getPlayer().gainMeso(1000000, true);
		qm.gainItem(4032031, -30);
		qm.completeQuest();
		qm.dispose();
	}
}