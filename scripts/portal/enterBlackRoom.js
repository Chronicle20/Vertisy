function enter(pi) {
	pi.playPortalSound();
	if(pi.isQuestStarted(22566))pi.warp(200080601, 0);
	return true;
}