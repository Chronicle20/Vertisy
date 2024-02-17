function enter(pi) {
	if (pi.haveItem(4032405, 1)) {
		//pi.gainItem(4032405, -1);
		pi.warp(106021001, 0);
		pi.getPlayer().dropMessage(5, "Defeat the rest of them!");
		return true;
	}
	pi.getPlayer().dropMessage(5, "You may not enter this room.");
	return false;
}