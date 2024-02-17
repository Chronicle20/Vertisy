/**
 * @Author: iPoopMagic
 * @Description: Pirate PQ Doors - (925100400)
 */
function act() {
	var em = rm.getEventManager("PiratePQ");
	if (em != null) {
		rm.mapMessage(6, "One of the doors have been activated.");
		em.setProperty("stage4", parseInt(em.getProperty("stage4")) + 1);
		if (em.getProperty("stage4").equals("4")) {
			rm.mapMessage(6, "You may proceed!");
			rm.getReactor().getMap().killAllMonsters();
			rm.getReactor().getMap().allowSummonState(false);
		}
	}
}