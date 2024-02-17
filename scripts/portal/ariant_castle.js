function enter(pi) {
	if (!pi.haveItem(4031582)) {
		pi.playerMessage(5, "You do not have an Entry Pass to the Palace.");
		return false;
	}
    pi.playPortalSound();
    pi.warp(260000301, "out00");
	return true;
}