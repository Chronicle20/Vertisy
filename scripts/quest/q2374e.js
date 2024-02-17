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
		qm.sendSay("I've been waiting for you. Do you have Arec's answer?\r\nPlease give me his letter.", false, true);
	}else if(status == 1){
		qm.sendSay("We have finally received Arec's official recognition. This is an important movement for us. It's also time that you experience a change.", true, true);
	}else if(status == 2){
		if(qm.hasItem(4032619) && !qm.isQuestCompleted()){
			if(qm.canHold(1132021)){
				qm.gainItem(4032619, -1);
				qm.gainItem(1132021, 1);
				qm.forceCompleteQuest();
				qm.changeJobById(432);
				qm.teachSkill(4321000, 0, 20);
				qm.sendSay("Now that we have Arec's Recognition, you can make a job advancement by going to see him when you reach Lv. 70. Finally, a new future has been opened for the Dual Blades.", false, false);
			}else qm.sendSay("Please free at least one Equip slot before advancing to Blade Specialist.");
		}
	}
}