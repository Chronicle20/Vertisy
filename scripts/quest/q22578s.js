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
		qm.sendSay(1013000, "Master! Master! Nicely done! Do you think your last mission was of great help to the people of Maple World?", 0, false, true);
	}else if(status == 1){
		qm.sendSayUser("#bWell, I defeated all the zombies in El Nath, so it must have been a good thing.", true, true);
	}else if(status == 2){
		qm.sendSay(1013000, "The more monsters you defeat, the better, I suppose? But what about that Black Key at the end? What do you think that was about", 0, false, true);
	}else if(status == 3){
		qm.sendSayUser("#bI'm not sure. But this organization is all about doing good deeds, so it's got to be for a good purpose", true, true);
	}else if(status == 4){
		qm.sendSay(1013000, "I suppose... But why do you think this organization carries out its activities in secret? How is anyone suppose to know of their good deeds if no one even knows they exist?", 0, false, true);
	}else if(status == 5){
		qm.sendSayUser("#bWell, like the saying goes, let not your left hand know what your right hand is doing!", true, true);
	}else if(status == 6){
		qm.sendSay(1013000, "Left hand? Right hand? What?! Are you saying you should let your left hand be a loser that doesn't know anything?", 0, false, true);
	}else if(status == 7){
		qm.sendSayUser("#bHaha, no! I think it just means you should keep your good deeds to yourself since it's not virtuous to brag about your good deeds.", true, true);
	}else if(status == 8){
		qm.sendAcceptDecline("I don't get it. I love to let people know what I'm up to. Anyway, it just seems so secretive and calculated. It's exciting, yet I don't understand it. Don't you agree master?\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#10000 exp\r\n#fUI/UIWindow.img/QuestIcon/10/0#2 sp");
	}else if(status == 9){
		qm.forceStartQuest();
		qm.forceCompleteQuest();
		qm.gainExp(10000);
		qm.getPlayer().gainSp(2);
		qm.sendSay("#bBut I'm sure there's a reason for it. It's for a good cause, so... I'll ask about it next time. Yeah, I'll just ask what the organization is really about when I'm given my next mission", false, false);
		qm.dispose();
	}
}