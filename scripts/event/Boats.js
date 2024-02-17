//Time Setting is in millisecond
var closeTime = 4 * 60 * 1000; //The time to close the gate
var beginTime = 5 * 60 * 1000; //The time to begin the ride
var rideTime = 10 * 60 * 1000; //The time that require move to destination
var invasionTime = 2 * 60 * 1000; //The time that spawn balrog
var invasionVariance = 6;
var Orbis_btf;
var Boat_to_Orbis;
var Orbis_Boat_Cabin;
var Orbis_docked;
var Ellinia_btf;
var Ellinia_Boat_Cabin;
var Ellinia_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMap(200000112);
    Ellinia_btf = em.getChannelServer().getMap(101000301);
    Boat_to_Orbis = em.getChannelServer().getMap(200090010);
    Boat_to_Ellinia = em.getChannelServer().getMap(200090000);
    Orbis_Boat_Cabin = em.getChannelServer().getMap(200090011);
    Ellinia_Boat_Cabin = em.getChannelServer().getMap(200090001);
    Ellinia_docked = em.getChannelServer().getMap(101000300);
    Orbis_Station = em.getChannelServer().getMap(200000100);
    Orbis_docked = em.getChannelServer().getMap(200000111);
    OBoatsetup();
    EBoatsetup();
    
    Ellinia_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog","false");
    
    var cal = Packages.java.util.Calendar.getInstance();
    cal.setTime(new Packages.java.util.Date());
    var unroundedMins = cal.get(Packages.java.util.Calendar.MINUTE);
    var mod = unroundedMins % 15;
    cal.add(Packages.java.util.Calendar.MINUTE, 15 - mod);
    cal.set(Packages.java.util.Calendar.SECOND, 0);
    cal.set(Packages.java.util.Calendar.MILLISECOND, 0);
    
    em.scheduleAtTimestamp("stopentry", cal.getTimeInMillis() - 60000);
    em.scheduleAtTimestamp("takeoff", cal.getTimeInMillis());
}

function scheduleNew() {
    Ellinia_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    Ellinia_docked.broadcastMessage(Packages.tools.MaplePacketCreator.boatPacket(true));
    Orbis_docked.broadcastMessage(Packages.tools.MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog","false");
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopentry() {
    em.setProperty("entry","false");
    Orbis_Boat_Cabin.resetReactors();
    Ellinia_Boat_Cabin.resetReactors();
}



function takeoff() {
    em.setProperty("docked","false");
	Orbis_btf.warpEveryone(Boat_to_Ellinia.getId());
	Ellinia_btf.warpEveryone(Boat_to_Orbis.getId());
    Ellinia_docked.setDocked(false);
    Orbis_docked.setDocked(false);
    Ellinia_docked.broadcastMessage(Packages.tools.MaplePacketCreator.boatPacket(false));
    Orbis_docked.broadcastMessage(Packages.tools.MaplePacketCreator.boatPacket(false));
    em.schedule("invasion", invasionTime + (60 * 1000 * Math.random() * invasionVariance));
    em.schedule("arrived", rideTime);
}

function arrived() {
	Boat_to_Orbis.warpEveryone(Orbis_Station.getId());
	Orbis_Boat_Cabin.warpEveryone(Orbis_Station.getId());
	Boat_to_Ellinia.warpEveryone(Ellinia_docked.getId());
	Ellinia_Boat_Cabin.warpEveryone(Ellinia_docked.getId());
    Boat_to_Orbis.killAllMonsters();
    Boat_to_Ellinia.killAllMonsters();
    scheduleNew();
}

function invasion() {
	var chance = Math.random() * 10;
	var numspawn = chance > 5 ? 2 : 0;
     if(numspawn > 0) {
        for(var i=0; i < numspawn; i++) {
            Boat_to_Orbis.spawnMonsterOnGroundBelow(Packages.server.life.MapleLifeFactory.getMonster(8150000), new java.awt.Point(485, -221));
            Boat_to_Ellinia.spawnMonsterOnGroundBelow(Packages.server.life.MapleLifeFactory.getMonster(8150000), new java.awt.Point(-590, -221));
        }
        Boat_to_Orbis.setDocked(true);
        Boat_to_Ellinia.setDocked(true);
        Boat_to_Orbis.broadcastMessage(Packages.tools.MaplePacketCreator.contiMoveShip(true));
        Boat_to_Ellinia.broadcastMessage(Packages.tools.MaplePacketCreator.contiMoveShip(true));
        em.setProperty("haveBalrog","true");
    }
}

function OBoatsetup() {
    em.getChannelServer().getMap(200090011).getPortal("out00").setScriptName("OBoat1");
    em.getChannelServer().getMap(200090011).getPortal("out01").setScriptName("OBoat2");
}

function EBoatsetup() {
    em.getChannelServer().getMap(200090001).getPortal("out00").setScriptName("EBoat1");
    em.getChannelServer().getMap(200090001).getPortal("out01").setScriptName("EBoat2");
}

function cancelSchedule() {
}