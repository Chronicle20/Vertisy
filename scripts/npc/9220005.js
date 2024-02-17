/**
 *	@Name: Roudolph
 *	@Description: Warp to Extra Frosty Snow Zone
 *	@Author: iPoopMagic (David)
*/
var status = 0;

function start() {
	if (cm.getMapId() != 209080000) {
		cm.sendYesNo("Do you want to go to the Extra Frosty Snow Zone?");
	} else {
		cm.sendYesNo("I can send you back to Happyville. Would you like to go back?");
	}
}

function action(mode, type, selection) {
	if (mode != 1) {
		cm.sendOk("Happyville needs your help to make this a wonderful White Christmas!");
		cm.dispose();
		return;
	}
	status++;
	if (status == 1) {
		if (cm.getMapId() != 209080000) {
			if (cm.getPlayer().getItemQuantity(1472063, true) < 1) {
				cm.sendYesNo("But first, you'll need a #b#v1472063:# #t1472063##k to enter the #bExtra Frosty Snow Zone#k, or you'll freeze to death. You can purchase these from me for 100,000 mesos. Would you like one?");
			} else {
				cm.warp(209080000, 0);
				cm.dispose();
			}
		} else {
			cm.warp(209000000, 0);
			cm.dispose();
		}
	} else if (status == 2) {
		if (cm.getMapId() != 209080000) {
			if (cm.getPlayer().getMeso() > 99999 && cm.canHold(1472063)) {
				cm.gainMeso(-100000);
				cm.gainItem(1472063, 1);
				cm.warp(209080000, 0);
			} else {
				cm.sendOk("Don't think you can rip me off! Where's the holiday spirit in you?");
			}
			cm.dispose();
		}
	}
}