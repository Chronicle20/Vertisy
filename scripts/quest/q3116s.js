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
		qm.sendSay("Hey, you're #b#h ##k, right? My name is #bShammos#k. I've called you here because I have a very important request. But first, I'm sure you have many questions about me", false, true);
	}else if(status == 1){
		qm.sendAcceptDecline("As you can see, I'm not human. I am part of the Hoblin race. At least that's what people tell me. Truth is, I don't really know who I am or why I'm trapped here. For some reason. I can't remember much of anything. It's so frustrating! I really need your help, yeah?");
	}else if(status == 2){
		qm.sendSay("Please come see me now. As you many already know. I am in the #bBasement of the Chief's Residence in El Nath#k.", false, true);
	}else if(status == 3){
		qm.forceStartQuest();
		qm.dispose();
	}
}