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
 * @author Twdtwd
 * @purpose Warps to Rider's Field for the Aran mount quest.
 */
function enter(pi) {
    if(pi.isQuestStarted(21610) && pi.getPlayer().getQuestInfo(21610) == "") {
        pi.getPlayer().updateQuestInfo(21610, "0");
        pi.getPlayer().dropMessage(5, "The Rider's Field has been cleared. Head to Scadur.");
        return false;
    }
}