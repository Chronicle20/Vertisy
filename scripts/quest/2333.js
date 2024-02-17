/**
 *	@Name: The Story of Betrayal
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	if (mode < 0) {
        qm.dispose();
        return;
    }
	(mode > 0 ? status++ : status--);
	if (status == 0) {
		qm.sendNext("Ah, you're the brave hero that has come to save me. #b#h0##k! I knew you'd come. *Sniff sniff*");
	} else if (status == 1) {
		qm.sendNextPrev("Are you alright, Princess?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("Yes, I'm fine. But what about Mushroom Kingdom? And my father?");
	} else if (status == 3) {
		qm.sendNextPrev("Everything is okay, we put everything back the way it's supposed to be. Now all it needs is you.", 2);
	} else if (status == 4) {
		qm.sendNextPrev("How dare you step foot in here! You're terribly mistaken if you think this is how it ends!", 1, 1300001);
	} else if (status == 5) {
		qm.sendNextPrev("Watch out! It's dangerous. He's trying to summon the one who's behind all of this!");
	} else if (status == 6) {
		qm.sendNextPrev("The one who is behind all of this isn't who you think!", 2);
	} else if(status == 7) {
        qm.sendNextPrev("Silence! He'll be here soon!", 1, 1300001);
    } else if(status == 8) {
        qm.sendNextPrev("#bThe Prime Minister#k! Please defeat the Prime Minister!");
    } else if (status == 9) {
		qm.earnTitle("New Mission: Defeat the Prime Minister!");
		qm.getPlayer().getMap().spawnMonsterOnGroundBelow(3300008, 200, 142);
		qm.forceStartQuest(2333);
		qm.dispose();
	} else {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode < 0) {
        qm.dispose();
        return;
    }
	(mode > 0 ? status++ : status--);
    if(status == 0) {
        qm.sendNext("You did it, #b#h ##k! I don't know how to thank you.");
    } else if(status == 1) {
        qm.sendNextPrev("No way! Even the Prime Minister?!", 1, 1300001);
    } else if(status == 2) {
        qm.sendNextPrev("#bKing Pepe#k! This is where your foolhardy dreams end! I will spare your life, but you must head back to where you came from. Go back to #bIce Land#k at once!", 2);
    } else if(status == 3) {
        qm.sendNextPrev("Wait! Before you go, I must get something that can serve as evidence that I defeated you in battle.", 2);
    } else if(status == 4) {
        qm.sendNextPrev("Grrrr", 1, 1300001);
    } else if(status == 5) {
        qm.sendNextPrev("Give me your crown! Princess, please take the crown.", 2);
    } else if(status == 6) {
        qm.sendNextPrev("You mark my words. This isn't over between us!", 1, 1300001);
    } else if(status == 7) {
        qm.warp(106021700);
        qm.gainExp(3000);
        qm.forceCompleteQuest();
        qm.dispose();
    } else {
        qm.dispose();
    }
}	