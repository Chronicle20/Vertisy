/**
 * @NPC: The Ticket Gate
 * @Map: NLC Subway Station (600010001)
 */
var status = -1;
var close = false;
var oldSelection = -1;

function start() {
	var text = "Here's the ticket reader.";
	var hasTicket = false;
	if (cm.haveItem(4031713) && cm.getPlayer().getMapId() == 600010001){
		text += "\r\n#b#L0##t4031713#";
		hasTicket = true;
	}
	if (!hasTicket) {
		cm.sendOk("It seems you don't have a ticket! You can buy one from Bell.");
		cm.dispose();
	} else {
		cm.sendSimple(text);
	}
}

function action(mode, type, selection) {
	if (mode > 0) {
		status++;
	} else {
		cm.sendNext("You must have some business to take care of here, right?");
		cm.dispose();
		return;
	}
	if (status == 0) {
		if (selection == 0) {
			var em = cm.getEventManager("Subway");
			if (em.getProperty("entry") == "true") {
				cm.sendYesNo("It looks like there's plenty of room for this ride. Please have your ticket ready so I can let you in. The ride will be long, but you'll get to your destination just fine. What do you think? Do you wants to get on this ride?");
            } else if(em.getProperty("entry").equals("false") && em.getProperty("docked").equals("true")) {
				status = 2;
				cm.sendNext("The subway is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through the usher at the ticketing booth.");
			} else {
				status = 2;
				cm.sendNext("We will begin boarding 1 minute before the takeoff. Please be patient and wait for a few minutes. Be aware that the subway will take off right on time, and we stop receiving tickets 1 minute before that, so please make sure to be here on time.");
				cm.dispose();
			}
			oldSelection = selection;
		}
	} else if(status == 1) {
		if (oldSelection == 0 && cm.haveItem(4031713)) {
			cm.gainItem(4031713, -1);
			cm.warp(600010002);
		} else {
			cm.sendNext("Sorry, you need a ticket to enter!");
		}
		cm.dispose();
	} else if (status == 4) {
		if (cm.haveItem(4031713)) {
		   	cm.gainItem(4031713, -1);
	        cm.warp(103000100);
	    	cm.dispose();
	    	return;
		}
	}
}