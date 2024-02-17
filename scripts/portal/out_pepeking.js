function enter(pi) {
	pi.playPortalSound();
	if (pi.getPlayer().getEventInstance() != null) {
		pi.getPlayer().getEventInstance().removePlayer(pi.getPlayer());
	} else {
        pi.warp(106021400);
    }
	return true;
}