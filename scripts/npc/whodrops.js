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
		cm.sendGetText("Enter the Item Name you would like to get the droppers of.\r\nThe search will cost 250k Meso.");
	}else{
		var mob = cm.getText();
		if(cm.getMeso() >= 250000){
			cm.sendOk(cm.getWhoDrops(mob));
			cm.gainMeso(-250000);
			cm.dispose();
		}else{
			cm.sendOk("You don't have enough mesos.");
			cm.dispose();
		}
	}
}