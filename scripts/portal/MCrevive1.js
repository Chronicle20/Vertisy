/**
 * Monster Carnival Reviving Field 1
 */

function enter(pi) {
    if (pi.getPlayer().getCarnivalParty().getTeam() == 0) { //Red Team
		pi.warp(pi.getMapId() - 1, "red_revive");
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyBlue()); //update the enemy's (that's just the syntax...)
    } else {
		pi.warp(pi.getMapId() - 1, "blue_revive");
		pi.getPlayer().updatePartyCP(pi.getPlayer().getCarnival().getPartyRed());
    }
}