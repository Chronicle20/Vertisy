/**
 *	@Name: Silver Mane
 *	@Author: iPoopMagic (David)
 */
var status = 0;

function start(mode, type, selection) {
	if (mode > 0)
		status++;
	else {
		if (status == 2) {
			qm.sendOk("Raising Mimiana's egg is a big responsibility. Please come back when you feel that you are ready to grow with a new companion.");
			qm.dispose();
			return;
		}
		status--;
	}
	if (status == 0) {
		qm.sendNext("Ah! You learned the monster riding skill from me! You have a Pig. Wow! You really live with a monster! And it doesn't attack you? My research has been proven as viable! I'm as happy as a pig in mud!");
	} else if (status == 1) {
		qm.sendYesNo("You'll be happy to know that I learned how to strengthen monsters for riding a few days ago! The name tells you how strong it is. #bSilver Mane#k. Haha. Isn't it cool? What do you say? Would you like to strengthen your monster?");
	} 
	if (status == 2) {
		qm.forceStartQuest();
		qm.sendNext("I knew it! Okay, to strengthen your monsters, I'll need #b500 Pin Hov's Charms and 500 Cracked Shells. Upgraded monsters are faster and tougher, so you'll need more ingredients. When you have everything, come back and we'll continue! See you then!");
	} else if (status == 3) {
		qm.sendPrev("Ah, right. I'm sorry but it costs a lot for this type of research and I'm a bit short on funds. A donation of #b50 million mesos#k should do it. I can study further into the coexistence of humans and monsters. Thank you!");
	}
	qm.dispose();
}

function end(mode, type, selection) {
	if (mode > 0)
		status++;
	else
		status--;
	if (status == 0) {
		if (qm.haveItem(4000261, 500) && qm.haveItem(4000262, 500)) {
			if (!qm.getPlayer().getMeso() < 50000000) {
				qm.sendOk("Could you give a #B50,000,000 meso#k donation to my research?");
				qm.dispose();
				return;
			}
			if (!qm.haveItem(1902000, 1)) {
				qm.sendOk("You will need to unequip your #bHog#k so I can upgrade it!");
				qm.dispose();
				return;
			}
			qm.sendNext("Wow. You have everything that I need. Excellent! It's easy to strengthen monsters with this. I'll even change the color of the mane to gray!");
		} else {
			qm.sendOk("You don't seem to have all the items I need! I know it's a lot, but upgraded monsters require more ingredients.");
			qm.dispose();
			return;
		}
	} else if (status == 1) {
		qm.gainItem(1902000, -1);
		qm.gainItem(4000261, -500);
		qm.gainItem(4000262, -500);
		qm.gainMeso(-50000000);
		qm.gainItem(1902001, 1);
		qm.sendOk("Ok. Your Hog is changed to #bSilver Mane#k. I did it! Try it out! \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v1902001# 1 #t1902001# \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 50000 exp");
		qm.forceCompleteQuest();
		qm.dispose();
	}
}