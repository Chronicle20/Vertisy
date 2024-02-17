function enter(pi) {
	if (pi.isQuestStarted(22580)){
		pi.getPlayer().updateQuestInfo(22580, "" + 2);
		pi.warp(914100021, 1);
	}
	pi.enableActions();
	return true;
}