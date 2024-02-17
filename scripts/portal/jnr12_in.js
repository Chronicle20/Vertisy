/**
 *	@Description: To Boss Fight (Juliet)
 */
function enter(pi) {
    if (pi.getPlayer().getMap().getCharacters().size() == pi.getPlayer().getEventInstance().getPlayerCount()) {
		pi.warpParty(926110401);
    } else {
		pi.playerMessage(5, "Not all of your party members are here.");
    }
}