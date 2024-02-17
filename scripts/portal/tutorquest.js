function enter(pi) {
    var map = pi.getPlayer().getMapId() - 130030000;
    if(map < 1 || map > 4)
        return false;
        
    if(map == 1) {
        if(pi.isQuestStarted(20009 + map)) {
            pi.playPortalSound();
            pi.warp(130030001 + map);
        } else {
            pi.message("Please start the quest in this map first.");
            return false;
        }
    } else {
        if(pi.isQuestCompleted(20009 + map)) {
            pi.playPortalSound();
            pi.warp(130030001 + map);
        } else {
            pi.message("Please finish the quest in this map first.");
            return false;
        }
    }
	return true;
}