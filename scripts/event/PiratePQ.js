/**
 * @Author: iPoopMagic (David)
 */

var minPlayers = 2;
var exitMap;
var firstMap; // On the Way to the Pirate Ship
var secondMap; // Through the Head of the Ship
var thirdMap; // Through the Deck I
var fourthMap; // The Area of 100yrOld Bellflower I
var fifthMap; // Lord Pirate's Servant I
var thirdMap1; // Through the Deck II
var fourthMap1; // The Area of 100yrOld Bellflower II
var fifthMap1; // Lord Pirate's Servant II
var sixthMap; // Eliminate Pirates!
var seventhMap; // The Captain's Dignity
var eighthMap; // Wu Yang Giving Thanks

function init() {
	em.setProperty("state", "0");
}

function setup() {
	em.setProperty("state", "1");
	var eim = em.newInstance("PiratePQ_" + em.getProperty("channel"));
	em.setProperty("stage2", "0");
	em.setProperty("stage2a", "0");
	em.setProperty("stage3a", "0");
	em.setProperty("stage4", "0");
	em.setProperty("stage5", "0");
	exitMap = em.getChannelServer().getMap(925100700);
	firstMap = em.getChannelServer().getMap(925100000);
	firstMap.clearAndReset(true);
	secondMap = em.getChannelServer().getMap(925100100);
	secondMap.allowSummonState(false);
	secondMap.clearAndReset(true);
	thirdMap = em.getChannelServer().getMap(925100200);
	thirdMap.clearAndReset(true);
	thirdMap1 = em.getChannelServer().getMap(925100300);
	thirdMap1.clearAndReset(true);
	sixthMap = em.getChannelServer().getMap(925100400);
	sixthMap.allowSummonState(true);
	sixthMap.clearAndReset(true);
	seventhMap = em.getChannelServer().getMap(925100500);
	seventhMap.clearAndReset(true);
	finishMap = em.getChannelServer().getMap(925100600);
	eim.schedule("scheduledTimeout", 20 * 60 * 1000);
	eim.schedule("respawn", 15 * 1000);
    eim.startEventTimer(20 * 60 * 1000); //20 mins
    return eim;
}

function respawn(eim) {
	var currentMapId = eim.getPlayers().get(0).getMapId();
	if (currentMapId == secondMap.getId() || currentMapId == sixthMap.getId()) {
		//if (!em.getProperty("stage2").equals("6")) {
			if (eim.getMapInstance(currentMapId).getSummonState()) {
				eim.getMapInstance(currentMapId).instanceMapRespawn();
			}
		//}
	}
	eim.schedule("respawn", 15 * 1000);
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(firstMap.getId());
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}

function changedMap(eim, player, mapid) {
    if (mapid < 925100000 || mapid > 925100500) {
		eim.unregisterPlayer(player);
		player.changeMap(exitMap, exitMap.getPortal(0));
	} else if (eim.getPlayerCount() < 1) {
		end(eim);
    }
}

function playerDisconnected(eim, player) {
	removePlayer(eim, player);
	if (eim.getPlayerCount() < 1) {
		end(eim);
	}
}


function playerDead(eim, player) {
	if (eim.isLeader(player)) {
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++)
			playerExit(eim, party.get(i));
		eim.dispose();
	}
	else
		playerExit(eim, player);
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
    if (eim.getPlayerCount() < 1) {
		end(eim);
	}
}

function playerFinish(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(finishMap, finishMap.getPortal(0));
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerFinish(eim, party.get(i));
    }
    end(eim);
}

function leftParty(eim, player) {
	playerExit(eim, player);
}

function disbandParty(eim) {
	var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExit(eim, party.get(i));
    end(eim);
}

function end(eim) {
	em.setProperty("state", "0");
	eim.dispose();
}

function monsterValue(eim, mobId) {
    return 1;
}
function allMonstersDead(eim) {}
function playerRevive(eim, player) {}
function cancelSchedule() {}