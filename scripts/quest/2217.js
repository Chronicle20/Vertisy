var status = -1;

function start(mode, type, selection) {
	qm.sendNext("The sewers have been emitting a strange odor. Could you please go check it out?");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendNext("You might want to talk the others and report back to JM. I know he's investigating it.");
	qm.forceCompleteQuest();
	qm.dispose();
}
