/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("a1", "a2", "b1", "b2", "c1", "c2");
//a1, a2
//b1, b2
//c1, c2

function touch() {
	rm.mapMessage(6, "The top left flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The top left flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}