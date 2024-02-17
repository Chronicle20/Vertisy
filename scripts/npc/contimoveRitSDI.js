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
	
	if(cm.getPlayer().getMapId() == 914100000){
		if(status == 0){
			cm.sendAcceptDecline("Would you like to go back to Lith Harbor?");
		}else if(status == 1){
			cm.warp(200090090);
			cm.dispose();
		}
	}else{	
		if(status == 0){
			cm.sendAcceptDecline("Would you like to go back?");
		}else if(status == 1){
			cm.warp(104000000);
			cm.dispose();
		}
	}
}