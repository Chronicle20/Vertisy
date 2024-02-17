
925020100

var mainMap;
var exitMap;
var minPlayers = 1;

function init(){
	em.setProperty("Open", "true"); // allows entrance.
}


function setup(idfk){
    var eim = em.newInstance("Dojo_" + em.getProperty("latestLeader"));
    eim.setProperty("stage", "0");
	exitMap = em.getChannelServer().getMap(925020002); // <exit>
    mainMap = eim.getMapInstance(925020100);
    //var timer = 1000 * 60 * 60; // 1 hour
    //em.schedule("timeOut", eim, timer);
    //eim.startEventTimer(timer);
    return eim;
}


function playerEntry(eim, player) {
	player.removeClock();
    player.changeMap(mainMap, mainMap.getPortal(0));
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.changeMap(exitMap);
    player.removeClock();
}

function playerDead(eim, player){
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

function playerDisconnected(eim, player){
    if (eim.isLeader(player)) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            } else {
                playerExit(eim, party.get(i));
            }
        }
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


function leftParty(eim, player){
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim){
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}


function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExitClear(eim, party.get(i));
    eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap);
    player.removeClock();
}

function changedMap(eim, player, mapid){}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim){
	eim.setProperty("stage", parseInt(eim.getProperty("stage")) + 1);
}

function dispose() {
    em.cancelSchedule();
    em.schedule("OpenPQ", 5000);
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

function cancelSchedule(eim){}

function OpenPQ() {
    em.setProperty("open", "true");
}