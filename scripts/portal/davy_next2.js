/**
 *	@Author: iPoopMagic
 *	@Description: Pirate PQ Portal (to Key room)
 */
function enter(pi) {
    if (pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getMonstersEvent(pi.getPlayer()).size() < 1) {
		pi.warpParty(925100300); // Skip the "3rd" stage (doesn't exist except for special reason which I have to figure out
		pi.givePartyQuestExp("PiratePQ3");
    } else {
		pi.playerMessage(5, "The portal is not opened yet.");
    }
}