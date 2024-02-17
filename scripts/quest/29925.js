/**
 *	@Name: Aran in Memory
 */
var status = -1;

function start(mode, type, selection) {
	if (qm.canHold(1142130) && !qm.haveItem(1142130, 1) && qm.getPlayer().getLevel() >= 30 && ((qm.getPlayer().getJob() / 100) | 0) == 21) {
		qm.sendOk("Thank you Aran, for all that you do. One day, you will remember.");
		qm.gainItem(1142130, 1);
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	} else {
		qm.getPlayer().dropMessage(1, "Inventory is full.");
	}
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.canHold(1142130) && !qm.haveItem(1142130, 1) && qm.getPlayer().getLevel() >= 30 && ((qm.getPlayer().getJob() / 100) | 0) == 21) {
		qm.sendOk("Thank you Aran, for all that you do. One day, you will remember.");
		qm.gainItem(1142130, 1);
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	} else {
		qm.getPlayer().dropMessage(1, "Inventory is full.");
	}
	qm.dispose();
}