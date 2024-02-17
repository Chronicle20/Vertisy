/**
 * @author: Eric
 *   [MENTION=806871]NPC[/MENTION]: Assistant Travis
 *   [MENTION=443547]func[/MENTION]: The Official Nexon MapleStory Assistant Travis NPC ported from "waitingChapel" Nexon Script.
*/

var nRet = 0;
var result = 0;

function start() {
    if (cm.getPlayer().getMapId() == 680000100) {
        cm.sendYesNo("Salutations! Would you like to get into the Wedding Hall? ");
    } else if (cm.getPlayer().getMapId() == 680000110) {
        if (cm.getPlayer().isInParty() && cm.isWeddingCouple() == 1 && cm.getWeddingState() <= 2) {
            cm.sendOk("You both look fantastic! Please, wait the ceremony to end.");
            cm.dispose();
        } else if (cm.getPlayer().isInParty() && cm.isWeddingCouple() == 1) {
            nRet = 2;
            cm.sendYesNo("You both look fantastic! Are you ready to go to the Photo Map? ");
        } else if (cm.isWeddingCouple() == 0) {
            nRet = 3;
            cm.sendYesNo("Hi! Would you like to leave the Chapel? ");
        }
    } else if (cm.getPlayer().getMapId() == 680000300) {
        cm.sendOk("Welcome to Cherished Visage Photo Section! On here, we are taking a picture of you and your guests that will allow you to remember your very own Wedding day forever! The picture will be automatically taken after the two minute timer runs out. So you need to be ready and try out some new poses before it's taken~ ");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (cm.getPlayer().getMapId() == 680000100) {
        (mode > 0 ? cm.warp(680000110, 0) : cm.sendOk("Ok, please let me know when you're ready to go in."));
        if (mode > 0)
            cm.sendOk( "Feel free to head inside now. Give my regards to the newlyweds!");
    } else if (cm.getPlayer().getMapId() == 680000110 && nRet == 2) {
        if (mode < 1) {
            cm.sendOk("Ok, please remember that the Photo time will automatically start after the clock stops.");
        } else {
            result = 1; // 7: Wedding hasn't started 8: Without an invitation, can't let you in 9: Database error
            cm.sendNext("Tubular! Snap some nice shots for the Wedding book!");
        }
    } else if (cm.getPlayer().getMapId() == 680000110 && nRet == 3) {
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