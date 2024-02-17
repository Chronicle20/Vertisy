/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;

function start(){
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
	
	if(status == 0){
		cm.sendSay("Are you ok, master? You look so tired.", false, true);
	}else if(status == 1){
		cm.sendSayUser("#bI'm fine. Aran is the only one who's hurt, since he was fighting on the front lines. But everyone's all right. How about you? Are you ok?", true, true);
	}else if(status == 2){
		cm.sendSay("No problem whatsoever.", true, true);
	}else if(status == 3){
		cm.sendSayUser("#bI'm not worried about your physical state. I'm more worried about your head. Your entire race has been completely...", true, true);
	}else if(status == 4){
		cm.sendSay("...", true, true);
	}else if(status == 5){
		cm.sendSayUser("#bI'm so sorry. I got you into this whole mess. I should have let you go with the Black mage. If you had gone with the Black Mage, all the Onyx Dragons would have survived!", true, true);
	}else if(status == 6){
		cm.sendSay("Don't be silly, master. We thought because we chose to. it is not your fault.", true, true);
	}else if(status == 7){
		cm.sendSayUser("#bBut...", true, true);
	}else if(status == 8){
		cm.sendSay("I don't care how much the Black Mage wants our power. We'd never align ourselves with him. We Onyx Dragons belong with humans. You are the ones with such strong spirits. We could never become one with such an evil being.", true, true);
	}else if(status == 9){
		cm.sendSay("So, please do not apologize, master..Freud. Even if we are completely annihilated, that is our choice. You must respsect our wishes.", true, true);
	}else if(status == 10){
		cm.sendSayUser("#bAfrien...", true, true);
	}else if(status == 11){
		cm.sendSay("I have one request, though. If I...die in the final battle against the Black mage, could you watch over my child? it will be a long, long time before it hatches from its egg. but...I trust you with it.", true, true);
	}else if(status == 12){
		cm.sendSayUser("#bDOn't say things like that, Afrien. You must stay alive and take care of your own child!", true, true);
	}else if(status == 13){
		cm.sendSay("Who knows whether either of us will survive? That's is why I'm asking you. Promise me, master?", true, true);
	}else if(status == 14){
		cm.sendSayUser("#bOkay, okay. I promise. But you need to promise me something, too. You have to promise that you will do everything in your power to survive.", true, true);
	}else if(status == 15){
		cm.sendSay("Done, master.", true, true);
	}else if(status == 16){
		cm.sendSayUser("#bDo not sacrifice yourself on my behalf", true, true);
	}else if(status == 17){
		cm.getPlayer().updateQuestInfo(22591, "1");
		cm.warp(914100021);
		cm.dispose();
	}
}