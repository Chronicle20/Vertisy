var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Hm, what is this?", 3);
    qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.sendNext("Ah, a clue! Let's go back to Scadur.", 3);
	qm.forceCompleteQuest();
	qm.dispose();
}