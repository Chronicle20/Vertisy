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
	
	if(cm.isQuestCompleted(22579)){
		if(status == 0){
			cm.sendAcceptDecline("Would you like to go to the island?");
		}else if(status == 1){
			cm.warp(200090080);
			cm.dispose();
		}
	}else{
		cm.sendSay("What do you want?", false, false);
		cm.dispose();
	}
}