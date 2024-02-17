/**
 *	@Name: The Fallen Woods (oh Taggrin)
 */
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Adventurer, what brings you here. Do not speak to me until you bring back 50 #bPhantom Seeds#k.");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendOk("So you have come back.");
	qm.forceCompleteQuest();
	qm.dispose();
}
