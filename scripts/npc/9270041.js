status = -1;
oldSelection = -1;

function start() {
    cm.sendSimple("Hello, I am Irene from Singapore Airport. I can assist you in getting you to Singapore in no time. Do you want to go to Singapore?\r\n#b#L0#I would like to buy a plane ticket to Singapore\r\n#b#L1#Let me go in to the departure point.");
}

function action(mode, type, selection) {
	status++;
    if (mode <= 0){
		oldSelection = -1;
		cm.dispose();
		return;
	}
	
	if (status == 0) {
		if (selection == 0){
			cm.sendYesNo("The ticket will cost you 5,000 mesos. Will you purchase the ticket?");
		} else if (selection == 1) {
			cm.sendYesNo("Would you like to go in now? You will lose your ticket once you go in! Thank you for choosing Wizet Airline.");
		}
		oldSelection = selection;
	} else if (status == 1) {
		if (oldSelection == 0) {
			if (cm.canHold(4031731)) {
				cm.gainMeso(-5000);
				cm.gainItem(4031731);
			} else {
				cm.getPlayer().dropMessage(1, "Your inventory is full.");
			}
			cm.dispose();
		} else if (oldSelection == 1) {
			if (cm.haveItem(4031731)) {
				var em = cm.getEventManager("AirPlane");
				if (em.getProperty("entry") == "true") {
					cm.warp(540010000); // Find the actual map.
					cm.gainItem(4031731, -1);
					cm.dispose();
				} else {
					status = 2;
					cm.sendNext("We will begin boarding 5 minutes before the takeoff. Please be patient and wait for a few minutes. Be aware that the plane will take off right on time, and we stop receiving tickets 1 minute before that, so please make sure to be here on time.");
				}
			} else {
				cm.sendOk("I'm sorry, but you don't have a #t4031731# to get on the plane!");
				cm.dispose();
			}
		}
	} else if (status == 2) {
		cm.sendYesNo("Would you like to be taken straight to #bSingapore - CBD#k?");
	} else if (status == 3) {
		cm.warp(540010000);
		cm.gainItem(4031731, -1);
		cm.dispose();
	} else {
		cm.dispose();
	}
}