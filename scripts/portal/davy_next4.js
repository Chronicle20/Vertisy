function enter(pi) {
    if (pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getReactorByName("sMob1").getCurrState() >= 1 &&
		pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getReactorByName("sMob2").getCurrState() >= 1 &&
		pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getReactorByName("sMob3").getCurrState() >= 1 &&
		pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getReactorByName("sMob4").getCurrState() >= 1) {
		if (pi.isLeader()) {
			pi.warpParty(925100500);
		} else {
			pi.playerMessage(5, "The leader must be here.");
		}
    } else {
		pi.playerMessage(5, "The portal is not opened yet.");
    }
}