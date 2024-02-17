/** 
 * @author: Eric
 *   [MENTION=806871]NPC[/MENTION]: Robin the Huntress
 *   [MENTION=443547]func[/MENTION]: The Official Nexon MapleStory Robin The Huntress NPC ported from "weddingParty" Nexon Script.
*/

function start() {
    if (cm.getPlayer().getMapId() == 680000300) {
        if (cm.isWeddingCouple() == 1) { // if ( target.isWeddingCouple == 1 ) {
            if (cm.haveItem(4031375, 1) || cm.haveItem(4031376, 1)) {
                cm.sendOk("For your first minute, the wedding photo will be taken automatically. From there, five more minutes will be given to you and your friends to just hang around. When all that is over, your Premium Hunting Event is awaiting you just around the corner! Hang on tight and be ready to fight some monsters before time runs out!");
            } else {
                cm.sendOk(" Congratulations~ "); // self.say (" Congratulations~ ");
            }            
        } else {
            cm.sendOk(" Let's enjoy the party!~ "); // self.say( " Let's enjoy the party!~ ");
        }
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 680000400) {
        if (!cm.haveItem(4031409, 5)) {
            cm.sendOk(" Hmm, looks like you're not done collecting keys. Come back when you've got five! "); // if ( inventory.itemCount( 4031409 ) < 5 ) self.say(" Hmm, looks like you're not done collecting keys. Come back when you've got five! ");
        } else {
            // if( ret == 0) self.say("Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my database, please try again later!!!");
            cm.gainItem(4031409, -5); // ret = inventory.exchange(0, 4031409, -1, 4031409, -1, 4031409, -1, 4031409, -1, 4031409, -1);
            cm.getPlayer().changeMap(680000401); // registerTransferField( 680000401, "" );
            cm.sendNext(" There's only one place I've seen those keys unlock. You're more than worthy having obtained it, head on inside and do your best!");
        }
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 680000401) {
        cm.sendYesNo("Do you want to go back outside?");
    }
}  

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("Ok, please let me know when you're ready to go!");
    } else if (mode > 0) {
        if (cm.getPlayer().getMapId() == 680000401) {
            cm.getPlayer().changeMap(680000500);
        }
        cm.sendOk("Ok, I'll show you the way out. Have fun out there!"); // TODO: real results
    }
    cm.dispose();
}  