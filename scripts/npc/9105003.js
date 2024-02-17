/**
 *	@Name: Snow Spirit
 *	@Description: Entrance to the PQ (supposed to be quest guide, but it's not that time of year...)
 *	@Author: iPoopMagic (David)
 */
var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 3;
var maxPlayers = 6;

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
	var em = cm.getEventManager("HolidayPQ");
    if (status == 0) {
		if (!cm.haveItem(
		cm.sendSimple("#e <Party Quest: Save the Snowman!>#n \r\n#r(Required Level: 10+)#k\r\nScrooge is hatching up a plan to ruin Maplemas. We need adventurers to help bring Scrooge's plans to a halt!#b\r\n#L1#Start the quest.#l\r\n#L2#Find a party.#l\r\n#L3#Listen to the Snow Spirit's story.#l#k");
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
			cm.sendOk("Help defend Happyville and save Maplemas!\r\n #e - Level:#n 10+ \r\n #e - Time Limit:#n 10 min \r\n #e - Number of Players:#n 3 or more \r\n #e - Rewards: \r\n#v4001159:# Zenumist Marble \r\n#v4001160:# Alcadno Marble \r\n #bOther Random Useable Items.");
			cm.dispose();
		}
	} else if (status == 2 || status == 3) { 
		cm.openUI(0x16);
		cm.dispose();         
	} else if (mode == 0) { 
		cm.dispose();
	} 
}