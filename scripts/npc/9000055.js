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
/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */
 
 
 var idneeded = 4001126;
 var numneeded = 20000;
 
 var func = 0;
 var choice = 0;
 var section = 0;
 
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
        if(cm.getPlayer().getClient().getChannelServer().getStoredVar(9000056) == 0){//Get a random amount required
       		var rand = Math.round(Math.random() * 100000);
        	cm.getPlayer().getClient().getChannelServer().setStoredVar(9000056, rand);
        	numneeded = rand;
        }else{
        	numneeded = cm.getPlayer().getClient().getChannelServer().getStoredVar(9000056);
        }
        if (status == 0 && mode == 1) {
            if(cm.getMapId() == 970010000) {
            	var text = "Hello there, don't you just love maple trees? They are sooo beautiful! Well, enough of that how can I help you?\r\n\r\n#b#L0#I would like to donate leaves.#l\r\n#L1#I would like to see the current progress#l\r\n#L2#What is this all about?#l";
            	if(cm.getPlayer().isGM()){
            		text += "\r\n#L10#GM Info#l";
            	}
                cm.sendSimple(text);
			} else {
                cm.sendYesNo("Would you like to come see the pretty #rMaple Tree#k I am trying to grow?");
            }
		} else if(status == 1) {
            if(cm.getMapId() != 970010000) {
				cm.getPlayer().saveLocation("MIRROR");
                cm.warp(970010000, 1);
                cm.dispose();
                return;
            }
			if(selection == 0) {
                if(cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055) >= numneeded) {
                    cm.sendOk("It looks like the tree is about to blossom! Let's get ready.");
                    cm.dispose();
                    return;
                }
				if(cm.itemQuantity(idneeded) > 0) {
                    cm.sendGetNumber("Oh, well that would be nice! Hmm it seems you currently have #c" + idneeded + "# Maple Leaves. How many do you want to donate?", 1, 1, 10000);
				} else {
					cm.sendOk("Aww, it seems you don't have any #z" + idneeded + "# with you. Would mind going and finding some to help this tree grow?");
					cm.dispose();
				}
			} else if(selection == 1) {
				var percent = Math.floor(cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055) / numneeded * 100);
				if(percent > 100)
					percent = 100;
				cm.sendOk("Hmm, at this rate the tree is going to wilt. Look at the growth chart...\r\n\r\n#B" + percent + "#");
				cm.dispose();
            } else if (selection == 2) {
				cm.sendOk("If you can bring me #bMaple Leaves#k, it will help this tree grow to be big and strong.\r\nOnce it has grown all of the way, this species explodes into a lot of Maple Leaves, rare #rGolden Maple Leaves#k, and even sometimes rare #rDark Coins#k!\r\n\r\n#bIt will take a while to to make this tree grow tall and strong#k\r\nbut the rewards will be worth it. The tree will also give everyone in the channel a 1.5x exp boost. Now get collecting!");
				cm.dispose();
			}else if(selection == 10 && cm.getPlayer().isGM()){
				var text = "";
				text += "Required: " + numneeded;
				text += "\r\n";
				text += "Amount: " + cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055);
				cm.sendOk(text);
				cm.dispose();
				status = -1;
			}else {
                cm.dispose();
            }        
		} else if(status == 2) {
			var num = selection;
			if(cm.itemQuantity(idneeded) >= num) {
                var diff = numneeded - cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055);
                if(diff < num)
                    num = diff;
                
				cm.gainItem(idneeded, -num);
				cm.getPlayer().getClient().getChannelServer().setStoredVar(9000055, cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055) + parseInt(num));
			} else {
				cm.sendOk("Are you trying to hurt this poor tree? You need to give me the ammount of #z" + idneeded + "# as you said!");
				cm.dispose();
				return;
			}
			var percent = Math.floor(cm.getPlayer().getClient().getChannelServer().getStoredVar(9000055) / numneeded * 100);
			var tree = cm.getPlayer().getMap().getReactorById(9702000);
			var curstate = tree.getState();
			if(percent > 100) 
				percent = 100;
				
			if(percent < 20) {
				if(curstate != 0)
					changeState(tree, 0);
			} else if(percent < 30) {
				if(curstate != 1)
					changeState(tree, 1);
			} else if(percent < 60) {
				if(curstate != 2)
					changeState(tree, 2);
			} else if(percent < 90) {
				if(curstate != 3)
					changeState(tree, 3);
			} else if(percent < 100) {
				if(curstate != 4) {
					changeState(tree, 4);
                    var chars = Packages.net.server.Server.getInstance().getWorld(player.getWorld()).getPlayerStorage().getAllCharacters();
                    for each (player in chars) {
                        player.getMap().startMapEffect("The Maple Tree is about to blossom!", 5120008);
                    }
                }
			} else {
				if(curstate != 4) {
					changeState(tree, 4);
                    var chars = Packages.net.server.Server.getInstance().getWorld(player.getWorld()).getPlayerStorage().getAllCharacters();
                    for each (player in chars) {
                        player.getMap().startMapEffect("The Maple Tree is about to blossom!", 5120008);
                    }
                    var run = { run: function() {
                        drop(tree, cm.getPlayer().getMap());
                        cm.getPlayer().getMap().startMapEffect("", 5120008, 20000);
                        cm.getClient().getChannelServer().setStoredVar(9000055, 0);
                        cm.getClient().getChannelServer().setStoredVar(9000056, 0);
                        cm.getClient().getChannelServer().addExpEventTime(30 * 60 * 1000);
                    } }
                    Packages.server.TimerManager.getInstance().schedule(new java.lang.Runnable(run), 20000);
                } else {
                    drop(tree, cm.getPlayer().getMap());
                    cm.getPlayer().getMap().startMapEffect("", 5120008, 20000);
                    cm.getClient().getChannelServer().setStoredVar(9000055, 0);
                    cm.getClient().getChannelServer().setStoredVar(9000056, 0);
                    cm.getClient().getChannelServer().addExpEventTime(30 * 60 * 1000);
                }
			} 
			cm.sendOk("Ohh, wow! Thanks, the tree will love these maple leaves. Look at how much the tree has grown on the growth chart!\r\n#B" + percent + "#");
			cm.dispose();
		} else {
            cm.dispose();
        }
    }
}

function changeState(tree, state) {
	tree.setState(state);
	cm.getPlayer().getMap().broadcastMessage(Packages.tools.packets.field.ReactorPool.triggerReactor(tree, 1));
}

function drop(tree, map) {
	var y = 1244;
	var leaves = 100 + Math.floor(Math.random() * 50);
	var delay = 0;
	for(var i = 0; i < leaves; i++) {
		var run = { run: function() {
		var r = Math.random();
		var item = r < 0.2 ? 4310000 : r < 0.4 ? 4000313 : 4001126;
		map.spawnItemDrop(tree, cm.getPlayer(), new Packages.client.inventory.Item(item, 0, 1), new Packages.java.awt.Point(randX(), randY()), true, false);
		 } }
		Packages.server.TimerManager.getInstance().schedule(new java.lang.Runnable(run), delay);
		delay = delay + 100;
	}

	var run = { run: function() { changeState(tree, 0); } }
	Packages.server.TimerManager.getInstance().schedule(new java.lang.Runnable(run), delay + 1000);
}

function randX() {
	return 1000 - Math.floor(Math.random() * 1375);
}

function randY() {
    return 1100 + Math.floor(Math.random() * 144);
}

function isInt(x) { 
   var y=parseInt(x); 
   if (isNaN(y)) return false; 
   return x==y && x.toString()==y.toString(); 
}