function enter(pi) {
    var em = pi.getEventManager("Romeo");
    if (em != null && em.getProperty("stage6_" + (((pi.getMapId() % 10) | 0) - 1) + "_" + (pi.getPortal().getName().substring(2, 3)) + "_" + (pi.getPortal().getName().substring(3, 4)) + "").equals("1")) {
		pi.warp(pi.getPlayer().getMapId(), (pi.getPortal().getId() >= 51 ? 12 : (pi.getPortal().getId() + 4)));
		pi.playerMessage(5, "Correct combination!");
		pi.getPlayer().getMap().environmentChange("an" + pi.getPortal().getName().substring(2, 4), 2);
    } else {
		pi.warp(pi.getPlayer().getMapId(), 0);
		pi.playerMessage(5, "Incorrect combination.");
    }
}