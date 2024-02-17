function start(mode, type, selection) {
	qm.changeJobById(2210);
	qm.gainSP(3);
	qm.forceCompleteQuest();
	qm.message("The Dragon Grew!");
	qm.message("The Dragon can now use a new skill");
	qm.dispose();
}