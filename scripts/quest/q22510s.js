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
		if(sel == 0){
			qm.sendSimple("Hm? What is it, Evan? Are you here to help your old dad? Huh? What do you mean, you defeated the Strange Pigs?! Geez are you hurt?!\r\n\r\n#b#L1#I'm fine, Dad! It was easy.#l");
		}else if(sel == 1){
			qm.sendSay("Ok maybe another time.", false, false);
			qm.dispose();
		}
	}else if(status == 1){
		sel = selection;
		if(selection == 1){
			qm.sendAcceptDecline("What a relief. You need to be careful, though. It could've been dangerous... By the way, I've got something for you to do. Can you run an errand for me?");
		}
	}else if(status == 2){
		qm.sendSay("Could you tell #bChief Stan#k in #bHenesys#k that I'm not going to be able to deliver the #bPork#k on time? The Strange Pigs have caused so many problems.", true, true);
	}else if(status == 3){
		qm.sendSay("I've written everything down in this letter, so all you have to do is take this to him. I'd go myself, but I have to deal with problems here.", true, true);
	}else if(status == 4){
		qm.forceStartQuest();
		qm.gainItem(4032455, 1);
		qm.showInfo("UI/tutorial.img/13/0");
		qm.dispose();
	}
}