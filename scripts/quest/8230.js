/**
 *	@Name: Stemming the Tide
 */
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("We have uncovered the Versalion code, and some Twisted Masters are planning an attack on New Leaf City! Help me, find the Crimsonwood Keystone.");
	qm.forceStartQuest();
	if (!qm.isQuestStarted(8223) && !qm.isQuestCompleted(8223)) {
		qm.forceStartQuest(8223);
	}
	qm.dispose();
}
function end(mode, type, selection) {
	if (!qm.isQuestCompleted(8223)) {
		qm.sendNext("Please, find it! We cannot lose even a second in this time of anxiety.");
	} else {
		qm.forceCompleteQuest();
		qm.sendNext("Great job. Now we can proceed.");
	}
	qm.dispose();
}
