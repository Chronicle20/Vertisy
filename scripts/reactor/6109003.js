function act() {
	var em = rm.getEventManager("CWKPQ");
	if (em != null) {
		if (rm.getPlayer().getMapId() == 610030200) {
			rm.mapMessage(6, "The Thief Sigil has been activated!");
			var next = parseInt(em.getProperty("glpq2")) + 1;
			em.setProperty("glpq2", rm.convertToString(next));
			if (em.getProperty("glpq2").equals("5")) {
				rm.mapMessage(6, "All Sigils activated. The Antellion grants you access to the next portal! Proceed!");
				rm.getPlayer().getMap().environmentChange("2pt", 2);
			}
		} else if (rm.getPlayer().getMapId() == 610030300) {
			rm.mapMessage(6, "The Thief Sigil has been activated! You hear gears turning! The Menhir Defense System is active! Run!");
			var next = parseInt(em.getProperty("glpq3")) + 1;
	    	em.setProperty("glpq3", rm.convertToString(next));
			rm.getPlayer().getMap().environmentMove("menhir4", 1);
	    	if (em.getProperty("glpq3").equals("10")) {
				rm.mapMessage(6, "All Sigils activated. The Antellion grants you access to the next portal! Proceed!");
				rm.getPlayer().getMap().environmentChange("3pt", 2);
	    	}
		}
	}
}