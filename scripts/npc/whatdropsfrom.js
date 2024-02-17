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
		cm.sendGetText("Enter the Mob Name you would like to get the drops of.\r\nThe search will cost 50k Meso.");
	}else{
		var mob = cm.getText();
		if(cm.getMeso() >= 50000){
			var drops = cm.getWhatDropsFrom(mob);
			if(drops.length > 0){
				cm.sendOk(drops);
				cm.gainMeso(-50000);
			}else{
				cm.sendOk("No drops were found for the mob " + mob);
			}
			cm.dispose();
		}else{
			cm.sendOk("You don't have enough mesos.");
			cm.dispose();
		}
	}
}