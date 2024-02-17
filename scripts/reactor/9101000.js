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
/**
 * @author BubblesDev, peter
 * @purpose
 * [x]Full Moon,
 * [/]Summons Moon Bunny,
 * [x]shows animation,
 * [/]makes stirges and stuff appear
 */

function act() {
//    var map = rm.getPartyLeaderChar().getEventInstance().getMapInstance(rm.getReactor().getMapId());
    var map = rm.getPartyLeaderChar().getMap(); //this should do.
    if (map.getSummonState()) { //it means moon bunny is already summoned :p
        return;
    }

    var eim = rm.getEventManager("HenesysPQ").getInstance("HenesysPQ");
    if (eim !== null && rm.getReactor().getCurrState() === 6) {
        map.startMapEffect(
                "Protect the Moon Bunny that's pounding the mill, and gather up 10 Moon Bunny's Rice Cakes!",
                5120016, 10000);
        rm.mapMessage(6, "Protect the Moon Bunny!!!");
        map.allowSummonState(true);
        eim.schedule("respawn", 0);
        map.spawnMonsterOnGroundBelow(9300061, -190, -190);
    }
}