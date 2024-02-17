/**
 *	@Author: iPoopMagic
 *	@Description: Pirate PQ Portal (to Gather Marks room)
 */
function enter(pi) {
	if (pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getMonstersEvent(pi.getPlayer()).size() < 1) {
		pi.warpParty(925100100);
		pi.givePartyQuestExp("PiratePQ1");
    } else {
		pi.playerMessage(5, "The portal is not opened yet.");
    }
}