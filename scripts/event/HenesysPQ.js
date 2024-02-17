/* 
 *
 *Henesys PQ : FightDesign ->RaGEZONE / FXP
 *modified by peter
 *
 */
// Significant maps (this was already here, helpful though)
// 100000200 - Pig Park
// 910010000 - 1st Stage
// 910010100 - Shortcut
// 910010200 - Bonus
// 910010300 - Exit
// 910010400 - Shortcut
// Significant items
// 4001101 - Rice Cake
// 1002798 - A Rice Cake on Top of My Head
// Significant monsters
// 9300061 - Bunny
// 9300062 - Flyeye
// 9300063 - Stirge
// 9300064 - Goblin Fires
// Significant NPCs
// 1012112 - Tory
// 1012113 - Tommy
// 1012114 - Growlie
// map effects
// Map/Obj/Effect/quest/gate/3 - warp activation glow
// quest/party/clear - CLEAR text
// Party1/Clear - clear sound
/* INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300061,4001101,1);
 * seeds sql
 * INSERT INTO `reactordrops` (`reactorid`,`itemid`,`chance`) VALUES
 (9102002,4001095,5),
 (9102003,4001096,5),
 (9102004,4001097,5),
 (9102005,4001098,5),
 (9102006,4001099,5),
 (9102007,4001100,5);
 */

var exitMap;
var mainMap;
var minPlayers = 3;
var pqTime = 10; //10 Minutes
var map; //map instance of starting map

function init() {
    exitMap = em.getChannelServer().getMap(910010400); // <exit>
    exitClearMap = em.getChannelServer().getMap(910010100); // <clear>
    mainMap = em.getChannelServer().getMap(910010000); // <main>
    em.setProperty("HPQOpen", "true"); // allows entrance.
    //em.setProperty("shuffleReactors", "true");
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
//    em.setProperty("HPQOpen", "false"); //covered by em.startPQ
    var eim = em.newInstance("HenesysPQ");

    eim.setProperty("stage", "0");
    eim.setProperty("clear", "false"); //it's not used, not correctly at least. 
//    initFlowerStates(eim);

    map = eim.getMapInstance(mainMap.getId());
    map.allowSummonState(false);
    map.killAllMonsters();
//    respawn(eim); //moved to fullmoon, it's better that way.

    var timer = 1000 * 60 * pqTime; // 10 minutes
//    em.schedule("timeOut", eim, timer);
//    eim.startEventTimer(timer);
    eim.scheduleEventTimer(timer);
    eim.setClock(60 * pqTime);

    var HPQ = Java.type("server.partyquest.HPQ");
    var PartyQuest = Java.type("server.partyquest.PartyQuest");
    PartyQuest.create(HPQ.class, em.getParty("HPQ"), eim.getTimeStarted());

    return eim;
}

//a hack to know if reactors 9108000 - 9108005 has changed state (from 0 to 1)
//function initFlowerStates(eim) {
//    for (var i = 9108000; i <= 9108005; i++)
//        eim.setProperty(i, 0);
//}

function respawn(eim) {
//    var map = eim.getMapInstance(910010000); //ori
    if (map.getSummonState()) {
        map.instanceMapRespawn();
    }
    eim.schedule("respawn", 10000);
}

function playerEntry(eim, player) {
//    var map = eim.getMapInstance(mainMap.getId()); //ori
    player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    var party = eim.getPlayers();
    if (eim.isLeader(player) || party.size() <= minPlayers) { // Check for party leader
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function playerDead(eim, player) {
//    if (!player.isAlive()) {
//        var party = eim.getPlayers();
//        if (eim.isLeader(player)) {
//            for (var i = 0; i < party.size(); i++)
//                playerExit(eim, party.get(i));
//            eim.dispose();
//        } else {
//            if (party.size() < minPlayers) {
//                for (var j = 0; j < party.size(); j++)
//                    playerExit(eim, party.get(j));
//                eim.dispose();
//            } else
//                playerExit(eim, player);
//        }
//    }
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    var size = party.size();

    if (eim.isLeader(player) || size < minPlayers) {
        for (var i = 0; i < size; i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            } else {
                playerExit(eim, party.get(i));
            }
        }
    } else {
        removePlayer(eim, player);
    }
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
    } else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
}

function playerExitClear(eim, player) {
    player.endPQ(true, eim.getDuration());
    player.changeMap(exitClearMap, exitClearMap.getPortal(0));
}

function playerExit(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.changeMap(exitMap, exitMap.getPortal(0));
}

function removePlayer(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExitClear(eim, party.get(i));
}

function allMonstersDead(eim) {}

function dispose() {
    em.cancelSchedule();
    em.schedule("OpenHPQ", 5000);
}

function cancelSchedule(eim) {
    //This needed? It causes problem on reloadevents
    //eim.startEventTimer(0);
}

function timeOut(eim) {
    if (eim !== null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
    }
}

function OpenHPQ() { //this is not used anymore i think, HPQ.openPQ (subclass) handles it.
    em.setProperty("HPQOpen", "true");
}

function changedMap(eim, chr, mapId) {
    for (var i = 0; i <= 400; i += 100) {
        if (mapId === 910010000 + i) { //do nothing if its PQ map
            return;
        }
    }

    //if its not pq map, end the pq with failure, unregister player from eim and dispose the eim if no one is in it.
    chr.endPQ(false, eim.getDuration());
}