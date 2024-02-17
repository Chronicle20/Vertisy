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
/* Changes the players name.
	Can only be accessed with the item 2430026.
 */

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
        	var error = cm.getClient().canChangeName();
			if(error == 0) {
				cm.sendYesNo("You seem to qualify for a name change, are you sure you want to change your name? You can only do it once every 30 days on the same account.", 1);
			} else{
				if(error == 1){
					cm.sendOk("You need to have at least 5 days of Elite to change your name.");
				}else if(error == 2){
					cm.sendOk("You can only change your name on an account once every 30 days.");
				}
				cm.dispose();
			}
		} else if(status == 1) {
			cm.sendGetText("Please input your desired name below.\r\nPlease Note: Once you select a valid name it will take effect immeadiately.\r\n#eYou will not be able to go back to this page to select another name.#k");
		} else if(status == 2) {
			var text = cm.getText();
			var canCreate = Packages.client.MapleCharacter.canCreateChar(text);
			if(canCreate) {
				cm.getPlayer().changeName(text);
				cm.getClient().changedName();
				cm.sendOk("Your name has been changed to #b" + text + "#k. You will now be logged out for this to take effect.", 1);
			} else {
				cm.sendNext("I'm afraid you can't use the name #b" + text + "#k or it is already taken.", 1);
				cm.dispose();
			}
		} else if(status == 3) {
			cm.dispose();
			cm.getClient().disconnect(false, false);
		}
    }
}