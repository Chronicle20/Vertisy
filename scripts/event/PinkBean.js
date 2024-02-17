/**
 *	@Name: Pink Bean Expedition
 *	@Author: iPoopMagic (David)
 */
var battleMap;
var exitMap;
var minPlayers = 1;

function init() {
    em.setProperty("state", "0");
}

function setup() {
    em.setProperty("state", "1");
	em.setProperty("kirston", "false");
    var eim = em.newInstance("PinkBean_" + em.getProperty("channel"));
	battleMap = em.getChannelServer().getMap(270050100);
	battleMap.clearAndReset(true);
	battleMap.setPBStatus(0);
    exitMap = eim.getMap(270050300);
	em.schedule("timeOut", 60 * 60 * 1000); // 1 hour
	eim.startEventTimer(60 * 60 * 1000);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(battleMap, battleMap.getPortal(0));
}

function playerRevive(eim, player) {
	player.changeMap(exitMap, exitMap.getPortal(0));
    return false;
}

function timeOut(eim) {
	if (eim != null) {
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()){
				var player = pIter.next();
				player.dropMessage(6, "You have run out of time to defeat Pink Bean!");
				playerExit(eim, player);
			}
		}
		em.setProperty("state", "0");
		eim.dispose();
	}
}

function playerDisconnected(eim, player) {
	var exped = eim.getPlayers();
	if (player.getName().equals(eim.getProperty("leader"))) {
		var iter = exped.iterator();
		while (iter.hasNext()) {
			iter.next().getPlayer().dropMessage(6, "The leader of the expedition has disconnected.");
		}
	}
	//If the expedition is too small.
	if (exped.size() < minPlayers) {
		end(eim,"There are not enough players remaining. The Battle is over.");
	}
}

function monsterValue(eim, mobId) {
    return -1;
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
	if (eim.getPlayers().size() < minPlayers) {
		end(eim, "There are no longer enough players to continue, and those remaining shall be warped out.");
	}
}

function end(eim, msg) {
	var iter = eim.getPlayers().iterator();
	while (iter.hasNext()) {
		var player = iter.next();
		player.getPlayer().dropMessage(6, msg);
		eim.unregisterPlayer(player);
		if (player != null){
			player.changeMap(exitMap, exitMap.getPortal(0));
		}
	}
	em.setProperty("state", "0");
	eim.dispose();
}

function clearPQ(eim) {
    end(eim, "");
}

function allMonstersDead(eim) {
}

function leftParty (eim, player) {}
function disbandParty (eim) {}
function playerDead(eim, player) {}
function cancelSchedule() {}