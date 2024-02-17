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
 *	@Author: iPoopMagic (David)
 *	@NPC: Crystal of Roots
 */
function start() {
    cm.sendYesNo("Do you wish to leave? You cannot return to this expedition if you do so.");
}

function action(mode, type, selection) {
	var player = cm.getPlayer();
    if (mode < 1)
        cm.dispose();
    else {
		if (player.getEventInstance() != null) {
			player.getEventInstance().removePlayer(player);
		} else {
            cm.warp(240040700);
        }
		if (cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.HORNTAIL) != null) {
			cm.getExpedition(Packages.server.expeditions.MapleExpeditionType.HORNTAIL).removeMember(player);
		}
        if (player.getMap().getCharacters().size() < 2){
            player.getMap().killAllMonsters();
            player.getMap().resetReactors();
			if (player.getEventInstance() != null) {
				player.getEventInstance().dispose();
			}
        }
        cm.dispose();
    }
}