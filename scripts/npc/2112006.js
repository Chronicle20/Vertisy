/**
 *	@Name: Romeo
 *	@Author: iPoopMagic (David)
 */
function start() {
    var em = cm.getEventManager("Romeo");
	if (em == null) {
		cm.sendOk("The event is null, please contact a GM immediately.");
		cm.dispose();
		return;
	}
    switch(cm.getPlayer().getMapId()) {
		case 926100200:
			var prop = parseInt(em.getProperty("stage4"));
			if (cm.haveItem(4001130, 1)) {
				cm.sendOk("Oh, the Letter I wrote! Thank you!");
				cm.gainItem(4001130, -1);
				em.setProperty("stage", "1");
			} else if (cm.haveItem(4001134, 1)) {
				cm.gainItem(4001134, -1);
				cm.sendOk("Thank you! Now please find the Zenumist files.");
				em.setProperty("stage4", "1");
				if (em.getProperty("stage4").equals("2")) {
					cm.getPlayer().getMap().getReactorByName("rnj3_out3").hitReactor(cm.getClient());
				}
				cm.getPlayer().getEventInstance().getMapInstance(926100200).getPortal("in00").setPortalState(false);
				cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getReactorByName("rnj3_out1").setState(0);
			} else if (cm.haveItem(4001135, 1)) {
				cm.gainItem(4001135, -1);
				cm.sendOk("Thank you! Now please continue.");
				em.setProperty("stage4", "2");
				clear();
				cm.givePartyQuestExp("MagatiaPQ2");
				if (em.getProperty("stage4").equals("2")) {
					cm.getPlayer().getMap().getReactorByName("rnj3_out3").hitReactor(cm.getClient());
				}
				cm.getPlayer().getEventInstance().getMapInstance(926100200).getPortal("in01").setPortalState(false);
				cm.getPlayer().getEventInstance().getMapInstance(cm.getPlayer().getMapId()).getReactorByName("rnj3_out2").setState(0);
			} else if (em.getProperty("stage4").equals("2")) {
				em.setProperty("stage4", "2");
				cm.getPlayer().getMap().getReactorByName("rnj3_out3").hitReactor(cm.getClient());				
			} else {
				cm.sendOk("We must stop the conflict between Alcadno and Zenumist! Find me Alcadno files first, then Zenumist!");
			}
			break;
		case 926100600:
			cm.openNpc(2112018);
			break;
		case 926110600:
			cm.sendOk("Thank you for helping my love save me. A true hero in a tragedy. Please speak to her, I believe she has something for you.");
			cm.dispose();
			break;
    }
    cm.dispose();
}

function clear() {
    cm.showEffect("quest/party/clear");
    cm.playSound("Party1/Clear");
}