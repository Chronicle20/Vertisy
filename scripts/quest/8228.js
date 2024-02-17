/**
 *	@Name: Lost in Translation 2
 */
// NEED GMS-LIKE TEXT
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Hmm, this looks a little complicated for me. You'd better talk to #bElpam Gorlam#k in NLC.");
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendNext("What do we have here? Oh, this is neat. Here, I've translated it for you. Go and give this back to #bJohn#k.");
	if (qm.haveItem(4032032, 1)) {
		qm.gainItem(4032032, -1);
		if (qm.canHold(4032018)) {
			qm.gainItem(4032018, 1);
			qm.forceCompleteQuest();
		}
	}
	qm.dispose();
}
