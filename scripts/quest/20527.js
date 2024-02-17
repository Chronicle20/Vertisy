/**
 *	@Name: A Knight's Pride (first part of Mimio)
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
    } else {
        if (status == 0 && mode == 0) { // Need some GMS-like text
			qm.sendOk("Ah, I see you have made the decision to stick your mount. Well, if you ever want an upgrade, please let me know.");
			qm.dispose();
			return;
        }
        if (mode == 1)
            status++;
        else
            status--;
		if (status == 0) {
			qm.sendNext("Hello fellow Cygnus Knight. Wait a minute, you're still riding #bMimiana#k? My goodness, I worry, you really do need an upgrade.");
		} else if (status == 1) {
			qm.sendAcceptDecline("I can help you out here. Would you like to begin your journey to upgrade to a more high-ranked mount?");
		}
		if (status == 2) {
			qm.forceStartQuest();
			qm.forceCompleteQuest(); // lazy
			qm.dispose();
		}
	}
}

function end(mode, type, selection) {
	qm.dispose();
}