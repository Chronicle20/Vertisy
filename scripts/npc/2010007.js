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
/* guild creation npc */
var status = 0;
var sel;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		var text = "";
		
		if(cm.getPlayer().getGuildId() <= 0){
			text += "Hello #e#b#h ##k#n. What would you like to do?";
			text += "\r\n#L0#Create a Guild#l";
		}else{		
			text += "Hello #e#b#h ##k#n. What would you like to do?";
			if(cm.getPlayer().getGuildRank() == 1){
				text += "\r\n#L1#Disband your Guild.#l";
				text += "\r\n#L2#Increase your Guild's capacity.#l";
			}
			text += "\r\n#L3#Contribute to your Guild funds.#l";
			text += "\r\n#L4#Check current Guild funds.#l";
		}
		cm.sendSimple(text);
	}else if (status == 1) {
		sel = selection;
		if (selection == 0) {
			if (cm.getPlayer().getGuildId() > 0) {
				cm.sendOk("You may not create a new Guild while you are in one.");
				cm.dispose();
			} else
				cm.sendYesNo("Creating a Guild costs #b 1500000 mesos#k, are you sure you want to continue?");
		} else if (selection == 1) {
			if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
				cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");
				cm.dispose();
			} else
				cm.sendYesNo("Are you sure you want to disband your Guild? You will not be able to recover it afterward and all your GP will be gone.");
		} else if (selection == 2) {
			if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
				cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
				cm.dispose();
			} else
				cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b " + cm.getPlayer().getGuild().getIncreaseGuildCost(cm.getPlayer().getGuild().getCapacity()) +" mesos#k, are you sure you want to continue?");
		}else if(selection == 3){
			if (cm.getPlayer().getGuildId() < 1) {
				cm.sendOk("You can only help fund a Guild when in one.");
				cm.dispose();
			}else{
				cm.sendGetNumber("How much meso would you like to contribute towards the Guild?", 100, 1, 2147483647);
			}
		}else if(selection == 4){
			if (cm.getPlayer().getGuildId() < 1) {
				cm.sendOk("You can only check your Guilds funds while in a guild.");
				cm.dispose();
			}else{
				var guild = cm.getPlayer().getGuild();
				cm.sendOk("#e#b" + guild.getName() + "#k#n funds.\r\n#FItem/Special/0900.img/09000003/iconRaw/0# - #b" + cm.getFormattedInt(guild.getMeso()) + "#k\r\n #v4001254# - #b" + cm.getFormattedInt(guild.getCoins()) + "#k ");
				cm.dispose();
			}
		}
	} else if (status == 2) {
		if (sel == 0 && cm.getPlayer().getGuildId() <= 0) {
			cm.getPlayer().genericGuildMessage(1);
			cm.dispose();
		} else if (cm.getPlayer().getGuildId() > 0) {
			if(cm.getPlayer().getGuildRank() == 1){
				if (sel == 1) {
					cm.getPlayer().disbandGuild();
					cm.dispose();
				} else if (sel == 2) {
					cm.getPlayer().increaseGuildCapacity();
					cm.dispose();
				}
			}
			if(sel == 3){
				var guild = cm.getPlayer().getGuild();
				if(selection > 0 && selection <= 2147483647){
					if(cm.getPlayer().getMeso() >= selection){
						cm.gainMeso(-selection);
						cm.addGuildMeso(selection);
						guild.addMeso(selection);
						cm.sendOk("Your guild currently has #b" + cm.getFormattedInt(guild.getMeso()) + "#k meso stored.");
						cm.dispose();
					}else{
						cm.sendOk("You do not have enough meso to contribute that much.");
						cm.dispose();
					}
				}else{
					cm.sendOk("Invalid meso input.");
					cm.dispose();
				}
			}
		}
	}
}
