/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

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
		if(cm.getPlayer().getMapId() != 970030000){
			cm.sendYesNo("Leaving so early?");
		}
	}else{
		if(cm.getPlayer().getMapId() != 970030000){
			cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
			cm.dispose();
		}
	}
}