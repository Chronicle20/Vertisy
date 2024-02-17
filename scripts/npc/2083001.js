/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*Hornd Tail's Schedule
 * Cave Entrance (240050000)
 */

var status = 0;
var minLevel = 80;
var maxLevel = 255;
var minPlayers = 1;
var maxPlayers = 6;

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
		if(cm.getPlayer().getMapId() == 240050000){
            if (cm.getPlayer().getPartyId() == -1) {
                cm.sendOk("If you want to attempt the quest, try making a party and gathering " + minPlayers + " people...");
                cm.dispose();
                return;
            }
			if (!cm.isLeader()) {
                cm.sendSimple("Get your party leader to talk to me.");
                cm.dispose();
            } else {
                var party = cm.getParty().getMembers();
                var mapId = cm.getPlayer().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                if (party.size() < minPlayers || party.size() > maxPlayers)
                    next = false;
                else {
                    for (var i = 0; i < party.size() && next; i++) {
                        if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                            levelValid += 1;
                        if (party.get(i).getMapId() == mapId)
                            inMap += 1;
                    }
                    if (levelValid < party.size() || inMap < party.size())
                        next = false;
                }
                if (next) {
                    var em = cm.getEventManager("HorntailPQ");
                    if (em == null) {
                        cm.dispose();
                    } else {
						if(em.getProperty("state").equals("0")) {
                            // Begin the PQ.
                            em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                            // Remove pass/coupons
                            party = cm.getPlayer().getEventInstance().getPlayers();
                        } else {
                            cm.sendOk("There is currently another party inside.");
                        }
                    }
                    cm.dispose();
                }else{
                    cm.sendOk("Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. ");
                    cm.dispose();
                }
			}
		}
		
	}
        
		/*if (status == 0) {
            var eim = cm.getPlayer().getEventInstance();
            if(eim != null) {
                // Inside the PQ.
                if(cm.getPlayer().getMapId() == 240050100) {
                    if(cm.isLeader()) {
                        var complete = eim.getProperty("6stageclear");
                        if(complete == null) {
                            
                        } else {
                            var players = eim.getPlayers();
                            var len = players.size();
                            for(var i  = 0; i < len; i++) {
                                var player = players.get(i);
                                player.changeMap(eim.getMapInstance(240050200));
                            }
                        }
                        cm.dispose();
                    } else {
                        cm.dispose();
                    }
                } else { // One of the last two stages.
                    if(cm.isLeader()) {
                        var complete = eim.getProperty("stopRespawn");
                        if(complete == null) {
                            // If they have the 6 keys, stop the mob respawn.
                            if(cm.haveItem(4001093, 6)) {
                                eim.setProperty("stopRespawn", "true");
                                cm.sendOk("You have done well. Please eliminate the remaining monsters to continue.");
                                cm.gainItem(4001093, -6);
                            } else {
                                cm.sendOk("Please eliminate the monsters and bring me 6 #rBlue Keys#k. They will weaken the seal so you may procede.");
                            }
                            cm.dispose();
                        } else {
                            // Check how many mobs are on the map. If there are none, complete the PQ.
                            if(cm.getPlayer().getMap().getMonstersEvent(cm.getPlayer()).size() == 0) {
                                eim.finishPQ();
                            } else {
                                cm.sendOk("There are still some monsters remaining. Please eliminate them.");
                                
                            }
                            cm.dispose();
                        }
                    } else {
                        cm.dispose();
                    }
                }
                return;
            }
            // Slate has no preamble, directly checks if you're in a party
            if (cm.getParty() == null) { // no party
                cm.sendOk("If you want to attempt the quest, try making a party and getting 6 People...");
                cm.dispose();
                return;
            }
            if (!cm.isLeader()) { // not party leader
                cm.sendSimple("You are not the party leader.");
                cm.dispose();
            } else {
                // Check teh partyy
                var party = cm.getParty().getMembers();
                var mapId = cm.getPlayer().getMapId();
                var next = true;
                var levelValid = 0;
                var inMap = 0;
                // Temp removal for testing
                if (party.size() < minPlayers || party.size() > maxPlayers)
                    next = false;
                else {
                    for (var i = 0; i < party.size() && next; i++) {
                        if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                            levelValid += 1;
                        if (party.get(i).getMapId() == mapId)
                            inMap += 1;
                    }
                    if (levelValid < party.size() || inMap < party.size())
                        next = false;
                }
                if (next) {
                    // Kick it into action.  Slate says nothing here, just warps you in.
                    var em = cm.getEventManager("HorntailPQ");
                    if (em == null) {
                        cm.dispose();
                    } else {
                            if(em.getProperty("state").equals("0")) {
                            // Begin the PQ.
                            em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                            // Remove pass/coupons
                            party = cm.getPlayer().getEventInstance().getPlayers();
                        } else {
                            cm.sendOk("There is currently another party inside.");
                        }
                    }
                    cm.dispose();
                }
                else {
                    cm.sendOk("Your party is not a party of six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
                    cm.dispose();
                }
            }
        } else {
            cm.dispose();
        }
    }*/
}
					
					
