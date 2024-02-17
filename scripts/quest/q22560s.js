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
		qm.sendSay("Great to see you again, lifesaver! My master has been very busy lately, letting his wounds heal and finding a new base for us, which is why there has been no communication from him lately, it seems that you were finally contacted!", false, true);
	}else if(status == 1){
		qm.sendSay("I told my master about you and he agreed that you could join the secret organization! There is one condition however, I think it must be the entrance exam to join the organization.", true, true);
	}else if(status == 2){
		qm.sendAcceptDecline("A strong and fancy hero like you should easily be able to pass the test, I think. Should I tell you about the test?");
	}else if(status == 3){
		qm.sendSay("The test is simple! Go to #bHunting Ground in the Deep Forest II#k and defeat #r150 Curse Eyes#k. My master is planning to build a base there but he's been having trouble with the Curse Eyes, which have been wrecking havoc.", false, true);
		qm.forceStartQuest();
	}else if(status == 4){
		qm.sendSay("I don't know why he doesn't just build the base somewhere else... He apparently tried building it in some garden, but had to halt the project because of some monsters that kept attacking. I guess that's why he's been a little more cautious this time", true, false);
		qm.dispose();
	}
}