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
/* The five caves
 * @author Twdtwd
 */
function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    if(pi.getPlayer().getMapId() >= 240050101 && pi.getPlayer().getMapId() <= 240050105) {
        var mapID = pi.getPlayer().getMapId();
        var stage = mapID - 240050099;
		pi.getPlayer().dropMessage(6, stage + ": " + eim.getProperty("maze" + stage));
		if(eim.getProperty("maze" + stage) == "1"){
			if (pi.getPlayer().getMapId() == 240050105) {
				pi.warp(240050100, "st00");
				return true;
			}else{
				pi.warp(mapID + 1, "sp");
				return true;
			}
		}else{
			pi.getPlayer().dropMessage(6, "This door is closed.");
            return false;
		}
	}
	
	/*if(pi.getPlayer().getMapId() >= 240050101 && pi.getPlayer().getMapId() <= 240050104) {
        var mapID = pi.getPlayer().getMapId();
        var stage = mapID - 240050099;
        var avail = eim.getProperty(stage + "stageclear");
        if (avail == null) {
            pi.getPlayer().dropMessage(6, "This door is closed.");
            return false;
        }else {
            pi.warp(mapID + 1, "sp");
            return true;
        }
    } else if (pi.getPlayer().getMapId() == 240050105) {
        var avail = eim.getProperty("6stageclear");
        if (pi.haveItem(4001092, 1) && pi.isLeader()) {
            pi.gainItem(4001092, -1);
            eim.broadcastPlayerMsg(6, "The six keys break the seal for a flash...");
            eim.setProperty("6stageclear", "true");
            pi.warp(240050100, "st00");
            return true;
        } else if(avail != null) {
            pi.warp(240050100, "st00");
            return true;
        } else {
            pi.getPlayer().dropMessage(6, "Horntail\'s Seal is blocking this door.");
            return false;
        }
    }*/
    return true;
}
