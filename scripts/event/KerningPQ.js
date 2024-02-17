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

/* npc
 * 9020000 - Lakelis
 * 9020001 - Cloto
 * 9020002 - Nella
 */

/* map
 * 103000000 - kerning city
 * 103000800 - 103000804 : 1st - 5th stage
 */

/*
 INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300001,4001007,5);
 INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300000,4001008,1);
 INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300002,4001008,1);
 INSERT monsterdrops (monsterid,itemid,chance) VALUES (9300003,4001008,1);
 */

var exitMap;
var minPlayers = 4;

function init() { // Initial loading.
    exitMap = em.getChannelServer().getMap(103000890);
    em.setProperty("KPQOpen", "true"); // allows entrance.
    em.setProperty("shuffleReactors", "true");
//    instanceId = 1; //dont think this is used
}

function monsterValue(eim, mobId) { // Killed monster.
    return 1; // returns an amount to add onto kill count.
}

function setup() { // Invoked from "EventManager.startInstance()"
    var eim = em.newInstance("KerningPQ"); // adds a new instance and returns EventInstanceManager.
    var eventTime = 30 * (1000 * 60); // 30 mins.
    var firstPortal = eim.getMapInstance(103000800).getPortal("next00");
    respawn(eim);
    firstPortal.setScriptName("kpq0");
    eim.scheduleEventTimer(eventTime);
    eim.setClock(30 * 60);

    var KPQ = Java.type("server.partyquest.KPQ");
    var PartyQuest = Java.type("server.partyquest.PartyQuest");
    PartyQuest.create(KPQ.class, em.getParty("KPQ"), eim.getTimeStarted());

    return eim; // returns the new instance.
}

function playerEntry(eim, player) { // this gets looped for every player in the party.
    var map = eim.getMapInstance(103000800);
    player.changeMap(map, map.getPortal(0)); // We're now in KPQ :D
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    var party = eim.getPlayers();
    if (eim.isLeader(player) || party.size() <= minPlayers) { // Check for party leader
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
    } else
        playerExit(eim, player);
}

function respawn(eim) {
    var map = eim.getMapInstance(103000800);
    var map2 = eim.getMapInstance(103000805);
    if (map.getSummonState()) { //Map spawns are set to true by default
        map.instanceMapRespawn();
    }
    if (map2.getSummonState()) {
        map2.instanceMapRespawn();
    }
    eim.schedule("respawn", 10000); //10 seconds
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (eim.isLeader(player) || party.size() < minPlayers) {
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
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
    } else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
}

function playerExit(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.changeMap(exitMap, exitMap.getPortal(0));
    
}

function removePlayer(eim, player) {
    player.endPQ(false, eim.getDuration());
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function clearPQ(eim) { // dont think this is used.
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++)
        playerExit(eim, party.get(i)); // dont worry endPQ(true) is done in 9020001 clear()
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

function changedMap(eim, chr, mapId) {
    for (var i = 0; i <= 5; i++) {
        if (mapId === 103000800 + i) { //do nothing if its PQ map
            return;
        }
    }
    //if its not pq map, end the pq with failure, unregister player from eim and dispose the eim if no one is in it.
    if(!chr.endPQ(false, eim.getDuration())){
    	eim.unregisterPlayer(chr, true);
    }
}