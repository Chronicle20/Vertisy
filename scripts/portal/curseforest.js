function enter(pi) {
	if (pi.isQuestStarted(2224) || pi.isQuestStarted(2226)) {
		pi.warp(910100000,"out00");
	} else {
		pi.warp(910100001,"out00");
	}
	return true;
}