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
		qm.sendSay("Let's train and get stronger, then! Let's train until we can help beat a Blue Mushroom with ease, then go back and help that woman! All you have to do is train! Train!\r\nTraaaaaaaaaaiiiiiiiiinnnn!!", false, true);
	}else if(status == 1){
		qm.sendSayUser("#b(Geez, this dragon isn't going to let up, is he? You hear that there's a training center around Henesys, Chief Stan might know something about that.)", true, true);
	}else if(status == 2){
		qm.sendSay("Let's train, master. Let's go!", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bOkay, okay! Fine. geez. I'll go talk to Chief Stan.", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("Really? We're really going to train?");
	}else if(status == 5){
		qm.forceStartQuest();
		qm.sendSay("Yippee! That's why I love you!", false, true);
	}else if(status == 6){
		qm.sendSayUser("#b(You finally calmed him a bit. Now go talk to Chief Stan about the training center.)", true, false);
		qm.dispose();
	}
}