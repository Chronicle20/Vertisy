/**
 *	@Name: Information from Nella
 */
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("Rumors had it that there's some kind of magic being emitted from the sewage down there. That's as much as I can tell ya.");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendNext("Why don't you go check it out? You might want to talk the others and report back to JM. I know he's investigating it.");
	qm.forceCompleteQuest();
	qm.dispose();
}
