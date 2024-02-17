/**
 *	@Modified: iPoopMagic (David)
 */

function enter(pi) {
	var em = pi.getEventManager("CWKPQ");
	if (em != null) {
		if (em.getProperty("glpq6") == null || !em.getProperty("glpq6").equals("3") || pi.getPlayer().getEventInstance() == null || !pi.getPlayer().getEventInstance().getName().startsWith("CWKPQ")){
			pi.playerMessage(5, "The portal is not opened yet.");
		} else {
			pi.warp(610030700, 0);
		}
	}
}