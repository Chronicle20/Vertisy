/**
 *	@Author: iPoopMagic (David)
 */
function touch() {
	rm.mapMessage(6, "All stirges have disappeared.");
	rm.getPlayer().getMap().killAllMonsters();
}

function untouch() {
	rm.mapMessage(6, "All stirges have reappeared.");
	rm.getPlayer().getEventInstance().getMapInstance(rm.getPlayer().getMapId).instanceMapRespawn();
}