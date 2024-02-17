var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			qm.sendNext("With all the strange occurrences in Masteria and New Leaf City, Lita Lawless must be busy! I'll bet that she's willing to accept my help...maybe I can earn some mesos in the process! A quick jaunt to the Kerning City subway and I'll be on my way to New Leaf City!", 2);
		} else if (status == 1) {
			qm.sendNextPrev("I spoke with Lita, and it seems that there are strange, powerful spirits drifting about the Phantom Forest. These strange spirits seemingly have no desire, save for tormenting others. I've agreed to eliminate 30 of them, and bring their soiled rags to Lita as proof of valor. I'd better keep sharp-that forest has driven quite a few travelers mad with its confusion...", 2);
		} else if (status == 2) {
			qm.sendAcceptDecline("So you'll help me?");
		} else if (status == 3) {
			//qm.completeQuest(4918);
			//qm.completeQuest(4911);
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}