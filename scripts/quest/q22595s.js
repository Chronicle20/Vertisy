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
		qm.sendSay("Hi there. I'm a ludibrium Guard and my name is Marcel. Is there something I can help you with? Huh? What, the Door Block?!", false, true);
	}else if(status == 1){
		qm.sendAcceptDecline("Shhhh! How do you know about the Door Block? I'm an undercover guard watching over the secret Safe, so that's how I know about it. Um.... Well, ok, anyway, I can tell you about the door block.");
	}else if(status == 2){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.gainExp(23000);
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSay("The Door Block was broken into by someone some time ago, and broke as a result. #bNo one was watching the secret Safe at the time, and so a burglar came in and stole the treasture#k. No one knows what kind of a treasure it was, but... It's a big deal.", false, true);
	}else if(status == 3){
		qm.sendSay("Uh... why are you making such a scary face? Please remember, this issue must be kept a secret so please watch what you say!", true, false);
		qm.dispose();
	}
}