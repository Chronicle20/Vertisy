/**
 *	@Name: Re-acquiring the Alcadno Cape
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) { // Need GMS text
			qm.sendAcceptDecline("Howdy, you must be new. Would you like to learn more about the #rMonster Book#k?");
		} else if (status == 1) {
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) { // Need GMS text
			qm.sendNext("The #rMonster Book#k is a tool you can use to find monster stats, drops, and other information about a particular mob.");
		} else if (status == 1) {
			qm.sendPrev("In order to do that, you must pick up a Monster Card, dropped by all monsters for their specific one. If you pick up enough of them, you'll fill up this book with some very handy information!");
			qm.forceCompleteQuest(); // no rewards LOL
			qm.gainExp(1); // but we're nice
			qm.dispose();
		}
	}
}