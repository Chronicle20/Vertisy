//Time Setting is in millisecond
var closeTime = 1 * 60 * 1000; //[60 seconds] The time to close the gate
var beginTime = 2 * 60 * 1000; //[120 seconds] The time to begin the ride
var rideTime = 3 * 60 * 1000; //[120 seconds] The time that require move to destination
var KC_Waiting;
var Subway_to_KC;
var KC_docked;
var NLC_Waiting;
var Subway_to_NLC;
var NLC_docked;

function init() {
    KC_Waiting = em.getChannelServer().getMap(600010004);
    NLC_Waiting = em.getChannelServer().getMap(600010002);
    Subway_to_KC = em.getChannelServer().getMap(600010003);
    Subway_to_NLC = em.getChannelServer().getMap(600010005);
    KC_docked = em.getChannelServer().getMap(103000100);
    NLC_docked = em.getChannelServer().getMap(600010001);
    
    var cal = Packages.java.util.Calendar.getInstance();
    cal.setTime(new Packages.java.util.Date());
    var unroundedMins = cal.get(Packages.java.util.Calendar.MINUTE);
    var mod = unroundedMins % 5;
    cal.add(Packages.java.util.Calendar.MINUTE, 5 - mod);
    cal.set(Packages.java.util.Calendar.SECOND, 0);
    cal.set(Packages.java.util.Calendar.MILLISECOND, 0);
    
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    
    em.scheduleAtTimestamp("stopEntry", cal.getTimeInMillis() - 60000);
    em.scheduleAtTimestamp("takeoff", cal.getTimeInMillis());
}

function scheduleNew() {
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    em.setProperty("docked","false");
	KC_Waiting.warpEveryone(Subway_to_NLC.getId());
	NLC_Waiting.warpEveryone(Subway_to_KC.getId());
    em.schedule("arrived", rideTime);
}

function arrived() {
	Subway_to_KC.warpEveryone(KC_docked.getId());
	Subway_to_NLC.warpEveryone(NLC_docked.getId());
    scheduleNew();
}

function cancelSchedule() {
}