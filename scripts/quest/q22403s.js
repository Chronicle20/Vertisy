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
		qm.sendSay("What gives you the right to step into Ereve without permission? State your name, job, and purpose. If you lie or if your purpose is not adequate, you will not be allowed to enter.", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bThis is a restricted area? But I've seen so many people enter and leave freely...", true, true);
	}else if(status == 2){
		qm.sendSay("This island may only be accessed by Empress Cygnus's knights. Since you did not realize this, I will let it slide. Now leave immediately.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bWa..wait! Can you just answer one question?", true, true);
	}else if(status == 4){
		qm.sendSay("Oh? You do have a purpose here then? Then state your name, job, and purposely.", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bOkay...Evan. Dragon Master. Searching for a saddle. Look, all I need is a saddle, and I heard you can find great ones here. Just let me get a saddle and I'll be on my way.", true, true);
	}else if(status == 6){
		qm.sendSay("Dragon Master? Are you like a Magician that commands a Bahamut?", true, true);
	}else if(status == 7){
		qm.sendSayUser("#bHuh? Er, no...I don't think so?", true, true);
	}else if(status == 8){
		qm.sendSay("Hm! You're a strange one. Dragon Master... Never heard of it. I must make a note to look into that. You asked about a saddle?", true, true);
	}else if(status == 9){
		qm.sendAcceptDecline("The Knight's saddles aren't made here. We just don't have the resources for that. We outsource our saddle production. Would you like to find out where?");
	}else if(status == 10){
		qm.forceStartQuest(22403);
		qm.sendSay("All our saddles are made by #bKenta#k at the Zoo #bAquarium. They are high quality saddles, but they cost so much you'll feel your eyeballs are popping out of your head. So prepare yourself.", false, true);
	}else if(status == 11){
		qm.sendSay("Is that all you need? Then please remove yourself from this Island at once. You seem nice, but rules are rules. We don't permit outsiders to linger.", true, false);
		qm.dispose();
	}
}