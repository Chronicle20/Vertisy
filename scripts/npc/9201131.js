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
    }//https://www.youtube.com/watch?v=EntR4KpEoTc
    if (status == 0) {
		if (cm.getPlayer().getLevel() < 40 && (cm.haveItem(4032492, 1) || cm.haveItem(4032482, 1))) {
			cm.sendYesNo("Would you like to move to Amdusias' Strolling Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and #bDarkween's Monster Drum#k to enter. Also make sure you have #bSolomon's Bow#k.");
			cm.dispose();
		}
	} else if (status == 1) {
		var AmdusiasMap = cm.getPlayer().getClient().getChannelServer().getMap(677000003);
		if (AmdusiasMap.getAllPlayer().size() < 1) {
			cm.warp(677000002, 0);
			if (AmdusiasMap.getMonsters().size() < 1) {
				AmdusiasMap.spawnMonsterOnGroundBelow(9400610, 0, 35);
			}
		} else {
			cm.getPlayer().dropMessage(5, "Someone is currently fighting Amdusias. Please try a different channel.");
		}
		cm.dispose();
    }
}