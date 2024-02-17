function start() {
	if(cm.getPlayer().getMap().getMonsterById(5090000) != null){
	    cm.sendOk("The light has destroyed the darkness.");
	    cm.getPlayer().getMap().killMonster(cm.getPlayer(), cm.getPlayer().getMap().getMonsterById(5090000), false);
	    cm.dispose();
	} else {
	    cm.sendOk("The light is strong.");
	    cm.dispose();
	}
}