/**
 *	@Name: Lost in Translation 3
 */
// NEED GMS-LIKE TEXT
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Wow. Get this to Jack!");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendNext("Thank you, you've done well, adventurer.");
	if (qm.haveItem(4032018, 1)) {
		qm.gainItem(4032018, -1);
		qm.gainExp(50000);
		qm.forceCompleteQuest();
	}
	qm.dispose();
}
