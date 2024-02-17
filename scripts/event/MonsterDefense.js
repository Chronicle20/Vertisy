
var map;
var exitMap = 980000000;
var mapId = 200;

var mobLimit = 20;

//0 = Red?
//1 = Blue?
var pos_x = Array(-1554, 1551, -276, 270);
var pos_y = Array(110, 110, 120, 120);

var redPartyId = -1;
var redMobId;
var redMobAverage;
var redPartySize;

var bluePartyId = -1;
var blueMobId;
var blueMobAverage;
var bluePartySize;

var keepSpawning = true;

function init() {
	em.setProperty("state", "1");
}

function setup(roomID) {
	em.setProperty("state", "1");
	
	var eim = em.newInstance("MD_" + roomID, false);
	eim.setProperty("roomID", roomID);
	eim.setProperty("started", "false");
	map = em.getChannelServer().getMap(mapId + parseInt(eim.getProperty("roomID")));
	map.clearAndReset(true);
	
	return eim;
}

var team = 0;
function playerEntry(eim, player) {
	if(eim.getProperty("started").equals("false")){
		if(redPartyId == -1 || player.getParty().getId() == redPartyId){
			redPartyId = player.getParty().getId();
			player.getParty().resetMonsterKills();//reset kill count
			redPartyId = player.getParty().getId();
			redMobAverage = player.getParty().getAverageLevel();
			redPartySize = player.getParty().getPartySize();
		}else if(redPartyId != player.getParty().getId()){
			team = 1;
			bluePartyId = player.getParty().getId();
			player.getParty().resetMonsterKills();//reset kill count
			bluePartyId = player.getParty().getId();
			blueMobAverage = player.getParty().getAverageLevel();
			bluePartySize = player.getParty().getPartySize()
			eim.setProperty("started", "true");
			eim.schedule("doStart", 1000);
		}
	}
	player.changeMap(map, map.getPortal(team));
	//player.setTeam(team);
}

function doStart(eim){
	//eim.sendClock(5);
	eim.schedule("start", 5000); // 5 seconds
	map.broadcastMessage(Packages.tools.MaplePacketCreator.showEffect("praid/clear"));
	eim.broadcastPlayerMsg(5, "A Party has entered, starting in 5 seconds.");
}

function start(eim){
	for (var i = 0; i < players.size(); i++){
		var player = players.get(i);
		if(player != null)player.getPlayerInChannel().setProgressValue("md_last", Packages.java.lang.System.currentTimeMillis());
	}
	redMobId = eim.getMobByAverageLevel(redMobAverage);
	blueMobId = eim.getMobByAverageLevel(blueMobAverage);
	keepSpawning = true;
	eim.sendClock(10 * 60);
	eim.schedule("finish", 10 * 60 * 1000);
	respawn(eim);
}

function respawn(eim){
	var players = eim.getPlayers();
	var teamsChecked = 0;
	for (var i = 0; i < players.size(); i++){
		var player = players.get(i);
		if(player.isInParty()){
			if(player.getParty().getId() == redPartyId){
				redMobAverage = player.getParty().getAverageLevel();
				teamsChecked++;
			}else{
				blueMobAverage = player.getParty().getAverageLevel();
				teamsChecked++;
			}
		}
		if(teamsChecked == 2)break;
	}
	
	var mobs = map.getMonsters();
	var redMobs = map.getMonsterSizeOnTeam(0);
	var blueMobs = map.getMonsterSizeOnTeam(1);
	
	if(mobLimit - redMobs > 0){
		spawn(eim, 0, redPartySize, redMobId, redMobAverage);
	}
	if(mobLimit - blueMobs > 0){
		spawn(eim, 1, bluePartySize, blueMobId, blueMobAverage);
	}
	
	if(keepSpawning)eim.schedule("respawn", 1000);//make shit respawn again
}

function spawn(eim, team, partySize, mobid, teamAverage){
	var mob = Packages.server.life.MapleLifeFactory.getMonster(mobid, partySize);
	mob.setTeam(team);
	mob.disableDrops();
	eim.registerMonster(mob);
	var rmid = parseInt(eim.getProperty("roomID"));
	map.spawnMonsterOnGroundBelow(mob, new Packages.java.awt.Point(pos_x[team + ((rmid) * 2)], pos_y[team + ((rmid) * 2)]));
	//map.spawnMonsterOnGroundBelow(mob, new Packages.java.awt.Point(spawn_pos[0][0], spawn_pos[0][1]));
}

function finish(eim){
	keepSpawning = false;
	map.killAllMonsters();
	if(eim.getPlayerCount() > 0){
		var highestKills = 0;
		var winnerPartyId;
		var winnerLeader;
		
		var loserLeader;
		var loserKills;

		var redLowestLevel = 200;
		var blueLowestLevel = 200;
		
		var draw = false;
		
		var players = eim.getPlayers();
		for (var i = 0; i < players.size(); i++){
			var player = players.get(i);
			if(player.isInParty()){
				if(player.getParty().getId() == redPartyId){
					if(player.getLevel() < redLowestLevel){
						redLowestLevel = player.getLevel();
					}
				}else{
					if(player.getLevel() < blueLowestLevel){
						blueLowestLevel = player.getLevel();
					}
				}
				var kills = player.getParty().getMonsterKillsWorld();
				if(kills > highestKills){
					highestKills = kills;
					winnerPartyId = player.getParty().getId();
					winnerLeader = player.getParty().getLeader().getName();
				}
			}
		}
		if(loserKills == highestKills)draw = true;
		
		var redExp = 0;
		if(redLowestLevel >= 130)redExp = 700000;
		else if(redLowestLevel >= 110)redExp = 400000;
		else if(redLowestLevel >= 90)redExp = 200000;
		else if(redLowestLevel >= 70)redExp = 45000;
		else if(redLowestLevel >= 50)redExp = 20000;
		else if(redLowestLevel >= 30)redExp = 10000;
		else if(redLowestLevel >= 20)redExp = 1500;
		else if(redLowestLevel >= 10)redExp = 300;
		
		var blueExp = 0;
		if(blueLowestLevel >= 130)blueExp = 700000;
		else if(blueLowestLevel >= 110)blueExp = 400000;
		else if(blueLowestLevel >= 90)blueExp = 200000;
		else if(blueLowestLevel >= 70)blueExp = 45000;
		else if(blueLowestLevel >= 50)blueExp = 20000;
		else if(blueLowestLevel >= 30)blueExp = 10000;
		else if(blueLowestLevel >= 20)blueExp = 1500;
		else if(blueLowestLevel >= 10)blueExp = 300;
		
		if(draw){
			redExp *= 1.05;
			blueExp *= 1.05;
		}else{
			if(winnerPartyId == redPartyId){
				redExp *= 1.05;
			}else{
				blueExp *= 1.05;
			}
		}
		
		
		for (var i = 0; i < players.size(); i++){
			var player = players.get(i);
			if(player.isInParty()){
				if(player.getParty().getId() != winnerPartyId){
					loserLeader = player.getParty().getLeader().getName();
					loserKills = player.getParty().getMonsterKillsWorld();
				}
				if(player.getParty().getId() == redPartyId){
					player.gainExp("SCRIPT", redExp, "MonsterDefense", true);
				}else if(player.getParty().getId() == bluePartyId){
					player.gainExp("SCRIPT", blueExp, "MonsterDefense", true);
				}
			}
		}
		
		for (var i = 0; i < players.size(); i++){
			var player = players.get(i);
			var playerI = map.getCharacterById(player.getId());
			if(playerI != null && playerI.isInParty()){
				if(draw){
					playerI.announce(Packages.tools.MaplePacketCreator.showEffect("praid/timeout"));
				}else{
					if(playerI.getParty().getId() == winnerPartyId){
						playerI.announce(Packages.tools.MaplePacketCreator.showEffect("quest/carnival/win"));
					}else{
						playerI.announce(Packages.tools.MaplePacketCreator.showEffect("quest/carnival/lose"));
					}
				}
			}
		}
		if(draw){
			eim.broadcastPlayerMsg(5, "Both parties have tied with " + highestKills + " monster kills!");
		}else{
			eim.broadcastPlayerMsg(5, winnerLeader + "'s party has won with " + highestKills + " monster kills!");
			eim.broadcastPlayerMsg(5, loserLeader + "'s party has lost with " + loserKills + " monster kills!");
		}
		eim.schedule("reset", 10000);
		eim.broadcastPlayerMsg(5, "Exiting in 10 seconds.");
		map.killAllMonsters();
	}else{
		reset(eim);
	}
}


function reset(eim){
	map.killAllMonsters();
	redPartyId = -1;
	bluePartyId = -1;
	team = 0;
	em.setProperty("started", "false");
	eim.disposeIfPlayerBelow(100, exitMap);
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	var map = em.getChannelServer().getMap(exitMap);
    player.changeMap(map, map.getPortal(0));
	if(eim.getPlayerCount() <= 1){
		reset(eim);
	}
	var players = eim.getPlayers();
    for (var i = 0; i < players.size(); i++){
    	var pParty = players.get(i).getParty();
    	if(pParty != null && pParty.getOnline() <= 0){
    		playerExit(eim, players.get(i));
    	}
    }
}

function playerDead(eim, player) {
	playerRevive(eim, player);//?
}

function playerRevive(eim, player) {
	player.addHP(player.getMaxHp());
	var tm = 0;
	if(player.getParty().getId() == bluePartyId){
		tm = 1;
	}
	//player.changeMap(map, map.getPortal(tm));
	player.getMap().broadcastMessage(player, Packages.tools.packets.CUserPool.removePlayerFromMap(player.getId()), false);
	player.getMap().broadcastMessage(player, Packages.tools.packets.CUserPool.spawnPlayerMapobject(player), false);
	return true;
}

function leftParty(eim, player) {
    playerExit(eim, player);
}

function disbandParty(eim) {
	var players = eim.getPlayers();
    for (var i = 0; i < players.size(); i++)
        playerExit(eim, players.get(i));
}

function removePlayer(eim, player) {
	playerExit(eim, player);
}

function playerDisconnected(eim, player){
	playerExit(eim, player);
}

function changedMap(eim, player, mapid){
	if(mapid != (mapId + parseInt(eim.getProperty("roomID")))){
		playerExit(eim, player);
	}
}


function monsterValue(eim, mobid){
	return 0;
}
function allMonstersDead(eim) {}
function cancelSchedule() {}