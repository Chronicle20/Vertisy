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
/* Portal for the LightBulb Map...

**hontale_c.js
@author Jvlaple
*/
function enter(pi) {

    if (pi.isLeader() == true) {
        var eim = pi.getPlayer().getEventInstance();
        var party = eim.getPlayers();
        
        
        var reactor = pi.getPlayer().getMap().getReactorById(2408001);
        if(reactor.getState() != 0) {
            var toMap = 240050300;
            if(reactor.getLastHitFrom() == 1)
                toMap = 240050310;
            
            var target = eim.getMapInstance(toMap);
            var targetPortal = target.getPortal("sp");
            //Warp the full party into the map...
            var partyy = pi.getPlayer().getEventInstance().getPlayers();
            for (var i = 0; i < partyy.size(); i++) {
                party.get(i).changeMap(target, targetPortal);
            }
            return true;
        } else {
            pi.playerMessage(6, "Hit the orb to choose your path.");
            return false;
        }
    } else {
        pi.playerMessage(6, "You are not the party leader. Only the party leader may proceed through this portal.");
        return false;
    }
}