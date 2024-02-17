/**
 *	@Name: Re-acquiring the Alcadno Cape
 */
var status = -1;

function start(mode, type, selection) {
	if (mode == -1) {
		qm.dispose();
	} else {
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) { // Need GMS text
			qm.sendAcceptDecline("The wondrous Alcadno Cape must be re-acquired.");
		} else if (status == 1) {
			qm.sendOk("Please bring me #b10 #i4000021#, 5 #i4021006#, and 10000 mesos.#k");
			qm.forceStartQuest();
			qm.dispose();
		}
	}
}

function end(mode, type, selection) { // Need GMS text
	if (qm.canHold(1102136) && qm.haveItem(4000021, 10) && qm.haveItem(4021006, 5) && qm.getPlayer().getMeso() > 9999) {
		qm.sendOk("Thank you, the cape has been re-acquired. I advise you keep this until you reach Lv. 75, when you can acquire the more coveted one.");
		qm.gainItem(4000021, -10);
		qm.gainItem(4021006, -5);
		qm.gainMeso(-10000);
		qm.gainItem(1102136, 1);
		qm.forceCompleteQuest();
	} else {
		qm.sendOk("Either you don't have all the items required, or you have a full inventory.");
	}
	qm.dispose();
}