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
		qm.sendSay(4, 1013000, "Master, what is it? Huh? Mounting? Isn't that like riding around on pigs or birds of rwolves? What about it?", false, true);
	}else if(status == 1){
		qm.sendSayUser("#bWell, I was wondering if you think its possible for me to ride and Onyx Dragon?", true, true);
	}else if(status == 2){
		qm.sendSay(4, 1013000, "Ride an Onyx Dragon... HOLD ON JUST A MINUTE. What are you saying? You want to...ride me? But I'm your partner not some little pet! How could you, master..", true, true);
	}else if(status == 3){
		qm.sendSayUser("#bDon't be silly! That's why I want to know if I can ride you. BECAUSE you're my partner.", true, true);
	}else if(status == 4){
		qm.sendSay(4, 1013000, "Oooh, I get... Wait, huh?! Well, I suppose it would make things more convenient, but if I ever get tired, I get to ride on you, okay?", true, true);
	}else if(status == 5){
		qm.sendSayUser("#bWhat?! Are you trying to kill your one and only master?", true, true);
	}else if(status == 6){
		qm.sendSay(4, 1013000, "Fine, fine. Nevermind. I was just joking anyways. If I tried to ride on your back, you'd turn into a flat old pancake. But if you want to ride me, no problem. You're no that big.", true, true);
	}else if(status == 7){
		qm.sendSayUser("#bSo it's okay for me to ride on your back then?", true, true);
	}else if(status == 8){
		qm.sendSay(4, 1013000, "Yeah, why not? I can fly a whole lot faster than you anyway. But we can't just take off here and now. Two things must be prepared first", true, true);
	}else if(status == 9){
		qm.sendAcceptDecline(1013000, "You need a #bsaddle#k and the #bMonster Riding skill#k! I don't think you'd survive long on my back without something to sit on. Think you can prepare both these things?", 4);
	}else if(status == 10){
		qm.sendSayUser("#bYou should go talk to the person riding a Dragon that Grendel the Really Old#k mentioned. Talk to #bGrendel the Really Old#k first, though.", false, false);
		qm.forceCompleteQuest();
		qm.dispose();
	}
}