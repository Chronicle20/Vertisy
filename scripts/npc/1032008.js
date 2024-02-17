/**
 *	@Map: Ellinia Station
 */
var status = 0;

function start() {
    if(cm.haveItem(4031045)){
        var em = cm.getEventManager("Boats");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("This will not be a short flight, so if you need to take care of some things, I suggest you do that first before getting on board. Do you still wish to board the ship?");
        } else if (em.getProperty("entry").equals("false") && em.getProperty("docked").equals("true")) {
			cm.dispose();
			cm.sendNext("The ship is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through #bJoel#k at the ticketing booth.");
		} else {
			cm.sendNext("We will begin boarding 10 minutes before the takeoff. Please be patient and wait for a few minutes. Be aware that the ship will take off right on time, and we stop receiving tickets 1 minute before that, so please make sure to be here on time.");
            cm.dispose();
        }
	} else {
		cm.sendOk("Make sure you got a Orbis ticket to travel in this boat. Check your inventory.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (mode <= 0) {
		cm.sendOk("Okay, talk to me if you change your mind!");
		cm.dispose();
		return;
	}
    cm.gainItem(4031045, -1);
    cm.warp(101000301);
    cm.dispose();
}