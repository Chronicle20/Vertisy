/**
 *	@Description: You lost the crumpled paper and need another one.
 */
var status = -1;

function start(mode, type, selection) {
 	if (!qm.canHold(4031894, 1)) {
	    qm.sendNext("Please make some space in your inventory.");
	} else {
	    qm.sendNext("Seriously, you lost it again? -scowls-");
		qm.forceStartQuest();
	}
	qm.dispose();
}
function end(mode, type, selection) {
 	if (!qm.canHold(4031894, 1)) {
	    qm.sendNext("Please make some space..");
	} else {
	    qm.sendNext("There. Don't lose it again. That contains valuable information.");
	    qm.gainItem(4031894, 1);
	    qm.forceCompleteQuest();
	}
	qm.dispose();
}
