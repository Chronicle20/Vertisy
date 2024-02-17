/**
 *	@Name: Where's Violetta?
 *	@Author: iPoopMagic (David)
 */
var status = 0;

function start(mode, type, selection) {
	qm.forceStartQuest();
	qm.completeQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendNext("I found Violetta! She looks rather sad. I better find out what's going on.", 3);
	qm.forceCompleteQuest();
	qm.dispose();
}
	