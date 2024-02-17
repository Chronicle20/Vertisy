/**
 *	@Author: iPoopMagic (David)
 */
var flames = Array("g1", "g2", "h1", "h2", "i1", "i2");
//g1, h1, i1
//g2, h2, i2

function touch() {
	rm.mapMessage(6, "The bottom left flame trigger has been turned off.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 2);
	}
}

function untouch() {
	rm.mapMessage(6, "The bottom left flame trigger has been turned on.");
	for (var i = 0; i < flames.length; i++) {
		rm.getPlayer().getMap().environmentToggle(flames[i], 1);
	}
}