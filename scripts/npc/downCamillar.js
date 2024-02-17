var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;

	if(status == 0){
		cm.sendSay("You... you rescued me. Thank you... Now, lets get out of here.", false, true);
	}else if(status == 1){
		cm.getPlayer().updateQuestInfo(22557, "2");
		cm.warp(100000000);
	}
}
