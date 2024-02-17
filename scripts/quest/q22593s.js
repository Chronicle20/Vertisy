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
		qm.sendAcceptDecline("Hmm? Is there something I can do for you? You have such a determined expression... Huh? You want to know if the plants in Orbis grew abnormally fast? Whoa, how did you know about that?\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 23000 exp");
	}else if(status == 1){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.gainExp(23000);
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSay("Yes, it was quite a dilemma for us when the #bNependeaths started growing like crazy! Thankfully, someone that was passing by did some investigating on our behalf, and we were able to resolve the issue, but boy, it was a big deal!", false, true);
	}else if(status == 2){
		qm.sendSay("Why are you making that face? We've already resolved the issue so you don't need to worry about it.", true, false);
		qm.dispose();
	}
}