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
 * @author BubblesDev
 * @NPC Tory
 * @modified iPoopMagic (David), peter
 */
var status = 0;
//var min = 3;
//var minLevel = 10;
var min = 1;
var minLevel = 10;
var riceCakes = 20; //rice cakes needed for the hat
var riceCakeGiven;
var HPQ = Java.type("server.partyquest.HPQ");
var PartyQuest = Java.type("server.partyquest.PartyQuest");

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1
            || (mode === 0 && status === 0)) {
        cm.dispose();
    } else {
        mode === 1 ? status++ : status--;

        if (cm.getPlayer().getMapId() === 100000200) { // 100000200 - Pig Park
            if (cm.getParty() === null || !cm.isLeader()) { //the player is not pt leader
                if (status === 0) {
                    cm.sendNext("Hi there! I'm Tory. This place is covered with mysterious aura of the full moon, and no one person can enter here by him/herself.");
                } else if (status === 1) {
                    cm.sendOk("If you'd like to enter here, the leader of your party will have to talk to me. Talk to your party leader about this.");
                    cm.dispose();
                }
            } else { //the player is pt leader
                if (status === 0) {
                    cm.sendNext("I'm Tory. Inside here is a beautiful hill where the primrose blooms. There's a tiger that lives in the hill, Growlie, and he seems to be looking for something to eat.");
                } else if (status === 1) {//why not sendYesNo instead?
                    cm.sendSimple("Would you like to head over to the hill of primrose and join forces with your party members to help Growlie out?\r\n\
#b#L0# Yes, I will go.#l");
                } else if (status === 2) {//try to start the pq
                    var em = cm.getEventManager("HenesysPQ");
                    if (em === null) {
                        cm.sendOk("This PQ is currently broken. Please report it on the forum!");
                        cm.dispose();
                        return;
                    }

                    var party = cm.getParty().getMembers(); //i prefer this than the ori, "no" processing.
                    var inMap = cm.partyMembersInMap();

                    if (party.size() !== inMap || inMap < min) {
                        cm.sendOk("Your party is not a party of " + min
                                + ". Please make sure all your members are present and qualified to participate in this quest.");
//                        cm.sendOk("To attempt this party quest, your party needs to have a minimum of " + min + " members.");
                        cm.dispose();
                        return;
                    }

                    for (var i = 0; i < party.size(); i++) { //check members for min lvl requirement.
                        if (party.get(i).getLevel() < minLevel) {
                            cm.sendOk("One or more of your party members does not meet the level requirement of " + minLevel);
                            cm.dispose();
                            return;
                        }
                    }

                    if (em.getProperty("HPQOpen").equals("true")) { //Start the PQ
                        for (var i = 4001095; i < 4001102; i++) {
                            cm.removePartyItems(i); // No cheating!
                        }
                        em.startPQ("HPQ", cm.getParty(), cm.getPlayer().getMap());
                    } else {
                        cm.sendOk("Someone is already attempting the PQ. Please wait for them to finish, or find another channel.");
                    }
                    cm.dispose();
                }
            }
        } else if (cm.getPlayer().getMapId() === 910010100  // 910010100, 910010400 - Shortcut
                || cm.getPlayer().getMapId() === 910010400) {
            
            if (status === 0) {
                cm.sendSimple("I appreciate you giving some rice cakes for the hungry Growlie. It looks like you have nothing else to do now. Would you like to leave this place?\r\n\
#L0#I want to give you the rest of my rice cakes.#l\r\n\
#L1#Yes, please get me out of here.#l");
            } else if (status === 1) {
            	var player = cm.getPlayer();
                if (selection === 0) {
                    riceCakeGiven =PartyQuest.getItemQty(player, HPQ.class);
                    if (riceCakeGiven >= riceCakes) {
                        if (cm.haveItem(1002798)) { //1002798 - A Rice Cake on Top of My Head
                            cm.sendNext("Do you like the hat I gave you? I ate so much of your rice cake that I will have to say no to your offer of rice cake for a little while.");
                            cm.dispose();
                        } else {
                            cm.sendYesNo("I appreciate the thought, but I am okay now. I still have some of the rice cakes you gave me stored at home.\r\n\
To show you my appreciation, I prepared a small gift for you. Would you like to accept it?");
                        }
                    } else {
                        riceCakeGiven += cm.removeAll(4001101);
                        PartyQuest.updateItemQty(player, HPQ.class, riceCakeGiven); // add to the rice cake given count.
                        var output = "Aww, you have given me #b" + riceCakeGiven
                                + "#k rice cakes, you are spoiling me! Thank you for the rice cakes, I really appreciate it!\r\n";
                        if (riceCakeGiven >= riceCakes) {
                            output += "#bTalk to me again, I might or might not have something for you!";
                        } else if (riceCakeGiven > 0) {
                            output += "Is it weird that I remember exactly how many rice cakes you have given me....?";
                        } else {
                            output = "I love rice cakes, can never get enough of it!\r\n\
With the right amount of rice cakes, I can even make a hat, am I not the rice cake expert?";
                        }
                        cm.sendOk(output);
                        cm.dispose();
                    }
                } else if (selection === 1) {
                    for (var i = 4001095; i < 4001102; i++) {
                        cm.removeAll(i); // Don't go smuggling seeds or cakes! Erg.
                    }
                    cm.warp(100000200);
                    cm.dispose();
                }
            } else if (status === 2) {
                if (cm.canHold(1002798)) {
                    var player = cm.getPlayer();
                    var hadHat = PartyQuest.checkItemAndUpdate(player, HPQ.class);
                    //update progress id 1 cuz the player exchanged some rice cakes for the hat.
                    PartyQuest.updateItemQty(player, HPQ.class, (riceCakeGiven - riceCakes));
                    if (hadHat && player.checkEquippedFor(1002798)) { //shh
                        //the cute player get hat with 1 stat (1-2) if he/she is wearing one! :D
                        HPQ.giveRandomizedRiceCakeHat(player);
                    } else {
                        cm.gainItem(false, 1002798, 1);
                    }
                    cm.sendNext("It will really go well with you. I promise.");
                }
                cm.dispose();
            }
        }
    }
}