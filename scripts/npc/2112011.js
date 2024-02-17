/**
 *	@Name: Yulete (Juliet)
 */
function start() {
	cm.sendNext("I ... I ... I don't understand ...");
}

function action(mode, type, selection) {
    for (var i = 0; i < 6; i++) {
	    cm.removeAll(4001130 + i);
	}
	cm.warp(926110600, 0);
    cm.dispose();
}