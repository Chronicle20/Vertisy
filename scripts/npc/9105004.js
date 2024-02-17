/**
 *	@Name: Snow Spirit
 *	@Description: PQ Guide
 *	@Author: iPoopMagic (David)
 */
var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 3;
var maxPlayers = 6;
var rewards = new Array(1302015, 1312039, 1322065, 1402053, 1412035, 1422039, 1432050, 1442071, // 1H Sword, Axe, BW, 2H Sword, Axe, BW, Spear, Pole Arm
						1372046, 1382062, // Wand, Staff
						1452062, 1462056, // Bow, Xbow
						1332081, 1472077, // Dagger, Claw
						1482029, 1492030 // Knuckler, Gun
						);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
   	if (status >= 1 && mode == 0) {
        cm.sendOk("Ask your friends to join your party. You can use the Party Search funtion (hotkey O) to find a party anywhere, anytime.");
        cm.dispose();
        return;
    }
    if (mode < 1 && status == 0) {
    	cm.dispose();
    	return;
    }
    if (mode == 1)
    	status++;
    else
    	status--;
    if (status == 0) {
		if (cm.getPlayer().getMapId() == 889100100) {
			cm.sendYesNo("Seasons greetings #h0#, \r\nI'd like to thank you once again for joining us and saving Happyville from the evil Scrooge. You've come just in time, he's about to send his minions through to take down our snowman! Would you like to enter? You will be warped to the entrance map based on level. #b" +
						"\r\nLv. 10 ~ 44 - Easy Mode\r\nLv. 45 ~ 69 - Medium Mode\r\nLv. 70 ~ 200 - Hard Mode");
		} else if (cm.isLeader() && cm.getPlayer().getMapId() != 889100100) {
			if (cm.getPlayer().getMapId() % 10 == 1) {
				var eim = cm.getPlayer().getEventInstance();
				if (eim.getProperty("stage").equals("clear")) {
					cm.sendSimple("Here's your Maplemas gift. Please select the one you'd like to have: #b" +
									"\r\n#L10##v2000002# #t2000002# : 20, #v1002850# #t1002850##l\r\n" + 
									"\r\n#L11##v2000006# #t2000006# : 20, #i1302105# Special Seraphim for the Holidays#l#k");
				}
				var mobId = 100100;
				if (cm.getPlayer().getMapId() == 889100001) mobId = 9400322;
				if (cm.getPlayer().getMapId() == 889100011) mobId = 9400327;
				if (cm.getPlayer().getMapId() == 889100021) mobId = 9400332;
				if (eim.getProperty("stage").equals("0")) {
					cm.sendNext("#b#h0##k... you're finally here. This is the place where the residents of Happyville build the giant snowman. But Scrooge's Subordinates are attacking it right now. Now hurry! " +
							"Our mission is for you and your party to protect the snowman from Scrooge's men within the time limit. If you eliminate them, then they'll drop an item called Snow Vigor. Gather them up " +
							"and drop them on the snowman, and you'll literally see it grow. Once it returns to its original size, then your task is complete. Just beware of one thing. Some of the subordinates may " +
							"drop a fake Snow Vigor. A fake Snow Vigor will actually cause the snowman to melt even faster than usual. Best of luck to you.");
					eim.setProperty("stage", "snowman");				
					eim.getMapInstance(cm.getPlayer().getMapId()).spawnMonsterOnGroundBelow(mobId, -180, 34);
					eim.getMapInstance(cm.getPlayer().getMapId()).allowSummonState(true);
					cm.dispose();
				} else if (eim.getProperty("stage").equals("snowman")) {
					if (eim.getMapInstance(cm.getPlayer().getMapId()).getMonsterById(mobId + 4) != null) {
						cm.sendNext("Awesome! Just as I expected, you managed to defeat Scrooge's subordinates. Thank you so much! (Stands silent for a minute....) Unfortunately, Scrooge doesn't seem like he's going to stop right here. One of his men have already told him what happened, which means... he'll show up soon. Please keep fighting, and again, best of luck to you.");
						eim.setProperty("stage", "scrooge");
						var cross = 100100;
						if (cm.getPlayer().getMapId() == 889100001) cross = 9400319;
						if (cm.getPlayer().getMapId() == 889100011) cross = 9400320;
						if (cm.getPlayer().getMapId() == 889100021) cross = 9400321;								
						eim.getMapInstance(cm.getPlayer().getMapId()).spawnMonsterOnGroundBelow(cross, -180, 34);
						eim.getMapInstance(cm.getPlayer().getMapId()).allowSummonState(false);
						cm.dispose();
					} else {
						cm.sendOk("We need to build a bigger snowman! Keep those Snow Vigors coming!");
						cm.dispose();
						return;
					}
				} else if (eim.getProperty("stage").equals("scrooge")) {
					clear();
					cm.sendNext("Wow!! You defeated Scrooge! Thank you so much! You have managed to make this Maplemas the most memorable one yet!");
					eim.setProperty("stage", "clear");
					cm.dispose();
				}
			} else {
				cm.sendSimple("#e <Party Quest: Save the Snowman!>#n \r\n#r(Required Level: 10+)#k\r\nScrooge is hatching up a plan to ruin Maplemas. We need adventurers to help bring Scrooge's plans to a halt!#b\r\n#L1#Start the quest.#l\r\n#L2#Find a party.#l\r\n#L3#Listen to the Snow Spirit's story.#l#k");
			}
		} else if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
			cm.sendOk("Please form a party and have your party leader talk to me.");
			cm.dispose();
		}
	} else if (status == 1) {
		if (cm.getPlayer().getMapId() == 889100100) {
			var level = cm.getPlayer().getLevel();
			if (level < 45) {
				cm.warp(889100000, 0);
			} else if (level < 70) {
				cm.warp(889100010, 0);
			} else {
				cm.warp(889100020, 0);
			}
			cm.dispose();
			return;
		}
		if (selection == 1) {
			if (cm.getParty() == null) {
				cm.sendYesNo("You need to create a party to do the Party Quest. Do you want to use the Party Search helper?");
			} else {
				if (cm.getParty() == null) {
				cm.sendOk("Please come back to me after you've formed a party.");
				cm.dispose();
				return;
			}
			if (!cm.isLeader()) {
				cm.sendOk("Please have your leader speak with me.");
				cm.dispose();
			}
			var party = cm.getParty().getMembers();
			var mapId = cm.getPlayer().getMapId();
			var next = true;
			var levelValid = 0;
			var inMap = 0;
			if (cm.getPlayer().isGM()) {
				minPlayers = 1;
			}
			if (party.size() < minPlayers || party.size() > maxPlayers) {
				next = false;
			} else {
				for (var i = 0; i < party.size() && next; i++) {
					if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
						levelValid += 1;
					if (party.get(i).getMapId() == mapId)
						inMap += 1;
				}
				if (levelValid < minPlayers || inMap < minPlayers)
					next = false;
				}
				if (cm.getPlayer().isGM()) {
					next = true;
				}
				if (next) {
					var em = cm.getEventManager("HolidayPQ");
					if (em == null) {
						cm.sendOk("Holiday PQ does not wor, please contact a GM immediately.");
						cm.dispose();
					} else {
						var prop = em.getProperty("state");
						if (prop == null || prop.equals("0")) {
							em.setProperty("leaderID", cm.getPlayer().getClient().getAccID());
							em.startInstance(cm.getParty(), cm.getPlayer().getMap(), em.getChannelServer().getMap(cm.getPlayer().getMapId() + 1), true);
						} else {
							cm.sendOk("Someone is already attempting the PQ. Please wait or try a different channel.");
						}
					}
					cm.dispose();
				} else {
					cm.sendOk("Your party is not a party of 3 or more.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid + " #kmembers are in the right level range, and #b" + inMap + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
					cm.dispose();
				}
			}
		} else if (selection == 2) {
			cm.openUI(0x16);
			cm.dispose();
		} else if (selection == 3) {
			cm.sendOk("Help defend Happyville and save Maplemas!\r\n #e - Level:#n 10+ \r\n #e - Time Limit:#n 10 min \r\n #e - Number of Players:#n 3 or more \r\n #e - Rewards:#n #rA Happy Maplemas!#k");
			cm.dispose();
		} else if (selection == 10 || selection == 11) {
			if (selection == 10) {
				
			} else if (selection == 11) {
				
			}
			cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
			cm.dispose();
		}
	} else if (status == 2 || status == 3) { 
		cm.openUI(0x16);
		cm.dispose();         
	} else if (mode == 0) { 
		cm.dispose();
	} 
}

function clear() {
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
}