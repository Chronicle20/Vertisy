function start(mode, type, selection) {
	qm.changeJobById(2218);
	qm.gainSP(5);
	qm.forceStartQuest();
	qm.forceCompleteQuest();
	qm.message("The Dragon Grew!");
	qm.message("The Dragon can now use a new skill");
	qm.dispose();
}