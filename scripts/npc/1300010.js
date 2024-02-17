
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
		if(status == 0 && mode == 0){
			cm.dispose();
		}
		if (mode == 1)
            status++;
        else
            status--;
		}
	if(status == 0){
		cm.sendYesNo("#bDo you want to use the Killer Mushroom Spore? \r\n\r\n#r#e<Caution>#n\r\nNot for human consumption!\r\nIf ingested, seek medical attention immediately!");
	} else if(status == 1)
        if(cm.getMapId() == 106020300)
            cm.sendNext("Success! The barrier is broken!");
        else {
            cm.sendOk("It doesn't look like there's anything to use the Killer Mushroom Spore on around here!");
            cm.dispose();
        }
	else if(status == 2){
		cm.earnTitle("Mushroom Forest Barrier Removal Complete 1/1");
		cm.getPlayer().dropMessage(5, "The Mushroom Forest Barrier has been removed.");
		cm.gainItem(2430014, -1);
        cm.completeQuest(2343);
		cm.dispose();
	} else {
        cm.dispose();
    }
}