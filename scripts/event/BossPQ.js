
var hard = 0;
var hardbosses = [9420513, 9400549, 9400596, 9400597, 9400594, 9500361, 9400635, 9400637, 9300215, 9400014, 9400121, 9300158, 9300159, 9600002];

function init(){
	em.setProperty("Open", "true"); // allows entrance.
}

function setup(idfk){
	hard = parseInt(em.getProperty("hard"));
	var eim = em.newInstance("BossPQ_" + hard + "_" + em.getChannelServer().getId(), false);
	eim.setProperty("stage", "0");
	eim.setProperty("hard", "" + hard);
	var startmapid = 970030000 + hard;
	for(var i = 1; i < (hard == 1 ? 15 : 22); i++){
		var map = eim.getMapInstance(startmapid + (i * 100));
		map.clearAndReset(false);
		if(hard == 0){
			map.customMapRespawn();
		}else{
			var mob = hardbosses[i - 1];
			map.spawnMonsterOnGroundBelow(mob, map.getPointOfCustomFirstMob());
		}
	}
    var timer = 1000 * 60 * (hard == 1 ? 30 : 100);
    em.schedule("timeOut", eim, timer);
    eim.startEventTimer(timer);
    return eim;
}


function playerEntry(eim, player) {
	player.removeClock();
    var map = eim.getMapInstance(970030100 + hard);
    player.changeMap(map, map.getPortal(0));
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    playerExit(eim, player);
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
            if (partyz.size() > 1) {
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
        if (partyz.size() < 1) {
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
    if (party.size() < 1) {
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
    player.changeMap(970030000);
    player.removeClock();
    if (eim.getPlayers().size() <= 0) {
    	eim.dispose();
    }
}

function changedMap(eim, player, mapid){}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim){}

function dispose() {
    em.cancelSchedule();
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