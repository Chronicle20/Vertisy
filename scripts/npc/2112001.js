/**
 *	@Name: Yulete (Romeo)
 */
function start() {
	if (cm.getPlayer().getMapId() == 926100500) {
		cm.sendNext("I ... I ... I don't understand ...");
	} else {
		cm.sendOk("...");
		cm.dispose();
	}
}

function action(mode, type, selection) {
    for (var i = 0; i < 6; i++) {
	    cm.removeAll(4001130 + i);
	}
	cm.warp(926100600, 0);
    cm.dispose();
}