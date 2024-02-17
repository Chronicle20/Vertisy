function enter(pi) {
	switch(pi.getPlayer().getMapId()){
		case 930000100:{ 
			if (pi.getPlayer().getMap().getMonstersEvent(pi.getPlayer()).size() < 1) {
				pi.warpParty(pi.getPlayer().getMapId() + 100);
			} else {
				pi.playerMessage(5, "You must kill all monsters before you can progress.");
			}
			break;
		}
		case 930000200:{
			if(pi.getPlayer().getMap().getReactorByName("spine").getCurrState() == 4){
				pi.warpParty(pi.getPlayer().getMapId() + 100);
			}
			break;
		}
	}
}