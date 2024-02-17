function start() {
    cm.sendSimple("You will be moved to the #b#m106021401##k. Where would like to go?#b\r\n#L0#1. Bringing Down King Pepe (Party : 2-6 / Level : 30 or higher)#l\r\n#L1#2. Saving Violetta (Solo only / Level : 30 or higher)#l#k");
}

function action(mode, type, selection) {
    if (mode == 1) { //or 931000400 + selection..?
		switch(selection) {
			case 0:
				if (cm.getPlayer().getParty() == null || !cm.isLeader()) {
					cm.sendOk("The leader of the party must be here.");
				} else {
					var party = cm.getPlayer().getParty().getMembers();
					var mapId = cm.getPlayer().getMapId();
					var next = true;
					var size = 0;
					var it = party.iterator();
					while (it.hasNext()) {
						var cPlayer = it.next();
						var ccPlayer = cm.getPlayer().getMap().getCharacterById(cPlayer.getId());
						if (ccPlayer == null) {
							next = false;
							break;
						}
						size += (ccPlayer.isGM() ? 4 : 1);
					}
					var kppq = cm.getEventManager("KingPepePQ");
					if (kppq == null) {
						cm.sendOk("The event has not started yet, please contact a GM.");
						cm.dispose();
						return;
					}
					if (next && (cm.getPlayer().isGM() || size >= 1)) {
						for (var i = 0; i < 10; i++) {
							if (kppq.getInstance("KingPepePQ_" + i) == null) {
								if (cm.getMap(106021500 + i).getCharacters().size() == 0) {
									cm.getEventManager("KingPepePQ").startInstance(cm.getPlayer().getParty(), cm.getPlayer().getMap(), cm.getClient().getChannelServer().getMap(106021500 + i), true);
									//cm.warpParty(106021500 + i);
									cm.dispose();
									return;
								}
							} else {
								// Do nothing
							}
						}
						cm.sendOk("Another party has already entered this channel.");
					} else {
//						cm.sendOk("All 3+ members of your party must be here.");
						cm.sendOk("Something wrong has occurred. Please report this to a GM immediately.");
					}
				}
				break;
			case 1:
				if (!cm.getPlayer().haveItem(4032388)) {
					cm.sendOk("You do not have the #rWedding Hall Key#k. #bKing Pepe#k still has it.");
					cm.dispose();
					return;
				}
				cm.warp(106021401,0);
				break;
		}
    }
    cm.dispose();
}