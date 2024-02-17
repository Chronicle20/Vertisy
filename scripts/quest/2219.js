/**
 *	@Name: Information from Jake
 */
var status = -1;

function start(mode, type, selection) {
	qm.sendNext("So you want to know more about it too? Well, that thing is bigger than it was before. I think some Growth Powder made it grow exponentially.");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	qm.sendNext("Why don't you go check it out? You might want to talk the others and report back to JM. I know he's investigating it.");
	qm.forceCompleteQuest();
	qm.dispose();
}
