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
		if (cm.getPlayer().getLevel() < 40 && cm.haveItem(4032494)) {
			cm.sendYesNo("Would you like to move to Crocell Strolling Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and need the Crocell's Necklace to enter.");
			cm.dispose();
		}
	} else if (status == 1) {
		var CrocellMap = cm.getPlayer().getClient().getChannelServer().getMap(677000007);
		if (CrocellMap.getAllPlayer().size() < 1) {
			cm.warp(677000006, 0);
			if (CrocellMap.getMonsters().size() < 1) {
				CrocellMap.spawnMonsterOnGroundBelow(9400611, 0, 73);
			}
		} else {
			cm.getPlayer().dropMessage(5, "Someone is currently fighting Crocell. Please try a different channel.");
		}
		cm.dispose();
    }
}