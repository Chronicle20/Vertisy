function enter(pi) {
	if(pi.isQuestStarted(22596)){
		var em = pi.getEventManager("EvanRage");
 		if(em.getProperty("state") != "0"){
 			pi.message("It seems somebody may be inside.");
 			pi.enableActions();
 			return true;
 		}else{
 			em.startInstance(pi.getPlayer());
 		}
	}else{
		//this check is fucked and just allows you in.. Too many quests to check for tho
		if (!pi.isQuestStarted(22582) || pi.isQuestStarted(22582))pi.warp(922030000, 1);
	}
	return true;
}