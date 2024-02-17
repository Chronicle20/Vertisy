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
		qm.sendSimple("Hi, traveler. What brings you here today? How can I help you?\r\n#b#L0#Do you know anything about a ghost tree?#l#k\r\n");
	} else if (selection == 0) {
		qm.sendSimple("Oh, my! You also heard about the story? #p1012108# of #m100000000# said she saw the ghost on the way back to her home after visiting #m102000000# for her mom's errand.\r\n#b#L1#Are you serious?#l#k\r\n");  
	} else if (selection == 1) {
		qm.sendOk("#p1012108# was terrified and simply passed out at that moment. After dawn, a number of adults went up there, but found nothing. It must be ghosts. What can we do? I don't think I can even get out of town. I'm too scared!");
		qm.forceCompleteQuest();
		qm.gainExp(40, true);
		qm.dispose();
	}
}

function end(mode, type, selection) {
	qm.dispose();
}
