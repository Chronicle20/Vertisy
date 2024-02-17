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
 * @Author: iPoopMagic (David)
 * @Description: Engine Room Exit (Bob)
 */
function start() {
	/*var timeData = cm.getPlayer().getProgressValue("gs7-time");
	var customData = cm.getPlayer().getProgressValue("gs7-kills");
	if (timeData == "") {
		timeData = "0";
	}
	var time = parseInt(timeData);
	if (customData == "") {
		customData = "0";
	}
	var amtFought = parseInt(customData);
	if (amtFought < 1 || !cm.getPlayer().isProgressValueSet("gs7-time")) {
		if(cm.getPlayer().isProgressValueSet("gs7-time")){
			cm.getPlayer().setProgressValue("gs7-time", cm.getCurrentTime());
		}else cm.getPlayer().addProgressValue("gs7-time", cm.getCurrentTime());
	}
	if(cm.getPlayer().isProgressValueSet("gs7-kills")){
		cm.getPlayer().setProgressValue("gs7-kills", amtFought + 1);
	}else cm.getPlayer().addProgressValue("gs7-kills", amtFought + 1);*/
	//cm.getPlayer().setProgressValue("gs7-kills", cm.convertToString(amtFought + 1));
    cm.warp(541010110);
    cm.dispose();
}