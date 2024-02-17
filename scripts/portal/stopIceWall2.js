function enter(pi) {
	var map = pi.getPlayer().getMap();
	
	var mob = map.getMonsterById(9300391);
	if(mob == null){
		if(pi.getPlayer().getQuetInfo(22588) !== "1") pi.getPlayer().updateQuestInfo(22588, "1");
	}
	pi.enableActions();
	return true;
}