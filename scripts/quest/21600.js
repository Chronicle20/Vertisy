var status = -1;

function start(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
        qm.sendOk("Please make some time to hear me out, you're the only person I have left to turn to.");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendAcceptDecline("Hello. My name is Pucci and I take care of huskies. I'm sorry to ask you a favor out of nowhere, but I don't have anyone else to turn to. Thing is, I've been put in a very awkward situation. If you're not in a hurry, would you mind hearing me out?");
    } else if (status == 1) {
    	qm.sendNext("Not too long ago, I was taking care of my adorable huskies when I noticed that one of them seemed different. It had fangs! And its fur was unlike anything I've ever seen. The more I looked at it, the more I was convinced that it wasn't a husky at all.");
    } else if (status == 2) {
    	qm.sendNextPrev("I thought maybe it could be a mixed breed of some sort, so I looked into it and discovered something shocking! It's not a huskey... It's a #bWolf#k! Wolves don't live in Rien Island, so I can't quite figure out how this one got here. Isn't that strange?");
    } else if (status == 3) {
    	qm.sendNextPrev("I don't think raising dogs and wolves together is a good idea, but I can't just abandon this Wolf Pup, especially since it's not in good health. I'm planning to look after it until it becomes strong enough to take care of itself.");
    } else if (status == 4) {
    	qm.sendNextPrev("But the problem is, I know how to raise dogs, but I don't know a thing about raising wolves. So I'm looking for help. I herd that someone by the name of #bNanuke#k in #bAqua Road#k knows how to raise wolves, and it would mean the world to me if you could meet her and ask for help. Please...");
    } else if (status == 5) {
		qm.sendPrev("After you receive some pointers from Nanuke, if she happens to give you an item, you can just bring it back to me. I'll be setting up a home around #bSnowy Field 2#k near the town of Rien.");
		if (!qm.isQuestStarted(21600) && !qm.isQuestCompleted(21600)) {
			qm.forceStartQuest();
		}
    } else if (status == 6) {
		qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}