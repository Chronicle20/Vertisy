
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
		if(status == 0 && mode == 0){
			//cm.gainItem(2430014, 1);
			cm.dispose();
		}
		if (mode == 1)
            status++;
        else
            status--;
		}
	if(status == 0){
        if(cm.getMapId() == 106020500) {
            cm.sendYesNo("Do you wish to use the #bThorn Remover#k?");
        } else {
            cm.sendOk("There's nothing to use the #bThorn Remover#k on around here.");
            cm.dispose();
        }
	}else if(status == 1) {
		cm.warp(106020502);
		cm.gainItem(2430015, -1)
		cm.getPlayer().updateQuestInfo(2324, "1");
		cm.dispose();
	}
}
			