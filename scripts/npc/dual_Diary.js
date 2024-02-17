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
		var data = cm.getPlayer().getQuestInfo(2369);
		if(data == null || data.isEmpty()){
			cm.useSummoningBag(2109012, new Packages.java.awt.Point(98, 149));
			cm.getPlayer().updateQuestInfo(2369, "1");
		}else if(cm.getPlayer().getMap().countMonster(9001019) == 0){
			if(cm.canHold(4032617)){
				cm.gainItem(4032617, 1);
				cm.sendSay("You've obtained the Former Dark Lord's Diary. You better leave before someone comes in.", false, false);
			}else{
				cm.sendSay("Open up one slot in your Etc inventory before continuing", false, false);
			}
		}
		cm.dispose();
	}
}
