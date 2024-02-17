/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

 //Fishing net: 2270008
 var net = 2270008;
 var chair = 3011000;
 
 
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
			if (cm.getPlayer().getMapId() == 970020000) {
				cm.sendSimple("What would you like to do?\r\n#L0##bHead back to Lith Harbor#k#l");
			} else if (cm.haveItem(chair) && cm.haveItem(net)) {
				cm.sendSimple("Where would you like to go? \r\n#L1##bCassandra's Shore#k#l");
			} else if (cm.haveItem(chair)) {
				cm.sendYesNo("Would you like to purchase some fish nets?\r\nOnly 1,000 Meso each!");
			} else if (cm.haveItem(net)) {
				cm.sendOk("You must use all your fishing nets before purchasing more.");
				cm.dispose();
			} else {
				cm.sendSimple("I see you don't have a fishing chair. Would you like to buy one for #b4,000,000#k Meso?\r\n#L2##bYes#k#l\r\n#L3##bNo#k#l");
			}
		} else if (status == 1) {
				if (selection == 0) {
						cm.warp(104000000);
						cm.dispose();
				} else if (selection == 1) {
						cm.warp(970020000);
						cm.dispose();
				} else if (selection == 2) {
						if (cm.getMeso() >= 4000000) {
							cm.gainMeso(-4000000);
							cm.gainItem(chair, 1);
							cm.sendOk("Happy fishing!");
							cm.dispose();
						}
				} else if (selection == 3) {
					cm.dispose();
				} else {
					var nets = 3000 / cm.getPlayer().getFishingCooldown();
					if(cm.getPlayer().isGM()){
						nets = 9999;
					}
					cm.sendGetNumber("How many would you like to purchase?\r\nYou can buy a max of " + nets, 1, 1, nets);
				}
		} else if (status == 2) {
				var nets = 3000 / cm.getPlayer().getFishingCooldown();
				if(cm.getPlayer().isGM()){
					nets = 9999;
				}
				if(selection > nets){
					cm.dispose();
				}
				if (cm.getMeso() >= selection*1000) {
					cm.gainMeso(-(selection*1000));
					cm.gainItem(net, selection);
					cm.sendOk("Happy fishing!");
					cm.dispose();
				}
		}
}