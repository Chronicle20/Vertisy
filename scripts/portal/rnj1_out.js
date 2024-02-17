/**
 *	@Author: iPoopMagic (David)
 *	@Description: Dark Tunnel (kill monsters) - Romeo
 */
function enter(pi) {
    if (pi.getPlayer().getMap().getMonstersEvent(pi.getPlayer()).size() < 1) {
		pi.givePartyQuestExp("MagatiaPQ01");
		pi.warpParty(926100100);
    } else {
		pi.playerMessage(5, "The portal has not been opened yet.");
    }
}