function start(mode, type, selection) {
	qm.changeJobById(2211);
	qm.gainSP(3);
	qm.teachSkill(22111001, 0, 20);
	qm.forceStartQuest();
	qm.forceCompleteQuest();
	qm.message("The Dragon Grew!");
	qm.message("The Dragon can now use a new skill");
	qm.dispose();
}