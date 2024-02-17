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
		qm.sendSay("Well it seems that you have completed the mission. Let us check the Free Spirits you've brought back with you.\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 63300 exp", false, true);
	}else if(status == 1){
		if(qm.hasItem(4000144, 100) && qm.isQuestStarted()){
			qm.forceCompleteQuest();
			qm.gainExp(63300);
			qm.gainItem(4000144, -100);
			qm.sendSay("Heh heh heh... This should be plenty for us to carry out our plans...", true, true);
		}else if(qm.isQuestCompleted()){
			qm.sendSay("Heh heh heh... This should be plenty for us to carry out our plans...", true, true);
		}
	}else if(status == 2){
		qm.sendSayUser("#L0# #bLook... I have a question I want to ask...#l", true, true);
	}else if(status == 3){
		qm.sendSay("I'm sorry, but my hands are tied taking care of all the Free Spirits you broguht back. Can you come back later, when I'm finished making the item? It's going to take a little while.", true, true);
	}else if(status == 4){
		qm.sendSayUser("#b(What good is defeating some monsters deep inside the Clocktower, where no one even goes? You want to find out, but he does look pretty busy. You'll have to come back and speak to him later.", true, false);
		qm.dispose();
	}
}