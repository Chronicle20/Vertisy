function start(mode, type, selection) {
	qm.forceStartQuest();
}

function end(mode, type, selection) {
	qm.sendNext("Where did you find this?");
	qm.gainItem(4031624, -1);
	qm.forceCompleteQuest();
	qm.dispose();
}