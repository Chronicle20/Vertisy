/**
 *	@Name: The Fallen Warriors
 */
// NEED GMS-LIKE TEXT
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Bring me Elder Ashes.");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.dispose();
}
