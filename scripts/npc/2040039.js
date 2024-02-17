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
@	Author : Raz
@	Modified: iPoopMagic (David)
@	NPC = Lime Balloon
@	Map = Hidden-Street <Stage 4>
@	NPC MapId = 922010400
@	Function = LPQ - 4th Stage
@
*/

var status = 0;
var party;
var preamble;
var gaveItems;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) cm.dispose();
    else {
        (mode == 1 ? status++ : status --);
        var eim = cm.getPlayer().getEventInstance();
        var nthtext = "4th";
        if (status == 0) {
            party = eim.getPlayers();
            preamble = eim.getProperty("leader" + nthtext + "preamble");
            gaveItems = eim.getProperty("leader" + nthtext + "gaveItems");
            if (preamble == null) {
                cm.sendNext("Hi. Welcome to the " + nthtext + " stage. You need to collect 25 #b#t4001022#'s#k.");
                eim.setProperty("leader" + nthtext + "preamble","done");
                cm.dispose();
            } else {
                if (!isLeader()) {
                    if (gaveItems == null) {
                        cm.sendOk("Please tell your #bParty Leader#k to come talk to me");
                        cm.dispose();
                    } else {
                        cm.sendOk("Hurry, go to the next stage, the portal is open!");
                        cm.dispose();
                    }
                } else {
                    if (gaveItems == null) {
						if (cm.getPlayer().getMap().getCharacters().size() != eim.getPlayers().size()) {
							cm.sendOk("Please wait for all of your party members to get here.");
							cm.dispose();
                        } else if (cm.itemQuantity(4001022) >= 25) {
                            cm.sendOk("Good job! You have collected all 25 #b#t4001022#'s#k.");
                        } else {
                            cm.sendOk("Sorry, you have not collected all 25 #b#t4001022#'s#k.");
                            cm.dispose();
                        }
                    } else {
                        cm.sendOk("Hurry, go to the next stage, the portal is open!");
                        cm.dispose();
                    }
                }
			}
		} else if (status == 1){
            cm.sendOk("You may continue to the next stage!");
            
			var map = eim.getMapInstance(cm.getPlayer().getMapId());
			map.broadcastMessage(Packages.tools.MaplePacketCreator.showEffect("quest/party/clear"));
			map.broadcastMessage(Packages.tools.MaplePacketCreator.playSound("Party1/Clear"));
			map.broadcastMessage(Packages.tools.MaplePacketCreator.environmentChange("gate", 2));
	
            cm.removeAll(4001022);
            cm.givePartyQuestExp("LudiPQ4th");
            eim.setProperty("4stageclear","true");
            eim.setProperty("leader" + nthtext + "gaveItems","done");
            cm.dispose();
        }
    }
}


function isLeader(){
    if(cm.getParty() == null){
        return false;
    } else {
        return cm.isLeader();
    }
}
