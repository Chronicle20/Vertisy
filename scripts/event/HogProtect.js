/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 * @author AngelSL
 * 
 * 4th Job Rush Quest.
 * Based on Kerning City PQ script by Stereo
 */

var exitMap;
var mainMap;

function init() {
	em.setProperty("Open", "true"); // allows entrance.
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(player) {
    exitMap = em.getChannelServer().getMap(230000003); // <exit>
	mainMap = em.getChannelServer().getMap(923010000); //
	mainMap.clearAndReset(true);
    var eim = em.newInstance("HogProtect-" + em.getChannelServer().getId(), false);
    eim.startEventTimer(1000 * 60 * 10);//10 minutes
    em.schedule("timeOut", eim, 1000 * 60 * 10);
    return eim;
}

function playerEntry(eim, player) {
    player.changeMap(mainMap, mainMap.getPortal(0));
}

function playerDead(eim, player) {}

function playerRevive(eim, player) {
	playerExit(eim, player);
    eim.dispose();
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
    eim.dispose();
}

function leftParty(eim, player){}

function disbandParty(eim){}

function playerExit(eim, player) {
	player.removeClock();
    eim.unregisterPlayer(player);
    if(player.getMapId() != 230000003)player.changeMap(exitMap, exitMap.getPortal(0));
	if(eim.getPlayerCount() <= 0){
		eim.dispose();
	}
}

//for offline players
function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
	var pIter = eim.getPlayers().iterator();
	while (pIter.hasNext()) {
		playerExit(eim, pIter.next());
	}
	eim.dispose();
}

function clearPQ(eim) {
	var pIter = eim.getPlayers().iterator();
	while (pIter.hasNext()) {
		playerExit(eim, pIter.next());
	}
    eim.dispose();
}

function changedMap(eim, player, mapid){
	if(mapid == 230000003){
		player.removeClock();
		//if(player.getMapId() != 230000003)player.changeMap(exitMap);
		if(player.getItemQuantity(4031508, false) < 5 || player.getItemQuantity(4031507, false) < 5){
			player.removeAll(4031508);
			player.removeAll(4031507);
		}
		playerExit(eim, player);
		eim.dispose();
	}
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose() {
    em.cancelSchedule();
}

function timeOut(eim) {
	if (eim.getPlayerCount() > 0) {
		var pIter = eim.getPlayers().iterator();
		while (pIter.hasNext()) {
			playerExit(eim, pIter.next());
		}
	}
	eim.dispose();
}
