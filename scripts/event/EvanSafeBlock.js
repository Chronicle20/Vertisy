var exitMap = 220011000;
var terrace = 922030020;
var safe = 922030021;
var safe2 = 922030022;

function init() {
	em.setProperty("state", "0");
}


function setup(eim) {
	em.setProperty("state", "1");
	var eim = em.newInstance("EvanSafeBlock_" + em.getProperty("channel"), false);
	
	var map = eim.getMap(safe2);
	map.clearAndReset(true);
	return eim;
}



function allMonstersDead(eim) {

}

function playerEntry(eim, player) {
	var map = eim.getMap(terrace);
	player.changeMap(map, map.getPortal(0));
	eim.setClock(10 * 60);
	eim.schedule("timeout", 10 * 60 * 1000);
}

function playerRevive(eim, player) {
	playerExit(eim, player);
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player, true);
    em.setProperty("state", "0");
    eim.dispose();
	player.changeMap(exitMap, 1);
}

function removePlayer(eim, player) {
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
    eim.unregisterPlayer(player, true);
    eim.dispose();
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
}

function changedMap(eim, player, mapid){
	if(mapid != terrace && mapid != safe && mapid != safe2){
		eim.unregisterPlayer(player, true);
	    eim.dispose();
	    em.setProperty("state", "0");
	}
}

function monsterValue(eim, mobId) {
	return 1;
}

function cancelSchedule() {}