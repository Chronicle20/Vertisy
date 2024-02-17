/**
 *	@Name: Finding Jack
 */
// NEED GMS-LIKE TEXT

var status = -1;

function start(mode, type, selection) {
	qm.sendNext("After all that, you come back! Great, I need you to find my brother. He's probably lurking somewhere in #bCrimsonwood Mountain#k right now...");
	qm.forceStartQuest();
	qm.dispose();
}
function end(mode, type, selection) {
	if(qm.canHold(3992040)) {
        qm.sendNext("...");
        qm.gainItem(3992040, 1);
        qm.gainExp(175000);
        qm.forceCompleteQuest();
    } else {
        qm.sendOk("Hmm. You don't have enough inventory space. Please clear some SET UP space and talk to me again.");
    }
    qm.dispose();
}
