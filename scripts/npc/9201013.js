/**
 * NPC: 9201013 (Victoria)
 * Description: The Official Nexon MapleStory Victoria NPC ported from "cathedralCoordinator" Nexon Script.
*/

var result = -1;
var v1 = -1;

function start() {
    cm.sendNext("Before I help you make a reservation for the Cathedral Wedding, I strongly recommend that #bboth you and your partner#k need to have at least #b3 Etc. slots#k available. Please check your etc. inventory.");
}

function action(m,t,s) {
    if (m <= 0) {
        cm.dispose();
        return;
    }
    (m == 1 ? result++ : result--);
    if (result == 0) {
        cm.sendSimple("And by the way, I must tell you, you look wonderful today! I'm here to help you prepare for your Wedding. I can help you make a Reservation, get additional Invitations, or tell you what you'll need to get married in our Cathedral. What would you like to know?\r\n#b#L0#How can I get married here?#l\r\n#L1#I'd like to make a Premium Reservation.#l\r\n#L2#I'd like to make a Normal Reservation.#l\r\n#L3#I have more guests coming, I'd like some more Invitations.#l#k");
    } else if (result == 1) {
        v1 = s;
        if (v1 == 0) {
            cm.sendOk( "To get married in the Cathedral, you'll need #ra Cathedral Wedding Ticket, any Engagement Ring or an Empty Engagement Ring Box and most of all, love#k. Soon as you have them, we'll be happy to assist with your Wedding plans! If you reserved the Cathedral don't forget to see High Priest John for the Officiator's permission." );
            cm.dispose();
        } else if (v1 == 1) {
            if (cm.getPlayer().getParty() == null) {
                cm.sendOk("To make a Reservation, you'll need to be grouped with your fiance.. ");
                cm.dispose();
            } else {
                result = 10; // 10 : Premium Cathedral
                cm.sendNext( "Ready to walk down the aisle. Let's book your reservation now." );
            }
        } else if (v1 == 2) {
            if (cm.getPlayer().getParty() == null) {
                cm.sendOk("To make a Reservation, you'll need to be grouped with your fiance.. ");
                cm.dispose();
            } else {
                result = 11; // 11 : Normal Cathedral
                cm.sendNext( "To make a Reservation, you'll need to be grouped with your fiance, engaged, and ready to walk down the aisle. Let's book your reservation now. " );
            }
        } else if (v1 == 3) {
            if (cm.getPlayer().getParty() == null) {
                cm.sendOk("To receive some more invitations, you'll need to be grouped with your fiance.. ");
                cm.dispose();
			} else {
                result = 20; // 20 : Wedding Invitations
                cm.sendYesNo("That's wonderful! I thought you might need a few more, so here you go. Pass them out to many people as you want! Do you have your #bReservation Receipt and the Wedding Invitation Ticket#k?");
            }
        }
    } else if (result == 11) {
        if (cm.canHold(4031375) && !cm.haveItem(4031375) && !cm.haveItem(4031480)) {
            if (cm.haveItem(5251003, 1)) {
                // retPos = target.hasRequestedTransferWorld; // TODO: Code world transferring once we fully implement multi-world
                // if ( retPos == 0 ) {
                // result = target.makeReservation(10); // 10 : Premium Cathedral
                if(!cm.haveItem(4031374)){
                	cm.sendOk("Talk to High Priest John for the Officators permission first!");
                }else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()) == null || cm.getPlayer().getGender() != 0) {
                    cm.sendOk("You also need to be in a #b2-person party with your fiance#k and in the same map to get married.");
                } else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()).getMapId() != cm.getPlayer().getMapId()) {
                    cm.sendOk("You need to be in a 2-person party with your fiance and in #bthe same map#k to get married.");
                } else if (cm.getPartyMembers().size() > 2 || cm.getPlayer().getParty().getMemberById(cm.getPlayer().getMarriedTo()) == null) {
                    cm.sendOk("You also need to be in a 2-person party with #byour fiance#k and in the same map to get married.");
                }/* else if (cm.haveItem(1112803, 1)) { // TODO: check for reservation id not this lol xD
                    cm.sendOk("Sorry... but your wedding reservation is already done.");
                } */else if (!isEngaged(cm) || cm.getPlayer().getMarriedTo() <= 0) {
                    cm.sendOk("Please remember that you need #ba Cathedral Wedding Ticket, any Engagement Ring or an Empty Engagement Ring Box#k to make a reservation.");
                } else if (!cm.canHold(4031395)) {
                    cm.sendOk("You need an etc slot open to receive the Reservation receipt and Invitations, as soon as you make room, we can begin.");
                } else {
                    cm.gainItem(5251003, -1);
                    cm.makeReservation(true, true); //Premium Cathedral. Sets up MapleWedding instance and marriage id. Gives invites.
                }
                // } else self.say( "Sorry, but you are disabled from getting married." ); 
            } else {
                cm.sendOk(" Looks like you're missing something you need. Please remember that you need #ra Cathedral Wedding Ticket, any Engagement Ring or an Empty Engagement Ring Box#k to make a reservation.");
            }
        } else {
            cm.sendOk("You need an etc slot open to receive the Reservation receipt and Invitations, as soon as you make room, we can begin. Additionally, check if you have an old Reservation receipt.");
        }
        cm.dispose();
    } else if (result == 12) {
        if (cm.canHold(4031480) && !cm.haveItem(4031480) && !cm.haveItem(4031375)) {
            if (cm.haveItem(5251000, 1)) {
                // retPos = target.hasRequestedTransferWorld;
                // if ( retPos == 0 )     {
                // result = target.makeReservation(11); // 11 : Normal Cathedral
                if(!cm.haveItem(4031374)){
                	cm.sendOk("Talk to High Priest John for the Officators permission first!");
                }else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()) == null || cm.getPlayer().getGender() != 0) {
                    cm.sendOk("You also need to be in a #b2-person party with your fiance#k and in the same map to get married.");
                } else if (cm.getPlayer().getClient().getChannelServer().getPlayerStorage().getCharacterById(cm.getPlayer().getMarriedTo()).getMapId() != cm.getPlayer().getMapId()) {
                    cm.sendOk("You need to be in a 2-person party with your fiance and in #bthe same map#k to get married.");
                } else if (cm.getPartyMembers().size() > 2 || cm.getPlayer().getParty().getMemberById(cm.getPlayer().getMarriedTo()) == null) {
                    cm.sendOk("You also need to be in a 2-person party with #byour fiance#k and in the same map to get married.");
                }/* else if (cm.haveItem(1112803, 1)) { // TODO: check for reservation id not this lol xD
                    cm.sendOk("Sorry... but your wedding reservation is already done.");
                } */else if (!isEngaged(cm) || cm.getPlayer().getMarriedTo() <= 0) {
                    cm.sendOk("Please remember that you need #ba Cathedral Wedding Ticket, any Engagement Ring or an Empty Engagement Ring Box#k to make a reservation.");
                } else if (!cm.canHold(4031395)) {
                    cm.sendOk("You need an etc slot open to receive the Reservation receipt and Invitations, as soon as you make room, we can begin.");
                } else {
                    cm.gainItem(5251000, -1);
                    cm.makeReservation(true, false); // Normal Cathedral. Sets up MapleWedding instance and marriage id. Gives invites.
                }
                // } else self.say( "Sorry, but you are disabled from getting married." );
            } else {
                cm.sendOk(" Looks like you're missing something you need. Please remember that you need #ra Cathedral Wedding Ticket, any Engagement Ring or an Empty Engagement Ring Box#k to make a reservation.");
            }
        } else {
            cm.sendOk("You need an etc slot open to receive the Reservation receipt and Invitations, as soon as you make room, we can begin. Additionally, check if you have an old Reservation receipt.");
        }
        cm.dispose();
    } else if (result == 21) {
        if (!cm.haveItem(4031375, 1) && !cm.haveItem(4031480, 1)) {
            cm.sendOk("Oh dear, it looks like you're missing a Cathedral Reservation Receipt. I'm afraid I'll have to postpone those invitations for now. When you get one, be sure to return!");
        } else if (cm.haveItem(4031375, 1) && !cm.haveItem(5251100, 1)) {
            cm.sendOk("Oh dear, it looks like you're missing #ba Wedding Invitation Ticket#k. I'm afraid I'll have to postpone those invitations for now. When you get one, be sure to return!");
        } else if (cm.haveItem(4031480, 1) && !cm.haveItem(5251100, 1)) { // lol nexon
            cm.sendOk("Oh dear, it looks like you're missing #ba Wedding Invitation Ticket#k. I'm afraid I'll have to postpone those invitations for now. When you get one, be sure to return!");
        } else if (cm.haveItem(4031375, 1) && cm.haveItem(5251100, 1) && cm.canHold(4031395)) {
            cm.gainItem(4031395, 15);
			cm.gainItem(5251100, -1);
			cm.gainItem(cm.getPlayer().getPartner(), 4031395, 15, false, false, -1);
            cm.sendOk("Here you are~");
        } else if (cm.haveItem(4031480, 1) && cm.haveItem(5251100, 1) && cm.canHold(4031395)) {
            cm.gainItem(4031395, 15);
			cm.gainItem(5251100, -1);
            cm.gainItem(cm.getPlayer().getPartner(), 4031395, 15, false, false, -1);
            cm.sendOk("Here you are~");
        } else {
            cm.sendOk(" Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my database, please try again later. Please check your inventory is full and come to see me again!!");
        }
        cm.dispose();
    }
}

function isEngaged(cm) {
    var engaged = false;
    if (cm.haveItem(5251000, 1) || cm.haveItem(5251003, 1)) {    
        for (var x = 4031357; x <= 4031364; x++ ) {
            if (cm.haveItem(x, 1)) {
                engaged = true;
                break;
            }
        }
    } else {
        engaged = false;
    }
    return engaged;
}  