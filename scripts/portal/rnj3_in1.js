/**
 *	@Description: Into the Laboratory (Magatia PQ) - Romeo
 */
function enter(pi) {
    if (pi.haveItem(4001133) && pi.getPlayer().getEventInstance().getMapInstance(926100200).getReactorByName("rnj3_out2").getCurrState() < 1) {
		pi.getPlayer().getEventInstance().getMapInstance(926100200).getPortal("in01").setPortalState(true);
		pi.getPlayer().getEventInstance().getMapInstance(926100200).getReactorByName("rnj3_out2").setState(1);
		pi.gainItem(4001133, -1);
		pi.warp(926100202, 0);
	} else if (pi.getPlayer().getEventInstance().getMapInstance(926100200).getReactorByName("rnj3_out2").getCurrState() > 0) {
		pi.warp(926100202, 0);
    } else {
		pi.playerMessage(5, "The door is not open.");
    }
}