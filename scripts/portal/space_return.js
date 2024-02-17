//Author: kevintjuh93

function enter(pi) {  
	var loc = pi.getPlayer().getSavedLocation("EVENT");
	if(loc != null)pi.warp(loc);
	else pi.warp(100000000);
	return true;
}