function enter(pi) {
	if (pi.isQuestCompleted(2324) || (pi.isQuestStarted(2324) && pi.getQuestProgress(2324) == 1)) {
		pi.warp(106020501, "left00");
		return true;
	}
	pi.warp(106020500, "left00");
	return true;
}