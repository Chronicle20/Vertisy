/**
 *	@Name: Investigation Result (Romeo)
 *	@Author: iPoopMagic
 */
var status = -1;

function start() {
    var em = cm.getEventManager("Romeo");
    if (em == null) {
		cm.dispose();
		return;
    }
    if (!cm.canHold(4001130)) {
		cm.sendOk("You will need 1 ETC space.");
		cm.dispose();
		return;
    }
    if (cm.getPlayer().getMapId() == 926100000) { // 26 npcs
		if (!em.getProperty("stage1").equals("finished")) {
			if (java.lang.Math.random() < 0.2) {
				em.setProperty("stage1", "finished");
				clear();
				cm.getPlayer().getMap().setReactorState();
				cm.mapMessage(6, cm.getPlayer().getName() + " pressed a button, and a special portal appeared.");
				cm.givePartyQuestExp("MagatiaPQ0");
			} else if (java.lang.Math.random() < 0.05) {
				if (em.getProperty("stage").equals("0")) {
					cm.gainItem(4001130, 1);
					em.setProperty("stage", "1");
					cm.sendOk("You've gained an item!");
				} else {
					cm.sendOk("It doesn't look like anything is here.");
				}
			} else {
				cm.sendOk("It doesn't look like anything is here.");
			}
		}
    } else if (cm.getPlayer().getMapId() == 926100203) {
		var npc = Packages.server.life.MapleLifeFactory.getNPC(2112000); // Yulete
		var player = cm.getPlayer();
		var x = 282;
		var y = 243;
		if (npc != null) {
			npc.setPosition(new java.awt.Point(x, y));
			npc.setCy(y);
			npc.setRx0(x + 50);
			npc.setRx1(x - 50);
			npc.setFh(player.getMap().getFootholds().findBelow(new java.awt.Point(x, y)).getId());
			player.getMap().addMapObject(npc);
			player.getMap().broadcastMessage(Packages.tools.packets.field.NpcPool.spawnNPC(npc));
		}
	}
    cm.dispose();
}

function clear() {
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
}