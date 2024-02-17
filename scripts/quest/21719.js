var status = -1;

function start(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 2) {
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("Aren''t you the one that used to be in #m101000000# until not too long ago? I finally found you! Do you know how long it took for me to finally find you?", 8);
    } else if (status == 1) {
    	qm.sendNextPrev("Who are you?", 2);
    } else if (status == 2) {
    	qm.sendAcceptDecline("Me? If you want to know, stop by my cave. I'll even send you an invitation. You'll be directly sent to my cave as soon as you accept. Look forward to seeing you there.");
    } else if (status == 3) {
		qm.forceCompleteQuest();
		qm.warp(910510200, 0);
		qm.dispose();
    }
}

function end(mode, type, selection) {
	/*
	if (mode == 1) {
	status++;
    } else {
	if (status == 2) {
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
		qm.sendSimple("Hmmm? I still haven't found a suitable Informant Assignment for you... well, do you need me for anything else? Or do you have some juicy information for me... ?\r\n#b#L0#(You tell him about your encounter with Francis the Puppeteer.)#l\r\n#k");
	} else if (selection == 0) {
		qm.sendOk("#p1204001#, the Black Wing Puppeteer. Okay, now this all makes sense. What happened with #o1210102#s in #m101000000# and the #o1110100#s in #m101000000# are all being done by the same guy. But wait... are you telling me he also mentioned the Black Mage?");
		qm.forceCompleteQuest();
		qm.dispose();
	}*/
	qm.dispose();
}