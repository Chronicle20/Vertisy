/**
 *	@Name: Save the Snowman! (Holiday PQ Pre-Quest)
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		qm.sendOk("Are you plotting against us with Scrooge?");
		qm.dispose();
		return;
	}
	if (status == 0) {
		qm.sendNext("Okay... so here's our plan to defeat Scrooge and his dastardly plans. The Force of the Spirit I gave you is an item packed with mana. It's an item you'll definitely use at the map I am about to send you. In order to do that, you'll also have to bring your party members with you as well. You should bring your party members here or form one right now!");
	} else if (status == 1) {
		qm.sendYesNo("Would you like to move forward?");
	} else if (status == 2) {
		qm.forceStartQuest();
		qm.sendNext("We are now entering the entrance of the Happyville Holy Ground.");
	} else if (status == 3) {
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.forceCompleteQuest();
	qm.dispose();
}