/**
 * @NPC: Guon
 * @Description: Pirate PQ Entrance
 * @Author: iPoopMagic (David)
 */
var minLevel = 55;
var maxLevel = 100;
var minPlayers = 3;
var maxPlayers = 4;
var lobbyMapId = 251010404;

function start() {
	if (cm.getPlayer().getMapId() != lobbyMapId) {
		cm.sendSimple("#e <Party Quest: Lord Pirate> #r(Required Level: Lv. 55 ~ 100)#k#n \r\nHow would you like to complete a quest by working with your party members? Inside, you will find many obstacles that you will have to overcome with the help of your party members.#b\r\n#L0#Go to the Pirate PQ Lobby.#l");
	} else if (cm.getPlayer().getMapId() == lobbyMapId) {
		cm.sendSimple("#e <Party Quest: Lord Pirate> #r(Required Level: Lv. 55 ~ 100)#k#n \r\nThe pirate ship is near.. how can I help you? \r\n#rPlease note that only the regular PQ is available, you may not be able to spawn the harder Lord Pirate until further notice.#k#b\r\n#L1#I want to participate in the Party Quest.#l\r\n#L2#I want to listen to the explanation.#l\r\n#L3#I want to get the #rLord Pirate Hat#k.#l");
	} else {
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (mode < 1) {
		cm.dispose();
		return;
	} else {
		if (selection == 0) {
			cm.saveLocation("MIRROR");
			cm.warp(lobbyMapId, 0);
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
			if ((party.size() < minPlayers || party.size() > maxPlayers) && !cm.getPlayer().isGM()) {
				next = false;
			} else {
				for (var i = 0; i < party.size() && next; i++) {
					if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
						levelValid += 1;
					if (party.get(i).getMapId() == mapId)
						inMap += 1;
				}
				if ((levelValid < minPlayers || inMap < minPlayers) && !cm.getPlayer().isGM())
					next = false;
			}
			if (next) {
				var em = cm.getEventManager("PiratePQ");
				if (em == null) {
					cm.sendOk("PiratePQ does not work.");
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
				cm.sendOk("Your party is not a party of 3, or is more than 4.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
				cm.dispose();
			}
		} else if (selection == 2) {
			cm.sendOk("#e#b#m251000000##k#n, where Bellflowers live, has been attacked by the #r#o9300119##k, and #e#b#p2094001##k#n, the king of the Bellflowers, has been kidnapped. Gather your allies and attack the pirate ship to drive the #o9300119# and his men away.\r\n #e - Level:#n 55 ~ 100\r\n #e - Players:#n 3 - 6 \r\n #e - Reward:#n#v1002571:##b Lord Pirate Hat#k");
			cm.dispose();
		} else if (selection == 3) {
			cm.sendSimple("Thank you for saving #e#b#p2094001##k#n from the #r#o9300119##k. As a reward, I will make you a #bLord Pirate Hat#k if you bring me Lost Treasures. Now, exactly which hat would you like?" +
			"#b\r\n#L10#(Lv. 60) Lord Pirate's Hat\r\n#r(Need 30 Lost Treasures)#k#l\r\n" +
			"#b\r\n#L11#(Lv. 70) Lord Pirate's Hat\r\n#r(Need 1 (Lv. 60) Lord Pirate's Hat,\r\n 80 Lost Treasures)#k#l\r\n" +
			"#b\r\n#L12#(Lv. 80) Lord Pirate's Hat\r\n#r(Need 1 (Lv. 70) Lord Pirate's Hat,\r\n 200 Lost Treasures)#k#l\r\n" +
			"#b\r\n#L13#(Lv. 90) Lord Pirate's Hat\r\n#r(Need 1 (Lv. 80) Lord Pirate's Hat,\r\n 350 Lost Treasures)#k#l");
			// for some reason, #v1002573:# doesn't work
		} else if (selection == 10) {
			if (!cm.canHold(1002571)) {
				cm.sendOk("You do not have enough space in your inventory for this hat.");
			} else if (cm.haveItem(4001455, 30)) {
				cm.gainItem(1002571, 1);
				cm.sendOk("Thank you very much.");
			} else {
				cm.sendOk("It seems like you don't have #r30 Lost Treasures#k! Come back when you get them all.");
			}
			cm.dispose();
		} else if (selection == 11) {
			if (!cm.canHold(1002572)) {
				cm.sendOk("You do not have enough space in your inventory for this hat.");
			} else if (cm.haveItem(4001455, 80) && cm.haveItem(1002571, 1)) {
				cm.gainItem(1002571, -1);
				cm.gainItem(1002572, 1);
				cm.sendOk("Thank you very much.");
			} else {
				cm.sendOk("It seems like you don't have #r80 Lost Treasures#k and/or a #r(Lv. 60) Lord Pirate's Hat#k!  Come back when you get them all.");
			}
			cm.dispose();
		} else if (selection == 12) {
			if (!cm.canHold(1002573)) {
				cm.sendOk("You do not have enough space in your inventory for this hat.");
			} else if (cm.haveItem(4001455, 200) && cm.haveItem(1002572, 1)) {
				cm.gainItem(1002573, 1);
				cm.gainItem(1002572, -1);
				cm.sendOk("Thank you very much.");
			} else {
				cm.sendOk("It seems like you don't have #r200 Lost Treasures#k and/or a #r(Lv. 70) Lord Pirate's Hat#k! Come back when you get them all.");
			}
			cm.dispose();
		} else if (selection == 13) {
			if (!cm.canHold(1002574)) {
				cm.sendOk("You do not have enough space in your inventory for this hat.");
			} else if (cm.haveItem(4001455, 350) && cm.haveItem(1002573, 1)) {
				cm.gainItem(1002574, 1);
				cm.gainItem(4001455, -350);
				cm.gainItem(1002573, -1);
				cm.sendOk("Thank you very much.");
			} else {
				cm.sendOk("It seems like you don't have #r350 Lost Treasures#k and/or a #r(Lv. 80) Lord Pirate's Hat#k! Come back when you get them all.");
			}
			cm.dispose();
		}
	}
}