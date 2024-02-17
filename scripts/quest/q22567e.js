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
		qm.sendSay("#b(You place the Growth Accelerant you got for Hidden Brick.)", false, true);
	}else if(status == 1){
		qm.sendSay("#b(You plae the Hidden Brick back into the hole and restore the brick wall to its original form.)#k\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0#25000 exp\r\n#fUI/UIWindow.img/QuestIcon/10/0#2 sp", true, true);
	}else if(status == 2){
		if(qm.isQuestStarted()){
			qm.gainExp(25000);
			qm.getPlayer().gainSp(2);
			qm.gainItem(4032468, -10);
		}
		qm.forceCompleteQuest();
		qm.sendSayUser("Phew... I thought the mission would be easy, since I'm a temporary member and all, but it was tough! It's so exciting being part of this secret organization.", true, true);
	}else if(status == 3){
		qm.sendSay(1013000, "Pretty thrilling, right, master? I wonder what this Growth Accelerant is for. Do you think I would grow like crazy if I ate some?", 4, true, true);
	}else if(status == 4){
		qm.sendSayUser("#bI don't know. Huckle says there could be side effects if consumed by an animal, so I don't think you should try..", true, true);
	}else if(status == 5){
		qm.sendSay(1013000, "Master! Are you calling me an animal?", 4, true, true);
	}else if(status == 6){
		qm.sendSayUser("#bWell, humans are animals too! Hahaha.", true, true);
	}else if(status == 7){
		qm.sendSay(1013000, "I don't know about that... Fine. I'll let that one go", 4, true, true);
	}else if(status == 8){
		qm.sendSayUser("#bDon't you think this Growth Accelerant must be to help crops grow? Some kind of fertilizer or something?", true, true);
	}else if(status == 9){
		qm.sendSay(1013000, "That makes sense. Faster-growing. bigger cops means more food for more people. Less people will go hungry. This organization is all about doing good deeds to improve people's lives, right?", 4, true, true);
	}else if(status == 10){
		qm.sendSayUser("#bYeah. I think that makes sense!", false, false);
		qm.dispose();
	}
}