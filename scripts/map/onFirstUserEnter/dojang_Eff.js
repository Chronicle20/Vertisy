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
 *@Author:     Moogra, Traitor
 *@Map(s):     All Dojo fighting maps
 *@Function:   Spawns dojo monsters and handles time
*/
var bosses = [9420513, 9400549, 9400596, 9400597, 9400594, 9500361, 9400635, 9400637, 9300215, 9400014, 9400121, 9300158, 9300159, 9600002];

function start(ms) {
    ms.getPlayer().resetEnteredScript();
    var stage = (ms.getPlayer().getMap().getId() / 100) % 100;
    if (stage % 6 == 1)
        ms.getPlayer().setDojoStart();
    if (ms.getPlayer().getMap().getCharacters().size() == 1)
        ms.getPlayer().showDojoClock();
    if (stage % 6 > 0) {
        var realstage = stage - ((stage / 6) | 0);
        ms.dojoEnergy();
        //var mob = ms.getMonsterLifeFactory(9300183 + realstage);
		var mob = ms.getMonsterLifeFactory(bosses[realstage - 1]);
        if (mob != null && ms.getPlayer().getMap().getMonsterById(bosses[realstage - 1]) == null && ms.getPlayer().getMap().getMonsterById(9300216) == null) {
            mob.setBoss(false);
            ms.getPlayer().getMap().spawnDojoMonster(mob);
            ms.playSound("Dojang/start");
            ms.showEffect("dojang/start/stage");
            ms.showEffect("dojang/start/number/" + realstage);
        }
    }
}