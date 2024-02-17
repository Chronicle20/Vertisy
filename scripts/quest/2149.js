var status = -1; 

function start(mode, type, selection) {
	status++;
	if (mode != 1) {
		if (type == 1 && mode == 0) {
			status -= -2;
		} else {
			qm.dispose();
			return;
		}
	}
	if (status == 0) {
		qm.sendSimple("What's is it?\r\n#b#L0#Have you heard about the Ghost Tree?#l#k\r\n");
	} else if (selection == 0) {
		qm.sendSimple("You must have the story from cowards. There's no such thing as a ghost tree. I've been training myself for a long time around the rocks, trees and mountains in #m102000000#. I have never seen or heard about it for long.\r\n#b#L1#Ah, is that true?#l#k\r\n");  
	} else if (selection == 1) {
		qm.sendOk("I heard unknown forces on the east side of the rocky mountain have been attacking people. This news is a bit disturbing.");
		qm.forceCompleteQuest();
		qm.gainExp(40, true);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}
