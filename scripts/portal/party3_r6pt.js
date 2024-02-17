function enter(pi) {
	var em = pi.getEventManager("OrbisPQ");
	pi.mapMessage(5, pi.getPortal().getName());
	if (em != null && em.getProperty("stage6_" + (pi.getPortal().getName().substring(2, 5)) + "").equals("1")) {
		if (pi.getPortal().getName().startsWith("rp16")) {
			pi.warp(pi.getPlayer().getMapId(), 4);
		} else {
			pi.warp(pi.getPlayer().getMapId(), (pi.getPortal().getName().startsWith("rp08") ? 2 : (pi.getPortal().getId() + 4)));
		}
	} else {
		pi.warp(pi.getPlayer().getMapId(), (pi.getPortal().getName().startsWith("rp01") ? 5 : (pi.getPortal().getName().startsWith("rp05") ? 1 : (pi.getPortal().getId() - 4))));
	}
}