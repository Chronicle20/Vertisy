/**
 * NPC: 92010000 (Moony)
 * Description: The Official Nexon MapleStory Moony NPC ported from "EngageRing" Nexon Script.
*/

var ret = -1;
var proofOfLoves = true;
// The fee multiplier for mesos. | 1 = Same price as GMS
var fee = 1000;

function start() {
    cm.sendSimple("Have you found true love? If so, I can make you a ring worthy of your devotion...\r\n#b#L0#I would like to make an engagement ring for my lover.");//\r\n#L1#I want an annulment.#l#k");
}

function action(m, t, choosemenu) {
    if (m <= 0) {
        cm.dispose();
        return;
    }
    (m == 1 ? ret++ : ret--);
    if (ret == 0) {
        if (choosemenu == 0) {
            // if female user clicks to make a ring
            //if (cm.getPlayer().getGender() == 1) cm.sendOk ( "Let's see... I only make rings for guys who want to get married.");
            // if a user whose level is less than 10 clicks to make a ring
            if ( cm.getPlayer().getLevel() < 10 )    cm.sendOk ( "Let's see... I don't think you are strong enough. You'll have to be at least #bLevel 10#k to get married." );
            else {
                if (checkQuestRecord(cm) == "end") {
                    // if a user who got Engagement ring 
                    cm.sendOk( "Hey, I've already given you an engagement ring already!");
                } else if (checkQuestRecord(cm) == "ing") { // lol nexon o.o
                    var canMakeRing = false;
					var count = 0;
					for (var i = 0; i <= 5; i++) {
						if (cm.haveItem(4031367 + i, 1)) {
							count++;
						}
					}
					// if a user comes without enough (4) "proof of love"s.
					if (count < 4) {
						cm.sendOk("Looks like you're not quite done. If you want that ring, you'd better hurry and get me the following all materials to make an engagement ring. I need you to bring me #b4 Proof of Love#k. One from each Nana");
						canMakeRing = false;
						cm.dispose();
					} else {
						ret = -1;
						// if a user comes with enough "proof of love"s.
						//cm.sendNext ("Wow, I am impressed! Your mate is a very lucky person to have someone so willing to prove their love. I am honored to make you an engagement ring.");
						canMakeRing = true;
					}
                    if (canMakeRing) {
                        // explanation
                        ret = 9;
                        cm.sendNext ("You need the following raw materials to make an engagement ring.\r\n#b#eMoonstone Ring:#k\r\n #v4011007# #t4011007# 1, #v4021007# #t4021007# 1, 30,000,000 Meso \r\n#b#eStar gem ring:#k\r\n #v4021009# #t4021009# 1, #v4021007# #t4021007# 1, 20,000,000 Meso \r\n#b#eGolden Heart Ring:#k\r\n #v4011006# #t4011006# 1, #v4021007# #t4021007# 1, 10,000,000 Meso \r\n#b#eSilver Swan Ring:#k\r\n #v4011004# #t4011004# 1, #v4021007# #t4021007# 1, 5,000,000 Meso");
                    }
                } else { // -.-
                    cm.sendYesNo("So you want a special ring, eh? Well you've come to the right guy. My rings are for those who want to truly prove their love. Are you up for that?");
                }
            }
        }
    } else if (ret == 10) {
        cm.sendSimple(" What kind would you like?\r\n#b#L0##v2240000# #t2240000##l\r\n#L1##v2240001# #t2240001##l\r\n#L2##v2240002# #t2240002##l\r\n#L3##v2240003# #t2240003##l\r\n#L4#I don't want to pick one right now.#l#k");
    } else if (ret == 11) {
        v1 = choosemenu;
        if (v1 == 0) { // moonrock
            if (cm.haveItem(4011007, 1) && cm.haveItem(4021007, 1) && cm.canHold(2240000) && cm.getMeso() >= 30000000) {
                // user succeeded to get a moonrock engagement ring
                cm.gainItem(4011007, -1); // Moon Rock
                cm.gainItem(4021007, -1); // Diamond
                cm.gainMeso(-(30000 * fee));
                cm.gainItem(2240000, 1);
                cm.gainExp(2360);
                cm.sendOk( "Here is your well-earned, hand-crafted engagement ring. It is worthy of the love shared between the both of you-I hope she accepts your proposal. It always pleasures me to help two lovebirds. Good luck!");
            } else {
                // if a user doesn't fulfill items to make a ring 
                cm.sendOk ( "Looks like you're not quite done. If you want that ring, you'd better hurry!" );
            }
            cm.dispose();
        } else if ( v1 == 1 ) {    //starrock
            if (cm.haveItem(4021009, 1) && cm.haveItem(4021007, 1) && cm.canHold(2240001) && cm.getMeso() >= 20000000) {
                cm.gainItem(4021009, -1); // Star Rock
                cm.gainItem(4021007, -1); // Diamond
                cm.gainMeso(-(20000 * fee));
                cm.gainItem(2240001, 1);
                cm.gainExp(2360);
                cm.sendOk( "Here is your well-earned, hand-crafted engagement ring. It is worthy of the love shared between the both of you-I hope she accepts your proposal. It always pleasures me to help two lovebirds. Good luck!");
            } else {
                cm.sendOk ( "Looks like you're not quite done. If you want that ring, you'd better hurry!" );
            }
            cm.dispose();
        } else if ( v1 == 2 ) {    //gold
            if (cm.haveItem(4011006, 1) && cm.haveItem(4021007, 1) && cm.canHold(2240002) && cm.getMeso() >= 10000000) {
                cm.gainItem(4011006, -1); // Gold Plate
                cm.gainItem(4021007, -1); // Diamond
                cm.gainMeso(-(10000 * fee));
                cm.gainItem(2240002, 1);
                cm.gainExp(2360);
                cm.sendOk( "Here is your well-earned, hand-crafted engagement ring. It is worthy of the love shared between the both of you-I hope she accepts your proposal. It always pleasures me to help two lovebirds. Good luck!");
            } else {
                cm.sendOk ( "Looks like you're not quite done. If you want that ring, you'd better hurry!" );
            }
            cm.dispose();
        } else if ( v1 == 3 ) {    //silver
            if (cm.haveItem(4011004, 1) && cm.haveItem(4021007, 1) && cm.canHold(2240003) && cm.getMeso() >= 5000000) {
                cm.gainItem(4011004, -1); // Silver Plate
                cm.gainItem(4021007, -1); // Diamond
                cm.gainMeso(-(5000 * fee));
                cm.gainItem(2240003, 1);
                cm.gainExp(2360);
                cm.sendOk( "Here is your well-earned, hand-crafted engagement ring. It is worthy of the love shared between the both of you-I hope she accepts your proposal. It always pleasures me to help two lovebirds. Good luck!");
            } else {
                cm.sendOk ( "Looks like you're not quite done. If you want that ring, you'd better hurry!" );
            }
            cm.dispose();
        } else if (v1 == 4) { // no
            cm.sendOk( "Ok, feel free to return at anytime and I will give you the ring you want.");
            cm.dispose();
        }
    }
}

function checkQuestRecord(cm) {
    if (cm.getPlayer().getEngagementRingID() > 0 || cm.getPlayer().getMarriedTo() > 0) {
        return "end";
    }
    for (var i = 0; i <= 5; i++) {//Empty ring boxes
        if (cm.haveItem(4031357 + i, 1)) {
            return "ing";
        }
    }
    for (var i = 0; i <= 8; i++) {//Filled ring boxes
        if (cm.haveItem(4031357 + i, 1)) {
            return "end";
        }
    }
    return "ing"; //return "start";
}  