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
		qm.sendSay("Look, master. Don't you think the mission you just completed for the Black Wings is a little strange? Things just don't add up. I thought dropping off the Free Spirit you got from Master Soul Teddy was supposed to be a good thing...", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bDoesn't it seem unnecessary for them to have wrapped it in a pouch like that? And what about the fact that you could only unwrap the pouch in front of the Sky Terrace? If the intention was to free it, why does it matter where you let it go?", true, true);
	}else if(status == 2){
		qm.sendSay("And then, did you hear the Guards screaming when you unwrapped the pouch? Remember how mad they were that we were getting in their way? Do you think the Guards were bad guys?", true, true);
	}else if(status == 3){
		qm.sendSay("And what about what Door Block said to us as it was disappearing... I don't know, it just bothers me. He called us thieves. I don't know, killing evil monsters should make me feel better, but I feel terrible!", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("That Hiver or whatever his name is told us not to worry, but something tells me that this last mission wasn't for such a good cause. Don't you agree, master?\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#20000 exp\r\n#fUI/UIWindow.img/QuestIcon/10/0#1 sp");
	}else if(status == 5){
		if(!qm.isQuestStarted()){
			qm.forceStartQuest();
			qm.forceCompleteQuest();
			qm.gainExp(20000);
			qm.getPlayer().gainSp(1);
		}
		qm.sendSay("So, the Black wings... I don't want to be suspicious of them but I can't help it...", false, false);
		qm.dispose();
	}
}