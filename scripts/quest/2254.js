/**
 *	@Name: Karcasa of the Desert
 */
var status = 0;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) { // need GMS text
			qm.sendNext("Ah, you have answered my call. It seems like something is stirring up in the depths of the desert.");
		} else if (status == 1) {
			qm.sendOk("Please go talk to #bKarcasa#k to find out more about it.");
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) { // need GMS text
			qm.sendNext("Welcome, stranger. It seems like #bThe Rememberer#k sent you here. In any event, there seems to be something stirring up at the Mushroom Castle; it's in some deep trouble.");
		} else if (status == 1) {
			qm.forceCompleteQuest();
			qm.dispose();
		}
	}
}