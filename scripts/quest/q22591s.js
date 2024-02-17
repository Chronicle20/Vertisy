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
		qm.sendSay("Hundreds of years ago in Maple World, there were many Onyx Dragons. There were just as many humans who loved Onyx Dragons very much... We, my friend Freud and I always hoped that humans and the Onyx Dragons could forever live in peace...", false, true);
	}else if(status == 1){
		qm.sendSay("As powerful as we are, Onyx Dragons are born with incomplete spirits. Humans are born with strong wills but weak bodies. Put the two together, and a Dragon Master is born. We wanted the two races to exist in a symbiotic relationship, each helping each.", true, true);
	}else if(status == 2){
		qm.sendSay("Unfortunately, our wish was destroyed by the #rBlack Mage#k.", true, true);
	}else if(status == 3){
		qm.sendSayUser("#b(The Black Mage? The Black Wings were saying they wanted to resurrect the Black Mage to bring peace to Maple World, weren't they?)", true, true);
	}else if(status == 4){
		qm.sendAcceptDecline("Perhaps it would be best to show you. #bI will send you on a journey through my memory...#k Travel back hundreds of years to just before the war against the Black mage started. Go to my memory of when Freud and I conversed about making our dream a reality");
	}else if(status == 5){
		qm.forceStartQuest();
		qm.warp(900030000);
		qm.dispose();
	}
}