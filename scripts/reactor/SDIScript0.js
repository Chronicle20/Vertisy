function act() {
	var map = rm.getPlayer().getMap();
	
	var mob = map.getMonsterById(9300391);
	if(mob != null){
		map.killMonster(mob, null, false);
	}
}