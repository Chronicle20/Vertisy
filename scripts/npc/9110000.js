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
	if (status == 0) {
		if (cm.getPlayer().getLevel() >= 130 && cm.getPlayer().getMapId() == 200000100) {
			cm.sendYesNo("On my way to #dOrbis#k I found some strange floating ruins. Would you like to check them out with me?");
		} else if (cm.getPlayer().getMapId() == 100000) {
			cm.sendYesNo("Would you like me to fly you back to #dOrbis#k?");
		} else {
			cm.sendOk("Hey what's up?");
			cm.dispose();
		}
	} else if (status == 1) {
		if (cm.getPlayer().getMapId() == 200000100) {
			cm.warp(100000);
			cm.dispose();
		} else if (cm.getPlayer().getMapId() == 100000) {
			cm.warp(200000100);
			cm.dispose();
		}
	}
}