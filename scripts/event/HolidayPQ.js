/**
 *	@Name: Holiday PQ
 *	@Author: iPoopMagic (David)
 *	@Information:
 *		Maps
 *		-Path to Snow Man's Land (889100100)
 *		-Entrance - Snow Man's Land (8891000(0/1/2)0)
 *		-Snow Man's Land (8891000(0/1/2)1)
 *		-Exit - Snow Man's Land (8891000(0/1/2)2)
 *		NPCs
 *		-Snow Spirit (910500(3/4))
 *		Mobs
 *		-Tic (940031(0/3/6)) - Easy/Medium/Hard
 *		-Tac (940031(1/4/7)) - Easy/Medium/Hard
 *		-Toe (940031(2/5/8)) - Easy/Medium/Hard
 *		-Giant Snowman (Lvl 1) (94003(22/27/32))- Easy/Medium/Hard
 *		-Giant Snowman (Lvl 2) (94003(23/28/33))- Easy/Medium/Hard
 *		-Giant Snowman (Lvl 3) (94003(24/29/34))- Easy/Medium/Hard
 *		-Giant Snowman (Lvl 4) (94003(25/30/35))- Easy/Medium/Hard
 *		-Giant Snowman (Lvl 5) (94003(26/31/36))- Easy/Medium/Hard
 *		-Cross (94003(19/20/21)) - Easy/Medium/Hard
 */
var exitMap;
var mainMap;
var minPlayers = 3;
var pqTime = 10; //10 Minutes

function init() {
    em.setProperty("HPQOpen", "true"); // allows entrance.
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(mapid) {
    em.setProperty("HPQOpen", "false")
    var eim = em.newInstance("HolidayPQ_" + em.getProperty("leaderID"));
    eim.setProperty("stage", "0");
    eim.setProperty("clear", "false");
    mainMap = eim.getMapInstance(mapid); // <main>
    exitMap = eim.getMapInstance(mapid + 1); // <exit>
    mainMap.allowSummonState(false);
    mainMap.killAllMonsters();
    respawn(eim);
    var timer = 1000 * 60 * pqTime; // 10 minutes
    em.schedule("timeOut", eim, timer);
    eim.startEventTimer(timer);
    return eim;
}

function respawn(eim) {
	if (mainMap.getSummonState()) {
		mainMap.instanceMapRespawn();
	}
	eim.schedule("respawn", 15 * 1000);
}

function playerEntry(eim, player) {
    player.changeMap(mainMap, mainMap.getPortal(0));
}

function changedMap(eim, player, mapid) {
	if (mapid != 889100001 && mapid != 889100011 && mapid != 889100021) {
		playerExit(eim, player);
	}
}
function playerDead(eim, player) {
    if (player.isAlive()) {
        if (eim.isLeader(player)) {
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
            eim.dispose();
        } else {
            var partyz = eim.getPlayers();
            if (partyz.size() < minPlayers) {
                for (var j = 0; j < partyz.size(); j++)
                    playerExit(eim,partyz.get(j));
                eim.dispose();
            } else
                playerExit(eim, player);
        }
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            } else {
                playerExit(eim, party.get(i));
            }
		}
		em.setProperty("stage", "0");
		em.setProperty("HPQOpen", "true");
        eim.dispose();
    } else {
        var partyz = eim.getPlayers();
        if (partyz.size() < minPlayers) {
            for (var j = 0; j < partyz.size(); j++) {
                playerExit(eim,partyz.get(j));
			}
            eim.dispose();
        } else {
            playerExit(eim, player);
		}
    }
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
	if (eim.getPlayerCount() < 1) {
		em.setProperty("stage", "0");
		em.setProperty("HPQOpen", "true");
		eim.dispose();
	}
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function allMonstersDead(eim) {}

function cancelSchedule(eim) {
	//This needed? It causes problem on reloadevents
    //eim.startEventTimer(0);
}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}