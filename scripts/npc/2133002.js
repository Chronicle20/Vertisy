/**
 * @NPC: Ellin
 * @MapID: Forest of Haze
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
	
	if(status == 0){
		cm.sendYesNo("Would you like to leave the Party Quest?");
	}else if(status == 1){
		cm.warp(300030100);
		cm.dispose();
	}
}