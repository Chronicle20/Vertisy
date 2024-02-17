/**
 *	@Name: Raising Mimiana
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
    } else {
        if (status == 2 && mode == 0) {
			qm.sendOk("Raising Mimiana's egg is a big responsibility. Please come back when you feel that you are ready to grow with a new companion.");
			qm.dispose();
			return;
        }
        if (mode == 1)
            status++;
        else
            status--;
		if (status == 0) {
			qm.sendNext("The riding for Knights are a bit different from the rides available for regular folks. This takes place through a creature that is of the Mimi race that can be found on this island; they are called #bMimianas#k. Instead of riding monsters, the Knights ride Mimiana. There's one thing that should never, ever forget.");
		} else if (status == 1) {
			qm.sendNextPrev("Don't think of this as just a form of mount or transportation. These mounts can be your friend, your comrade, your colleague... all of the above. Even a friend close enough to entrust your life! That's why the Knights of Ereve actually grow their own mounts.");
		} else if (status == 2) {
			qm.sendAcceptDecline("Now, here's a Mimiana egg. Are you ready to raise a Mimiana and have it as your traveling companion for the rest of its life?");
		}
		if (status == 3) {
			qm.forceStartQuest();
			qm.gainItem(4220146, 1); // THERE'S FOUR DIFFERENT EGGS IDK
			qm.sendOk("Mimiana's egg can be raised by #bsharing your daily experiences with it#k. Once Mimiana fully grows up, please come see me.");
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	if (mode > 0)
		status++;
	else
		status--;
	if (status == 0) {
		qm.sendNext("Hey there! How's Mimiana's egg?");
	} else if (status == 1) {
		if (qm.haveItem(4220146)) {
			qm.sendNextPrev("Oh, were you able to awaken Mimiana's Egg? That's amazing... Most knights can't even dream of awakening it in such a short amount of time.");
		} else {
			qm.sendPrev("Hmmm... I don't think Mimiana has been fully awakened yet. There's no need to rush here, though. Mimiana will grow as much as you grow.");
			qm.dispose();
		}
	} else if (status == 2) {
		qm.forceCompleteQuest();
		qm.dispose();
	}
}