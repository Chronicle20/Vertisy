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
		if (cm.getPlayer().getLevel() < 40 && cm.haveItem(4032491)) {
			cm.sendYesNo("Would you like to move to Andras' Strolling Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and need the Andras' Necklace to enter.");
			cm.dispose();
		}
	} else if (status == 1) {
		var AndrasMap = cm.getPlayer().getClient().getChannelServer().getMap(677000005);
		if (AndrasMap.getAllPlayer().size() < 1) {
			cm.warp(677000004, 0);
			if (AndrasMap.getMonsters().size() < 1) {
				AndrasMap.spawnMonsterOnGroundBelow(9400609, 0, 86);
			}
		} else {
			cm.getPlayer().dropMessage(5, "Someone is currently fighting Andras. Please try a different channel.");
		}
		cm.dispose();
    }
}