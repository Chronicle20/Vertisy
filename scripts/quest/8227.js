/**
 *	@Name: Lost in Translation 1
 */
// NEED GMS-LIKE TEXT
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Find my brother, and give this to him so he can translate it. Don't ask why.");
	if (qm.canHold(4032032)) {
		qm.gainItem(4032032, 1);
		qm.forceStartQuest();
	}
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendNext("Ah, looks like you found my brother. Oh, and you brought me something?");
	qm.forceCompleteQuest();
	qm.dispose();
}
