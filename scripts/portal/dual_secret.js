function enter(pi) {
	if(pi.isQuestStarted(2369)){
		pi.getPlayer().updateQuestInfo(2369, "");
		var em = pi.getEventManager("DualSecret");
		if(em.getProperty("state") != "0"){
			pi.message("It seems somebody may be inside.");
			pi.enableActions();
			return true;
		}else{
			em.startInstance(pi.getPlayer());
		}
	}
	pi.enableActions();
	return true;
}