/**
 *	@Name: Eliminating the Rest
 *	@Author: iPoopMagic (David)
 */

var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Thank you for saving me, #h0#. But there are still some left over mobs near the central tower. Please take this key to go in and eliminate them.");
	qm.gainItem(4032405, 1);
	qm.forceStartQuest();
	qm.dispose();
}

function end(mode, type, selection) {
	qm.forceCompleteQuest();
	qm.dispose();
}
	