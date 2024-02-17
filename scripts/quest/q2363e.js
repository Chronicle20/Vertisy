var status = -1;

function end(mode, type, selection) {
    if (mode < 0 || (status == 0 && mode == 0)){
        qm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if(status == 0){
		qm.sendAcceptDecline("This is great. The Mirror of Insight has chosen you, Are you ready to awaken as a Dual Blade?");
	}else if(status == 1){
		if(qm.hasItem(4032616) && !qm.isQuestCompleted()){
			qm.gainItem(4032616, -1);
			qm.gainItem(1342000, 1);
			qm.forceCompleteQuest();
			qm.changeJobById(430);
			qm.gainSP(2);
			qm.sendSay("From this moment, you are a #bBlade Recruit#k. Please have pride in all that you do.", false, true);
		}
	}
}