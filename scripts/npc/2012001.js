/**
 * @NPC: Rini
 * @Map: Station<To Ellinia>
 */
var status = 0;

function start() {
    if(cm.haveItem(4031047)){
        var em = cm.getEventManager("Boats");
        if (em.getProperty("entry") == "true") {
			cm.sendYesNo("It looks like there's plenty of room for this ride. Please have your ticket ready so I can let you in. The ride will be long, but you'll get to your destination just fine. What do you think? Do you want to get on this ride?");
        } else if (em.getProperty("entry").equals("false") && em.getProperty("docked").equals("true")) {
			cm.sendNext("The ship is getting ready for takeoff. I'm sorry, but you'll have to get on the next ride. The ride schedule is available through the usher at the ticketing booth.");
            cm.dispose();
        } else {
            cm.sendNext("We will begin boarding 5 minutes before the takeoff. Please be patient and wait for a few minutes. Be aware that the ship will take off on time, and we stop receiving tickets 1 minute before that, so please make sure to be here on time.");
            cm.dispose();
        }
    } else {
        cm.sendOk("Make sure you got a Ellinia ticket to travel in this boat. Check your inventory.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
	if (mode <= 0) {
		cm.sendOk("Okay, talk to me if you change your mind!");
		cm.dispose();
		return;
    }
	cm.gainItem(4031047, -1);
	cm.warp(200000112);
	cm.dispose();
}