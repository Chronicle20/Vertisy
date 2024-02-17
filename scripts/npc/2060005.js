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

function start() {
    if (cm.haveItem(4031508, 5) && cm.haveItem(4031507, 5)) {
		cm.getPlayer().changeSkillLevel(Packages.client.SkillFactory.getSkill(20001004), 1, 1, -1);
        cm.gainItem(1912011, 1);
		cm.gainItem(4031508, -5);
		cm.gainItem(4031507, -5);
		cm.sendOk("Thank you for protecting the Hog! You can ride your mount now.");
	}else{
		if(cm.isQuestStarted(6002)){
			cm.removeAll(4031508);
			cm.removeAll(4031507);
			var em = cm.getEventManager("HogProtect");
			var currentEIM = em.getInstance("HogProtect-" + cm.getClient().getChannel());
			if(currentEIM == null){
				em.startInstance(cm.getPlayer());
			}else{
				cm.sendOk("There is currently someone in this map, come back later.");
			}
		}else{
			cm.sendOk("Can I help you?");
		}
	}
	cm.dispose();
}