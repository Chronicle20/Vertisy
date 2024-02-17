/**
 * NPC: 9201002 (High Priest John)
 * Description: The Official Nexon MapleStory High Priest John NPC ported from "HighPriest" Nexon Script.
*/

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}


function action(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (cm.getMapId() == 680000000) {
		if(status == 0){
			if(cm.getPlayer().getMarriedTo() > 0 && cm.getPlayer().getEngagementRingID() > 0 && cm.getPlayer().getMarriageRingID() == 0){
				if(!cm.getPlayer().isProgressValueSet("marriage")){
					cm.sendYesNo("Ah, there is seldom a sight more beautiful than two people in love. I can see that you want to get married. Have you got your Parent's Blessing yet? It is important that your parents give their blessing for a happy marriage. Do you wish to go visit your parents now?");
					status = 9;
				}else{
					if(cm.haveItem(4031373, 1)){
						cm.gainItem(4031373, -1);
						cm.gainItem(4031374, 1);
						cm.getPlayer().setProgressValue("marriage", "permission");
						cm.sendOk("I see a smile on your face...you received your Parent's Blessing, didn't you? Great! Now, take the Officiator's Permission. You'll need to get married in cathedral. See you at the wedding!");
						cm.dispose();
					}else{
						cm.sendOk("You still need your parents blessing, my friend. True Love knows no bounds, head out there and obtain your Parent's Blessing.");
						cm.dispose();
					}
				}
			}else{
				if(cm.getPlayer().getMarriageRingID() > 0){
					cm.sendOk("You are already married!");
					cm.dispose();
					//Married, and talking to him
				}else{
					cm.sendOk("You are currently not engaged.");
					cm.dispose();
					//This bitch aint even engaged and is trying to get funky.
				}
				cm.dispose();
			}
		}else if(status == 10){
			cm.getPlayer().addProgressValue("marriage", "mom&dad");
			cm.sendOk("Fantastic. It's always great to see a couple fall in love. Why don't you go speak with Mom and Dad for their blessing? I'm sure they will see that you two are meant to be. While going there, why don't you tell Cody that I said Hello if you have time.");
			cm.dispose();
		}
	}else if (cm.getMapId() == 680000210) { //High priest is in the cathedral
        cm.sendOk ( "Humm...");
		cm.dispose();
    }
}

/*function action(mode, type, selection) {
    // 680000300 -- Cherished Visage Photos
    // 680000500 -- Wedding Exit Map
    var status = -1;
    if (status == 0) {
        cm.sendYesNo("Do you want to get this show on the road?");
    } else if (status == 1) {
        if (cm.isWeddingCouple() == 0) {
            cm.sendOk("You guys look fantastic!");
            cm.dispose();
            return;
        }
        var chr = cm.getMap().getCharacterById(cm.getPlayer().getRelationship());
        if (chr == null) {
            cm.sendOk("Make sure your partner is in the map.");
            cm.dispose();
            return;
        }
        cm.startMarriage();
        cm.dispose();
    }
}*/

/**
    field = self.field;
    qr = target.questRecord;
    val = qr.get( 8816 );
    inventory = target.inventory;
        
    if (val == "end" )        // user already finished the quest 
        self.say( "You have already received the Officiator's Permission.");
    else if (val == "ing"){
           nItem = inventory.itemCount( 4031373 ); // checkikng : parent bless (complete this Quest)
           if (nItem > 0)     {   // user got the parents blessing
               self.say ("I see a smile on your face...you received your Parent's Blessing, didn't you? Great! Now, take the Officiator's Permission. You'll need to get married in cathedral. See you at the wedding!");
               if (inventory.itemCount( 4031375 )>0) {
                ret = inventory.exchange( 0, 4031373, -1, 4031374, 1);
                if (ret !=0) {
                    qr.set( 8816, "end" );                            
                    target.incEXP (500, 0);
                }
                else self.say("Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my database, please try again later");
               }
               else if (inventory.itemCount( 4031480 )>0) {
                ret = inventory.exchange( 0, 4031373, -1, 4031374, 1);
                if (ret !=0) {
                    qr.set( 8816, "end" );
                    target.incEXP (500, 0);
                    }
                else self.say("Oh dear, looks like I can't find that information right now...I'm having a bit of trouble with my database, please try again later");
                }
           }
           else  self.say(" You still need your parents blessing, my friend. True Love knows no bounds, head out there and obtain your Parent's Blessing.");

    }
    else {    //quest starts
    nRet = self.askYesNo("Ah, there is seldom a sight more beautiful than two people in love. I can see that you want to get married. Have you got your Parent's Blessing yet? It is important that your parents give their blessing for a happy marriage. Do you wish to go visit your parents now?");
        if(nRet!=0) {    // user accepts the quest
            qr.set( 8816, "ing" );
            self.say( "Fantastic. It's always great to see a couple fall in love. Why don't you go speak with Mom and Dad for their blessing? I'm sure they will see that you two are meant to be. While going there, why don't you tell Cody that I said Hello if you have time." );

        }
        // user doesn't accept the quest
        else self.say( "Well, let's not rush things. Come back when you're ready to visit your Parents. " );
    }
*/  