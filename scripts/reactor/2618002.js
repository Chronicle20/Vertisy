/**
 *	@Description: Boxes (Romeo & Juliet PQ)
 */
function act() {
	rm.getPlayer().getEventInstance().getMapInstance(rm.getPlayer().getMapId() + 1).getReactorByName(rm.getPlayer().getMapId() == 926100200 ? "rnj31_out" : "jnr31_out").setState(1);
}