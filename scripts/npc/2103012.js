/**
 *	@Name: Ariant Private House Cupboard (#6)
 *	@Quest: Sejan's Test
 */

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
			if (cm.isQuestStarted(3929) && cm.haveItem(4031580, 1)) {
				if (cm.getPlayer().getQuestNAdd(3929).getStorage() == null || !cm.getPlayer().getQuestNAdd(3929).getStorage().contains(2103012)) {
					cm.sendOk("#b(This house looks okay. You leave the wrapped food here.)", 2);
					cm.gainItem(4031580, -1);
					cm.getPlayer().getQuestNAdd(3929).getStorage().add(2103012);
					cm.getPlayer().updateQuestInfo(3929, parseInt(cm.getPlayer().getQuestInfo(3929)) + 1);
				} else {
					cm.sendOk("#b(You've already dropped off food at this house.)", 2);
				}
			} else {
				cm.sendOk("#b(You think to yourself, don't go sneaking into someone else's cupboard!)", 2);
			}
			cm.dispose();
		}
    }
}