/**
 * @NPC: Guon
 * @Description: Pirate PQ Exit
 * @Author: iPoopMagic (David)
 */

function start() {
    //(mode > 0 ? status++ : status--);
    if (cm.getPlayer().getMapId() == 925100700) {
		cm.removeAll(4001117);
		cm.removeAll(4001120);
		cm.removeAll(4001121);
		cm.removeAll(4001122);
		cm.warp(251010404, 0);
		cm.dispose();
		return;
    }
    var em = cm.getEventManager("PiratePQ");
    if (em == null) {
		cm.sendNext("The event hasn't started, please contact a GM immediately.");
		cm.dispose();
		return;
    }
    if (!cm.isLeader()) {
		cm.sendNext("Only your party leader may talk to me.");
		cm.dispose();
		return;
    }
    switch(cm.getPlayer().getMapId()) {
		case 925100000:
		   cm.sendNext("We are heading into the Pirate Ship now! First, we must kill all the monsters guarding it.");
		   cm.dispose();
		   break;
		case 925100100:
			var eim = cm.getPlayer().getEventInstance();
			var secondMap = cm.getPlayer().getMap();
			var emp = em.getProperty("stage2");
			if (emp == null) {
				em.setProperty("stage2", "0");
				emp = "0";
			}
			if (emp.equals("0") || emp.equals("1")) {
				if (cm.haveItem(4001120, 20)) {
					cm.gainItem(4001120, -20);
					cm.sendNext("Well done!");
					secondMap.clearAllowedMonsters();
					cm.getPlayer().getMap().killAllMonsters();
					em.setProperty("stage2", "2");
				} else {
					cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Hunt me 20 #rMark of the Rookie Pirate#k.");
					if (emp.equals("0")) {
						// LOL, non-GMS-like :D
						secondMap.addAllowedMonster(9300114);
						secondMap.allowSummonState(true);
						eim.getMapInstance(cm.getPlayer().getMapId()).instanceMapRespawn();
						em.setProperty("stage2", "1");
					}
				}
			} else if (emp.equals("2") || emp.equals("3")) {
				if (cm.haveItem(4001121, 20)) {
					cm.sendNext("Well done!");
					secondMap.clearAllowedMonsters();
					cm.getPlayer().getMap().killAllMonsters();
					cm.gainItem(4001121, -20);
					em.setProperty("stage2", "4");
				} else {
					cm.sendNext("Hunt me 20 #rMark of the Rising Pirate#k.");
					if (emp.equals("2")) {
						secondMap.addAllowedMonster(9300115);
						eim.getMapInstance(cm.getPlayer().getMapId()).instanceMapRespawn();
						em.setProperty("stage2", "3");
					}
				}
			} else if (emp.equals("4") || emp.equals("5")) {
				if (cm.haveItem(4001122, 20)) {
					cm.sendNext("Well done! Now let us go.");
					secondMap.clearAllowedMonsters();
					cm.getPlayer().getMap().killAllMonsters();
					cm.getPlayer().getMap().allowSummonState(false);
					cm.gainItem(4001122, -20);
					em.setProperty("stage2", "6");
				} else {
					cm.sendNext("Hunt me 20 #rMark of the Veteran Pirate#k.");
					if (emp.equals("4")) {
						secondMap.addAllowedMonster(9300116);
						eim.getMapInstance(cm.getPlayer().getMapId()).instanceMapRespawn();
						em.setProperty("stage2", "5");
					}
				}
			} else {
				cm.sendNext("The next stage has opened. GO!");
			}
			cm.dispose();
			break;
		case 925100200:
			cm.sendNext("To fully take back this the pirate ship, we must destroy the guards first.");
			cm.dispose();
			break;
		case 925100201:
			if (cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getMonstersEvent(cm.getPlayer()).size() < 1) {
				cm.sendNext("Well done.");
				if (em.getProperty("stage2a").equals("0")) {
					cm.getMap().setReactorState();
					em.setProperty("stage2a", "1");
				}
			} else {
				cm.sendNext("These bellflowers are in hiding. We must liberate them.");
			}
			cm.dispose();
			break;
		case 925100301:
			if (cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getMonstersEvent(cm.getPlayer()).size() < 1) {
				cm.sendNext("Well done.");
				if (em.getProperty("stage3a").equals("0")) {
					cm.getPlayer().getMap().setReactorState();
					em.setProperty("stage3a", "1");
				}
			} else {
				cm.sendNext("These bellflowers are in hiding. We must liberate them.");
			}
			cm.dispose();
			break;
		case 925100202:
		case 925100302:
			cm.sendNext("These are the Captains and Krus which devote their whole life to Lord Pirate. Kill them as you see fit.");
			cm.dispose();
			break;
		case 925100400:
			cm.sendNext("These are the sources of the ship's power. We must seal it by using the Old Metal Keys on the doors!");
			cm.dispose();
			break;
		case 925100500:
			if (cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getMonstersEvent(cm.getPlayer()).size() < 1) {
				cm.warpParty(925100600);
				cm.givePartyQuestExp("PiratePQ");
				cm.getPlayer().getEventInstance().finishPQ();
			} else {
				cm.sendNext("Defeat all monsters! Even Lord Pirate's minions!");
			}
			cm.dispose();
			break;
    }
}

function action(m, t, s) {
}