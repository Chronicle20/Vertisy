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
	
	var adding = "1";
	if(cm.getMapId() == 102040000)adding = "2";
	if(cm.getMapId() == 103000003)adding = "3";
	if(cm.isQuestStarted(2358)){
		if(status == 0){
			cm.sendYesNo("There is an empty space here for you to put up the poster.\r\nDo you wish to attach the poster here?");
		}else if(status == 1){
			var data = cm.getPlayer().getQuestInfo(2358);
			if(data.indexOf("100") != -1)data = "";
			if(data.indexOf(adding) == -1)data += adding;
			if(data.indexOf("1") != -1 && data.indexOf("2") != -1 && data.indexOf("3") != -1){
				cm.getPlayer().updateQuestInfo(2358, "211");
			}else cm.getPlayer().updateQuestInfo(2358, data);
			cm.sendSay("The poster has been attached.", false, false);
		}
	}else cm.dispose();
}
