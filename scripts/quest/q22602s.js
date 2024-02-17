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
		qm.sendSay("Master! Look. I've grown some more.", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bOh my! You really grew! Whoa, your voice is event different.", true, true);
	}else if(status == 2){
		qm.sendSay("Ahem... Really? Do I sound cool?", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bDefinitely! Dragons really do grow in leaps and bounds!", true, true);
	}else if(status == 4){
		qm.sendSay("Yep! I shed my old scales and grew new ones. I guess in human terms. It would be something like... buying new clothes as your body grows?", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bYour new scales are so shiny and nice.", true, true);
	}else if(status == 6){
		qm.sendSay("Yuuup. They are. aren't they?", true, true);
	}else if(status == 7){
		qm.sendSayUser("#b(His body's grown but he still talks the same.)", true, true);
	}else if(status == 8){
		qm.sendSay("Anyway, master, could you take a look at this?\r\n #i4032502:#\r\nThis is one of the scales I shed. For some reason, this one's still shiny. All the others sort of fell apart. I feel like thi3s scale still carries my strength in it. Do you think we could use it for something?", true, true);
	}else if(status == 9){
		qm.sendSayUser("#bHmm, maybe.", true, true);
	}else if(status == 10){
		qm.sendSay("Yippee! Humans don't have horns, scales, or claws like Dragons do, but they do have the ability to make useful things! That scale is extremely sturdy and carries with it my strength, so it will make you that much more powerful, master!", true, true);
	}else if(status == 11){
		qm.sendSayUser("#bMir, you are awesome. When did you start thinking like that?", true, true);
	}else if(status == 12){
		qm.sendSay("Ahem. It's not like I was born yesterday. I know a whole lot about humans now.", true, true);
	}else if(status == 13){
		qm.sendAcceptDecline("Here you go, master. Take my scale. I know you'll be able to make something really great with it!");
	}else if(status == 14){
		if(!qm.isQuestStarted() && !qm.isQuestCompleted()){
			qm.gainItem(4032502);
			qm.forceStartQuest();
			qm.forceCompleteQuest();
		}
		qm.sendSay("#b(You Received Mir's dragon scale. )", false, false);
		qm.dispose();
	}
}