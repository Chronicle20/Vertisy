function act() {
	var total = Integer.parseInt(rm.getEventManager("OrbisPQ").getProperty("stage6levers"));
	rm.mapMessage("Total:" + total);
	if (rm.getReactor().getState() == 0) {
		rm.getReactor().setState(1);
		total--;
		rm.getEventManager("OrbisPQ").setProperty("stage6levers", String.valueOf(total));
	} else {
		rm.getReactor().setState(0);
		total++;
		rm.getEventManager("OrbisPQ").setProperty("stage6levers", String.valueOf(total));
	}
}