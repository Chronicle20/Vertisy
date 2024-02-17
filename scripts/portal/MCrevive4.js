/**
 * Monster Carnival Reviving Field 4
 */
 
function enter(pi) {
	var portal = 0;
	switch (pi.getPlayer().getCarnivalParty().getTeam()) {
		case 0:
			portal = 4;
			break;
		case 1:
			portal = 3;
			break;
	}
	pi.warp(980000401, portal);
	pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyBlue());
	pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyRed());
	return true;
}
