/*
 * Kyrin's Training Ground for Super Transform 
 * By: Justin :)
 */

 function init() {
	// Just a requirement for the script to work?
 }
 
 function monsterValue(eim, mobId) {
	return 1; // Just for mob events 
}

function setup() {
	em.setProperty("started", "true"); 
	
	var eim = em.newInstance("SuperTransform");
	
	var map = em.getChannelServer().getMap(912010000);
	//map.resetFully();
	map.respawn();
	eim.startEventTimer(2 * 60 * 1000);
	eim.schedule("scheduledTimeout", 2 * 60 * 1000);
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapFactory().getMap(912010000);
    player.changeMap(map, map.getPortal(0));
	player.dropMessage(6, "You must endure Kyrin's attacks for more than 2 minutes!");
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
}

function scheduledTimeout(eim) {
	eim.disposeIfPlayerBelow(100, 912010200);
	em.setProperty("started", "false");
}

function playerDisconnected(eim, player) {
	return 0;
}

function leftParty(eim, player) {
	playerExit(eim, player);
}

function disbandParty(eim) {
	eim.disposeIfPlayerBelow(100, 120000101);
	em.setProperty("started", "false");
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	var map = eim.getMapFactory(120000101);
	player.changeMap(map, map.getPortal(0));
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	var map = eim.getMapFactory().getMap(120000101);
	player.changeMap(map, map.getPortal(0));
}

function clearPQ(eim) {
	eim.disposeIfPlayerBelow(100, 120000101);
	em.setProperty("started", "false");
}

function cancelSchedule() {
}