/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("a6", "a7", "b6", "b7", "c6", "c7");
//a6, a7
//b6, b7
//c6, c7

function touch() {
	rm.mapMessage(6, "The top right flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The top right flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}