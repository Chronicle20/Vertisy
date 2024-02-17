/**
 * @NPC: Ellin
 * @MapID: 930000100
*/

var status = -1;

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
	
	if(cm.getPlayer().getMapId() == 930000300){
		cm.warpParty(cm.getPlayer().getMapId() + 100);
		cm.dispose();
	}else if(cm.getPlayer().getMapId() == 930000400){
		if(status == 0){
			var text = "";
			if(cm.isLeader()){
				text += "#L0#Give me a Purification Marble.#l\r\n";
				text += "#L1#How do I get them in the marble?#l\r\n";
			}
			text += "#L2#I want to get out of here.#l";
			cm.sendNext(text);
		}else if(status == 1){
			if(selection == 0){
				cm.gainItem(2270004, 10);
				cm.dispose();
			}else if(selection ==1){
				cm.dispose();
			}else if(selection == 2){
				cm.warp(300030100);
				cm.dispose();
			}
		}
	}else{
		if(status == 0){
			cm.sendYesNo("Would you like to leave the Party Quest?");
		}else if(status == 1){
			cm.warp(300030100);
			cm.dispose();
		}
	}
}