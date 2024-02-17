//Time Setting is in millisecond
var closeTime = 4 * 60 * 1000; //The time to close the gate
var beginTime = 5 * 60 * 1000; //The time to begin the ride
var rideTime = 5 * 60 * 1000; //The time that require move to destination
var KC_bfd;
var Plane_to_CBD;
var CBD_docked;
var CBD_bfd;
var Plane_to_KC;
var KC_docked;

function init() {
    KC_bfd = em.getChannelServer().getMap(540010100);
    CBD_bfd = em.getChannelServer().getMap(540010001);
    Plane_to_CBD = em.getChannelServer().getMap(540010101);
    Plane_to_KC = em.getChannelServer().getMap(540010002);
    CBD_docked = em.getChannelServer().getMap(540010000);
    KC_docked = em.getChannelServer().getMap(103000000);
    
    var cal = Packages.java.util.Calendar.getInstance();
    cal.setTime(new Packages.java.util.Date());
    var unroundedMins = cal.get(Packages.java.util.Calendar.MINUTE);
    var mod = unroundedMins % 10;
    cal.add(Packages.java.util.Calendar.MINUTE, 10 - mod);
    cal.set(Packages.java.util.Calendar.SECOND, 0);
    cal.set(Packages.java.util.Calendar.MILLISECOND, 0);
    
    em.scheduleAtTimestamp("stopEntry", cal.getTimeInMillis() - 60000);
    em.scheduleAtTimestamp("takeoff", cal.getTimeInMillis());
}

function scheduleNew() {
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("entry", "true");
	KC_bfd.warpEveryone(Plane_to_CBD.getId());
	CBD_bfd.warpEveryone(Plane_to_KC.getId());
    em.schedule("arrived", rideTime);
    scheduleNew();
}

function arrived() {
	Plane_to_CBD.warpEveryone(CBD_docked.getId());
	Plane_to_KC.warpEveryone(KC_docked.getId());
}



function cancelSchedule() {
}