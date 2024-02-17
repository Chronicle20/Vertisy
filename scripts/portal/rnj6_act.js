/**
 *	@Description: Spawns Mobs in Yulete's Office - Romeo
 *	@Author: iPoopMagic (David)
 */
function enter(pi) {
    var em = pi.getEventManager("Romeo");
    if (em != null && em.getProperty("stage5").equals("0")) {
		var pos = pi.getPlayer().getPosition();
		for (var i = 0; i < 10; i++) {
			pi.spawnMonster(9300142, pos.x, pos.y);
			pi.spawnMonster(9300143, pos.x, pos.y);
			pi.spawnMonster(9300144, pos.x, pos.y);
			pi.spawnMonster(9300145, pos.x, pos.y);
			pi.spawnMonster(9300146, pos.x, pos.y);
		}
		em.setProperty("stage5", "1");
    }
}