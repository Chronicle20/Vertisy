function enter(pi) {
    var allowEntry = false;
    for(var i = 0; i <= 10; i++) {
        if(allowEntry == false && (pi.isQuestStarted(2300 + i) || pi.isQuestCompleted(2300 + i)))
            allowEntry = true;
    }
	if (allowEntry) {
		pi.playPortalSound();
		pi.warp(106020000, "left00");
		return true;
	}
	pi.playerMessage(5, "A strange force is blocking you from entering.");
	return false;
}