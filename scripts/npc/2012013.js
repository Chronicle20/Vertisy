/**
 * @Map: Station<To Ludibrium>
 */
var status = 0;

function start() {
    if(cm.haveItem(4031074)) {
        var em = cm.getEventManager("Trains");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("This will not be a short flight, so if you need to take care of some things, I suggest you do that first before getting on board. Do you still wish to board the ship?");
        } else if (em.getProperty("entry").equals("false") && em.getProperty("docked").equals("true")) {
			cm.sendNext("The train is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through #lll#k at the ticketing booth.");
            cm.dispose();
        } else {
			cm.sendNext("We will begin boarding 5 minutes before the takeoff. Please be patient and wait for a few minutes. Be aware that the train will take off right on time, and we stop receiving tickets 1 minute before that, so please make sure to be here on time.");
            cm.dispose();
        }
    } else {
        cm.sendOk("Make sure you got a Ludibrium ticket to travel in this train. Check your inventory.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
		cm.sendOk("Okay, talk to me if you change your mind!");
		cm.dispose();
		return;
	}
	cm.gainItem(4031074, -1);
	cm.warp(200000122);
	cm.dispose();
}