/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("d6", "d7", "e6", "e7", "f6", "f7");
//d6, e6, f6
//d7, e7, f7

function touch() {
	rm.mapMessage(6, "The middle right flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The middle right flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}