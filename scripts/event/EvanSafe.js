var exitMap = 220011000;
var terrace = 922030010;
var safe = 922030011;

function init() {
	em.setProperty("state", "0");
}


function setup(eim) {
	em.setProperty("state", "1");
	var eim = em.newInstance("EvanSafe_" + em.getProperty("channel"), false);
	
	var map = eim.getMap(safe);
	map.clearAndReset(true);
	/*var mob = map.getMonsterById(9300387);
	if(mob != null){
		eim.registerMonster(mob);
	}*/
	return eim;
}



function allMonstersDead(eim) {
	var map = eim.getMap(safe);
	var safeMob = map.getMonsterById(9300389);
	if(safeMob != null){
		eim.getPlayers().get(0).mobKilled(9300389);
		map.killFriendlies(safeMob);
	}
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
	if(mapid != terrace && mapid != safe){
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