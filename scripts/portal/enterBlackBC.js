function enter(pi) {
	if (pi.isQuestStarted(22583)){
		var em = pi.getEventManager("EvanSafe");
		if(em.getProperty("state") != "0"){
			pi.message("It seems somebody may be inside.");
			pi.enableActions();
			return true;
		}else{
			em.startInstance(pi.getPlayer());
		}
	}else if(pi.isQuestStarted(22584)){
		var em = pi.getEventManager("EvanSafeBlock");
		if(em.getProperty("state") != "0"){
			pi.message("It seems somebody may be inside.");
			pi.enableActions();
			return true;
		}else{
			em.startInstance(pi.getPlayer());
		}
	}else pi.warp(220011001);
	return true;
}