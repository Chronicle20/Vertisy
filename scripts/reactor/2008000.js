function act() {
	var em = rm.getEventManager("OrbisPQ");
	if (em != null) {
		var stage = parseInt(em.getProperty("stageS")) + 1;
		var react = rm.getReactor().getMap().getReactorByName("minerva");
		react.setState(stage);
		var newStage = stage.toString();
		em.setProperty("stageS", newStage);
		rm.mapMessage(6, "The Statue of Goddess : 3rd Piece has been placed.");
		if (em.getProperty("stageS").equals("6")) {
			rm.getPlayer().dropMessage(5, "You need the Grass of Life in order to save the Goddess Minerva.");
		}
	}
}