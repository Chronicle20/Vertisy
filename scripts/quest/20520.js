/**
 *	@Description: Cygnus Mount (Lv. 50)
 */

function start(mode, type, selection) {
    qm.gainItem(4032208, 1);
    qm.forceStartQuest();
	qm.resetQuest(20522); //bc can't start the next quest otherwise...
}

function end(mode, type, selection) {
}