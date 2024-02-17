/**
 *	@Description: Boxes (Romeo & Juliet PQ)
 */
function act() {
	rm.getPlayer().getEventInstance().getMapInstance(rm.getPlayer().getMapId() + 2).getReactorByName(rm.getPlayer().getMapId() == 926100200 ? "rnj32_out" : "jnr32_out").setState(1);
}