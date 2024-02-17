/**
 * 	@Author: iPoopMagic (David)
 */
function enter(pi) {
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var map = eim.getMapInstance(920010100);
	if (pi.isLeader()) {
		for (var i = 0; i < party.size(); i++) {
			party.get(i).dropMessage(6, "The leader has changed to map <" + map.getMapName() + ">.");
			party.get(i).changeMap(map, map.getPortal(0));
		}
		return true;	
	} else {
		pi.getPlayer().dropMessage(5, "Only the party leader can decide to leave this room or not.");
		return false;
	}
}