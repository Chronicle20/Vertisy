/**
 * @Map: Cave of Trial II
 * @Author: iPoopMagic (David)
 */
function enter(pi) {
	var em = pi.getEventManager("HorntailFight");

	if (em != null) {
		var prop = em.getProperty("preheadCheck");

		if (prop != null && prop.equals("2")) {
			pi.mapMessage(6, "The enormous creature is approaching from the deep cave.");
			pi.spawnMonster(8810025, -340, 230);
			em.setProperty("preheadCheck", "3");
		}
	}
}