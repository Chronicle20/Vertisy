/**
 *	@Name: Demon's Doorway
 *	@Author: iPoopMagic (David)
 */
var status = -1;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
		status++;
    } else {
		cm.dispose();
		return;
    }
    if (status == 0) {
		if (cm.getPlayer().getLevel() < 40 && cm.haveItem(4032485)) {
			cm.sendYesNo("Would you like to move to Valefor's Strolling Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and have #rLarge Model of a Coin#k to enter.");
			cm.dispose();
		}
	} else if (status == 1) {
		var ValeforMap = cm.getPlayer().getClient().getChannelServer().getMap(677000009);
		if (ValeforMap.getAllPlayer().size() < 1) {
			cm.warp(677000008, 0);
			if (ValeforMap.getMonsters().size() < 1) {
				ValeforMap.spawnMonsterOnGroundBelow(9400613, 0, 66);
			}
		} else {
			cm.getPlayer().dropMessage(5, "Someone is currently fighting Valefor. Please try a different channel.");
		}
		cm.dispose();
    }
}