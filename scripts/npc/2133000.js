/**
 * @NPC: Ellin
 * @MapID: 300030100
 * Leaves Ellin Forest PQ
*/

var status = -1;

var minPlayers = 1;//4

var minLevel = 1;//44
var maxLevel = 200;//55

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
	
	var eventManager = cm.getEventManager("EllinForest");
	if (eventManager == null || eventManager != null) {//disabled
		cm.sendOk("Ellin Forest is currently unavailable.");
		cm.dispose();
		return;
	}
	
	if (cm.getPlayer().getParty() == null) {
		cm.sendOk("If you would like to participate in Ellin Forest PQ please enter a party.");
		cm.dispose();
		return;
	}else if (!cm.isLeader() && cm.getPlayer().getMapId() == exitMap) {
		cm.sendOk("Please tell your party leader to speak with me.");
		cm.dispose();
		return;
	}
	var players = cm.getPlayer().getParty().getMembers();
	
	if(players.size() < minPlayers){
		cm.sendOk("You need atleast 4 players to do Ellin Forest PQ");
		cm.dispose();
		return;
	}
	
	for (var i = 0; i < players.size(); i++){
		var player = players.get(i);
		if(player.getLevel() < minLevel || player.getLevel() > maxLevel){
			cm.sendOk("Please make sure everyone in your party is in level range.");
			cm.dispose();
			return;
		}
	}
	
	if(cm.getPlayer().getMapId() == 300030100){
		if(status == 0){
			var eim = eventManager.getInstance("EF");
			if(eim == null){
				eventManager.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap());
			}else{
				cm.sendOk("Another Party is already in the Ellin Forest PQ.");
			}
			cm.dispose();
		}
	}
}