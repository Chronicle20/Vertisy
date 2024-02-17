function enter(pi) {
	var em = pi.getEventManager("CWKPQ");
	if (em != null) {
		var stage = parseInt(em.getProperty("glpq3"));
		if (stage < 10){
			pi.playerMessage(5, "The portal is not opened yet.");
		} else {
			pi.warp(610030400, 0);
		}
	}
}