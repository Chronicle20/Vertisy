function enter(pi) {
	var eim = pi.getPlayer().getEventInstance();
	var hard = parseInt(eim.getProperty("hard"));
	var endmap = (hard == 1 ? 970031401 : 970032200);
    if(pi.getPlayer().getMapId() == endmap){//Completed
    	if(pi.getPlayer().getMap().getMobCount() == 0 || !pi.getPlayer().getMap().containsAnAliveBoss()){
        	pi.gainCashMap((hard == 1 ? 0 : 250));
        	if(hard == 1)pi.gainItemMap(4310000, 5);
	    	var party = eim.getPlayers();
	        for (var i = 0; i < party.size(); i++)
	            eim.removePlayer(party.get(i));
		}
    }else{
    	if(pi.getPlayer().getMap().getMobCount() == 0 || !pi.getPlayer().getMap().containsAnAliveBoss()){
    		var stage = (pi.getPlayer().getMapId() - 970030000 - hard) / 100;
    		var nx = 0;
    		if(hard == 1){
    			nx = stage * 20;
    			if(stage != 0 && stage % 3 == 0){
    				pi.gainItemMap(4310000, 1);
    			}
    		}else{
	    		/*if(stage <= 5)nx = 50;
	    		else if(stage <= 10)nx = 50;
	    		else if(stage <= 15)nx = 100;
	    		else if(stage <= 20)nx = 150;
	    		else if(stage <= 26)nx = 200;*/
	    		nx = stage * 5;
    		}
        	pi.gainCashMap(nx);
    		pi.warpMap(pi.getPlayer().getMapId() + 100);
    	}else{
    		pi.enableActions();
    		return false;
    	}
    }
	return true;
}