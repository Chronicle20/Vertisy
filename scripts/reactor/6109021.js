/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("a3", "a4", "a5", "b3", "b4", "b5", "c3", "c4", "c5");
//a3, a4, a5
//b3, b4, b5
//c3, c4, c5

function touch() {
	rm.mapMessage(6, "The top center flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The top center flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}