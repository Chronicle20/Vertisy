/**
 *	@Modified iPoopMagic (David)
 */


var status = 0;
var minLevel = 51;
var maxLevel = 70;
var minPlayers = 6; // Must have 6 people at all times!!
var maxPlayers = 6;

function start() {
	status = -1;
	action(1, 0, 0);
}
function action(mode, type, selection) {
	if (status >= 1 && mode == 0) {
        cm.sendOk("Ask your friends to join your party. You can use the Party Search function (hotkey O) to find a party anywhere, anytime.");
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
		if (cm.getPlayer().getMapId() != 200080101) { // not in pq lobby
			if (cm.getPlayer().getEventInstance() != null) {
				status = 10;
				cm.sendYesNo("You aren't finished yet! Do you still want to exit the party quest map? You won't be able to return until the party has finished.");
			} else {
				cm.sendSimple("#e <Party Quest: Remnants of Goddess>#n \r\n#r(Required Level: 51 ~ 70)#k\r\n How would you like to complete a quest by working with your party members? Inside, you will find many obstacles that you will have to overcome with help of your party members.#b\r\n#L0#Go to the Orbis PQ Lobby.")
			}
		} else if (cm.getPlayer().getMapId() == 200080101) {
			cm.sendSimple("#e <Party Quest: Remnants of Goddess>#n \r\n#r(Required Level: 51 ~ 70)#k\r\nHi, I'm Wonky the Fairy. Talk to me if you want to explore the Goddess Tower. Oh also, if you have a Warrior, Magican, Thief, Bowman, and Pirate in your party, I will grant you Wonky's Blessing.#b\r\n#L1#Request admission.#l\r\n#L2#Ask about Tower of Goddess.#l\r\n#L3#Give food to Wonky.#l\r\n#L4#Exchange Feathers of Goddess.#l\r\n#L5#Find party members.#l");
		} else {
			cm.dispose();
		}
	} else if (status == 1) {
		if (selection == 0) {
			cm.getPlayer().saveLocation("MIRROR");
			cm.warp(200080101, 0);
			cm.dispose();
		} else if (selection == 1) {
			if (cm.getParty() == null) {
				cm.sendOk("Please come back to me after you've formed a party.");
				cm.dispose();
				return;
			}
			if (!cm.isLeader()) {
				cm.sendOk("You are not the party leader.");
				cm.dispose();
				return;
			}
			var party = cm.getParty().getMembers();
			var mapId = cm.getPlayer().getMapId();
			var next = true;
			var levelValid = 0;
			var inMap = 0;
			for (var i = 0; i < party.size() && next; i++) {
				if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
					levelValid += 1;
				if (party.get(i).getMapId() == mapId)
					inMap += 1;
			}
			if ((levelValid < minPlayers || inMap < minPlayers) && !cm.getPlayer().isGM())
				next = false;
			if (next) {
				var em = cm.getEventManager("OrbisPQ");
				if (em == null) {
					cm.sendOk("OrbisPQ does not work.");
					cm.dispose();
				} else {
					var prop = em.getProperty("state");
					if (prop == null || prop.equals("0")) {
//						if (cm.checkForAllJobs(cm.getPlayer())) { // WONKY'S BLESSING!
					        var party = cm.getPlayer().getParty().getMembers();
							for (var i = 0; i < party.size(); i++) {
								cm.useItem(2022090, party.get(i).getPlayer());
								cm.useItem(2022091, party.get(i).getPlayer());
								cm.useItem(2022092, party.get(i).getPlayer());
								cm.useItem(2022093, party.get(i).getPlayer());
							}
//						}
						em.setProperty("channel", cm.getPlayer().getClient().getChannel());
						em.startInstance(cm.getParty(), cm.getPlayer().getMap());
					} else {
						cm.sendOk("Someone is already attempting the PQ. Please wait or try a different channel.");
					}
				}
				cm.dispose();
			} else {
				cm.sendOk("Your party is not a party of 6. Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid + " #kmembers are in the right level range, and #b" + inMap + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
				cm.dispose();
			}
		} else if (selection == 2) {
			cm.sendOk("After a heavy rainfall on El Nath Mountains, a new cloud path opened behind the #bStatue of Goddess Minerva#k at the top of Orbis Tower. When a giant cloud far away split open, a mysterious tower appeared. It's the tower of #bGoddess Minerva#k, who ruled Orbis a long time ago. Would you like to begin your adventure at this legendary tower where Goddess Minerva is said to be trapped?\r\n #e - Level:#n 51 ~ 70 \r\n #e - Time Limit:#n 60 min \r\n #e - Players:#n 6 \r\n #e - Reward:#n \r\n#v1082232:# Goddess Wristband #b \r\n(Can be traded for 15 Feathers of Goddess.)#k");
			cm.dispose();
		} else if (selection == 3) {
			cm.sendOk("Whoa, you got something to eat!?!");// add food selections....
			cm.dispose();
		} else if (selection == 4) {// first talks about being a friend and feeding? skip for now i think..
			cm.sendSimple("You are #b#h0#!#k You gave me delicious food! How about I treat you to something today? #b\r\n#L10#Goddess Wristband#k #r(15 Feathers of Goddess required)#k#b#l");
		} else if (selection == 5) {
			cm.openUI(0x16);
			cm.dispose();
		}
	} else if (status == 2) {
    	if (selection == 10) {
    		if (!cm.canHold(1082232)) {
    			cm.getPlayer().dropMessage(1, "Your inventory is full.");
    		} else if (cm.haveItem(4001158, 15)) {
				cm.gainItem(1082232, 1);// Goddess Wristband
				cm.gainItem(4001158, -15);
				cm.sendOk("Thank you so much. And enjoy your new Wristband");
			} else {
				cm.sendOk("Please make sure you have the amount of Feathers needed.");
			}
			cm.dispose();
		}
	} else if (status == 3) {
		cm.openUI(0x16);
		cm.dispose();
	} else if (status == 11) {
		cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
		cm.dispose();
	} else if (mode == 0) {
		cm.dispose();
	}
}