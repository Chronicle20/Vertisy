function act() {
	var monster = rm.getPlayer().getMap().getMonsterById(6090004);
	
	if(monster != null){
		rm.getPlayer().getMap().killMonster(monster, null, false);
	}
}