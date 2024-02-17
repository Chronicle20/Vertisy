/**
 *	@Name: The Brewing Storm
 */
// NEED GMS-LIKE TEXT
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Bring me 10 Stormbreaker Badges.");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.haveItem(4032006, 10)) {
		qm.sendNext("Good job! Let us proceed.");
		qm.gainExp(85000);
		qm.forceCompleteQuest();
		qm.gainItem(4032006, -10);
	} else {
		qm.sendNext("Please find 10 Stormbreaker Badges.");
	}
	qm.dispose();
}
