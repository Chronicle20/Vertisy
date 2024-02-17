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
		qm.sendSay(4, 1013000, "Master, do you think I'm the only survior of my race? Are the others really all gone? Why were they killed? And why was I spared? I just can't figure it out, and it makes me so sad...", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bMir...", true, true);
	}else if(status == 2){
		qm.sendAcceptDecline(1013000, "But I refuse to give up. I beat the odds, so there must be others. I will find them! Master, you'll help, wont you?\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#3000 exp\r\n#fUI/UIWindow.img/QuestIcon/10/0#2 sp", 4);
	}else if(status == 3){
		qm.forceStartQuest();
		qm.forceCompleteQuest();
		qm.gainExp(3000);
		qm.getPlayer().gainSp(2);
		qm.sendSay("All right, then. We'll give that Grendel the Really Old or whatever his name is, time to find our more. In the meanwhile, we'll get stronger. Let's become heroes! Let's go help people!", false, false);
		qm.dispose();
	}
}