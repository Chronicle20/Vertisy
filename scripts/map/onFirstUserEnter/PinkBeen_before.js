/**
 * @Map: Twilight of Gods
 * @Author: iPoopMagic (David)
 */
function start(ms) {
    ms.getPlayer().getMap().clearAndReset(true);
	if(!ms.getPlayer().getMap().containsNPC(2141000)) {
		ms.spawnNpc(2141000, new java.awt.Point(-190, -42));
    }
//	for (var i = 8820019; i < 8820024; i++) {
//		ms.spawnMonster(ms.getPlayer().getMap().getMonsterById(i), 0, -42);
//	}
}