/**
 * @author: Eric
 * NPC: 9201006 (Assistant Debbie)
 * Script: The Official Nexon MapleStory Assistant Debbie NPC ported from "watingCathedral" Nexon Script.
*/

var nRet = 0;
var result = 0;

function start() {
    if (cm.getPlayer().getMapId() == 680000200) {
        cm.sendYesNo("Salutations! Would you like to get into the Wedding Hall? ");
    } else if (cm.getPlayer().getMapId() == 680000210) {
        if (cm.getPlayer().isInParty() && cm.isWeddingCouple() == 1 && cm.getWeddingState() <= 2) {
            cm.sendOk("You both look fantastic! Please, wait the ceremony to end.");
            cm.dispose();
        } else if (cm.getPlayer().isInParty() && cm.isWeddingCouple() == 1) {
            nRet = 2;
            cm.sendYesNo("You both look fantastic! Are you ready to go to the Photo Map? ");
        } else if (cm.isWeddingCouple() == 0) {
            nRet = 3;
            cm.sendYesNo("Hi! Would you like to leave the Cathedral? ");
        }
    } else if (cm.getPlayer().getMapId() == 680000300 && cm.isWeddingCouple() == 1) { // not in Nexon's "waitingCathedral" script.
        cm.sendOk("Welcome to Cherished Visage Photo Section! On here, we are taking a picture of you and your guests that will allow you to remember your very own Wedding day forever! The picture will be automatically taken after 1 minute timer runs out. So you need to be ready and try out some new poses before it's taken~ To see the picture that was taken, please visit [url=http://dchaosms.net/]D.ChaosMS[/url] for more details.");
        cm.dispose();
    } else if (cm.getMapId() == 680000300 && cm.isWeddingCouple() == 0) {
        cm.sendYesNo("Do you want to go back outside?");
    }
}

function action(mode, type, selection) {
    if (cm.getPlayer().getMapId() == 680000200) {
        (mode > 0 ? cm.warp(680000210, 0) : cm.sendOk("Ok, please let me know when you're ready to go in."));
        if (mode > 0)
            cm.sendOk( "Feel free to head inside now. Give my regards to the newlyweds!");
    } else if (cm.getPlayer().getMapId() == 680000300 && cm.isWeddingCouple() == 0) {
        (mode > 0 ? cm.warp(680000500, 0) : cm.sendOk("Ok, please let me know when you're ready to go!"));
        if (mode > 0)
            cm.sendOk( "Ok, I'll show you the way out. Have fun out there!");
    } else if (cm.getPlayer().getMapId() == 680000210 && nRet == 2) {
        if (mode < 1) {
            cm.sendOk("Ok, please remember that the Photo time will automatically start after the clock stops.");
        } else {
            result = 1; // 7: Wedding hasn't started 8: Without an invitation, can't let you in 9: Database error
            cm.sendNext("Sounds good, take some good pictures. Off you go!");
        }
    } else if (cm.getPlayer().getMapId() == 680000210 && nRet == 3) {
        (mode < 1 ? cm.sendOk("Ok, please let me know when you're ready to go out.") : cm.warp(680000500, 0));
        if (mode > 0)
            cm.sendOk("Ok, I'll show you the way out..."); // result++
    } else if (result > 0) {
        // set = FieldSet( "Wedding30" ); // OH, this is saveLocation in OdinMS.. never realized that!
        // result = set.enter( target.nCharacterID, 0 );
        if (result == 1) {
            cm.warp(680000300, 0);
        } else if (result == 7) {
            cm.sendOk("I apologize, but the Wedding hasn't started yet. When it does, I'll be sure to let you in. ");
        } else if (result == 8) {
            cm.sendOk("Sorry, but without an invitation, I can't let you in.");
        } else if (result == 9) {
            cm.sendOk("Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my logbook, please try again later");
        }
        cm.dispose();
    }
    if (result == 0) {
        cm.dispose();
    }
}  
