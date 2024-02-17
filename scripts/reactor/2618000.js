/**
 *	@Description: Romeo & Juliet PQ Beakers
 */
function act() {
    if (rm.getReactor().getCurrState() >= 7) {
		rm.mapMessage(6, "One of the beakers has been completed.");
		var em = rm.getEventManager(rm.getMapId() == 926100100 ? "Romeo" : "Juliet");
		if (em != null && rm.getReactor().getCurrState() >= 7) {
			var react = rm.getPlayer().getMap().getReactorByName(rm.getMapId() == 926100100 ? "rnj2_door" : "jnr2_door");
			em.setProperty("stage3", parseInt(em.getProperty("stage3")) + 1);
			//react.forceHitReactor(react.getState() + 1);
			if (em.getProperty("stage3").equals("3")) {
				clear();
				rm.givePartyQuestExp("MagatiaPQ1");
			}
		}
    }
}

function clear() {
    rm.showEffect("quest/party/clear");
    rm.playSound("Party1/Clear");
}