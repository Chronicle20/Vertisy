/* NPC Base
	Abandoned Subway Station (910320000)
	Used for SubwayPQ in Kerning City.
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
	if(cm.getPlayer().getMapId() == 910320000){//Normal area
		
	}else if(cm.getPlayer().getMapId() == 910320001){//They are in the PQ
		
	}
}