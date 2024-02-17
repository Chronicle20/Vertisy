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
 */

/*
 * @Author Raz
 * @modified peter
 * 
 * Ludi PQ
 */
var exitMap;
var finishMap;
var bonusMap;
var bonusTime = 60;//1 Minute
var pqTime = 3600;//60 Minutes

function init() {
    exitMap = em.getChannelServer().getMap(922010000);//Exit
    em.setProperty("LPQOpen", "true"); // allows entrance.
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup() {
    var eim = em.newInstance("LudiPQ");
    var eventTime = pqTime * 1000; // 60 mins.

    em.schedule("timeOut", eim, eventTime); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(eventTime); // Sends a clock packet and tags a timer to the players.
    //eim.schedule("respawn", 5000);

    for (var i = 100; i <= 800; i += 100) {
        eim.getMapInstance(922010000 + i).getPortal("next00").setScriptName("lpq" + (i / 100));
    }
    return eim;
}

function playerEntry(eim, player) {
    var map0 = eim.getMapInstance(922010100);
    player.changeMap(map0, map0.getPortal(0));
}

function playerDead(eim, player) {
    if (player.isAlive()) { //don't trigger on death, trigger on manual revive
        if (eim.isLeader(player)) {
            var party = eim.getPlayers();
            for (var i = 0; i < party.size(); i++)
                playerExit(eim, party.get(i));
        } else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
    } else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExit(eim, party.get(i));
}


function playerExit(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerFinish(eim, player) {
    player.endPQ(true, eim.getDuration());
    var map = eim.getMapInstance(922011100);
    player.changeMap(map, map.getPortal(0));
}

//for offline players
function removePlayer(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerFinish(eim, party.get(i));
}

function changedMap(eim, chr, mapid) {
    if(mapid >= 922010000 && mapid <= 922011100){
    	return true;
    }
    //if its not pq map, end the pq with failure, unregister player from eim and dispose the eim if no one is in it.
    chr.endPQ(false, eim.getDuration());
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function timeOut(eim) {
    if (eim !== null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext())
                playerExit(eim, pIter.next());
        }
    }
}

function finishBonus(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        if (party.get(i).getMap().getId() === 922011000)
            playerFinish(eim, party.get(i));
}

function startBonus(eim) {
    var bonusMap = eim.getMapInstance(922011000);
    var party = eim.getPlayers();

    em.schedule("finishBonus", eim, 60000); // invokes "timeOut" in how ever many seconds.
    eim.startEventTimer(60000);

    for (var i = 0; i < party.size(); i++) {
        if (party.get(i).getMap().getId() === 922010900) {
            party.get(i).changeMap(bonusMap, bonusMap.getPortal(0));
        }
    }

}