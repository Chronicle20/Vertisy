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
		if(cm.hasItem(4032616)){
			cm.sendSay("You already have a Mirror of Insight", false, false);
			return;
		}
		if(cm.canHold(4032616)){
			if(Math.random() <= 0.25){
				cm.gainItem(4032616, 1);
			}
			cm.gainItem(2430071, -1);
		}else cm.sendSay("Please make room in your inventory.", false, false);
		cm.dispose();
	}
}
