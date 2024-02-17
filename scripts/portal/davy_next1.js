/**
 *	@Author: iPoopMagic
 *	@Description: Pirate PQ Portal (to kill mob room)
 */
function enter(pi) {
	var em = pi.getEventManager("PiratePQ");
	if (em != null && em.getProperty("stage2").equals("6")) {
		pi.warpParty(925100200);
		pi.givePartyQuestExp("PiratePQ2");
	} else {
		pi.playerMessage(5, "The portal is not opened yet.");
	}
}