/**
 * Monster Carnival Reviving Field 6
 */

function enter(pi) {
	pi.warp(980000601, 0);
    if (pi.getPlayer().getCarnivalParty().getTeam() == 0) { //Red Team
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyBlue());
    } else {
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyRed());
    }
}