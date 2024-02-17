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
		if (cm.getPlayer().getLevel() < 40 && cm.haveItem(4032495)) {
			cm.sendYesNo("Would you like to move to Marbas' Strolling Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and need the Marbas' Necklace to enter.");
			cm.dispose();
		}
	} else if (status == 1) {
		var marbasMap = cm.getPlayer().getClient().getChannelServer().getMap(677000001);
		if (marbasMap.getAllPlayer().size() < 1) {
			cm.warp(677000000, 0);
			if (marbasMap.getMonsters().size() < 1) {
				marbasMap.spawnMonsterOnGroundBelow(9400612, 0, 60);
			}
		} else {
			cm.getPlayer().dropMessage(5, "Someone is currently fighting Marbas. Please try a different channel.");
		}
		cm.dispose();
    }
}