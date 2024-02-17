/**
 *	@Description: Back to Roid Map
 */
function enter(pi) {
	if (pi.getPlayer().getMap().getReactorByName("jnr32_out").getCurrState() > 0) {
		pi.warp(926110200, 2);
    } else {
		pi.playerMessage(5, "The door is not open.");
    }
}