var exitMap = 251010403;
var vault = 925110000;

function init() {
	em.setProperty("state", "0");
}


function setup(eim) {
	em.setProperty("state", "1");
	var eim = em.newInstance("Pottery" + em.getProperty("channel"));
	
	var map = eim.getMap(vault);
	map.clearAndReset(true);
	return eim;
}

function allMonstersDead(eim) {
	
}

function playerEntry(eim, player) {
	var map = eim.getMap(vault);
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
	if(mapid != vault){
		eim.unregisterPlayer(player, true);
	    eim.dispose();
	    em.setProperty("state", "0");
	}
}

function timeout(eim){
	if(eim.getPlayerCount() > 0){
		playerExit(eim, eim.getPlayers().get(0));
	}else{
		eim.dispose();
		em.setProperty("state", "0");
	}
}

function monsterValue(eim, mobId) {
	return 1;
}

function cancelSchedule() {}