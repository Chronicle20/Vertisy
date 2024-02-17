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
		qm.sendSay("Finally! You're back. I heard from Betty that you traveled all around Victoria Island to get that book. So, did it contain the info you needed? What did you want to find out, anyway?", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bI wanted to learn about Onyx Dragons.", true, true);
	}else if(status == 2){
		qm.sendYesNo("Onyx dragons? Well, those have been extinct for quite some time, as you probably learned from that book. Are you doing a research project? You're a scholar in the making, It ell you. I'd be happy to lend a hand to your research.");
		
	}else if(status == 3){
		if(qm.isQuestStarted()){
			qm.forceCompleteQuest();
			qm.gainItem(4161050, -1);
			qm.gainExp(2000);//also says +200 exp in sendYesNo but I didn't do that
		}
		qm.sendSay("There are lots of books about dragons in Magic Library but there aren't any other books that discuss Onyx Dragons in particular. if a new book about Onyx Dragons ever arrives in Magic Library. I'll let you know.", false, true);
	}else if(status == 4){
		qm.sendSay("Oh, by the way, I have a friend in Leafre named #bChief Tatamo#k of the halfingers. I'll ask and see if he knows anything about Onyx Dragons.", true, true);
	}else if(status == 5){
		qm.sendSay("Onyx dragons... I hear they're covered in dark, clear scales and have golden horns. You little lizard has golden horns... but it doesn't have the dark scales... Hmmm.", true, true);
	}else if(status == 6){
		qm.sendSayUser("#b(He might try to kill Mir if he finds out he's a dragon!)\r\nHe isn't a dragon. He's just a lizard!", true, true);
	}else if(status == 7){
		qm.sendSay("Ah, geez. Did I imply otherwise? Of course it's a lizard. Hmm, but Onyx Dragons.", true, false);
		qm.dispose();
	}
}