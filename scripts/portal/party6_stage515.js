function enter(pi) {
	var portalid = getRandom(0, pi.getPlayer().getMap().getPortals().size());
	pi.getPlayer().warpToPortal(portalid);
}

function getRandom(min, max) {
    return Math.random() * (max - min) + min;
}