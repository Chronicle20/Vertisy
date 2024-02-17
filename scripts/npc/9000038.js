/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;

function start(){
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
		
		
	if(cm.getPlayer().getMapId() == 970030000){
		var hard = cm.getPlayer().getLevel() > 119 ? true : false;
		var party = cm.getPartyMembers();
		if (cm.getParty() == null || party == null || !cm.isLeader()) {
       		cm.sendOk("If you'd like to enter here, the leader of your party will have to talk to me");
			cm.dispose();
			return;
		}
        
        for (var i = 0; i < party.size(); i++) {
            if (party.get(i).getMap().getId() != 970030000) {
                cm.sendOk("A member of your party is not presently in the map.");
                cm.dispose();
                return;
            }
            if(hard && party.get(i).getLevel() < 120){
            	cm.sendOk("A member of your party does not meet the level requirement for Hard BossPQ.");
                cm.dispose();
                return;
            }else if(!hard && party.get(i).getLevel() > 119){
            	cm.sendOk("A member of your party does not meet the level requirement for Normal BossPQ.");
                cm.dispose();
                return;
            }
        }
		
		var em = cm.getEventManager("BossPQ");
	    if (em == null) { 
	        cm.sendOk("BossPQ is currently disabled. Try again at another time.");
	        cm.dispose();
	        return;
	    }
		
		if(status == 0){
			var hardInt = (hard ? 1 : 0);
			var currentEIM = em.getInstance("BossPQ_" + hardInt + "_" + cm.getClient().getChannel());
			if(currentEIM === null || currentEIM === undefined){
	            var prop = em.getProperty("state");
	            if (prop == null || prop.equals("0")) { //Start the PQ
					em.setProperty("hard", (hard ? "1" : "0"));
	                var eim = em.startInstance(cm.getParty(), cm.getPlayer().getMap());
	            }
            }else{
            	cm.sendOk("Someone is already in " + (hard ? "hard" : "easy") + " BossPQ on this channel.");
            }
	        cm.dispose();
		}
	}else{
		if(status == 0){
			cm.sendYesNo("Leaving so early?");
		}else{
			cm.getPlayer().changeMap(970030000);
			cm.getPlayer().removeClock();
			cm.dispose();
		}
	}
}