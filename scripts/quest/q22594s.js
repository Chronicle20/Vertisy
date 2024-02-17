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
		qm.sendSay("What is it? You don't look like you need my teachings... Hmm? Is it a good thing to kill monsters? Well, of course! If it weren't for the zombies. El Nath could develop a lot further. If you have the energy, keep getting rid of those zombies.", false, true);
	}else if(status == 1){
		qm.sendSayUser("#b(Perhaps this mission was indeed for a good cause.)", true, true);
	}else if(status == 2){
		qm.sendSay("But you'd better take care of the teeth after you've killed the zombies, because Zombie's Lost Tooths have a dark force inside. if you're not careful, you may end up getting corrupted, just like Shammos. He wishes to be redeemed from his wrongdoings, but he just keeps becoming more evil...", true, false);
	}else if(status == 3){
		qm.sendSayUser("#bHas Shammos done something wrong?", true, true);
	}else if(status == 4){
		qm.sendSay("Shammos was caught with a copy of the basement key to the Chief's Resident some time ago. The key was taken away, but he most likely made copies. We'll have to keep a closer lookout on the basement for the time being.", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bWhat's in the basement?", true, true);
	}else if(status == 6){
		qm.sendAcceptDecline("There is an old treasure that's been stored in the El Nath for a long time. I can't tell you anything more. It is something that must not get lost. Don't ask me any more questions about it\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 23000 exp");
	}else if(status == 7){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.gainExp(23000);
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSay("No need to look so gloomy. It's not like you stole the treasture or even helped steal the treasure. #rIt's true our security has become weaker lately#k but we just need to be more watchful, that's all.", false, false);
		qm.dispose();
	}
}