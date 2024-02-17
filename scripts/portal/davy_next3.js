function enter(pi) {
	if (pi.getPlayer().getEventInstance().getMapInstance(pi.getPlayer().getMapId()).getMonstersEvent(pi.getPlayer()).size() < 1) {
		pi.warpParty(925100400);
		pi.givePartyQuestExp("PiratePQ4");
    } else {
		pi.playerMessage(5, "The portal is not opened yet.");
    }
}