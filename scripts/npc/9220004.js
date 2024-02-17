/** 
 *	@Name: Happy the Snow Fairy
 *	@Function: Snow Blower!
 *	@Author: iPoopMagic (David)
 */


var idneeded = 4031875;
var numneeded = 25000;
 
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
            if (cm.getMapId() == 209080000) {
				var numMobs = 0;
				var mobs = cm.getPlayer().getMap().getMapObjects().iterator();
				while (mobs.hasNext()) {
					if (mobs.next().getType() == Packages.server.life.MapleMapObjectType.MONSTER) {
						numMobs++;
					}
				}
				if (numMobs > 1) {
					cm.sendOk("The Snowman is alive! Defeat it with the snowballs that are falling from the sky to cover this town in snow!");
					cm.dispose();
					return;
				} else {
					cm.sendSimple("Hi there, you're just in time! Happyville needs some snow!\r\n\r\n#b#L0#I would like to give you snow.#l\r\n#L1#I would like to see the current progress.#l\r\n#L2#What is this all about?#l");
				}
			} else {
                cm.sendYesNo("Happyville needs your help! We're running out of snow for this White Christmas! Could you help us?");
            }
		} else if (status == 1) {
            if (cm.getMapId() != 209080000) {
				if (cm.getPlayer().getItemQuantity(1472063, true) < 1) {
					cm.sendYesNo("But first, you'll need a #b#v1472063:# #t1472063##k to enter the #bExtra Frosty Snow Zone#k, or you'll freeze to death. You can purchase these from me for 100,000 mesos. Would you like one?");
				} else {
					cm.warp(209080000, 0);
					cm.dispose();
					return;
				}
            }
			if (selection == 0) {
				if (cm.itemQuantity(idneeded) > 0) {
					cm.sendGetNumber("Oh, well that would be nice! Hmm it seems you currently have #c" + idneeded + "# #t" + idneeded + "#. How many do you want to donate?", 1, 1, cm.getPlayer().getItemQuantity(idneeded, false));
				} else {
					cm.sendOk("Aww, it seems you don't have any #z" + idneeded + "# with you. Would mind going and finding some to help this Happyville have a White Christmas?");
					cm.dispose();
				}
			} else if (selection == 1) {
				var percent = Math.floor(cm.getPlayer().getClient().getChannelServer().getStoredVar(9220004) / numneeded * 100);
				if (percent > 100)
					percent = 100;
				cm.sendOk("Look at the growth chart...\r\n\r\n#B" + percent + "#");
				cm.dispose();
            } else if (selection == 2) {
				cm.sendOk("Happyville is in need of #b#t" + idneeded + "##k to make this the best White Christmas ever.\r\nYou will find the mobs within your level range will drop #v" + idneeded + ":##b#t" + idneeded + "##k. Bring them to me so that I can fill this #bSnow Blower#k up with more snow. Once this is full, snow will rain down everywhere in Happyville!");
				cm.dispose();
            } else {
                cm.dispose();
            }            
		} else if (status == 2) {
			if (cm.getMapId() != 209080000) {
				if (cm.getPlayer().getMeso() > 99999 && cm.canHold(1472063)) {
					cm.gainMeso(-100000);
					cm.gainItem(1472063, 1);
					cm.warp(209080000, 0);
				} else {
					cm.sendOk("Don't think you can rip me off! Where's the holiday spirit in you?");
				}
				cm.dispose();
				return;
            }
			var num = selection;
			if (cm.itemQuantity(idneeded) >= num) {
				cm.gainItem(idneeded, -num);
				cm.getPlayer().getClient().getChannelServer().setStoredVar(9220004, cm.getPlayer().getClient().getChannelServer().getStoredVar(9220004) + parseInt(num));
			} else {
				cm.sendOk("Happyville really needs your help! You need to give me the amount of #z" + idneeded + "# as you said!");
				cm.dispose();
				return;
			}
			var percent = Math.floor(cm.getPlayer().getClient().getChannelServer().getStoredVar(9220004) / numneeded * 100);
			var frostyMap = cm.getPlayer().getMap();
			var curstate = 9400714;
			if (percent > 100) 
				percent = 100;
			// I'm tired of looping logic, so ... don't care =)
			if (percent < 10) {
				if (frostyMap.getMonsterById(9400714) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400714, 1450, 154);					
				}
			} else if (percent < 20) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400715) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400715, 1450, 154);					
				}
			} else if (percent < 30) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400716) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400716, 1450, 154);					
				}
			} else if (percent < 40) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400717) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400717, 1450, 154);					
				}
			} else if (percent < 50) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400718) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400718, 1450, 154);					
				}
			} else if (percent < 60) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400719) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400719, 1450, 154);					
				}
			} else if (percent < 70) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400720) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400720, 1450, 154);					
				}
			} else if (percent < 80) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400721) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400721, 1450, 154);					
				}
			} else if (percent < 90) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400722) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400722, 1450, 154);					
				}
			} else if (percent < 100) {
				for (var i = 0; i < 10; i++) {
					if (frostyMap.getMonsterById(9400714 + i) != null) {
						curstate = curstate + i;
						frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
						break;
					}
				}
				if (frostyMap.getMonsterById(9400723) == null) {
					frostyMap.spawnMonsterOnGroundBelow(9400723, 1450, 154);					
				}
			} else {
				if (curstate != 9400724) {
					for (var i = 0; i < 10; i++) {
						if (frostyMap.getMonsterById(9400714 + i) != null) {
							curstate = curstate + i;
							frostyMap.killMonster(frostyMap.getMonsterById(curstate), null, false);
							break;
						}
					}
					frostyMap.spawnMonsterOnGroundBelow(9400724, 1450, 154);
				}
				drop(frostyMap.getReactorById(2092000), cm.getPlayer().getMap());
				cm.getPlayer().getMap().startMapEffect("A Snowman has spawned somewhere in the Extra Frosty Snow Zone.", 5120000, 30000);
				cm.getPlayer().getClient().getChannelServer().setStoredVar(9220004, 0);
			} 
			cm.sendOk("Ohh, wow! Thanks, Happyville is so grateful for your donation of snow. Look at how much the snow blower has grown on the growth chart!\r\n#B" + percent + "#");
			cm.dispose();
		} else {
            cm.dispose();
        }
    }
}

function drop(reactor, map) { // Total 540,000 HP, each hit does 10, so we need 54,000+ snowballs to drop!
	map.spawnMonsterOnGroundBelow(9400708, randX(), randY());
	Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[Happyville] A Snowman has spawned somewhere in the Extra Frosty Snow Zone."));
	var snow = 700 + Math.floor(Math.random() * 40); // IDC
	var delay = 0;
	for (var i = 0; i < snow; i++) {
		var run = {
			run: function() {
				map.spawnItemDrop(reactor, cm.getPlayer(), new Packages.client.inventory.Item(2060006, 0, 100), new Packages.java.awt.Point(randX(), randY()), true, true);
			}
		}
		Packages.server.TimerManager.getInstance().schedule(new java.lang.Runnable(run), delay);
		delay = delay + 100;
	}
	var run = {
		run: function() {
				reactor.getMap().killMonster(reactor.getMap().getMonsterById(9400724), null, false);
				reactor.getMap().spawnMonsterOnGroundBelow(9400714, 1450, 154);
			}
		}
	Packages.server.TimerManager.getInstance().schedule(new java.lang.Runnable(run), delay + 1000);
}

function randX() {
	return Randomizer.rand(-999, 1899);
}

function randY() {
    return 154;
}