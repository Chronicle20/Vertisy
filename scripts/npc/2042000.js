/**
 * @NPC: Spiegelmann
 * @MapID: 980000000 (Spiegelmann's Office)
*/

var status = -1;
var exitMap = 980000000;

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
	
	var eventManager = cm.getEventManager("MonsterDefense");
	if (eventManager == null) {
		cm.sendOk("Monster Defense is currently unavailable.");
		cm.dispose();
		return;
	}
	
	if (cm.getPlayer().getParty() == null) {
		cm.sendOk("If you would like to participate in Monster Defense please enter a party.");
		cm.dispose();
		return;
	}else if (!cm.isLeader() && cm.getPlayer().getMapId() == exitMap) {
		cm.sendOk("Please tell your party leader to speak with me.");
		cm.dispose();
		return;
	}
	
	var players = cm.getPlayer().getParty().getMembers();
    var overLevelTen = 0;
    var cooldown;
	for (var i = 0; i < players.size(); i++){
		var player = players.get(i);
		if(player.getLevel() >= 10){
			overLevelTen++;
		}
		var playerChr = player.getPlayerInChannel();
		if(playerChr != null && playerChr.isProgressValueSet("md_last")){
			var enterTime = parseInt(playerChr.getProgressValue("md_last"));
			if((Packages.java.lang.System.currentTimeMillis() - enterTime) < 10 * 60 * 1000){
				if(cooldown != null)cooldown += "\r\n";
				else cooldown = "";
				cooldown += player.getName() + " - " + Packages.tools.StringUtil.getReadableMillis(Packages.java.lang.System.currentTimeMillis(), enterTime + 10 * 60 * 1000);
			}
		}
	}
	
	if(overLevelTen != players.size()){
		cm.sendOk("You must be atleast level 10 to enter Monster Defense.");
		cm.dispose();
		return;
	}
	
	if(cooldown != null){
		cm.sendOk("The following players still have a 10 minute cooldown till they can enter Monster Defense.\r\n\r\n" + cooldown);
		cm.dispose();
		return;
	}
	
	if(cm.getPlayer().getMapId() == exitMap){
		if(status == 0){
			var selStr = "Select the Field you would like to join.";
			var hasRoomAvailable = false;
			for (var i = 0; i <= 1; i++) {
				var room = getRoom(i);
				if (room != "") {
					selStr += "\r\n#L" + i + "# " + room + "#l";
					hasRoomAvailable = true;
				}
			}
			if(hasRoomAvailable){
				cm.sendSimple(selStr);
			}else{
				cm.sendOk("No fields are available at this time.");
				cm.dispose();
				return;
			}
		}else if(status == 1){
			if(selection == 0 || selection == 1){
				var eim = eventManager.getInstance("MD_" + selection);
				if(eim == null){
					eventManager.startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap(), null, selection);
				}else if(eim.getProperty("started").equals("false")){
					eim.registerParty(cm.getPlayer().getParty(), cm.getPlayer().getMap());
				}else{
					cm.sendOk("Someone has already entered that field.");
				}
				cm.dispose();
			}
		}
	}else{
		//not in entrance
	}
}


function getRoom(roomNum) {
    var status = "";
    var eventManager = cm.getEventManager("MonsterDefense");
    if (eventManager != null) {
        var event = eventManager.getInstance("MD_" + roomNum);
        if (event == null) {
            status = "Field " + roomNum;
        } else if (event != null && (event.getProperty("started").equals("false"))) {
            var averagelevel = 0;
            for (i = 0; i < event.getPlayerCount(); i++) {
                averagelevel += event.getPlayers().get(i).getLevel();
            }
            averagelevel /= event.getPlayerCount();
            status = "Field " + roomNum + " - Players: " + event.getPlayerCount();
        }
    }
    return status;
}