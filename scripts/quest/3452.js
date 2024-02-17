var status = -1;

function start(mode, type, selection) {
}

function end(mode, type, selection) {
	qm.sendOk("Thank you so much. I finally have hard evidence of my theory. Please allow me to examine it. Have a good day.");
	qm.forceCompleteQuest();
	qm.dispose();
}
