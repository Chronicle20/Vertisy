/**
 *	@Name: Juliet
 *	@Description: PQ Guide
 *	@Modified: iPoopMagic (David)
 */

var status = 0;
var minLevel = 71;
var maxLevel = 200;
var minPlayers = 4;
var maxPlayers = 4;

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
    if (mode == 0 && status == 0) {
    	cm.dispose();
    	return;
    }
    if (mode == 1)
    	status++;
    else
    	status--;
    if (status == 0) {
		var em = cm.getEventManager("Juliet");
    	var mapid = cm.getPlayer().getMapId();
    	if (mapid == 926110000) {
    		cm.sendOk("You should try investigating around here. Look at the files in the Library until you can find the entrance to the Lab.");
    		cm.dispose();
    	} else if (mapid == 926110001) {
    		cm.sendOk("Please, eliminate all the monsters! I'll come right behind you.");
    		cm.dispose();
    	} else if (mapid == 926110100) {
    		cm.sendOk("These beakers have leaks in them. We must pour the Suspicious Liquid to the beakers' brims so we can continue.");
			em.setProperty("stage3", "3");
    		cm.dispose();
    	} else if (mapid == 926110300) {
    		cm.sendOk("We must get to the top of the Lab, each of your members.");
    		cm.dispose();
    	} else if (mapid == 926110400) {
    		if ((em.getProperty("stage4talk") == null || !em.getProperty("stage4talk").equals("done")) && cm.getPlayer().getMap().getCharacters().size() == cm.getPlayer().getEventInstance().getPlayerCount()) {
				cm.sendOk("Let us go and save my Romeo.");
				cm.givePartyQuestExp("MagatiaPQ2");
				clear();
				em.setProperty("stage4talk", "done");
			} else {
				cm.sendOk("Please talk to me when all of your members are here. Whenever you are ready, we shall go and save my love.");
			}
			cm.dispose();
    	} else if (mapid == 926110401) {
			clear();
			cm.warpParty(926110500);
			cm.givePartyQuestExp("Romeo&JulietBoss");
			cm.dispose();
		} else if (cm.getPlayer().getMapId() == 261000021) {
			cm.sendSimple("#e <Party Quest: Romeo and Juliet>#n #r(Required Level: " + minLevel + " ~ " + maxLevel + ")#k\r\nMagatia faces a grave threat. We need brave adventurers to answer our call.#b\r\n#L1#Start the quest.#l\r\n#L2#Find a party.#l\r\n#L3#Listen to Juliet's story.#l#k");
		} else {
			cm.dispose();
		}
	} else if (status == 1) {
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
				cm.sendOk("You are not the party leader.");
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
					var em = cm.getEventManager("Juliet");
					if (em == null) {
						cm.sendOk("Magatia PQ (Juliet) does not work.");
						cm.dispose();
					} else {
						var prop = em.getProperty("state");
						if (prop == null || prop.equals("0")) {
							em.setProperty("channel", cm.getPlayer().getClient().getChannel());
							em.startInstance(cm.getParty(), cm.getPlayer().getMap());
						} else {
							cm.sendOk("Someone is already attempting the PQ. Please wait or try a different channel.");
						}
					}
					cm.dispose();
				} else {
					cm.sendOk("Your party is not a party of 4.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid + " #kmembers are in the right level range, and #b" + inMap + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
					cm.dispose();
				}
			}
		} else if (selection == 2) {
			cm.openUI(0x16);
			cm.dispose();
		} else if (selection == 3) {
			cm.sendOk("Show your bravery, and help defend the peace in Magatia!\r\n #e - Level:#n " + minLevel + " ~ " + maxLevel + " \r\n #e - Time Limit:#n 20 min \r\n #e - Number of Players:#n " + minPlayers + " \r\n #e - Rewards: \r\n#v4001159:# Zenumist Marble \r\n#v4001160:# Alcadno Marble \r\n #bOther Random Useable Items.");
			cm.dispose();
		}
	} else if (status == 2) {
		if (selection == 10) {
			// apparently there's a pendant...?
		} else if (cm.getMapId != 910002000) {
			cm.openUI(21);
			cm.dispose();
		}
	} else if (status == 3) { 
		cm.openUI(21);
		cm.dispose();         
	} else if (mode == 0) { 
		cm.dispose();
	} 
}

function clear() {
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
}