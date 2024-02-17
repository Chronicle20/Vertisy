/**
 *	@Modified: iPoopMagic (David)
 *	@NPC: Chamberlain Eak
 */

function start() {
	if (cm.getPlayer().getMapId() == 920011200) {
		for (var i = 4001044; i < 4001064; i++) {
			cm.removeAll(i); // NO CHEATING OR STEALING!
		}
		cm.warp(200080101);
		cm.dispose();
		return;
	}
    var em = cm.getEventManager("OrbisPQ");
    if (em == null) {
		cm.sendOk("The event has broken. Please contact a GM immediately!");
		cm.dispose();
		return;
    }
    if (em.getProperty("pre").equals("0")) {
		for (var i = 4001044; i < 4001064; i++) {
			cm.removeAll(i);
		}
		em.setProperty("pre", "1");
		cm.getPlayer().getMap().getReactorById(2006000).setState(20); // FUNKY
		var texttt = "Hi, my name is Eak, the Chamberlain of the Goddess. Don't be alarmed; you won't be able to see me right now. Back when the Goddess turned into a block of stone, I simultaneously lost my own power. If you gather up the power of the Magic Cloud of Orbis, however, then I'll be able to recover my body and re-transform back to my original self. Please collect #b20#k Magic Clouds and bring them back to me. Right now, you'll only see me as a tiny, flickering light."
		cm.sendOk(texttt);
		cm.dispose();
		return;
    }
    if (!cm.isLeader() && cm.getPlayer().getMapId() != 920010000 && (em.getProperty("pre").equals("1") || em.getProperty("pre").equals("2"))) {
		cm.sendOk("Please have your leader speak with me.");
		cm.dispose();
		return;
    }
    switch(cm.getPlayer().getMapId()) {
		case 920010000:
			if (em.getProperty("pre").equals("2")) {
				cm.givePartyQuestExp("OrbisPQPre");
				em.setProperty("pre", "3");
				clear();
			} else if (em.getProperty("pre").equals("3")) {
				cm.warp(920010000, 2);
			}
			break;
		case 920010100:
			if (em.getProperty("stageS").equals("6")) {
				if (em.getProperty("finished").equals("0")) {
					cm.warpParty(920010800); // Garden Map
				} else {
					cm.sendOk("Thank you for saving Minerva! Please, talk to her!");
				}
			} else {
				cm.sendOk("Please, save Minerva! Gather the six pieces of her statue and talk to me to retrieve the final piece!");
			} 
			break;
		case 920010200: // Walkway
			if (!cm.haveItem(4001050, 30) && !em.getProperty("stage1").equals("1")) {
				cm.sendOk("Gather the 30 Statue Pieces from the monsters in this stage, and please bring them to me so I can put them together!");
			} else if (cm.haveItem(4001050, 30) && !em.getProperty("stage1").equals("1")) {
				cm.removeAll(4001050);
				cm.gainItem(4001044, 1); // 1st piece
				cm.givePartyQuestExp("OrbisPQWalk"); // How much EXP, Idk...
				em.setProperty("stage1", "1");
				clear();
			} else {
				cm.sendOk("Thank you!");
			}
			break;
		case 920010300: // Storage
			if (!cm.haveItem(4001051, 15) && !em.getProperty("stage2").equals("15")) {
				cm.sendOk("Gather 15 Statue Pieces from the monster in this stage, and please bring it to me so I can put it together with my spares!");
			} else if (cm.haveItem(4001051, 15) && !em.getProperty("stage2").equals("15")) {
				cm.removeAll(4001051);
				cm.gainItem(4001045, 1); // 2nd piece
				cm.givePartyQuestExp("OrbisPQStore");
				em.setProperty("stage2", "15");
				clear();
			} else {
				cm.sendOk("Thank you!");
			}
			break;
		case 920010400: // Lobby
			if (em.getProperty("stage3").equals("0")) {
				cm.sendOk(cm.getPlayer().getMap().getReactorById(2008006).getState() + "#bToday is #r" + getDay() + ".#k Please, find the LP for the current day of week and place it on the music player.\r\n#v4001056#Sunday\r\n#v4001057#Monday\r\n#v4001058#Tuesday\r\n#v4001059#Wednesday\r\n#v4001060#Thursday\r\n#v4001061#Friday\r\n#v4001062#Saturday\r\n" + cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getReactorById(2008006).getState());
//				cm.sendOk("Currently the Tower of Goddess has been frozen in time, so the current day is #rSunday#k. Please, find the LP for the current day of the week and place it on the music player.\r\n#v4001056#Sunday");
			} else if (em.getProperty("stage3").equals("1")) {
				if (cm.canHold(4001046)) {
					cm.gainItem(4001046, 1); // 3rd piece
					cm.givePartyQuestExp("OrbisPQLobby");
					clear();
					em.setProperty("stage3", "2");
				} else {
					cm.sendOk("Please make room!");
				}
			} else {
				cm.sendOk("Thank you so much!");
			}
			break;
		case 920010500: // Sealed Room
			if (em.getProperty("stage4").equals("0")) {
				var eim = cm.getPlayer().getEventInstance();
				var map = eim.getMapInstance(920010500);
				if ((map.getPlayersInRange(map.getArea(0), eim.getPlayers()).size() + map.getPlayersInRange(map.getArea(1), eim.getPlayers()).size() + map.getPlayersInRange(map.getArea(2), eim.getPlayers()).size()) < 5) {
					cm.sendOk("There needs to be 5 players on the platforms.");
				} else {
					var currentCombo = em.getProperty("sealedCombo");
					if (currentCombo == null || currentCombo.equals("reset")) {
						var newCombo = makeCombo();
						em.setProperty("sealedCombo", newCombo);
						em.setProperty("stage4attempt", "1");
//						cm.getPlayer().dropMessage(5, "[DEBUG]: Sealed Combo - " + newCombo);
						cm.sendOk("In this map, you must distribute the weight of the players evenly on the three platforms in order to unlocked the sealed treasure above.");
						cm.dispose();
						return;
					}
					var attempt = parseInt(em.getProperty("stage4attempt"));
					var combo = parseInt(currentCombo);
					var guess = getPlayersOnPlatforms();
					if ((combo == guess || em.getProperty("sealedCombo").equals("clear")) && !em.getProperty("stage4").equals("1")) {
						clear();
						em.setProperty("sealedCombo", "clear");
						if (cm.canHold(4001047)) {
							cm.gainItem(4001047, 1); // 4th Piece
							cm.givePartyQuestExp("OrbisPQSealed");
							em.setProperty("stage4", "1");
						}
//						em.setProperty("sealedCombo", "reset"); // FOR RETESTING ONLY
						// Why won't this spawn?
//						var reactor = new MapleReactor(MapleReactorFactory.getReactor(2002012), 2002012);
//						reactor.setPosition(new java.awt.Point(-47, -1417));
//						cm.getPlayer().dropMessage(5, "Spawning reactor..." + reactor + reactor.getPosition());
//						map.spawnReactor(reactor);
//						reactor.setState(2);
					} else if (!em.getProperty("stage4").equals("1")) {
//						cm.showEffect("quest/party/wrong_kor");
//						cm.playSound("Party1/Failed");
						if (attempt < 7) {
							var parsedCombo = parsePattern(combo);
							var parsedGuess = parsePattern(guess);
							var results = compare(parsedCombo, parsedGuess);
							var string = "";
							string += "Results - #b" + results[0] + " SAME | " + results[1] + " DIFFERENT";
							string += "\r\nThis is your ";
							switch (attempt) {
								case 1:
								string += "1st";
								break;
								case 2:
								string += "2nd";
								break;
								case 3:
								string += "3rd";
								break;
								default:
								string += attempt + "th";
								break;
							}
							string += " attempt.";
							cm.sendOk(string);
							em.setProperty("stage4attempt", attempt + 1);
						} else {
							em.setProperty("sealedCombo", "reset");
							cm.warpParty(920010100);
							cm.sendOk("You have attempted 7 times to find the secret combination. I'm sorry, but you'll have to start over and try again.");
						}							
					} else {
						cm.sendOk("Thank you once again!");
					}
				}
			} else {
				cm.sendOk("Thank you once again!");
			}
			cm.dispose();
			break;
		case 920010600: // Lounge
			if (!cm.haveItem(4001052, 30) && !em.getProperty("stage5").equals("1")) {
				cm.sendOk("Gather the 30 Statue Pieces from the monsters in this stage, and please bring them to me so I can put them together!");
			} else if (!em.getProperty("stage5").equals("1")) {
				cm.removeAll(4001052);
				cm.gainItem(4001048, 1); // Fifth piece
				cm.givePartyQuestExp("OrbisPQLounge");
				em.setProperty("stage5", "1");
				clear();
				cm.warpParty(920010100);
			} else {
				cm.sendOk("Thank you!");
			}
			break;
		case 920010700: // On The Way Up
			if (em.getProperty("stage6").equals("0")) {
				var react = Array();
				var total = 0;
				for(var i = 0; i < 3; i++) {
					if (cm.getPlayer().getMap().getReactorByName("" + (i + 1)).getCurrState() > 0) {
						react.push("1");
						total += 1;
					} else {
						react.push("0");
					}
				}
				if (total != 2) {
					cm.sendOk("There needs to be 2 levers at the top of the map pushed on.");
				} else {
					var num_correct = 0;
						for (var i = 0; i < 3; i++) {
						if (em.getProperty("stage62_" + i).equals("" + react[i])) {
							num_correct++;
						}
					}
					if (num_correct == 3) {
						if (cm.canHold(4001049,1)) {
							clear();
							cm.gainItem(4001049, 1); //sixth
							cm.givePartyQuestExp("OrbisPQUp");
							em.setProperty("stage6", "1");
						} else {
							cm.sendOk("Please make room!");
						}
					} else {
						cm.showEffect("quest/party/wrong_kor");
						cm.playSound("Party1/Failed");
						if (num_correct >= 1) { //this should always be true
							cm.sendOk("One of the levers is correct.");
						} else {
							cm.sendOk("Both of the levers are wrong.");
						}
					}
				}
			} else {
				cm.sendOk("Thank you!!");
			}
			break;
		case 920010800: // Garden (Papa Pixie)
			if (em.getProperty("finished").equals("2")) {
				clear();
				cm.gainItem(4001055, 1);
				cm.givePartyQuestExp("OrbisPQBoss");
				cm.warpParty(920010100);
				cm.sendOk("You've defeated Papa Pixie! He dropped the Grass of the Life, which we need to revive the Goddess Minerva. Take this and go to the center room to rescue the Goddess.");
			} else {
				cm.sendNext("Please, find a way to defeat Papa Pixie! Once you've found the Dark Nependeath by placing seeds, you've found Papa Pixie! Defeat it, and get the Grass of Life to save Minerva!!!");
			}
			break;
		case 920010900: // Jail
			cm.sendNext("This is the jail of the tower. You may find some goodies here, but other than that I don't think we have any pieces here."); 
			break;
		case 920011000: // Hidden Room
			cm.sendNext("This is the hidden room of the tower. You may find some goodies here, but other than that I don't think we have any pieces here."); 
			break;
    }
    cm.dispose();
}

function clear() {
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
}

function makeCombo() {
	var comboArray = Array(0, 0, 0);
	for (var i = 0; i < 5; i++) { // 5 players required
		var picked = Math.floor(Math.random() * comboArray.length);
		comboArray[picked] += 1;
	}
	var combo = (comboArray[0] * 100) + (comboArray[1] * 10) + (comboArray[2] * 1);
	return combo;
}

// Returns an integer of players on platform (i.e. 500 - 5 players on 1st platform, and 0 on 2nd and 3rd platforms)
function getPlayersOnPlatforms() {
	var em = cm.getPlayer().getEventInstance();
	var map = em.getMapInstance(920010500);
	return (map.getPlayersInRange(map.getArea(0), em.getPlayers()).size() * 100) + (map.getPlayersInRange(map.getArea(1), em.getPlayers()).size() * 10) + (map.getPlayersInRange(map.getArea(2), em.getPlayers()).size() * 1);
}

// Convert integer to array for comparison
function parsePattern(pattern) {
    var tempPattern = pattern;
    var items = new Array(-1, -1, -1);
    for (var i = 0; i < 3; i++) {
		items[i] = Math.floor(tempPattern / Math.pow(10, 2 - i));
		tempPattern = tempPattern % Math.pow(10, 2 - i);
    }
    return items;
}

// Compare two int arrays
function compare(answer, guess) {
    var correct = 0;
    for (var i = 0; i < answer.length; i) {
		if (answer[i] == guess[i]) {
			correct++;
			if (i != answer.length - 1) {
				answer[i] = answer[answer.length - 1];
				guess[i] = guess[guess.length - 1];
			}
			answer.pop();
			guess.pop();
		} else {
			i++;
		}
	}
    return new Array(correct, 3 - correct); // Is this right? IDK
}

function getDay() {
	var number = java.util.Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	var day = "Sunday";
	switch (number) {
		case 1:
			day = "Sunday";
			break;
		case 2:
			day = "Monday";
			break;
		case 3:
			day = "Tuesday";
			break;
		case 4:
			day = "Wednesday";
			break;
		case 5:
			day = "Thursday";
			break;
		case 6:
			day = "Friday";
			break;
		case 7:
			day = "Saturday";
			break;
	}
	cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getReactorById(2008006).setState(number);
	return day;
}