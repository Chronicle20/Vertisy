var exitMap;
var loungeMap;
var altarMap;
var photoMap;
var keyMap;
var boxMap;

var instanceid;

var vows = ["Dearly beloved, we are gathered here today to celebrate the marriage of these two fine, upstanding people. One can clearly see the love between you two, and it's a sight I'll never tire of. You have proved your love and received your Parent's Blessing. Do you wish to seal your love in the eternal embrace of marriage?", "Very well. Guests may now Bless the couple if they choose.", "By the power vested in me through the mighty Maple tree. I now pronounce you Husband and Wife. You may kiss the bride!", "With the Blessing of the Maple tree, I wish both of you a long and safe marriage."];

function init(){
	em.setProperty("Open", "true"); // allows entrance.
	loungeMap = em.getChannelServer().getMap(680000200);
	altarMap = em.getChannelServer().getMap(680000210);
	photoMap = em.getChannelServer().getMap(680000300);
	keyMap = em.getChannelServer().getMap(680000400);
	boxMap = em.getChannelServer().getMap(680000401);
	exitMap = em.getChannelServer().getMap(680000500);
	instanceid = 1;
}

function setup(party){
	em.setProperty("Open", "false");
	var eim = em.newInstance("CathedralWedding" + instanceid);
	eim.setProperty("weddingid", "" + party.getMembers().get(0).getPlayer().getMarriageID());
	eim.setProperty("vow", "0");
	instanceid++;
    var timer = 1000 * 60 * 20; //20 minutes
    em.schedule("altarTimeOut", eim, timer);
    eim.startEventTimer(timer);
	em.schedule("autoCeremony", eim, 1000 * 60 * 10);
    return eim;
}


function playerEntry(eim, player) {


}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.changeMap(exitMap);
}

function playerDead(eim, player){}

function playerDisconnected(eim, player){
	playerExit(eim, player);
}

function leftParty(eim, player){}

function disbandParty(eim){}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    if(player.getMapId() != 680000500)player.changeMap(exitMap, exitMap.getPortal(0));
	player.removeClock();
	if(eim.getPlayerCount() == 0){
		eim.removeCurrentWedding(true);
		em.setProperty("Open", "true");
	}
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function changedMap(eim, player, mapid){
	if(mapid < 680000200 || mapid > 680000401){
		playerExit(eim, player);
	}
}

function monsterValue(eim, mobId) {
    return 1;
}

function allMonstersDead(eim){}

function dispose() {
    em.cancelSchedule();
}

function altarTimeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            if(eim.getWeddingState() == 3){
				var length = 1000 * 60 * 5;
				em.schedule("photoTimeOut", eim, length);
				eim.startEventTimer(length);
				var pIter = eim.getPlayers().iterator();
				while (pIter.hasNext()){
					var chr = pIter.next();
					if(chr.getMapId() == 680000210){
						chr.changeMap(photoMap);
					}
				}
				eim.setWeddingState(4);
			}
        }else{
			eim.dispose();
		}
    }
}

function autoCeremony(eim){
	if(eim.getWeddingState() == 1){//Hasn't started
		eim.setWeddingState(2);
		eim.schedule("vow", 0);
	}
}

function vow(eim){
	var curVow = parseInt(eim.getProperty("vow"));
	if(curVow < 4){
		eim.dropMessage(6, vows[curVow]);
		curVow++;
		eim.setProperty("vow", "" + curVow);
	}
	if(curVow < 4){
		em.schedule("vow", eim, 1000);
	}else{
		eim.endCeremony();
	}
}

function photoTimeOut(eim){
	if(eim.getWeddingState() == 4){
        if (eim.getPlayerCount() > 0) {
			var premium = eim.getWedding().isPremium();
			if(premium){
				var time = 1000 * 60 * 10; //10 minutes
				em.schedule("boxTimeOut", eim, time);
				eim.startEventTimer(time);
			}
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()){
                var chr = pIter.next();
				if(chr.getMapId() == 680000300){
					chr.changeMap(premium ? keyMap : exitMap);
				}
			}
			eim.setWeddingState(5);
			if(!premium){
				eim.dispose();
			}
        }else{
			eim.dispose();
		}
	}
}

function boxTimeOut(eim){
	if(eim.getWeddingState() == 5){
        if (eim.getPlayerCount() > 0) {
			var premium = eim.getWedding().isPremium();
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()){
                var chr = pIter.next();
				if(chr.getMapId() == 680000400 || chr.getMapId() == 680000401){
					chr.changeMap(exitMap);
				}
			}
        }
        eim.dispose();
	}
}

function cancelSchedule(eim){}