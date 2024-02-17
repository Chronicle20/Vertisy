function start(ms) {
	var map = ms.getPlayer().getMap();
	
	var mob = map.getMonsterById(9300391);
	if(mob == null){
		map.spawnMonsterOnGroundBelow(9300391, 125, -76);
	}
}