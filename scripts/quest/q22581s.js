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
		qm.sendSay("Nice to see you again, Evan. Things have been running so smoothly for us lately, all thanks to you. In fact, I have another mission for you.", false, true);
		//qm.sendSay(1013000, "Master! Master! Nicely done! Do you think your last mission was of great help to the people of Maple World?", 0, false, true);
	}else if(status == 1){
		qm.sendSayUser("#bWait. Before you give me my next mission, I have a question.", true, true);
	}else if(status == 2){
		qm.sendSay("Sure. Ask away! I'll answer.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bAs a temporary member of the organization, I'm told so little... I want to know more about the organization", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("Oh, I see. Of course! That makes complete sense. It's quite complex, and I want to be sure I answer your question thoroughly, so I will tell you all about this organization when we meet in person to discuss the third mission. Is that acceptable?");
	}else if(status == 5){
		qm.forceStartQuest();
		qm.sendSay("All right. Come to #bLudibrium Village#k then. We have a base office there that is being used by our organization. I'll meet you there. Just come to #bFrog House#k.", false, false);
		qm.dispose();
	}
}