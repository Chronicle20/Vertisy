/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
	THIS  FILE WAS MADE BY JVLAPLE. REMOVING THIS NOTICE MEANS YOU CAN'T USE THIS SCRIPT OR ANY OTHER SCRIPT PROVIDED BY JVLAPLE.
 */

/**
 * @Author Jvlaple
 * @Modified iPoopMagic (David)
 * Orbis Party Quest
 */

// Define Maps
var exitMap;
var centerMap;
var walkwayMap;
var storageMap;
var lobbyMap;
var sealedRoomMap;
var loungeMap;
var onTheWayUpMap;
var bossMap;
var jailMap;
var roomOfDarknessMap;
var bonusMap;
var endMap;
var stg6_combo = Array("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16");
var cx = Array(200, -300, -300, -300, 200, 200, 200, -300, -300, 200, 200, -300, -300, 200); //even = 200 odd = -300
var cy = Array(-2321, -2114, -2910, -2510, -1526, -2716, -717, -1310, -3357, -1912, -1122, -1736, -915, -3116);

var minPlayers = 6;

function init() {
	em.setProperty("state", "0");
}

function monsterValue(eim, mobId) {
    if (mobId == 9300040) {
	var st = parseInt(em.getProperty("stage2"));
		if (st < 14) {
			 eim.broadcastPlayerMsg(6, "Cellion has been spawned somewhere in the map.");
			 var mob = em.getMonster(9300040);
			 em.setProperty("stage2", st + 1);
			 eim.registerMonster(mob);
			 eim.getMapInstance(3).spawnMonsterOnGroundBelow(mob, new java.awt.Point(cx[st], cy[st]));
		}
    }
    return 1;
}

function setup() {
	em.setProperty("state", "1");
    exitMap = em.getChannelServer().getMap(920011200);
	var eim = em.newInstance("OrbisPQ_" + em.getProperty("channel"));
    entranceMap = eim.getMapInstance(920010000);
    //Define all Maps and PortalScripts
    centerMap = eim.getMapInstance(920010100);
    walkwayMap = eim.getMapInstance(920010200);
    storageMap = eim.getMapInstance(920010300);
    lobbyMap = eim.getMapInstance(920010400);
    sealedRoomMap = eim.getMapInstance(920010500);
    loungeMap = eim.getMapInstance(920010600);
    onTheWayUpMap = eim.getMapInstance(920010700);
    bossMap = eim.getMapInstance(920010800);
    jailMap = eim.getMapInstance(920010900);
    roomOfDarknessMap = eim.getMapInstance(920011000);
    bonusMap = eim.getMapInstance(920011100);
    endMap = eim.getMapInstance(920011300);
    em.setProperty("killedCellions", "0");
    em.setProperty("papaSpawned", "no");
	em.setProperty("pre", "0");
	em.setProperty("stage1", "0"); // Walkway - (1st Small Pieces)
	em.setProperty("stage2", "0"); // Storage - (2nd Small Pieces)
	em.setProperty("stage3", "0"); // Lobby - CDs (3rd Piece)
	em.setProperty("stage4", "0"); // Sealed Room
	em.setProperty("stage5", "0"); // Lounge (5th Small Pieces)
	em.setProperty("stage6", "0"); // On The Way Up
	for (var b = 0; b < stg6_combo.length; b++) { //stage6_001
		for (var y = 0; y < 4; y++) { // Stage Number
			em.setProperty("stage6_" + stg6_combo[b] + "" + (y + 1) + "", "0");
		}
	}
	for (var b = 0; b < stg6_combo.length; b++) { //stage6_001	
		var found = false;
		while (!found) {
			for (var x = 0; x < 4; x++) {
				if (!found) {
					var founded = false;
					for (var z = 0; z < 4; z++) { //check if any other stages have this value set already.
						if (em.getProperty("stage6_" + stg6_combo[b] + "" + (z + 1) + "") == null) {
							em.setProperty("stage6_" + stg6_combo[b] + "" + (z + 1) + "", "0");
						} else if (em.getProperty("stage6_" + stg6_combo[b] + "" + (z + 1) + "").equals("1")) {
							founded = true;
							break;
						}
					}
					if (!founded && java.lang.Math.random() < 0.25) {
						em.setProperty("stage6_" + stg6_combo[b] + "" + (x + 1) + "", "1");
						found = true;
						break;
					}
				}
			}
		}
	}
	//STILL not done yet! levers = 2 of them
	for (var i = 0; i < 3; i++) {
		em.setProperty("stage62_" + i + "", "0");
	}
	var found_1 = false;
	while(!found_1) {
		for (var i = 0; i < 3; i++) {
			if (em.getProperty("stage62_" + i + "") == null) {
				em.setProperty("stage62_" + i + "", "0");
			} else if (!found_1 && java.lang.Math.random() < 0.2) {
				em.setProperty("stage62_" + i + "", "1");
				found_1 = true;
			}
		}
	}
	var found_2 = false;
	while(!found_2) {
		for (var i = 0; i < 3; i++) {
			if (em.getProperty("stage62_" + i + "") == null) {
				em.setProperty("stage62_" + i + "", "0");
			} else if (!em.getProperty("stage62_" + i + "").equals("1") && !found_2 && java.lang.Math.random() < 0.2) {
				em.setProperty("stage62_" + i + "", "1");
				found_2 = true;
			}
		}
	}
//	em.setProperty("stage6levers", "0"); // On The Way Up (levers)
	em.setProperty("stage", "0");
	em.setProperty("stageS", "0"); // Statue
	em.setProperty("finished", "0");
	// Properties like there's no tomorrow
	eim.schedule("respawn", 1500);
    eim.schedule("timeOut", 60 * 60 * 1000); // 1 hour
	eim.startEventTimer(60 * 60 * 1000); // 1 hour
    eim.setProperty("entryTimestamp", System.currentTimeMillis() + (60 * 60000));
	
    return eim;
}

function respawn(eim) {
	var leader = eim.getPlayers().get(0);
	if (leader.getMapId() == 920010200 || leader.getMapId() == 920010300)
		leader.getMap().instanceMapRespawn();
	
	eim.schedule("respawn", 10 * 1000);
}

function playerEntry(eim, player) {
    player.changeMap(entranceMap, entranceMap.getPortal(0));
	Packages.scripting.npc.NPCScriptManager.getInstance().dispose(player.getClient());
	Packages.scripting.npc.NPCScriptManager.getInstance().start(player.getClient(), 2013001, "2013001", null);
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i));
        }
        eim.dispose();
    }
    else { //boot dead player
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() <= minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //PWN THE PARTY (KICK OUT)
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            }
            else {
                playerExit(eim, party.get(i));
            }
        }
        eim.dispose();
    }
    else {
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() < minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function leftParty(eim, player) {			
    // If only 5 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() <= minPlayers) {
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim,party.get(i));
        }
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function changedMap(eim, player, mapid) {
    if (mapid < 920010000 || mapid > 920011300) {
		playerExit(eim, player);
	} else if (eim.getPlayerCount() < 1) {
		eim.dispose();
    }
}

function playerExit(eim, player) {
	em.setProperty("state", "0");
    eim.unregisterPlayer(player);
    player.cancelAllBuffs(false); //We don't want people going out with wonky blessing >=(
    player.changeMap(exitMap, exitMap.getPortal(0));
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    // W00t! Bonus!!
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        player.changeMap(bonusMap, bonusMap.getPortal(0));
        eim.setProperty("entryTimestamp", System.currentTimeMillis() + (1 * 60000));
        player.getClient().announce(Packages.tools.MaplePacketCreator.getClock(60));
    }
    eim.schedule("finish", 60000)
}

function finish(eim) {
    var iter = eim.getPlayers().iterator();
    while (iter.hasNext()) {
        var player = iter.next();
        eim.unregisterPlayer(player);
        player.changeMap(endMap, endMap.getPortal(0));
    }
    eim.dispose();
}

function allMonstersDead(eim) {
//Open Portal? o.O
}

function cancelSchedule() {
}

function timeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}

function dispose() {

}