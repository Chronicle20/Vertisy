function enter(pi) {
	if(pi.isQuestStarted(22556)){
		if(pi.getPlayer().getQuestInfo(22556) != "1")pi.getPlayer().updateQuestInfo(22556, "" + 1);
		pi.message("There's suspicious-looking puppet in this building. It seems to be locked. You cen't enter");
	}else if(pi.isQuestStarted(22557)){
		var em = pi.getEventManager("EvanDoll");
		if(em.getProperty("state") != "0"){
			pi.message("It seems somebody may be inside.");
			pi.enableActions();
			return true;
		}else{
			em.startInstance(pi.getPlayer());
		}
	}else if(pi.isQuestStarted(22559)){//should have a timer btw idc
		pi.warp(910600010, 1);
	}
	pi.enableActions();
	return true;
}