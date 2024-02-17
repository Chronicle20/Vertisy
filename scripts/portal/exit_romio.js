function enter(pi) {
	var loc = pi.getPlayer().getSavedLocation("MULUNG_TC");
	if(loc != null)pi.warp(loc);
	else pi.warp(261000010);
	return true;
}