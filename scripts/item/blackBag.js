function start(im) {
	if(im.getPlayer().getMapId() == 922030011){
		im.useSummoningBag(2100166);
		im.gainItem(2430032, -1);
	}else im.dispose();
	return true;
}