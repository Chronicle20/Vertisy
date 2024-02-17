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
		qm.sendSay("Master! I'm touched! You are such a good person. Let's help all the people who need our help, okay? That's our calling!", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bWhat the...? What calling?", true, true);
	}else if(status == 2){
		qm.sendSay("Well, master, you and I are so powerful together, you know? I have a feeling we were given these powers to help mankind! It's your calling as a Dragon Master, I think.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bMy calling as...a Dragon Master?", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("Yup! That's what I'm talking about! I just KNOW there are people out there in desperate need of the Dragon Master's help!");
	}else if(status == 5){
		qm.forceStartQuest();
		qm.sendSayUser("#b(You agree to help others using your powers as a Dragon Master. Sounds grandiose, even to you. But you'd better get started! Check around henesys to see if anyone needs help.)", false, false);
		qm.dispose();
	}
}