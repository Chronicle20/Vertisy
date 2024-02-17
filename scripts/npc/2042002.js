/* NPC Base
	Kerning City
	Spiegelmann
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
		if(cm.getPlayer().getMapId() == 103000000){
			cm.getPlayer().saveLocation("MIRROR");	
			cm.warp(980000000, "st00");
			cm.dispose();
		}
	}
}