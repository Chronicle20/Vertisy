/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("d1", "d2", "e1", "e2", "f1", "f2");
//d1, e1, f1
//d2, e2, f2

function touch() {
	rm.mapMessage(6, "The middle left flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The middle left flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}