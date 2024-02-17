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
		if(cm.getPlayer().getMap().getMonsters().isEmpty()){
			if(cm.canHold(4032497)){
				cm.gainItem(4032497);
				cm.sendSay("Thank you for rescuing me. Let's hurry and get back to the town.", false, true);
			}else{
				cm.sendSay("You're going to have to drop some of those things you're holding in your hands if you want to rescue me. You look like you've got too much baggage.", false, true);
			}
		}else{
			cm.sendSay("Please save me!", false, false);
			cm.dispose();
		}
	}else{
		cm.warp(251000000);
		cm.dispose();
	}
}