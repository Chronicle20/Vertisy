function enter(pi) {
	if(pi.getPlayer().haveItem(4000507)) {
        pi.gainItem(4000507, -1);
		pi.warp(106020400, "left00");
		return true;
	} else {
		if (pi.isQuestCompleted(2343)) {
			pi.warp(106020400, "left00");
			return true;
		} else {
			pi.earnTitle("You are blocked by the barrier and cannot move forward.");
			return false;
		}
	}
}