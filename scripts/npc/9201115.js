/**
 *	@Name: Battle Statue (CWKPQ)
 *	@Modified: iPoopMagic (David)
 */

var status = -1;
var expedition;

function start() {
	action(1, 0, 0);
}

function action(mode, type, selection) {
	expedition = cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.CWKPQ);	
    if (mode > 0) {
		status++;
    } else {
		status--;
    }
    if (!expedition.isLeader(cm.getPlayer())) {
		cm.sendNext("I wish for your expedition leader to talk to me.");
		cm.dispose();
		return;
    }
	var em = cm.getEventManager("CWKPQ");
	if (em != null) {
		if (em.getProperty("glpq6").equals("0")) {
			if (status == 0) {
				cm.sendNext("Welcome to the Twisted Masters' Keep. I will be your host for this evening...");
			} else if (status == 1) {
				cm.sendNext("Tonight, we have a feast of a squad of Maplers.. ahaha...");
			} else if (status == 2) {
				cm.sendNext("Let our specially trained Master Guardians escort you!");
				cm.mapMessage(6, "Prepare for battle!");
				for (var i = 0; i < 5; i++) { // Master Guardians
					var mob = Packages.server.life.MapleLifeFactory.getMonster(9400594);
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-1337 + (java.lang.Math.random() * 1337), 276));
				}
				for (var i = 0; i < 10; i++) { // Crimson Guardians
					var mob = Packages.server.life.MapleLifeFactory.getMonster(9400582);
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-1337 + (java.lang.Math.random() * 1337), 276));
				}
				em.setProperty("glpq6", "1");
				cm.dispose();
			}
		} else if (em.getProperty("glpq6").equals("1")) {
			if (cm.getPlayer().getMap().getMonstersEvent(cm.getPlayer()).size() < 1) {
				if (status == 0) {
					cm.sendNext("Eh, what is this? You've defeated them?");
				} else if (status == 1) {
					cm.sendNext("Well, no matter! The Twisted Masters will be glad to welcome you.");
					cm.mapMessage(6, "Beware, the Twisted Masters are approaching!");
					/* SPAWN THE BOSSES! */
//					var mob0 = Packages.server.life.MapleLifeFactory.getMonster(9400589); // MV
//					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob0, new java.awt.Point(-1000, 276));					
					var mob1 = Packages.server.life.MapleLifeFactory.getMonster(9400590); // Margana
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob1, new java.awt.Point(-22, 1));
					var mob2 = Packages.server.life.MapleLifeFactory.getMonster(9400591); // Red Nirg
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob2, new java.awt.Point(-22, 276));
					var mob3 = Packages.server.life.MapleLifeFactory.getMonster(9400592); // Rellik
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob3, new java.awt.Point(-496, 276));
					var mob4 = Packages.server.life.MapleLifeFactory.getMonster(9400593); // Hsalf
					cm.getPlayer().getMap().spawnMonsterOnGroundBelow(mob4, new java.awt.Point(496, 276));
					
					em.setProperty("glpq6", "2");
					cm.dispose();
				}
			} else {
				cm.sendOk("Pay no attention to me. The Master Guardians will escort you!");
				cm.dispose();
			}
		} else if (em.getProperty("glpq6").equals("2")) {
			if (cm.getPlayer().getMap().getMonstersEvent(cm.getPlayer()).size() < 1) {
				cm.sendOk("WHAT? Ugh... this can't be happening.");
				cm.mapMessage(6, "The portal to the next stage has opened!");
				em.setProperty("glpq6", "3");
				cm.dispose();
			} else {
				cm.sendOk("Pay no attention to me. The Twisted Masters will escort you!");
				cm.dispose();
			}
		} else {
			cm.dispose();
		}
	} else {
		cm.dispose();
	}

}