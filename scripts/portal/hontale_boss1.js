/**
 * @Map: Cave of Trial I
 * @Author: iPoopMagic (David)
 */
function enter(pi) {
	var em = pi.getEventManager("HorntailFight");

	if (em != null) {
		var prop = em.getProperty("preheadCheck");

		if (prop != null && prop.equals("0")) {
			pi.mapMessage(6, "The enormous creature is approaching from the deep cave.");
			pi.spawnMonster(8810024, 960, 230);
			em.setProperty("preheadCheck", "1");
		}
	}
}