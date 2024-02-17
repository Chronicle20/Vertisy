/**
 * @Event: CWKPQ (Pre-BigBang)
 * @Modified: iPoopMagic (David)
 * @Description:
 *		Inner Sanctum Hallway (610030100) - Find the hidden portal.
 *		Forgotten Storage Chamber (610030200) - Activate the sigils with 3rd job attacks.
 *		The Test of Agility (610030300) - Activate the sigils with 3rd job attacks, avoiding the perilous totem.
 *		The Test of Wit (610030400) - Activate the sigils, while stirges and fires (need spawn) are in your way (need sigils to spawn).
 *		The Test of Unity (610030500) - Talk to respective statues to be warped in. After obtaining the item, place on big statue to activate portal.
 *		Warrior Mastery Room (610030510) - Kill all the Crimson Guardians. Talk to statue to receive Sword ETC. item. (NPC: 9201107)
 *		Mage Mastery Room (610030521/2) - Make your way right and talk to the statue to receive Wand ETC. item. Go back into entrance portal. (NPC: 9201109)
 *		Thief Mastery Room (610030530) - Talk to statue, move around and kill all monsters with regular attack (HP?!). Talk to statue to receive Claw ETC. item. (NPC: 9201110)
 *		Bowman Mastery Room (610030540) - Kill all guardians. Talk to statue to receive Bow ETC. item. (NPC: 9201108)
 *		Pirate Mastery Room (610030550) - Hit the treasure chests to obtain the Pirate ETC. item. (NPC: 9201111)
 *		Grandmaster Council Hall (610030600) - The NPC will summon black guardians (9400594), then after you kill them, the 4 bosses (9400590-593), each drops of Mark of Narcain (1122059)
 *		Grandmaster Secret Chamber (610030700) - 1 minute timer (includes bonus stage)
 *		Crimsonwood Armory (610030800) - Rewards:
					2022123 - Banana Graham Pie
					1402005 - Berzerker
					1102206 - Blackfist Cloak
					1102043 - Brown Adventurer Cape
					1492012 - Concerto
					1102205 - Crimsonheart Cloak
					1092061 - Crossheider
					2041031 - Dark Scroll for Cape for HP 30%
					2041033 - Dark Scroll for Cape for MP 30%
					1472051 - Dragon Green Sleve
					1372039 - Elemental Wand 5
					2022121 - Gelt Choclaate
					1462016 - Golden Neschere
					1102207 - Goldensoul Cape
					1082228 - Green Mittens
					1092050 - Khanjar
					1482012 - King Cent
					2022195 - Mapleade
					2290027 - Big Bang 30
					2290047 - Blizzard 30
					2290091 - Boomerang Step 30
					2290011 - Brandish 30
					2290103 - Demolition 30
					2290061 - Hurricane 30
					2290071 - Piercing Arrow 30
					2290089 - Shadow Claw 30
					2290053 - Sharp Eyes 30
					2290111 - Time Leap 30
					1052131 - Red Belly Duke
					2022272 - Smoken Salmon
					4161018 - [Storybook] Ancient Book
					4001107 - [Storybook] Black Book
					4161015 - [Storybook] Burning Book of Fire
					4161021 - [Storybook] Formula for Black Cloud
					4161016 - [Storybook] Frozen Book of Ice
					2022273 - Ssiws Cheese
					1432056 - Stormshear
					2022274 - Sugar-Coated Olives
					2022277 - Sunblock
					4032015 - Tao of Shadows
					4032016 - Tao of Sight
					4032017 - Tao of Harmony
					1452019 - White Nisrock
					3010010 - White Seal Cushion
 */

var mapIds = Array(100, 200, 300, 400, 500, 510, /*520, */521, 522, 530, 540, 550, 600, 700, 800);
var pos_x = Array(944, 401, 28, -332, -855);
var pos_y = Array(-204, -384, -504, -384, -204);
var pos_y2 = Array(-144, -444, -744, -1044, -1344, -1644);
var minPlayers = 6;

function init() {
	em.setProperty("state", "0");
}

function setup(eim) {
	em.setProperty("state", "1");
	// Rooms / Bosses Status
	em.setProperty("current_instance", "0");
	// Portal Scripts
	em.setProperty("glpq1", "0");
	em.setProperty("glpq2", "0");
	em.setProperty("glpq3", "0");
	em.setProperty("glpq4", "0");
	em.setProperty("glpq4warrior", "0");
	em.setProperty("glpq4archer", "0");
	em.setProperty("glpq4mage", "0");
	em.setProperty("glpq4thief", "0");
	em.setProperty("glpq4pirate", "0");
	em.setProperty("glpq5", "0");
	em.setProperty("glpq6", "0");
	var eim = em.newInstance("CWKPQ_" + em.getProperty("channel"));
	for (var i = 0; i < mapIds.length; i++) {
		var map = eim.getMapInstance(610030000 + mapIds[i]);
		if (map != null) {
			map.clearAndReset(true);
			// Warrior room
			if (map.getId() == 610030510) {
				for (var z = 0; z < pos_y2.length; z++) {
					var mob = Packages.server.life.MapleLifeFactory.getMonster(9400582);
					eim.registerMonster(mob);
					map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(0, pos_y2[z]));
				}
			// Mage Room
			// Bowman Room
			} else if (map.getId() == 610030540) {
				for (var z = 0; z < pos_x.length; z++) {
					var mob = Packages.server.life.MapleLifeFactory.getMonster(9400594);
					eim.registerMonster(mob);
					map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(pos_x[z], pos_y[z]));
				}
			// Pirate Room
			} else if (map.getId() == 610030550) {
				map.shuffleReactors();
			}
		}
	}
	eim.restartEventTimer(3 * 60 * 1000); // 3 minutes for 1st Stage
	eim.schedule("spawnGuardians", 2 * 60 * 1000); // 2 minutes to spawn mobs
	return eim;
}

function playerEntry(eim, player) {
	eim.broadcastPlayerMsg(5, "[Expedition] " + player.getName() + " has entered the map.");
	var map = eim.getMapInstance(610030100 + (parseInt(em.getProperty("current_instance")) * 100));
	player.changeMap(map, map.getPortal(0));
}

function spawnGuardians(eim) {
	if (eim.getEm().getProperty("current_instance").equals("0")) {
		var map = eim.getMapInstance(eim.getPlayers().get(0).getMapId());
		if (map.getAllPlayer().size() <= 0) {
			return;
		}
		eim.broadcastPlayerMsg(6, "The Master Guardians have detected you.");
		for (var i = 0; i < 10; i++) { //spawn 10 guardians
			var mob = Packages.server.life.MapleLifeFactory.getMonster(9400594);
			eim.registerMonster(mob);
			map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(1000, 336));
		}
	}
}

function playerRevive(eim, player) {
}

function scheduledTimeOut() {
    var iter = em.getInstances().iterator();
    while (iter.hasNext()) {
        var eim = iter.next();
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()) {
                playerExit(eim, pIter.next());
            }
        }
        eim.dispose();
    }
}

function changedMap(eim, player, mapid) {
	if (mapid < 610030100 || mapid > 610030800) {
		playerExit(eim, player);
	} else {
		switch(mapid) {
			case 610030200:
				if (em.getProperty("current_instance").equals("0")) {
					eim.restartEventTimer(8 * 60 * 1000); // 8 minutes (Sigils)
					em.setProperty("current_instance", "1");
				}
				break;
			case 610030300:
				if (em.getProperty("current_instance").equals("1")) {
					eim.restartEventTimer(10 * 60 * 1000); //10 mins (JQ)
					em.setProperty("current_instance", "2");
				}
				break;
			case 610030400:
				if (em.getProperty("current_instance").equals("2")) {
					eim.restartEventTimer(10 * 60 * 1000); //10 mins (Stirge & Flames)
					em.setProperty("current_instance", "3");
				}
				break;
			case 610030500:
				if (em.getProperty("current_instance").equals("3")) {
					eim.restartEventTimer(15 * 60 * 1000); //15 mins (Respective job maps)
					em.setProperty("current_instance", "4");
				}
				break;
			case 610030600:
				if (em.getProperty("current_instance").equals("4")) {
					eim.restartEventTimer(90 * 60 * 1000); //1 hr & 30 minutes (Boss)
					em.setProperty("current_instance", "5");
				}
				break;
			case 610030800:
				if (em.getProperty("current_instance").equals("5")) {
					eim.restartEventTimer(1 * 60 * 1000); //1 min
					em.setProperty("current_instance", "6");
				}
				break;
		}
	}
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //boot whole party and end
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim, party.get(i));
        }
        eim.dispose();
    }
    else { //boot dead player
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() <= minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isLeader(player)) { //check for party leader
        //PWN THE PARTY (KICK OUT)
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).equals(player)) {
                removePlayer(eim, player);
            }
            else {
                playerExit(eim, party.get(i));
            }
        }
        eim.dispose();
    }
    else {
        // If only 5 players are left, uncompletable:
        var party = eim.getPlayers();
        if (party.size() < minPlayers) {
            for (var i = 0; i < party.size(); i++) {
                playerExit(eim,party.get(i));
            }
            eim.dispose();
        }
        else
            playerExit(eim, player);
    }
}

function leftParty(eim, player) {			
    // If only 5 players are left, uncompletable:
    var party = eim.getPlayers();
    if (party.size() <= minPlayers) {
        for (var i = 0; i < party.size(); i++) {
            playerExit(eim,party.get(i));
        }
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {
    //boot whole party and end
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function monsterValue(eim, mobId) {
	return 1;
}

function playerExit(eim, player) {
	eim.broadcastPlayerMsg(5, "[Expedition] " + player.getName() + " has left the map.");
	eim.unregisterPlayer(player);
	var exitMap = em.getChannelServer().getMap(610030010);
    player.changeMap(exitMap, exitMap.getPortal(0));
	end(eim);
}

function end(eim) {
	em.setProperty("state", "0");
	eim.dispose();
}

function clearPQ(eim) {
    end(eim);
}

function allMonstersDead(eim) {
}

function cancelSchedule() {}