/**
 * Monster Carnival Reviving Field 2
 */

function enter(pi) {
    if (pi.getPlayer().getCarnivalParty().getTeam() == 0) { // Red Team
		pi.warp(pi.getMapId() - 1, "red_revive");
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyBlue());
    } else {
		pi.warp(pi.getMapId() - 1, "blue_revive");
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyRed());
    }
}