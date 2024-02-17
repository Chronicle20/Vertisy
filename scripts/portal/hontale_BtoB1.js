function enter(pi) {
    
	var eim = pi.getPlayer().getEventInstance();
	
	if(pi.hasItem(4001087)){
		eim.setProperty("maze1", "1");
		pi.gainItem(4001087, -1);
	}/*else if(pi.hasItem(4001088)){
		eim.setProperty("maze2", "1");
		pi.gainItem(4001088, -1);
	}else if(pi.hasItem(4001089)){
		eim.setProperty("maze3", "1");
		pi.gainItem(4001089, -1);
	}else if(pi.hasItem(4001090)){
		eim.setProperty("maze4", "1");
		pi.gainItem(4001090, -1);
	}else if(pi.hasItem(4001091)){
		eim.setProperty("maze5", "1");
		pi.gainItem(4001091, -1);
	}*/
	
	if(eim.getProperty("maze5") == "1"){
		pi.warp(240050105, "sp");
	}else if(eim.getProperty("maze4") == "1"){
		pi.warp(240050104, "sp");
	}else if(eim.getProperty("maze3") == "1"){
		pi.warp(240050103, "sp");
	}else if(eim.getProperty("maze2") == "1"){
		pi.warp(240050102, "sp");
	}else if(eim.getProperty("maze1") == "1"){
		pi.warp(240050101, "sp");
	}else{
		pi.getPlayer().dropMessage(6, "Horntail's Seal is blocking this door.");
	}
	pi.enableActions();
	
	/*var nextMap = 240050101;
    var eim = pi.getPlayer().getEventInstance();
    // only let people through if the eim is ready
    var avail = eim.getProperty("1stageclear");
    if (!pi.haveItem(4001087, 1)) {
        // do nothing; send message to player
        pi.getPlayer().dropMessage(6, "Horntail's Seal is blocking this door.");
        return false;
    }else {
        pi.gainItem(4001087, -1);
        pi.getPlayer().dropMessage(6, "The key disentegrates as Horntail\'s Seal is broken for a flash...");
        pi.warp(nextMap, "sp");
        return true;
    }*/
}