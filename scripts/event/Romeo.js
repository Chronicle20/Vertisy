/**
 *	@Name: Romeo & Juliet PQ (Romeo)
 *	@Modified: iPoopMagic (David)
 */
var exitMap;
var firstMap0;
var firstMap1;
var firstMap;
var secondMap;
var secondMap1;
var secondMap2;
var secondMap3;
var thirdMap;
var thirdMap1;
var thirdMap2;
var thirdMap3;
var thirdMap4;
var fourthMap;
var fourthMap1;
var fifthMap;
var sixthMap;

var minPlayers = 4;

function init() {
	em.setProperty("state", "0");
}

function setup() {
	em.setProperty("state", "1");
	var eim = em.newInstance("Romeo_" + em.getProperty("channel"), false);
	em.setProperty("stage", "0"); // whether book.. gave report, whether urete.. accepted it
	em.setProperty("stage1", "0"); // Find the button
	em.setProperty("stage3", "0"); // How many beakers filled
	em.setProperty("stage4", "0"); // How many files given
	em.setProperty("stage5", "0"); // mobs spawns / Yulete opens portal
	em.setProperty("summoned", "0");
	var sn;
	for (sn = 0; sn < 4; sn++) { //stage number
		em.setProperty("stage6_" + sn, "0");
		for (var b = 0; b < 10; b++) {
			for (var c = 0; c < 4; c++) {
				em.setProperty("stage6_ " + sn + "_" + b + "_" + c + "", "0");
			}
		}
	}
	var i;
	for (sn = 0; sn < 4; sn++) { //stage number
		for (i = 0; i < 10; i++) {
			var found = false;
			while (!found) {
				for (var x = 0; x < 4; x++) {
					if (!found) {
						var founded = false;
						for (var z = 0; z < 4; z++) { //check if any other stages have this value set already
							if (em.getProperty("stage6_" + z + "_" + i + "_" + x + "") == null) {
								em.setProperty("stage6_" + z + "_" + i + "_" + x + "", "0");
							} else if (em.getProperty("stage6_" + z + "_" + i + "_" + x + "").equals("1")) {
								founded = true;
								break;
							}
						}
						if (!founded && java.lang.Math.random() < 0.25) {
							em.setProperty("stage6_" + sn + "_" + i + "_" + x + "", "1");
							found = true;
							break;
						}
					}
				}
			}
			//BUT, stage6_0_0_0 set, then stage6_1_0_0 also not set!
		}
	}
	em.setProperty("stage7", "0"); // Did Romeo / Juliet die?
	firstMap0 = eim.getMapInstance(926100000);
	firstMap0.clearAndReset(true);
	firstMap1 = eim.getMapInstance(926100001);
	firstMap1.clearAndReset(true);
	firstMap = eim.getMapInstance(926100100);
	firstMap.clearAndReset(true);
	secondMap = eim.getMapInstance(926100200);
    secondMap.clearAndReset(true);
	secondMap1 = eim.getMapInstance(926100201);
	secondMap1.clearAndReset(true);
	secondMap1.shuffleReactors();
	secondMap2 = eim.getMapInstance(926100202);
    secondMap2.clearAndReset(true);
    secondMap2.shuffleReactors();
	secondMap3 = eim.getMapInstance(926100203);
	secondMap3.clearAndReset(true);
	thirdMap = eim.getMapInstance(926100300);
	thirdMap.clearAndReset(true);
	thirdMap1 = eim.getMapInstance(926100301);
	thirdMap1.clearAndReset(true);
	thirdMap2 = eim.getMapInstance(926100302);
	thirdMap2.clearAndReset(true);
	thirdMap3 = eim.getMapInstance(926100303);
	thirdMap3.clearAndReset(true);
	thirdMap4 = eim.getMapInstance(926100304);
	thirdMap4.clearAndReset(true);
	fourthMap = eim.getMapInstance(926100400);
	fourthMap.clearAndReset(true);
	fourthMap1 = eim.getMapInstance(926100401);
	fourthMap1.clearAndReset(true);
	fifthMap = eim.getMapInstance(926100500); //spawn Yulete based on properties ?????
	fifthMap.clearAndReset(true);
	sixthMap = eim.getMapInstance(926100600); //spawn romeo&juliet OR fallen romeo/juliet based on properties???
	sixthMap.clearAndReset(true);
	exitMap = em.getChannelServer().getMap(926100700);
    eim.startEventTimer(20 * 60 * 1000); // 20 min
	eim.schedule("scheduledTimeout", 21 * 60 * 1000);
	eim.schedule("respawn", 10 * 1000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(firstMap0, firstMap0.getPortal(0));
}

function respawn(eim) {
	var currentMapId = eim.getPlayers().get(0).getMapId();
	if (currentMapId == firstMap.getId() || currentMapId == secondMap.getId()) {
		eim.getMapInstance(currentMapId).instanceMapRespawn();
	}
	eim.schedule("respawn", 15 * 1000);
}

function playerRevive(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i));
        }
        eim.dispose();
    }
    else { //boot dead player
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() <= minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function scheduledTimeout(eim) {
	eim.broadcastPlayerMsg(5, "You have run out of time to save Juliet.");
	var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}

function changedMap(eim, player, mapid) {
    if (mapid < 926100000 || mapid > 926100600) {
		eim.unregisterPlayer(player);
		em.setProperty("state", "0");
	}
    if (mapid == 926100401 && (em.getProperty("summoned") == null || em.getProperty("summoned").equals("0"))) { //last stage
		var mobId = 9300139;
		if (em.getProperty("stage").equals("2")) {
			mobId = 9300140;
		}
		var mob = Packages.server.life.MapleLifeFactory.getMonster(mobId);
		eim.registerMonster(mob);
		em.setProperty("summoned", "1");
		eim.getMapInstance(mapid).spawnMonsterOnGroundBelow(mob, new java.awt.Point(240, 150));
    }
	var map = eim.getMapInstance(mapid);
	if (mapid == 926100500) {
		eim.restartEventTimer(10 * 60 * 1000);
		if (em.getProperty("stage7").equals("0")) {
			map.spawnNpc(2112001, 232, 150, map);
		} else {
			map.spawnNpc(2112002, 232, 150, map);
		}
	}
	if (mapid == 926100600) {
		eim.restartEventTimer(10 * 60 * 1000);
		if (em.getProperty("stage7").equals("0")) {
			map.spawnNpc(2112006, 111, 128, map); // Romeo, 2112018 doesn't work for some reason
			map.spawnNpc(2112005, 66, 128, map); // Juliet
			map.spawnNpc(2112001, 320, 128, map); // Dead Yulete
		} else {
			map.spawnNpc(2112009, 66, 128, map); // Dead Juliet
			map.spawnNpc(2112008, 111, 128, map); // Dead Romeo
			map.spawnNpc(2112002, 320, 128, map); // Yulete
		}
	}
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //PWN THE PARTY (KICK OUT)
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            }
            else {
                playerExit(eim, party.get(i));
            }
        }
        eim.dispose();
    }
    else {
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() < minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function monsterValue(eim, mobId) {
    return 1;
}

function playerExit(eim, player) {
	em.setProperty("state", "0");
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerFinish(eim) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        eim.unregisterPlayer(player);
        player.changeMap(exitMap, exitMap.getPortal(0));
    }
    eim.dispose();
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    playerFinish(eim);
}

function allMonstersDead(eim) {
}

function leftParty(eim, player) {			
    // If only 5 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() <= minPlayers) {
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim,party.get(i));
        }
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerDead(eim, player) {}
function cancelSchedule() {}