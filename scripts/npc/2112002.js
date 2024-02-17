/**
 *	@Name: Yulete (You lost)
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start() {
	for (var i = 0; i < 6; i++) {
	    cm.removeAll(4001130 + i);
	}
	var em = cm.getEventManager("Romeo");
	if (!em.getProperty("stage").equals("finished")) {
		cm.givePartyQuestExp("MagatiaPQFail"); // 105,000
	}
	em.setProperty("stage", "finished");
    cm.warp(926100700);
    cm.dispose();
    return;
}