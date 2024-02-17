/*/*
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
//@Author FateJiki
//@Author Even (modifier)

var status = -1;

var balrog_easy = Packages.server.expeditions.MapleExpeditionType.BALROG_EASY;
var balrog_normal = Packages.server.expeditions.MapleExpeditionType.BALROG_NORMAL;

var difficulty = null;
var expedition;

function start(){
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
	
	if(cm.getPlayer().getLevel() < 50){
		cm.sendOk("You must be at least level 50 to even consider battling the monster.");
		cm.dispose();
		return;
	}
	
    if(cm.getPlayer().getParty() == null){
        cm.sendOk("You must be in a party.");
        cm.dispose();
        return;
    }
    
    if(difficulty == null){
    	if(status == 0){
    		cm.sendSimple("Please select the difficulty:\r\n#L0##i3994115##l\r\n#L1##i3994116##l");
    	}else if(status == 1){
    		difficulty = selection == 0 ? balrog_easy : balrog_normal;
    		status = -1;
    		action(mode, type, 0);
    	}
    }else{
    	if(cm.getPlayer().getLevel() > difficulty.getMaxLevel() || cm.getPlayer().getLevel() < difficulty.getMinLevel()){
    		cm.sendOk("You don't meet the level requirement.");
    		cm.dispose();
    	}else{
    		expedition = cm.getExpedition(difficulty);	
    		em = cm.getEventManager("Balrog");
    		
    		if(em == null){
    			cm.sendOk("The Balrog boss battle is currently disabled.");
    			cm.dispose();
    			return;
    		}
    		
    		if(expedition.isInProgress()){
    			cm.sendOk("I'm afraid another expedition is already attempting to defeat Balrog");
    			cm.dispose();
    			return;
    		}
    		
    	}
    }
    
}

/*function action(mode, type, selection) {
    if (mode <= 0) {
        cm.dispose();
    } else if (status == 0) {
        cm.sendNext("Hi there. I am #b#nMu Young#n#k, the temple Keeper.");
        status++;
    } else if (BalrogPQ.partyLeader == "undefined") {
        if (status == 1) {
        var text = "This temple is currently under siege by the Balrog troops. We currently do not know who gave the orders. " +
            "For a few weeks now, the #e#b Order of the Altair#n#k has been sending mercenaries, but they were eliminated every time." +
            " So, traveler, would you like to try your luck at defeating this unspeakable horror? \r\n\r\n " +
            "#L0#Yes. Please register me as party leader\r\n#L1#What is the #eOrder of the Altair?";
        cm.sendSimple(text);
        status++;
        } else if (selection == 0) {
            if (cm.getPlayer().getLevel() >= 70) {
                BalrogPQ.partyLeader = cm.getPlayer().getName();
                cm.sendOk("Success. Your name has been registered and you may enter the battlefield. Come speak to me when you're ready!");
                cm.getPlayer().getMap().broadcastMessage(Packages.tools.MaplePacketCreator.serverNotice(0, cm.getPlayer().getName() + " is currently fighting the balrog on CH" + cm.getPlayer().getClient().getChannel() + ". To join, do @balrogpq."))
                BalrogPQ.open(cm.getPlayer());
                cm.dispose();
            } else if (cm.getPlayer().getLevel() < 70) {
                cm.sendOk("You must be at least level 70 to even consider battling the monster.");
                cm.dispose();
            }
        } else if (selection == 1) {
            cm.sendOk("The Order of the Altair is a group of elite mercenaries that oversee the world's economy and battle operations. It was founded 40 years ago right after Black Mage was defeated in hopes of forseeing the next possible attack.");
            cm.dispose();
        } else if (status == 3) {
            cm.warp(105100300);
            cm.dispose();
        }
        } else {
            if (status == 1) {
            cm.sendYesNo(BalrogPQ.partyLeader + "'s party is currently battling the Balrog. Would you like to assist?");
            status++;
            } else if(status == 2){
                if (cm.getPlayer().getLevel() > 60 && cm.getPlayer().getClient().getChannel() == BalrogPQ.channel){
                cm.warp(105100300);
                cm.dispose();
                } else {
                    cm.sendOk("You may not battle the balrog when you are below Lv60! \r\n\r\n Or maybe you are not on the right channel.. Try CH" + BalrogPQ.channel + ".");
                    cm.dispose();
                }
            }
        }
}*/