
var exitMap = 300030100;

var minPlayers = 1;//4

var firstMap;
var secondMap;

function init() {
	em.setProperty("state", "0");
}

function setup(roomID) {
	em.setProperty("state", "1");
	
	var eim = em.newInstance("EF", false);
	firstMap = em.getChannelServer().getMap(930000100);
	firstMap.clearAndReset(true);
	secondMap = em.getChannelServer().getMap(930000200);
	secondMap.clearAndReset(true);
	
	eim.sendClock(30 * 60);//30 minutes
	return eim;
}

function playerEntry(eim, player) {
	player.changeMap(firstMap);
}

function finish(eim){
	var players = eim.getPlayers();
    for (var i = 0; i < players.size(); i++){
		var player = players.get(i);
		if(player.getMapId() != exitMap){
			player.changeMap(exitMap);
		}
	}
	eim.dispose();
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	if(player.getMapId() != 300030100){
		var map = em.getChannelServer().getMap(exitMap);
		player.changeMap(map, map.getPortal(0));
	}
	if(eim.getPlayerCount() < minPlayers){
		finish(eim);
	}
}

function playerDead(eim, player) {
	playerRevive(eim, player);//?
}

function playerRevive(eim, player) {
	//handle
	return true;
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
	var players = eim.getPlayers();
    for (var i = 0; i < players.size(); i++)
        playerExit(eim, players.get(i));
}

function removePlayer(eim, player) {
	playerExit(eim, player);
}

function playerDisconnected(eim, player){
	playerExit(eim, player);
}

function changedMap(eim, player, mapid){
	//if(mapid != (mapId + parseInt(eim.getProperty("roomID")))){
	//	playerExit(eim, player);
	//}
}


function monsterValue(eim, mobid){
	return 0;
}
function allMonstersDead(eim) {}
function cancelSchedule() {}