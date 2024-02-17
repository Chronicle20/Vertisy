/**
 * @NPC: Bell (NLC Subway Staff)
 * @Map: NLC Subway Station (600010001) & Subway Ticketing Booth (103000100)
 */
function start() {
	if (cm.getPlayer().getMapId() == 103000100 || cm.getPlayer().getMapId() == 600010001) {
		cm.sendYesNo("The ride to " + (cm.getPlayer().getMapId() == 103000100 ? "New Leaf City of Masteria" : "Kerning City of Victoria Island") + " takes off every minute, beginning on the hour, and it'll cost you #b5000 mesos#k. Are you sure you want to purchase #b#t" + (4031711 + parseInt(cm.getPlayer().getMapId() / 300000000)) + "##k?");
	} else if (cm.getPlayer().getMapId() == 600010002 || cm.getPlayer().getMapId() == 600010004) {
		cm.sendYesNo("Do you want to leave before the train starts? There will be no refund.");
	}
}

function action(mode, type, selection) {
	if (mode != 1) {
		cm.dispose();
		return;
	}
	if (cm.getPlayer().getMapId() == 103000100 || cm.getPlayer().getMapId() == 600010001) {
		if (cm.getMeso() >= 5000 && cm.canHold(4031711)) {
			cm.gainMeso(-5000);
			cm.gainItem(4031711 + parseInt(cm.getPlayer().getMapId() / 300000000), 1);
			cm.sendNext("There you go.");
		} else
			cm.sendNext("You don't have enough mesos or did not have enough inventory space.");
    } else {
        cm.warp(cm.getPlayer().getMapId() == 600010002 ? 600010001 : 103000100);
    }
    cm.dispose();
}