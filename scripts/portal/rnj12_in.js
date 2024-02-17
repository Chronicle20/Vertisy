/**
 *	@Description: To Boss Fight (Romeo)
 */
function enter(pi) {
    if (pi.getPlayer().getMap().getCharacters().size() == pi.getPlayer().getEventInstance().getPlayerCount()) {
		pi.warpParty(926100401);
    } else {
		pi.playerMessage(5, "Not all of your party members are here.");
    }
}