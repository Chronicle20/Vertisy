function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
		if (mode == 1)
            status++;
        else
            status--;
		}
	if (status == 0) {
		if (cm.getPlayer().getMapId() == 106020300) {
			if (cm.isQuestStarted(2314)) {
				cm.sendNext("This looks to be a type of 'Mushroom Spore' that has been transformed by magic into a strong defense barrier. It doesn't appear penetrable through physical force. Return to the #b#p1300003##k and report this matter.", 2);
			} else if (cm.isQuestStarted(2319)) {
				cm.sendNext("It seems as if the barrier could be broken using a #t2430014#.");
			} else {
                cm.dispose();
            }
		} else if (cm.getPlayer().getMapId() == 106020500) {
			if (cm.isQuestStarted(2322)) {
				cm.sendNext("The colossal castle wall is covered with thorny vines. It's going to be difficult getting into the castle. For now, go report this to the #b#p1300003##k.", 2);
			} else {
                cm.dispose();
            }
		}
	} else if (status == 1){
		if (cm.isQuestStarted(2314)){
			//cm.completeQuest(2314);
			cm.earnTitle("Mushroom Forest Barrier Investigation Completed 1/1");
			cm.getPlayer().updateQuestInfo(2314, "1");
			cm.showInfo("Effect/OnUserEff.img/normalEffect/mushroomcastle/chatBalloon1");
			cm.dispose();
		} else if (cm.getPlayer().getMapId() == 106020500) {
			cm.earnTitle("Castle Wall Investigation Completed 1/1");
			cm.getPlayer().updateQuestInfo(2322, "1");
			cm.dispose();
		}
	}
}