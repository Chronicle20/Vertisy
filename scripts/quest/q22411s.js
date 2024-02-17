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
		qm.sendSay("Master. Does seeing me all grown up remind you of anything?", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bAre you talking about your saddle?", true, true);
	}else if(status == 2){
		qm.sendSay("The old saddle was so small, I couldn't even fly in that thing, never mind trying to fly with you on my back. I'm telling you, we need to get a new saddle.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bUgh, I know...", true, true);
	}else if(status == 4){
		qm.sendSay("What is it? Is something wrong?", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bI'm just scared to find out how much it's going to cost me.", true, true);
	}else if(status == 6){
		qm.sendAcceptDecline("So... Kenta in the Aquarium is the one that makes the saddles, right?");
	}else if(status == 7){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSayUser("#bHmm. Well, I guess we don't have a choice. It's expensive, but I can't not get you one. I'll go get another saddle for you.", false, false);
		qm.dispose();
	}
}