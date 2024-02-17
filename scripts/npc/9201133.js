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
		if (cm.getPlayer().getLevel() < 40 && cm.getPlayer().getMapId() != 677000012 && cm.getPlayer().getMapId() != 677000011) {
			cm.sendYesNo("Would you like to move to Astaroth's Strolling Place?");
		} else if (cm.getPlayer().getMapId() == 677000011) {
			cm.sendYesNo("Would you like to move to Astaroth's Hiding Place?");
		} else if (cm.getPlayer().getMapId() == 677000012) {
			cm.sendYesNo("Would you like to leave Astaroth's Hiding Place?");
		} else {
			cm.sendOk("You need to be less than level 40 and need Astaroth's Necklace to enter.");
			cm.dispose();
		}
	} else if (status == 1) {
		var astarothMap = cm.getPlayer().getClient().getChannelServer().getMap(677000012);
		if (cm.getPlayer().getMapId() == 105050400) {
			if (astarothMap.getAllPlayer().size() < 1) {
				if (cm.getPlayer().isInParty()) {
					cm.warpParty(astarothMap.getId() - 2);
				} else {
					cm.warp(astarothMap.getId() - 2);
				}
				if (astarothMap.getMonsters().size() < 2) {
					astarothMap.spawnMonsterOnGroundBelow(9400633, 1000, 40);
				}
			} else {
				cm.getPlayer().dropMessage(5, "Someone is currently fighting Astaroth. Please try a different channel.");
			}			
			cm.dispose();
		} else if (cm.getPlayer().getMapId() == 677000011) {
			if (cm.getPlayer().isInParty()) {
				cm.warpParty(astarothMap.getId());
			} else {
				cm.warp(astarothMap.getId());
			}
			cm.dispose();
		} else if (cm.getPlayer().getMapId() == 677000012) {
			cm.warp(105050400, 0);
			cm.dispose();
		}
    }
}