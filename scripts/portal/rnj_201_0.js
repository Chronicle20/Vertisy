/**
 *	@Description: Back to Roid Map
 */
function enter(pi) {
	if (pi.getPlayer().getMap().getReactorByName("rnj31_out").getCurrState() > 0) {
		pi.warp(926100200, 1);
    } else {
		pi.playerMessage(5, "The door is not open.");
    }
}