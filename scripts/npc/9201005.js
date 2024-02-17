/**
 * NPC: 9201005 (Assistant Nicole)
 * Description: The Official Nexon MapleStory Assistant Nicole NPC ported from "cathedral" Nexon Script.
*/

var v1 = -1;
var v2 = -1;
var result = -1;
var mapid = 680000210; // Cathedral Wedding Altar

function start() {
    if (cm.getPlayer().getMapId() == 680000000) {
        cm.sendSimple("I can guide you to the Wedding. Which one suits you?\r\n#b#L0#I am now ready to get Married in cathedral.#l\r\n#L1#I am invited to the wedding!#l#k");
    } else if (cm.getPlayer().getMapId() == 680000200) {
        cm.sendYesNo("Would you like to go back outside? ");
    } else { // impossible unless used in a command or spawned improperly in a map
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (cm.getPlayer().getMapId() == 680000200) {
        if (mode < 1) {
            cm.sendOk("Please take a seat, and wait for the ceremony to begin.");
        } else if (mode > 0) {
            cm.warp(680000500, 0); // registerTransferField( 680000500, "" );
            cm.sendOk("Maybe we'll see you at the altar someday. Happy travels! ");
        }
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 680000000) {
        if (mode != 1 && v1 == -1) {
            cm.dispose();
            return;
        } else if (v1 > -1 && v2 > -1) { // have already selected and incremented action
            if (v2 == 1 && mode < 1) {
                cm.sendOk("Well, it looks like this isn't your cup of tea, please stand aside and let others enter. ");
                cm.dispose();
                return;
            }
        }
        (mode == 1 ? v1++ : v1--);
        if (v1 == 0) {
            v2 = selection;
            if (v2 == 0) {
                if (cm.getPlayer().getParty() == null) {
                    cm.sendOk("You need to be in #ba 2-person party with your fiance#k to get married.");
                } else {
                    if (cm.haveItem(4031375, 1) || cm.haveItem(4031480, 1)) { // Normal and Premium Receipts 
                        if(cm.getPlayer().getMarriageRingID() > 0){
                            result = 3;
                            cm.sendOk("You can't get married again.");
						}else if (cm.getPlayer().getMarriedTo() <= 0 || cm.getPlayer().getGender() != 0) {
                            result = 3;
                            cm.sendOk("You need to be in #ba 2-person party with your fiance#k to get married.");
                        } else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()) == null) {
                            result = 4;
                            cm.sendOk("You need to be in #ba 2-person party with your fiance#k to get married.");
                        } else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()).getMapId() != cm.getPlayer().getMapId()) {
                            result = 2;
                            cm.sendOk("You need to be in a 2-person party with your fiance and in #bthe same map#k to get married.");
                        } else if (cm.getPartyMembers().size() > 2 || cm.getPlayer().getParty().getMemberById(cm.getPlayer().getMarriedTo()) == null) {
                            result = 5;
                            cm.sendOk("You need to be in #ba 2-person party with your fiance#k to get married.");
                        } else if (!cm.canHold(1112803)) {
                            result = 6;
                            cm.sendOk("You need an equip slot open to receive the ring, as soon as you make room, we can begin.");
                        } else if (cm.haveItem(4031376, 1) && !cm.haveItem(4031375, 1)) {
                            result = 11;
                            cm.sendOk("It appears that you've made your reservation at another wedding hall");
                        } else if (cm.haveItem(4031381, 1) && !cm.haveItem(4031380, 1)) {
                            result = 11;
                            cm.sendOk("It appears that you've made your reservation at another wedding hall");
                        } else if (!cm.haveItem(4031375, 1) && !cm.haveItem(4031380, 1)) {
                            result = 10;
                            cm.sendOk ("You have to make a reservation to start the wedding");
							cm.dispose();
                        } else {
							var em = cm.getEventManager("CathedralWedding");
							if (em == null) { 
								cm.sendOk("Weddings are currently disabled. Try again at another time.");
								cm.dispose();
								return;
							}
							if(em.getProperty("Open") == "false"){
								result = 1;
								cm.sendOk(" Looks like another Wedding has begun, sweetie. When it finishes, I'll be sure to let you in!");
							}else{
								result = 0;
								var eim = em.startInstance(cm.getParty(), cm.getPlayer().getMap());
								cm.startWedding(eim);
							}
                        }
                    } else {
                        cm.sendOk("Let's see...I'm sorry, but I don't think you have the Reservation Receipt with you right now. Without the Reservation Receipt, I'm afraid I can't help you. You'd better talk to Victoria first Sorry. ");
                    }
                }
                cm.dispose();
            } else if (v2 == 1) {
				if (cm.isInvitedToCurrentWedding(true) && cm.haveItem(4031407, 1)) {
                    cm.sendYesNo("Greetings! I can tell that you're a guest of the Bride and Groom, would you like to enter the Cathedral?");					
				}else{
					cm.sendOk("Oh no, it seems you are not invited to the current wedding.");
                    cm.dispose();
				}
            }
        } else if (v1 == 1) {
            if (cm.haveItem(4031407, 1)) {
                if (cm.isInvitedToCurrentWedding(true)) {//Checks if they are invited
                    //cm.sendOk("I apologize, but the Wedding hasn't started yet. When it does, I'll be sure to let you in. ");
					cm.sendOk("Sorry, but without an invitation, I can't let you in.");
                } else {
					if(cm.getWeddingState() == 1){//Checks if ceremony started
						var wedding = cm.getCurrentWedding(true);
						var eim = wedding.getEIM();
						eim.registerPlayer(cm.getPlayer());
						cm.warp(680000200, 0);
						cm.dispose();
					}else{
						cm.sendOk("Sorry, but it seems the wedding has started, I can't let you in.");
					}
                }
                // else if ( result == 9) self.say ("Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my logbook, please try again later");
            } else {
                cm.sendOk("Sorry, but without an invitation, I can't let you in.");
            }
            cm.dispose();
        }
    }
}  