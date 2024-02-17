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
 * @author DiscoveryMS (Twdtwd)
 * @purpose Warps to the Forgotten Passage and gives you the needed item for the resurection.
 */
function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    if(eim != null) {
        if(true || eim.getProperty("complete") != null) {
            pi.getPlayer().dropMessage(pi.getPlayer().getQuestInfo(6330));
            pi.getPlayer().updateQuestInfo(6330, "2");
            eim.removePlayer(pi.getPlayer());
            return true;
        } else {
            pi.getPlayer().dropMessage(5, "The portal is currently blocked.");
        }
    }
    
    return false;
}