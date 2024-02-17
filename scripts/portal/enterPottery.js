function enter(pi) {
	if(pi.isQuestStarted(22408)){
		var em = pi.getEventManager("Pottery");
 		if(em.getProperty("state") != "0"){
 			pi.message("It seems somebody may be inside.");
 			pi.enableActions();
 			return true;
 		}else{
 			em.startInstance(pi.getPlayer());
 		}
	}
	return true;
}