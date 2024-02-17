var status = -1;
var sel = 0;

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
		qm.sendSay("I just don't believe it. That Stan is...the same miser who wouldn't speak to me for two years because I ate one of his candies. The same cheapskate that loaned me 3,000 mesos then calculated interest for each SECOND I was late... I just don't believe it!", false, true);
	}else if(status == 1){
		qm.sendSayUser("#b(You had no idea that Chief Stan was such a grinch.)", true, true);
	}else if(status == 2){
		qm.sendSay("I don't believe Stan would send such a strong adventurer my way to help my training center. It makes no sense. Stan has never helped me. But... fine. I'll test you once more but this is the last time. I know you and Stan are up to something", true, true);
	}else if(status == 3){
		qm.sendSayUser("#b(You try to tell him that you and Stan aren't trying to pull a fast one on him, but he ignores you)", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("This test is simple. You just have to defeat #r100 Trainee Spores#k in the training center, that's all..It's not going to be easy finding them, since they hang out amongst the Orange Mushrooms. Haha... Do you still want to enter?");
	}else if(status == 5){
		qm.forceStartQuest();
		qm.warp(910060100);
		qm.dispose();
	}
}