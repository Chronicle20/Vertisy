function start(mode, type, selection) {
	qm.changeJobById(2214);
	qm.gainSP(3);
	qm.teachSkill(22140000, 0, 15);
	qm.teachSkill(22141002, 0, 15);
	qm.forceStartQuest();
	qm.forceCompleteQuest();
	qm.message("The Dragon Grew!");
	qm.message("The Dragon can now use a new skill");
	qm.dispose();
}