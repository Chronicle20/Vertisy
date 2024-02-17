function init() {
	em.setProperty("state", "0");
}


function setup(eim) {
	em.setProperty("state", "1");
	var eim = em.newInstance("EvanDoll_" + em.getProperty("channel"), false);
	
	var map = eim.getMap(910600000);
	map.clearAndReset(true);
	var mob = map.getMonsterById(9300387);
	if(mob != null){
		eim.registerMonster(mob);
	}
	return eim;
}



function allMonstersDead(eim) {
	var map = eim.getMap(910600000);
	map.spawnNpc(1013201, 455, 305, map);
}

function playerEntry(eim, player) {
	var map = eim.getMap(910600000);
	player.changeMap(map, map.getPortal(0));
}

function playerRevive(eim, player) {
	playerExit(eim, player);
}

function playerExit(eim, player) {
    eim.unregisterPlayer(player, true);
    em.setProperty("state", "0");
    eim.dispose();
	player.changeMap(106010102, 1);
}

function removePlayer(eim, player) {
    player.getMap().removePlayer(player);
    player.setMap(106010102);
    eim.unregisterPlayer(player, true);
    eim.dispose();
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
}

function changedMap(eim, player, mapid){
	if(mapid != 910600000){
		eim.unregisterPlayer(player, true);
	    eim.dispose();
	    em.setProperty("state", "0");
	}
}

function monsterValue(eim, mobId) {
	return 1;
}

function cancelSchedule() {}