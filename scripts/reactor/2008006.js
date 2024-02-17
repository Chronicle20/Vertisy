/**
 *	@Author: iPoopMagic (David)
 *	@Description: CD Player (Orbis PQ)
 */
function act() {
    rm.mapMessage(5, "The CD is now playing.");
	rm.getEventManager("OrbisPQ").setProperty("stage3", "1");
}