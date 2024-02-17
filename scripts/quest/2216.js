/**
 *	@Name: Information from Mr. Pickall
 */
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Something strange is happening in the swamp. Mind checking it out?");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendNext("You might want to talk the others and report back to JM. I know he's investigating it.");
	qm.forceCompleteQuest();
	qm.dispose();
}
