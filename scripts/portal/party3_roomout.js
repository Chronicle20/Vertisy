/**
 * 	@Author: iPoopMagic (David)
 */
function enter(pi) {
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var map = eim.getMapInstance(920010100);
	var portal = 0;
	if (pi.isLeader()) {
		for (var i = 0; i < party.size(); i++) {
			if (pi.getPlayer().getMapId() == 920010200) { // Walkway
				portal = 4;
			} else if (pi.getPlayer().getMapId() == 920010300) { // Storage
				portal = 12;
			} else if (pi.getPlayer().getMapId() == 920010400) { // Lobby
				portal = 5;
			} else if (pi.getPlayer().getMapId() == 920010500) { // Sealed Room
				portal = 13;
			} else if (pi.getPlayer().getMapId() == 920010600) { // Lounge
				portal = 15;
			} else if (pi.getPlayer().getMapId() == 920010700) { // On The Way Up
				portal = 14;
			} else if (pi.getPlayer().getMapId() == 920010800) { // Garden
				portal = 0;
			} else if (pi.getPlayer().getMapId() == 920011000) { // Room of Darkness
				portal = 16;
			} else {
				portal = 0;
			}
			party.get(i).dropMessage(6, "The leader has changed to map <" + map.getMapName() + ">.");
			party.get(i).changeMap(map, map.getPortal(portal));
		}
		return true;	
	} else {
		pi.getPlayer().dropMessage(5, "Only the party leader can decide to leave this room or not.");
		return false;
	}
}