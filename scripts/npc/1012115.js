
function start() {
	if (cm.isQuestStarted(20706)) {
		cm.completeQuest(20706);
		cm.sendNext("You have spotted the shadow! Better report to #p1103001#.");
	} else if (cm.isQuestCompleted(20706)) {
		cm.sendNext("The shadow has already been spotted. Better report to #p1103001#.");
	} else {
        cm.sendNext("It looks like there's nothing suspecious in the area.");
	}
    cm.dispose();
}

function action(mode, type, selection) {
    cm.dispose();
}