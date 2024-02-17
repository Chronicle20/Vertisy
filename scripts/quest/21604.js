var status = -1;

function start(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	if (status == 0) {
        qm.sendOk("");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("Whoa, is that a wolf? I haven't seen anyone carry a wolf in ages! But why aren't you using it as a #bMount#k? From your bewildered expression, you probably don't know what I'm talking about...");
    } else if (status == 1) {
    	qm.sendNextPrev("Well, it's just as it sounds. You ride on the wolf's back and it allows you to move lightning fast. Ob boy, did I have some glorious days of riding around Hectors and White Fangs in the past!");
    } else if (status == 2) {
    	qm.sendAcceptDecline("Interested in riding one yourself? If so, then allow me, Scadur, to help you.");
    } else if (status == 3) {
    	qm.sendNext("Just because you want to ride your wolf as a Mount doesn't mean you can ride it just like that. Before anything, you must find a #bWolf Saddle#k so you won't hurt your wolf. I'll make the saddle for you if you bring me the materials to make it.");
        if (!qm.isQuestStarted(21604) && !qm.isQuestCompleted(21604)) {
			qm.forceStartQuest();
		}
    } else if (status == 4) {
    	qm.sendPrev("The key material for the Wolf Saddle is #bJr. Yeti Skin#k. I think about #b50#k should do it. When you bring me the materials, I'll give you the Monster Mount skill along with the Wolf Saddle. Now, hurry.");
    } else if (status == 5) {
		qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}