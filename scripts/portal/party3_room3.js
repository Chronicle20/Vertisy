/**
 * 	@Author: iPoopMagic (David)
 *	@Mapto: Lobby
 */

function enter(pi) {
	var em = pi.getEventManager("OrbisPQ");
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var map = eim.getMapInstance(920010400);
//	Sunday = 1, Saturday = 7
//	map.getReactorById(2008006).setState(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_THE_WEEK));
	if (pi.isLeader()) {
		if (em.getProperty("stage3").equals("0")) {
			for (var i = 0; i < party.size(); i++) {
				party.get(i).dropMessage(6, "The leader has changed to map <" + map.getMapName() + ">.");
				party.get(i).changeMap(map, map.getPortal(8));
			}
			return true;
		} else {
			pi.getPlayer().dropMessage(5, "You may not go back to this room.");
			return false;
		}
	} else {
		if (party.get(0).getMapId() == 920010400) {
			pi.warp(920010400, 8);
			return true;
		} else {
			pi.getPlayer().dropMessage(5, "Only the party leader can decide to leave this room or not.");
			return false;
		}
	}
}