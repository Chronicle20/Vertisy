function enter(pi) {
	var em = pi.getEventManager("CWKPQ");
	if (em != null) {
		var number = parseInt(em.getProperty("glpq4"));
		if (number < 5) {
			pi.playerMessage(5, "The portal is not opened yet.");
		} else {
			pi.warp(610030500, 0);
		}
	}
}