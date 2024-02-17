/**
 *	@Name: The Run-Down Huts in the Swamp
 */
function start(mode, type, selection) {
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendOk("Hmm, this is interesting. #b-You find a crumpled piece of paper with a list of the informants. Better go find them.-#k", 2);
	if (qm.canHold(4031894)) {
		qm.gainItem(4031894, 1);
		qm.forceCompleteQuest();
	}
	qm.dispose();
}