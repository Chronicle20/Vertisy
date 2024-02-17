function enter(pi) {
	if (pi.isQuestStarted(22580))pi.warp(914100020, 1);
	if(pi.isQuestStarted(22589)){
		var em = pi.getEventManager("EvanCaveAttack");
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