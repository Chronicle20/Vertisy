/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("g3", "g4", "g5", "h3", "h4", "h5", "i3", "i4", "i5");
//g3, h3, i3
//g4, h4, i4
//g5, h5, i5

function touch() {
	rm.mapMessage(6, "The bottom center flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The bottom center flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}