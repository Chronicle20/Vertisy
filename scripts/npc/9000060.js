/*
	Gingerman - Witch Tower
*/

function start() {
    cm.sendSimple("Whoa, I'm all the way up to the top floor! \r\n #L0##bI want to get out of here#k#l");
}

function action(mode, type, selection) {
	if (selection == 0) {
		var loc = cm.getPlayer().getSavedLocation("EVENT");
		if(loc != null)cm.warp(loc);
		else cm.warp(100000000);
	}
	cm.dispose();
}