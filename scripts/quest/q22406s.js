var status = -1;

function start(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendSay("Master.... Ughhh....", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bwhat is it? Are you ill?", true, true);
	}else if(status == 2){
		qm.sendSay("I... I... can't... breath...", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bYou can't breathe? Why? What's wrong?! Are you hurt?!", true, true);
	}else if(status == 4){
		qm.sendSay("No, no, that's not it... Ughhhhh...", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bSo, what is it then? Tell me!", true, true);
	}else if(status == 6){
		qm.sendSay("The saddles too tight!", true, true);
	}else if(status == 7){
		qm.sendSayUser("#bHuh...?", true, true);
	}else if(status == 8){
		qm.sendSay("Ungh... I can't even move my wings! This saddle is much too small it doesn't fit me! I don't think I could have you ride on my like this!", true, true);
	}else if(status == 9){
		qm.sendSayUser("#bOh, no... What should we do?", true, true);
	}else if(status == 10){
		qm.sendAcceptDecline("I need a new saddle, master! You need to go back to Kenta and ask.");
	}else if(status == 11){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSayUser("#b(Mir's saddle looks much too small on him. You'd better go back to Kenta in the Aquarium and ask for a new one. Hold off on Dragon riding for the time being.)", false, true);
	}else if(status == 12){
		qm.sendSay("Oh no, master.. Does this mean you're going to have to spent another ton of mesos to get a new saddle?", true, true);
	}else if(status == 13){
		qm.sendSayUser("#b...Whew...", false, false);
		qm.dispose();
	}
}