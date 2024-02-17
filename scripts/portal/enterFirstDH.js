var mapp = -1;
var map = 0;
function enter(pi) {
    if (pi.isQuestStarted(20701)) {
    	map = 913000000;
    } else if (pi.isQuestStarted(20702)) {
    	map = 913000100;
    } else if (pi.isQuestStarted(20703)) {
    	map = 913000200;
    }
    if (map > 0) {
		if (pi.getPlayerCount(map) == 0) {
		    pi.warp(map, 0);
		} else {
		    pi.playerMessage(5, "Someone is already in this map.");
		}
    } else {
    	pi.playerMessage(5, "Hall #1 can only be entered if you're engaged in Kiku's Acclimation Training.");
    }
}