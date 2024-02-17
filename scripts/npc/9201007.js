/**
 * @author: Eric
 * NPC: Assistant Nancy
 * Script: The Official Nexon MapleStory Assistant Nancy NPC ported from "beginCeremony" Nexon Script.
*/

function start() {
    if (cm.getPlayer().isInParty() && cm.getWeddingState() == 1) {
        // if(inventory.slotCount( 1 ) > inventory.holdCount( 1 ) and inventory.slotCount( 4 ) > inventory.holdCount( 4 )){
        if (cm.canHold(1112803) && cm.canHold(4031424)) {
            cm.sendYesNo( "You two both look fantastic! Are you ready to begin the Wedding Ceremony? ");
        } else {
            cm.sendOk("You need an equip slot and an etc slot open to receive #bthe Ring and the Onyx Chest#k, as soon as you make room, we can begin.");
            cm.dispose();
        }
    } else if (cm.getParty() == null && cm.isWeddingCouple() == 1) {
        cm.sendOk("You both look fantastic!");
        cm.dispose();
    } else {
        cm.sendOk(" Please wait for the ceremony to begin.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("Ok, please remember that the ceremony will automatically start 10 minutes after you enter the Cathedral.");
    } else if (mode > 0) { // Completely unknown beyond this point, Nexon runs a serversided function to run the wedding timers and system functions
        if (cm.getWeddingState() == 1) {
            cm.startCeremony();
        } else {
            cm.sendOk ("Please wait for the ceremony to end");
        }
    }
    cm.dispose();
}  