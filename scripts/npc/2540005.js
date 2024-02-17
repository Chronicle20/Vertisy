/* Alicia's Soul
	Map Name (Map ID)
	Warps you from F48 to FM and back
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
	
	if(cm.getPlayer().getMapId() == 910000000){
		if(status == 0){
			cm.sendSimple("I'll warp you to Oz Tower Floor F48. Which map would you like?\r\n#L0#Invisible Platforms#l\r\n#L1#Visible Platforms#l");
		}else if(status == 1){
			if(selection == 0 || selection == 1){
				cm.warp(500 + selection);
				cm.dispose();
			}
		}
	}else if(cm.getPlayer().getMapId() == 500 || cm.getPlayer().getMapId() == 501){
		if(status == 0){
			cm.sendYesNo("Would you like to go back to the Free Market?");
		}else if(status == 1){
			cm.warp(910000000);
			cm.dispose();
		}
	}
}