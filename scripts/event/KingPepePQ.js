/**
 * @Name: King Pepe PQ
 * @Author: David
 */
var battleMap;
var exitMap;
var mobType = 0;
var mobId = 3300007;
function init() {

}

function setup(mapid) {
	var roomNum = mapid % 10; // Room Number (0-9)
    var eim = em.newInstance("KingPepePQ_" + roomNum);
	// Setup the Maps
	battleMap = eim.getMap(106021500);
	exitMap = em.getChannelServer().getMap(106021400);
	battleMap.clearAndReset(true);
	eim.setProperty("started", "false"); // We have not started yet
    
    var rand = Math.random() * 100;
    if(rand < 50)
        mobType = 0;
    else if(rand < 80)
        mobType = 1;
    else
        mobType = 2;
    
    summonKing(eim);
	return eim;
}

function playerEntry(eim, player) {
	player.changeMap(battleMap, battleMap.getPortal(1));
    // The client seems to have these effects removed. Not sure why, even on clean v83.
    //player.announce(Packages.tools.packets.UserLocal.UserEffect.showIntro("Effect/Direction2.img/mushCatle/pepeKing" + mobType));
}

function changedMap(eim, player) {

}

function summonKing(eim) {
    battleMap.spawnMonsterOnGroundBelow(mobId - mobType, 270, -68);
}

function start(eim) {
	eim.setProperty("started", "true");
	eim.startEventTimer(10 * 60 * 1000); // 10 minutes
	eim.broadcastPlayerMsg(5, "Oh no, King Pepe has found you!");
	em.schedule("timeOut", 10 * 60 * 1000);
}

function timeOut(eim) {
	eim.broadcastPlayerMsg(5, "You have run out of time to kill King Pepe.");
	if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
        eim.dispose();
    }
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
	player.setMap(exitMap);
	eim.unregisterPlayer(player);
}

function leftParty(eim, player) {

}

function disbandParty(eim) {
	eim.dispose();
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
    if (eim.getPlayerCount() < 1) {
		eim.dispose();
	}
}

function removePlayer(eim, player) {
	playerExit(eim, player);
}

function monsterValue(eim, mobId) {
	return 1;
}

function cancelSchedule() {
}

function allMonstersDead(eim) {
}