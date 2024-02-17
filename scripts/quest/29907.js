/**
 *	@Description: Official Knight Medal
 */
var status = -1;

function start(mode, type, selection) {
	if (qm.getPlayer().getJob() > 1000 && qm.getPlayer().getJob() % 100 > 0 && qm.getPlayer().getJob() < 2000) {
		qm.sendOk("Please come talk to me.");
		qm.forceStartQuest();
	}
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.canHold(1142067) && !qm.haveItem(1142067, 1) && qm.getPlayer().getJob() > 1000 && qm.getPlayer().getJob() % 100 > 0 && qm.getPlayer().getJob() < 2000) {
		qm.sendOk("Congratulations on becoming an #bOfficial Knight#k.");
		qm.gainItem(1142067, 1);
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	} else {
		qm.getPlayer().dropMessage(1, "Inventory is full.");
	}
	qm.dispose();
}