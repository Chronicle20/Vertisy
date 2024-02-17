function act() {
    var eim = rm.getEventManager("HenesysPQ").getInstance("HenesysPQ");
    if (eim !== null) {
        var HPQ = Java.type("server.partyquest.HPQ");
        rm.mapMessage(6, "The purple seed has sprouted a flower.");
        HPQ.incrementMoonState(eim);
    }
}