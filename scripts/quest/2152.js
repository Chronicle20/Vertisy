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
		qm.sendSimple("Welcome. How can I help you?\r\n#b#L0#Do you know anything about a ghost tree?#l#k\r\n");
	} else if (selection == 0) {
		qm.sendSimple("You must be helping Doctor #p1022006#'s research, right? Well, I did a bit of research myself after he asked me, but I didn't get much. The only thing I figured out was that the forest on the border between #m102000000# and Elinia is drying up very quickly. It may not look so fast from the surface, but it is very drastic.\r\n#b#L1#Indeed. Thank you very much for your time.#l#k\r\n");  
	} else if (selection == 1) {
		qm.sendOk("I'm sorry I can't be of much help."); // baby dw i gotchu
		qm.forceCompleteQuest();
		qm.gainExp(40, true);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}
