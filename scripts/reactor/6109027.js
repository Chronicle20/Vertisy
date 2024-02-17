/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("g6", "g7", "h6", "h7", "i6", "i7");
//g6, h6, i6
//g7, h7, i7

function touch() {
	rm.mapMessage(6, "The bottom right flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The bottom right flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}